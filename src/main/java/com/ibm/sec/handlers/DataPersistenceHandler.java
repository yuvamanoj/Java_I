package com.ibm.sec.handlers;

import com.ibm.sec.dtos.CustomerRegistrationDto;
import com.ibm.sec.dtos.PrismaDetailsDto;
import com.ibm.sec.entities.*;
import com.ibm.sec.exceptions.*;
import com.ibm.sec.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ibm.sec.configurations.ExternalCallConstants.customerIdDelimiter;
import static com.ibm.sec.configurations.ExternalCallConstants.maxAllowedRetryCount;
import static com.ibm.sec.entities.Status.Statuses.*;
import static com.ibm.sec.entities.Task.Tasks.PERSIST_NEWUSERDETAILS;

@Component
public class DataPersistenceHandler {
    private static final Logger logger = LoggerFactory.getLogger(DataPersistenceHandler.class);
    @Autowired
    private ErrorMessages errorMessages;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskStatusRepository taskStatusRepository;

    @Autowired
    TermsAndConditionRepository termsAndConditionRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    UserRepository userRepository;

    @Value("${licence_key}")
    String licenseKey;

    int retryCount = 0;

    public Customer persistNewUserDetails(CustomerRegistrationDto registrationDto) {
        logger.debug("Entering persist new user details");
        //Generating user Id
        generateCustomerId(registrationDto);
        registrationDto.setCreatedDateTime(new Date());
        registrationDto.setCreatedBy(registrationDto.getId());
        registrationDto.setLicenseKey(Base64Utils.encodeToString(licenseKey.getBytes()));
        registrationDto.setApiKey(Base64Utils.encodeToString(registrationDto.getApiKey().getBytes()));
        registrationDto.setClusterId(Base64Utils.encodeToString(registrationDto.getClusterId().getBytes()));
        registrationDto.setRegion(Base64Utils.encodeToString(registrationDto.getRegion().getBytes()));
        registrationDto.setResourceGroup(Base64Utils.encodeToString(registrationDto.getResourceGroup().getBytes()));
        registrationDto.setTrialVersion(true);
        registrationDto.setPolicy("POC");
        Customer customer = null;
        try {
            Optional<User> optionalUser = userRepository.findByIbmId(registrationDto.getIbmId());
            if (optionalUser.isEmpty()){
                userRepository.save(new User(registrationDto.getIbmId(), registrationDto.getIbmEmailId()));
            }
            Optional<TermAndCondition> optionalTermAndCondition = termsAndConditionRepository.findById(1L);
            optionalTermAndCondition.ifPresent(termAndCondition -> registrationDto.setTncId(termAndCondition.getId()));
            customer = customerRepository.save(new Customer(registrationDto));
            createTasksForCustomer(customer);
            updateTaskStatusForCustomer(customer.getId(), PERSIST_NEWUSERDETAILS.name(), SUCCESS, retryCount, "");
        }catch (RuntimeException e){
            if (customer != null){
                findTaskStatusByCustomerIdAndTaskName(customer.getId(), PERSIST_NEWUSERDETAILS.name()).ifPresent(taskStatus -> {
                    retryCount = taskStatus.getRetry();
                });
                if (retryCount < maxAllowedRetryCount){
                    retryCount ++;
                    persistNewUserDetails(registrationDto);
                }else {
                    //KAFKA Integration
                    updateTaskStatusForCustomer(customer.getId(), PERSIST_NEWUSERDETAILS.name(), FAILED, retryCount, errorMessages.getDATABASE_UNAVAILABLE());
                    throw new NoResponseException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getDATABASE_UNAVAILABLE(), new RuntimeException()));
                }
            }else{
                throw new ClientDataException(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, errorMessages.getDATABASE_UNAVAILABLE(), new RuntimeException()));
            }
        }
        logger.debug("Exiting persist new user details");
        return customer;
    }

    private void generateCustomerId(CustomerRegistrationDto registrationDto) {
        registrationDto.setId(registrationDto.getEmail().split("@")[0]+customerIdDelimiter+System.currentTimeMillis());
    }

    public CustomerRegistrationDto updateCustomerPrismaDetails(String customerId, PrismaDetailsDto prismaDetailsDto) {
        logger.debug("Entering update customer prisma details");
        return customerRepository.findById(customerId).map(customer -> {
            customer.setPrismaUrl(Base64Utils.encodeToString(prismaDetailsDto.getPrismaUrl().getBytes()));
            customer.setPrismaUsername(Base64Utils.encodeToString(prismaDetailsDto.getPrismaUsername().getBytes()));
            customer.setPrismaPassword(Base64Utils.encodeToString(prismaDetailsDto.getPrismaPassword().getBytes()));
            customer.setUpdatedDateTime(new Date());
            customer.setUpdatedBy(customerId);
            customerRepository.save(customer);
            logger.debug("Exiting update customer prisma details after saving details");
            return new CustomerRegistrationDto(customer);
        }).orElseThrow(() -> new InvalidUserIdException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getINVALID_USER_ID(), new RuntimeException())));
    }

    public Optional<Customer> findByCustomerId(String customerId){
        return customerRepository.findById(customerId);
    }

    public Optional<Customer> findByClusterId(String clusterId){
        return customerRepository.findByClusterId(clusterId);
    }

    public void createTasksForCustomer(Customer customer) {
        logger.debug("creating tasks for customer");
        Status status = statusRepository.findByName(NOT_STARTED.name());
        taskRepository.findAll().forEach(task -> taskStatusRepository.save(new TaskStatus(customer.getId(), task.getId(), task.getName(), new Date(), new Date(), new Date(), status.getId(), status.getName(), 0, "")));
    }

    public void updateTaskStatusForCustomer(String customerId, String taskName, Status.Statuses statusVal,int retry, String error){
        logger.debug("Entering update task status for customer");
        taskStatusRepository.findByUserIdAndTaskName(customerId, taskName).ifPresent(taskStatus -> {
            taskStatus.setUpdatedDateTime(new Date());
            Status status = statusRepository.findByName(statusVal.name());
            taskStatus.setStatusName(status.getName());
            taskStatus.setStatusId(status.getId());
            taskStatus.setRetry(retry);
            taskStatus.setError(error);
            taskStatus.setUpdatedDateTime(new Date());
            taskStatusRepository.save(taskStatus);
        });
        logger.debug("Exiting update task status for customer");
    }

    public void updateSuccessfulBuildTaskStatus(TaskStatus taskStatus){
        logger.debug("Entering update build task status");
        Status status = statusRepository.findByName(SUCCESS.name());
        taskStatus.setStatusId(status.getId());
        taskStatus.setStatusName(status.getName());
        taskStatus.setUpdatedDateTime(new Date());
        taskStatus.setError("");
        taskStatusRepository.save(taskStatus);
        logger.debug("Exiting update build task status");
    }

    public Optional<TaskStatus> findTaskStatusByCustomerIdAndTaskName(String customerId, String taskName){
        return taskStatusRepository.findByUserIdAndTaskName(customerId, taskName);
    }

    public List<Customer> findByIbmId(String ibmId) {
        return customerRepository.findByIbmId(ibmId);
    }

    public List<TaskStatus> findTaskStatusByCustomerId(String customerId) {
        return taskStatusRepository.findByUserId(customerId);
    }

    public Task findTaskByTaskId(Long taskId) {
        return taskRepository.getOne(taskId);
    }
}
