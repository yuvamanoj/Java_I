package com.ibm.sec.handlers;

import com.ibm.sec.dtos.CustomerRegistrationDto;
import com.ibm.sec.dtos.PrismaDetailsDto;
import com.ibm.sec.entities.*;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.exceptions.InvalidUserIdException;
import com.ibm.sec.repositories.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Base64Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ibm.sec.entities.Status.Statuses.NOT_STARTED;
import static com.ibm.sec.entities.Status.Statuses.SUCCESS;

@ExtendWith(SpringExtension.class)
class DataPersistenceHandlerTest {

    @Mock
    ErrorMessages errorMessages;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    TaskRepository taskRepository;

    @Mock
    TaskStatusRepository taskStatusRepository;

    @Mock
    TermsAndConditionRepository termsAndConditionRepository;

    @Mock
    StatusRepository statusRepository;

    @InjectMocks
    DataPersistenceHandler dataPersistenceHandler;

    private final String customerId = "cust_1234";

    private Customer mockCustomer = new Customer();

    @BeforeEach
    void setUp(){
        mockCustomer.setId(customerId);
        mockCustomer.setFirstName("MockCustomer");
        mockCustomer.setLastName("User");
        mockCustomer.setPhoneNumber(12312123L);
        mockCustomer.setEmail("Some@mail.com");
        mockCustomer.setApiKey(Base64Utils.encodeToString("mockApiKey".getBytes()));
        mockCustomer.setRegion("mockRegion");
        mockCustomer.setResourceGroup("default");
        mockCustomer.setClusterId("c0ci0ikd0a0s7i0e7vm0");
        mockCustomer.setPlatform("mockPlatform");
        mockCustomer.setToolset("mockToolSet");
        mockCustomer.setPolicy("mockPolicy");
        mockCustomer.setTncCheck(true);
        mockCustomer.setTrialVersion(true);
        mockCustomer.setFullVersion(false);
        mockCustomer.setLicenseKey("mockLicenseKey");
        mockCustomer.setCreatedDateTime(new Date());
        mockCustomer.setUpdatedDateTime(new Date());
        mockCustomer.setCreatedBy("mockUser");
        mockCustomer.setUpdatedBy("mockUser");
        mockCustomer.setPrismaUrl("wwwMockPrismaUrlcom");
        mockCustomer.setPrismaUsername("mockPrismaUserName");
        mockCustomer.setPrismaPassword("mockPrismaPassword");
        mockCustomer.setCompanyName("mockCompany");
        mockCustomer.setTncId(12L);
        mockCustomer.setIbmId("0000XXXX");
    }

    @Test
    void persistNewUserDetailsTest(){
        /*TermAndCondition mockTermAndCondition = Mockito.mock(TermAndCondition.class);
        mockTermAndCondition.setId(1L);
        mockTermAndCondition.setTerms("Test term");
        mockTermAndCondition.setVersion(1);
        ReflectionTestUtils.setField(dataPersistenceHandler, "licenseKey", "12357512734127351");
        Mockito.when(termsAndConditionRepository.findById(1L)).thenReturn(Optional.of(mockTermAndCondition));
        Mockito.when(customerRepository.save(Mockito.any(Customer.class))).thenReturn(mockCustomer);
        Customer responseCustomer = dataPersistenceHandler.persistNewUserDetails(new CustomerRegistrationDto(mockCustomer));
        Assertions.assertEquals("Some@mail.com", responseCustomer.getEmail());
        Assertions.assertEquals("MockCustomer", responseCustomer.getFirstName());*/
        //Todo: Change test case as per new persist user method
    }

    @Test
    void updateCustomerPrismaDetailsTest(){
        PrismaDetailsDto prismaDetailsDto = new PrismaDetailsDto();
        prismaDetailsDto.setPrismaUrl(mockCustomer.getPrismaUrl());
        prismaDetailsDto.setPrismaUsername(mockCustomer.getPrismaUsername());
        prismaDetailsDto.setPrismaPassword(mockCustomer.getPrismaPassword());
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        Mockito.when(customerRepository.save(Mockito.any(Customer.class))).thenReturn(mockCustomer);
        CustomerRegistrationDto responseDto = dataPersistenceHandler.updateCustomerPrismaDetails(customerId, prismaDetailsDto);
        Assertions.assertEquals("wwwMockPrismaUrlcom", responseDto.getPrismaUrl());
        Assertions.assertEquals("Some@mail.com", responseDto.getEmail());
        Assertions.assertEquals("MockCustomer", responseDto.getFirstName());
    }

