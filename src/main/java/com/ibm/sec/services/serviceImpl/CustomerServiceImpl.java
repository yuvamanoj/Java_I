package com.ibm.sec.services.serviceImpl;

import static com.ibm.sec.configurations.ExternalCallConstants.maxAllowedRetryCount;
import static com.ibm.sec.entities.Status.Statuses.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.DASHBOARD_INSTALLATION;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.INSTALLATION;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.POLICY;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.UNINSTALLATION;
import static com.ibm.sec.services.JenkinsService.BuildInitializerType.INITIAL;
import static com.ibm.sec.services.JenkinsService.BuildInitializerType.RETRY;
import  com.ibm.sec.configurations.ExternalCallConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.sec.dtos.*;
import com.ibm.sec.entities.Status;
import com.ibm.sec.entities.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ibm.sec.entities.Customer;
import com.ibm.sec.entities.TaskStatus;
import com.ibm.sec.exceptions.ApiError;
import com.ibm.sec.exceptions.ClientDataException;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.exceptions.InvalidUserIdException;
import com.ibm.sec.handlers.CSSSchedulerHandler;
import com.ibm.sec.handlers.DataPersistenceHandler;
import com.ibm.sec.handlers.GithubInteractionHandler;
import com.ibm.sec.handlers.JenkinsInteractionHandler;
import com.ibm.sec.producer.MessageKafkaProducer;
import com.ibm.sec.services.CustomerService;
import org.springframework.util.Base64Utils;

@Component
public class CustomerServiceImpl implements CustomerService {
	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

	@Autowired
	ErrorMessages errorMessages;

	@Autowired
	DataPersistenceHandler dataPersistenceHandler;

	@Autowired
	GithubInteractionHandler githubInteractionHandler;

	@Autowired
	JenkinsInteractionHandler jenkinsInteractionHandler;

	@Autowired
	CSSSchedulerHandler schedulerHandler;

