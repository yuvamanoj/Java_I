package com.ibm.sec.services;

import com.ibm.sec.dtos.CustomerClusterDetailsDto;
import com.ibm.sec.dtos.CustomerRegistrationDto;
import com.ibm.sec.dtos.JobDetailsDto;
import com.ibm.sec.dtos.PrismaDetailsDto;
import com.ibm.sec.entities.Customer;
import com.ibm.sec.entities.TaskStatus;
import com.ibm.sec.exceptions.ClientDataException;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.exceptions.InvalidUserIdException;
import com.ibm.sec.handlers.DataPersistenceHandler;
import com.ibm.sec.handlers.GithubInteractionHandler;
import com.ibm.sec.handlers.JenkinsInteractionHandler;
import com.ibm.sec.producer.MessageKafkaProducer;
import com.ibm.sec.services.serviceImpl.CustomerServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Base64Utils;

import java.util.Date;
import java.util.Optional;

import static com.ibm.sec.entities.Status.Statuses.STARTED;
import static com.ibm.sec.entities.Status.Statuses.SUCCESS;
import static com.ibm.sec.entities.Task.Tasks.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.*;
import static com.ibm.sec.services.JenkinsService.BuildInitializerType.INITIAL;
import static com.ibm.sec.services.JenkinsService.BuildInitializerType.RETRY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class CustomerServiceTest {
    private final Customer mockCustomer = new Customer();

    private final TaskStatus mockTaskStatus = new TaskStatus();

    @Mock
    private ErrorMessages errorMessages;

    @Mock
    private DataPersistenceHandler dataPersistenceHandler;

    @Mock
    private JenkinsInteractionHandler jenkinsInteractionHandler;

    @Mock
    private GithubInteractionHandler githubInteractionHandler;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private MessageKafkaProducer producer;


    @BeforeEach
    void setUp(){
        mockCustomer.setId("Cust_1234");
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

        ReflectionTestUtils.setField(customerService,"dataPersistenceHandler", dataPersistenceHandler);
        ReflectionTestUtils.setField(customerService,"jenkinsInteractionHandler", jenkinsInteractionHandler);

        mockTaskStatus.setUpdatedDateTime(new Date());
        mockTaskStatus.setError("");
        mockTaskStatus.setRetry(0);
        mockTaskStatus.setStatusName(STARTED.name());
        mockTaskStatus.setStatusId(1L);
        mockTaskStatus.setCreatedDateTime(new Date());
        mockTaskStatus.setId(1L);
        mockTaskStatus.setTaskId(1L);
        mockTaskStatus.setTaskName(PERSIST_NEWUSERDETAILS.name());
        mockTaskStatus.setInitiatedDateTime(new Date());
        mockTaskStatus.setUserId(mockCustomer.getId());
    }

    @Test
    void getClusterDetailsByCustomerIdTest(){
        Mockito.when(dataPersistenceHandler.findByCustomerId(Mockito.anyString())).thenReturn(Optional.of(mockCustomer));
        CustomerClusterDetailsDto customerClusterDetailsDto = customerService.getClusterDetailsByCustomerId(Mockito.anyString());
        assertEquals(mockCustomer.getId(), customerClusterDetailsDto.getCustomerId());
        assertEquals(new String(Base64Utils.decodeFromString(mockCustomer.getApiKey())), customerClusterDetailsDto.getApiKey());
    }

    @Test
    void getClusterDetailsByCustomerIdExceptionTest(){
        Mockito.when(dataPersistenceHandler.findByCustomerId(Mockito.anyString())).thenReturn(Optional.empty());
        InvalidUserIdException exception = assertThrows(InvalidUserIdException.class ,
                () -> customerService.getClusterDetailsByCustomerId(Mockito.anyString()));
        assertEquals(400, exception.getApiError().getStatus().value());
    }

    @Test
    void updateCustomerPrismaDetailsTest(){
        CustomerRegistrationDto customerRegistrationDto = new CustomerRegistrationDto(mockCustomer);
        PrismaDetailsDto prismaDetailsDto = new PrismaDetailsDto();
        prismaDetailsDto.setPrismaUrl("test-url");
        prismaDetailsDto.setPrismaUsername("test-user");
        prismaDetailsDto.setPrismaPassword("test-pass");
        Mockito.when(dataPersistenceHandler.updateCustomerPrismaDetails(mockCustomer.getId(), prismaDetailsDto))
                .thenReturn(customerRegistrationDto);
        CustomerRegistrationDto response = customerService.updateCustomerPrismaDetails(mockCustomer.getId(), prismaDetailsDto);
        Assertions.assertEquals("MockCustomer", response.getFirstName());
        Assertions.assertEquals("Some@mail.com", response.getEmail());
    }

    @Test
    void getCustomerPrismaDetailsTest(){
        mockCustomer.setPrismaUrl(new String(Base64Utils.encode(mockCustomer.getPrismaUrl().getBytes())));
        mockCustomer.setPrismaUsername(new String(Base64Utils.encode(mockCustomer.getPrismaUsername().getBytes())));
        mockCustomer.setPrismaPassword(new String(Base64Utils.encode(mockCustomer.getPrismaPassword().getBytes())));
        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(mockCustomer));
        PrismaDetailsDto prismaDetailsDto = customerService.getCustomerPrismaDetails(mockCustomer.getId());
        Assertions.assertEquals(new String(Base64Utils.decodeFromString(mockCustomer.getPrismaUrl())), prismaDetailsDto.getPrismaUrl());
        Assertions.assertEquals(new String(Base64Utils.decodeFromString(mockCustomer.getPrismaUsername())), prismaDetailsDto.getPrismaUsername());
        Assertions.assertEquals(new String(Base64Utils.decodeFromString(mockCustomer.getPrismaPassword())), prismaDetailsDto.getPrismaPassword());
    }

    @Test
    void getCustomerPrismaDetailsInvalidUserExceptionTest(){
        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.empty());
        InvalidUserIdException exception = assertThrows(InvalidUserIdException.class ,
                () -> customerService.getCustomerPrismaDetails(mockCustomer.getId()));
        assertEquals(400, exception.getApiError().getStatus().value());
    }

    @Test
    void getCustomerPrismaDetailsNoUrlClientDataExceptionTest(){
        Customer customer = Mockito.mock(Customer.class);
        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(customer));
        Mockito.when(customer.getPrismaUrl()).thenReturn(null);
        ClientDataException exception = assertThrows(ClientDataException.class ,
                () -> customerService.getCustomerPrismaDetails(mockCustomer.getId()));
        assertEquals(400, exception.getApierror().getStatus().value());
    }

    @Test
    void getCustomerPrismaDetailsNoUserNameClientDataExceptionTest(){
        Customer customer = Mockito.mock(Customer.class);
        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(customer));
        Mockito.when(customer.getPrismaUrl()).thenReturn("somdummyUrl.com");
        Mockito.when(customer.getPrismaUsername()).thenReturn(null);
        ClientDataException exception = assertThrows(ClientDataException.class ,
                () -> customerService.getCustomerPrismaDetails(mockCustomer.getId()));
        assertEquals(400, exception.getApierror().getStatus().value());
    }

    @Test
    void getCustomerPrismaDetailsNoPasswordClientDataExceptionTest(){
        Customer customer = Mockito.mock(Customer.class);
        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(customer));
        Mockito.when(customer.getPrismaUrl()).thenReturn("somdummyUrl.com");
        Mockito.when(customer.getPrismaUsername()).thenReturn("somdummyUser");
        Mockito.when(customer.getPrismaPassword()).thenReturn(null);
        ClientDataException exception = assertThrows(ClientDataException.class ,
                () -> customerService.getCustomerPrismaDetails(mockCustomer.getId()));
        assertEquals(400, exception.getApierror().getStatus().value());
    }

    @Test
    void updateJobStatusForCustomerInstallationTest(){
        String taskName = BUILD_JENKINSJOB_FOR_INSTALLATION.name();
        mockTaskStatus.setTaskName(taskName);
        JobDetailsDto jobDetailsDto = new JobDetailsDto();
        jobDetailsDto.setJobName(INSTALLATION.name());
        jobDetailsDto.setStatus(Boolean.TRUE);

        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(mockCustomer));
        Mockito.when(jenkinsInteractionHandler.getBuildTaskName(jobDetailsDto.getJobName())).thenReturn(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(mockCustomer.getId(),taskName)).thenReturn(Optional.of(mockTaskStatus));
        Mockito.doNothing().when(dataPersistenceHandler).updateSuccessfulBuildTaskStatus(mockTaskStatus);

        ResponseEntity<Void> jenkinsResponse = ResponseEntity.ok().build();
        Mockito.when(jenkinsInteractionHandler.initiateJenkinsJobBuild(mockCustomer.getId(), POLICY, INITIAL)).thenReturn(jenkinsResponse);
        String response = customerService.updateJobStatusForCustomer(mockCustomer.getId(), jobDetailsDto);
        Assertions.assertEquals(taskName + " status updated successfully", response);
    }

    @Test
    void updateJobStatusForCustomerPolicyTest(){
        String taskName = BUILD_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        JobDetailsDto jobDetailsDto = new JobDetailsDto();
        jobDetailsDto.setJobName(POLICY.name());
        jobDetailsDto.setStatus(Boolean.TRUE);

        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(mockCustomer));
        Mockito.when(jenkinsInteractionHandler.getBuildTaskName(jobDetailsDto.getJobName())).thenReturn(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(mockCustomer.getId(),taskName)).thenReturn(Optional.of(mockTaskStatus));
        Mockito.doNothing().when(dataPersistenceHandler).updateSuccessfulBuildTaskStatus(mockTaskStatus);

        ResponseEntity<Void> jenkinsResponse = ResponseEntity.ok().build();
        Mockito.when(jenkinsInteractionHandler.initiateJenkinsJobBuild(mockCustomer.getId(), DASHBOARD_INSTALLATION, INITIAL)).thenReturn(jenkinsResponse);
        String response = customerService.updateJobStatusForCustomer(mockCustomer.getId(), jobDetailsDto);
        Assertions.assertEquals(taskName + " status updated successfully", response);
    }

    @Test
    void updateJobStatusForCustomerUninstallationTest(){
        String taskName = BUILD_JENKINSJOB_FOR_UNINSTALLATION.name();
        mockTaskStatus.setTaskName(taskName);
        JobDetailsDto jobDetailsDto = new JobDetailsDto();
        jobDetailsDto.setJobName(UNINSTALLATION.name());
        jobDetailsDto.setStatus(Boolean.TRUE);

        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(mockCustomer));
        Mockito.when(jenkinsInteractionHandler.getBuildTaskName(jobDetailsDto.getJobName())).thenReturn(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(mockCustomer.getId(),taskName)).thenReturn(Optional.of(mockTaskStatus));
        Mockito.doNothing().when(dataPersistenceHandler).updateSuccessfulBuildTaskStatus(mockTaskStatus);
        Mockito.doNothing().when(githubInteractionHandler).deleteGithubRepositoryForCustomer(mockCustomer.getId(), INSTALLATION);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().headers(httpHeaders).build();
        Mockito.when(jenkinsInteractionHandler.deleteJenkinsJob(mockCustomer.getId(), UNINSTALLATION, INITIAL)).thenReturn(responseEntity);

        String response = customerService.updateJobStatusForCustomer(mockCustomer.getId(), jobDetailsDto);
        Assertions.assertEquals(taskName + " status updated successfully", response);
    }

    @Test
    void updateJobStatusForCustomerJobRetryTest(){
        String taskName = BUILD_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        JobDetailsDto jobDetailsDto = new JobDetailsDto();
        jobDetailsDto.setJobName(POLICY.name());
        jobDetailsDto.setStatus(Boolean.FALSE);

        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.of(mockCustomer));
        Mockito.when(jenkinsInteractionHandler.getBuildTaskName(jobDetailsDto.getJobName())).thenReturn(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(mockCustomer.getId(),taskName)).thenReturn(Optional.of(mockTaskStatus));
        Mockito.doNothing().when(dataPersistenceHandler).updateSuccessfulBuildTaskStatus(mockTaskStatus);

        ResponseEntity<Void> jenkinsResponse = ResponseEntity.ok().build();
        Mockito.when(jenkinsInteractionHandler.initiateJenkinsJobBuild(mockCustomer.getId(), DASHBOARD_INSTALLATION, RETRY)).thenReturn(jenkinsResponse);
        String response = customerService.updateJobStatusForCustomer(mockCustomer.getId(), jobDetailsDto);
        Assertions.assertEquals(taskName + " status updated successfully", response);
    }

    @Test
    void updateJobStatusForCustomerInvalidUserException(){
        Mockito.when(dataPersistenceHandler.findByCustomerId( mockCustomer.getId())).thenReturn(Optional.empty());
        JobDetailsDto jobDetailsDto = new JobDetailsDto();
        jobDetailsDto.setJobName(POLICY.name());
        jobDetailsDto.setStatus(Boolean.TRUE);
        InvalidUserIdException exception = assertThrows(InvalidUserIdException.class ,
                () -> customerService.updateJobStatusForCustomer(mockCustomer.getId(), jobDetailsDto));
        assertEquals(400, exception.getApiError().getStatus().value());
    }

    @Test
    void registerUserTest(){
        String testConfigString = "Sample String";
        CustomerRegistrationDto registrationDto = new CustomerRegistrationDto(mockCustomer);
        Mockito.when(dataPersistenceHandler.findByClusterId( mockCustomer.getId())).thenReturn(Optional.empty());
        Mockito.when(dataPersistenceHandler.persistNewUserDetails(registrationDto)).thenReturn(mockCustomer);
        Mockito.doNothing().when(dataPersistenceHandler).createTasksForCustomer(mockCustomer);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(mockCustomer.getId(), PERSIST_NEWUSERDETAILS.name(), SUCCESS, 0, "");
        Mockito.doNothing().when(githubInteractionHandler).createGithubRepositoryForCustomer(mockCustomer, POLICY);
        Mockito.doNothing().when(githubInteractionHandler).createGithubRepositoryForCustomer(mockCustomer, INSTALLATION);
        Mockito.when(jenkinsInteractionHandler.getConfigFile(mockCustomer.getId())).thenReturn(testConfigString);
        ResponseEntity<String> jenkinsStringResponse = ResponseEntity.ok().build();
        Mockito.when(jenkinsInteractionHandler.createJenkinsJob(mockCustomer.getId(), testConfigString, POLICY)).thenReturn(jenkinsStringResponse);
        Mockito.when(jenkinsInteractionHandler.createJenkinsJob(mockCustomer.getId(), testConfigString, INSTALLATION)).thenReturn(jenkinsStringResponse);
        ResponseEntity<Void> jenkinsVoidResponse = ResponseEntity.ok().build();
        Mockito.when(jenkinsInteractionHandler.initiateJenkinsJobBuild(mockCustomer.getId(), INSTALLATION, INITIAL)).thenReturn(jenkinsVoidResponse);
        Mockito.when(jenkinsInteractionHandler.createJenkinsJob(mockCustomer.getId(), testConfigString, UNINSTALLATION)).thenReturn(jenkinsStringResponse);
        Mockito.when(jenkinsInteractionHandler.createJenkinsJob(mockCustomer.getId(), testConfigString, DASHBOARD_INSTALLATION)).thenReturn(jenkinsStringResponse);
        CustomerRegistrationDto response = customerService.registerUser(registrationDto);
        Assertions.assertEquals(mockCustomer.getId(), response.getId());
        Assertions.assertEquals(mockCustomer.getFirstName(), response.getFirstName());
        Assertions.assertEquals(mockCustomer.getLastName(), response.getLastName());
    }
}