    @Test
    void updateCustomerPrismaDetailsExceptionTest(){
        PrismaDetailsDto prismaDetailsDto = new PrismaDetailsDto();
        prismaDetailsDto.setPrismaUrl(mockCustomer.getPrismaUrl());
        prismaDetailsDto.setPrismaUsername(mockCustomer.getPrismaUsername());
        prismaDetailsDto.setPrismaPassword(mockCustomer.getPrismaPassword());
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        InvalidUserIdException exception = Assertions.assertThrows(InvalidUserIdException.class,
                () -> dataPersistenceHandler.updateCustomerPrismaDetails(customerId, prismaDetailsDto));
        Assertions.assertEquals(400, exception.getApiError().getStatus().value());
    }

    @Test
    void findByCustomerIdTest(){
        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        dataPersistenceHandler.findByCustomerId(customerId).ifPresent(customer -> {
            Assertions.assertEquals("wwwMockPrismaUrlcom", customer.getPrismaUrl());
            Assertions.assertEquals("Some@mail.com", customer.getEmail());
            Assertions.assertEquals("MockCustomer", customer.getFirstName());
        });
    }

    @Test
    void findByClusterIdTest(){
        Mockito.when(customerRepository.findByClusterId("c0ci0ikd0a0s7i0e7vm0")).thenReturn(Optional.of(mockCustomer));
        dataPersistenceHandler.findByClusterId("c0ci0ikd0a0s7i0e7vm0").ifPresent(customer -> {
            Assertions.assertEquals("wwwMockPrismaUrlcom", customer.getPrismaUrl());
            Assertions.assertEquals("Some@mail.com", customer.getEmail());
            Assertions.assertEquals("MockCustomer", customer.getFirstName());
            Assertions.assertEquals("c0ci0ikd0a0s7i0e7vm0", customer.getClusterId());
        });
    }

    @Test
    void createTasksForCustomerTest(){
        Status mockStatus = Mockito.mock(Status.class);
        mockStatus.setId(1L);
        mockStatus.setName(NOT_STARTED.name());
        Mockito.when(statusRepository.findByName(NOT_STARTED.name())).thenReturn(mockStatus);
        Task task = new Task();
        task.setId(1L);
        task.setName("Test Task");
        List<Task> mockTaskList = new ArrayList<>();
        mockTaskList.add(task);
        Mockito.when(taskRepository.findAll()).thenReturn(mockTaskList);
        Mockito.when(mockStatus.getId()).thenReturn(1L);
        dataPersistenceHandler.createTasksForCustomer(mockCustomer);
        Mockito.verify(taskStatusRepository, Mockito.times(1)).save(Mockito.any(TaskStatus.class));
    }

    @Test
    void updateTaskStatusForCustomerTest(){
        String taskName = "test task";
        TaskStatus mockTaskStatus = new TaskStatus(customerId, 1L, taskName,new Date(), new Date(), new Date(), 1L, NOT_STARTED.name(), 0, "");
        Mockito.when(taskStatusRepository.findByUserIdAndTaskName(customerId, taskName)).thenReturn(Optional.of(mockTaskStatus));
        Status mockStatus = Mockito.mock(Status.class);
        mockStatus.setId(1L);
        mockStatus.setName(NOT_STARTED.name());
        Mockito.when(statusRepository.findByName(NOT_STARTED.name())).thenReturn(mockStatus);
        dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName, NOT_STARTED, 0, "");
        Mockito.verify(taskStatusRepository, Mockito.times(1)).save(Mockito.any(TaskStatus.class));
    }

    @Test
    void updateSuccessfulBuildTaskStatusTest(){
        String taskName = "test task";
        Status mockStatus = Mockito.mock(Status.class);
        mockStatus.setId(1L);
        mockStatus.setName(NOT_STARTED.name());
        Mockito.when(statusRepository.findByName(SUCCESS.name())).thenReturn(mockStatus);
        dataPersistenceHandler.updateSuccessfulBuildTaskStatus(new TaskStatus(customerId, 1L, taskName,new Date(), new Date(), new Date(), 1L, NOT_STARTED.name(), 0, ""));
        Mockito.verify(taskStatusRepository, Mockito.times(1)).save(Mockito.any(TaskStatus.class));
    }

    @Test
    void findTaskStatusByCustomerIdAndTaskNameTest(){
        String taskName = "test task";
        TaskStatus mockTaskStatus = new TaskStatus(customerId, 1L, taskName,new Date(), new Date(), new Date(), 1L, NOT_STARTED.name(), 0, "");
        Mockito.when(taskStatusRepository.findByUserIdAndTaskName(customerId, taskName)).thenReturn(Optional.of(mockTaskStatus));
        dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, taskName).ifPresent(taskStatus -> {
            Assertions.assertEquals(taskName, taskStatus.getTaskName());
            Assertions.assertEquals(NOT_STARTED.name(), taskStatus.getStatusName());
        });
    }
}