	@Autowired
	MessageKafkaProducer producer;
	@Override
	public CustomerRegistrationDto registerUser(CustomerRegistrationDto registrationDto) {
		logger.debug("Entering register user");

		dataPersistenceHandler.findByClusterId(Base64Utils.encodeToString(registrationDto.getClusterId().getBytes())).ifPresent(user -> {
			throw new ClientDataException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getCLUSTER_ID_ALREADY_AVAILABLE(), new RuntimeException()));
		});

		//Step 1 : Persist User
		Customer customer = dataPersistenceHandler.persistNewUserDetails(registrationDto);

		//step 2 : Create a customer github policy repo using template
		githubInteractionHandler.createGithubRepositoryForCustomer(customer, POLICY);

		//step 3 : Create a customer github installation repo using template
		githubInteractionHandler.createGithubRepositoryForCustomer(customer, INSTALLATION);

		//step 4 : create jenkins job for policy
		String configXmlString = jenkinsInteractionHandler.getConfigFile(customer.getId());
		jenkinsInteractionHandler.createJenkinsJob(customer.getId(), configXmlString, POLICY);

		//step 5 : create jenkins job for installation
		jenkinsInteractionHandler.createJenkinsJob(customer.getId(), configXmlString, INSTALLATION);

		//step 7 : Initiate Installation jenkins job build
		jenkinsInteractionHandler.initiateJenkinsJobBuild(customer.getId(), INSTALLATION, INITIAL);

		//step 7 : create jenkins job for un-installation
		jenkinsInteractionHandler.createJenkinsJob(customer.getId(), configXmlString, UNINSTALLATION);

		//step 8 : Create jenkins job for Prisma dashboard installation
		jenkinsInteractionHandler.createJenkinsJob(customer.getId(), configXmlString, DASHBOARD_INSTALLATION);

		logger.debug("Exiting register user");

		producer.send(ExternalCallConstants.CSS_INSTALL_CONSUMER, new InstallationDto(customer.getId(),INSTALLATION.name()));
		return new CustomerRegistrationDto(customer);
	}


	@Override
	public CustomerRegistrationDto updateCustomerPrismaDetails(String customerId, PrismaDetailsDto prismaDetailsDto) {
		return dataPersistenceHandler.updateCustomerPrismaDetails(customerId, prismaDetailsDto);
	}

	@Override
	public CustomerClusterDetailsDto getClusterDetailsByCustomerId(String customerId) {
		logger.debug("Executing get cluster details by customer id");
		return dataPersistenceHandler.findByCustomerId(customerId).map(CustomerClusterDetailsDto::new)
				.orElseThrow(() -> new InvalidUserIdException(new ApiError(HttpStatus.BAD_REQUEST,
						errorMessages.getINVALID_USER_ID(), new RuntimeException())));
	}

	@Override
	public PrismaDetailsDto getCustomerPrismaDetails(String customerId) {
		logger.debug("Executing get customer prisma details");
		return dataPersistenceHandler.findByCustomerId(customerId).filter(customer -> {
			if (customer.getPrismaUrl() == null)
				throw new ClientDataException(new ApiError(HttpStatus.BAD_REQUEST,
						errorMessages.getPRISMA_URL_UNAVAILABLE(), new RuntimeException()));
			if (customer.getPrismaUsername() == null)
				throw new ClientDataException(new ApiError(HttpStatus.BAD_REQUEST,
						errorMessages.getPRISMA_USERNAME_UNAVAILABLE(), new RuntimeException()));
			if (customer.getPrismaPassword() == null)
				throw new ClientDataException(new ApiError(HttpStatus.BAD_REQUEST,
						errorMessages.getPRISMA_PASSWORD_UNAVAILABLE(), new RuntimeException()));
			return true;
		}).map(PrismaDetailsDto::new).orElseThrow(() -> new InvalidUserIdException(
				new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getINVALID_USER_ID(), new RuntimeException())));
	}

	@Override
	public String updateJobStatusForCustomer(String customerId, JobDetailsDto jobDetailsDto) {
		logger.debug("Executing update Job Status For Customer");
		return dataPersistenceHandler.findByCustomerId(customerId).map(customer -> {
			String taskName = jenkinsInteractionHandler.getBuildTaskName(jobDetailsDto.getJobName());
			Optional<TaskStatus> optionalTaskStatus = dataPersistenceHandler
					.findTaskStatusByCustomerIdAndTaskName(customerId, taskName);
			optionalTaskStatus.ifPresent(taskStatus -> {
				if (!taskStatus.getStatusName().equalsIgnoreCase(SUCCESS.name())) {
					if (jobDetailsDto.getStatus() && taskStatus.getRetry() < maxAllowedRetryCount)
						dataPersistenceHandler.updateSuccessfulBuildTaskStatus(taskStatus);
					if (jobDetailsDto.getJobName().equalsIgnoreCase(INSTALLATION.name()) && jobDetailsDto.getStatus()) {
						jenkinsInteractionHandler.initiateJenkinsJobBuild(customerId, POLICY, INITIAL);
					}
					if (jobDetailsDto.getJobName().equalsIgnoreCase(POLICY.name()) && jobDetailsDto.getStatus()) {
						//jenkinsInteractionHandler.initiateJenkinsJobBuild(customerId, DASHBOARD_INSTALLATION, INITIAL);
						producer.send(ExternalCallConstants.CSS_INSTALL_SUCCESS_CONSUMER, new InstallationDto(customerId,DASHBOARD_INSTALLATION.name()));
					}
					if (jobDetailsDto.getJobName().equalsIgnoreCase(UNINSTALLATION.name())
							&& jobDetailsDto.getStatus()) {
						//remove jenkins job for uninstallation
						jenkinsInteractionHandler.deleteJenkinsJob(customerId, UNINSTALLATION,
								INITIAL);
						//remove github installation repo on success of uninstallation job
						githubInteractionHandler.deleteGithubRepositoryForCustomer(customerId, GithubRepositoryType.INSTALLATION);
					}
					if (Arrays.stream(GithubRepositoryType.values()).anyMatch(
							p -> p.name().equalsIgnoreCase(jobDetailsDto.getJobName())) && !jobDetailsDto.getStatus()) {
						jenkinsInteractionHandler.initiateJenkinsJobBuild(customerId,
								GithubRepositoryType.valueOf(jobDetailsDto.getJobName().toUpperCase()), RETRY);
					}
				}
			});
			return taskName + " status updated successfully";
		}).orElseThrow(() -> new InvalidUserIdException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getINVALID_USER_ID(), new RuntimeException())));
	}
	@Override
	public ResponseEntity<Void> initiatePrismaConsoleInstallation(InstallationDto dto) {

		//step  : Initiate Installation jenkins job build for pcc console
		if(dto.getInstaller().toUpperCase().equals(INSTALLATION.name()))
			return  jenkinsInteractionHandler.initiateJenkinsJobBuild(dto.getCustomerId(),INSTALLATION, INITIAL);
		else if(dto.getInstaller().toUpperCase().equals(DASHBOARD_INSTALLATION.name()))
			return  jenkinsInteractionHandler.initiateJenkinsJobBuild(dto.getCustomerId(),DASHBOARD_INSTALLATION, INITIAL);
		else {
			throw new ClientDataException(new ApiError(HttpStatus.BAD_REQUEST, "Invald installation instruction given "+dto.getInstaller(), new RuntimeException()));
		}
	}

	@Override
	public List<TaskStatusDto> getAllCustomersRecentActivities(String ibmId) {
		List<Customer> customers = dataPersistenceHandler.findByIbmId(ibmId);
		List<TaskStatusDto> taskStatusDtos = new ArrayList<>();
		customers.forEach(customer -> {
			TaskStatusDto taskStatusDto = new TaskStatusDto();
			taskStatusDto.setCustomerId(customer.getId());
			taskStatusDto.setClusterId(customer.getClusterId());
			taskStatusDto.setPlatform(customer.getPlatform());
			taskStatusDto.setTrialVersion(customer.isTrialVersion());
			setLatestTaskForCustomer(customer, taskStatusDto);
			taskStatusDtos.add(taskStatusDto);
		});
		return taskStatusDtos;
	}

	private void setLatestTaskForCustomer(Customer customer, TaskStatusDto taskStatusDto) {
		List<TaskStatus> taskStatuses = dataPersistenceHandler.findTaskStatusByCustomerId(customer.getId());
		List<String> allTasks = Stream.of(Task.StatusResponseTasks.values())
				.map(Enum::name)
				.collect(Collectors.toList());
		taskStatuses.forEach(taskStatus -> {
			if (allTasks.contains(taskStatus.getTaskName())){
				Task task = dataPersistenceHandler.findTaskByTaskId(taskStatus.getTaskId());
				if(taskStatus.getStatusName().equals(STARTED.name())){
					taskStatusDto.setTaskName(task.getCustomerView());
					taskStatusDto.setInitiatedDateTime(setRequiredDateFormat(taskStatus.getInitiatedDateTime()));
					taskStatusDto.setStatus(TaskStatusResponses.Ongoing.name());
				}
				if (taskStatus.getStatusName().equals(FAILED.name())){
					taskStatusDto.setTaskName(task.getCustomerView());
					taskStatusDto.setRetry(taskStatus.getRetry());
					taskStatusDto.setError(taskStatus.getError());
					taskStatusDto.setStatus(TaskStatusResponses.Error.name());
				}
			}
		});
		if (taskStatusDto.getStatus() == null){
			boolean isCompleted = true;
			for (TaskStatus ts : taskStatuses){
				if (allTasks.contains(ts.getTaskName()) && ts.getStatusName().equals(NOT_STARTED.name())) {
					isCompleted = false;
					break;
				}
			}
			if (isCompleted){
				taskStatusDto.setStatus(TaskStatusResponses.Done.name());
			}
		}
	}

	private String setRequiredDateFormat(Date dateTime) {
		DateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
		return dateFormat.format(dateTime);
	}


}
