package com.ibm.sec.handlers;

import com.ibm.sec.entities.Customer;
import com.ibm.sec.entities.TaskStatus;
import com.ibm.sec.services.GithubService;
import com.ibm.sec.services.serviceImpl.GithubServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Date;
import java.util.Optional;

import static com.ibm.sec.configurations.ExternalCallConstants.*;
import static com.ibm.sec.entities.Status.Statuses.*;
import static com.ibm.sec.entities.Task.Tasks.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.INSTALLATION;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.POLICY;

@ExtendWith(SpringExtension.class)
public class GithubInteractionHandlerTest {

    @Mock
    GithubService githubService = new GithubServiceImpl();

    @Mock
    DataPersistenceHandler dataPersistenceHandler;

    @InjectMocks
    GithubInteractionHandler githubInteractionHandler;

    private final String customerId = "cust_1234";

    private Customer mockCustomer = new Customer();

    @Mock
    private TaskStatus mockTaskStatus = new TaskStatus();

    @BeforeEach
    void setUp(){
        mockCustomer.setId(customerId);
        mockTaskStatus.setId(1L);
        mockTaskStatus.setUserId(customerId);
        mockTaskStatus.setInitiatedDateTime(new Date());
        mockTaskStatus.setCreatedDateTime(new Date());
        mockTaskStatus.setUpdatedDateTime(new Date());
        mockTaskStatus.setTaskId(1L);
        mockTaskStatus.setStatusId(1L);
        mockTaskStatus.setStatusName(STARTED.name());
        mockTaskStatus.setRetry(0);
        mockTaskStatus.setError("");
    }

    @Test
    void createGithubRepositoryForCustomerPolicyTest(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(httpHeaders).body("Sample test response");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_REPOPOLICY.name(), STARTED, 0, "");
        Mockito.when(githubService.createGithubRepositoryFromTemplate(templatePolicyRepo, mockCustomer.getId()+customerPolicyGithubRepoExtension)).thenReturn(responseEntity);
        githubInteractionHandler.createGithubRepositoryForCustomer(mockCustomer, POLICY);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).findTaskStatusByCustomerIdAndTaskName(customerId, CREATE_REPOPOLICY.name());
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, CREATE_REPOPOLICY.name(), SUCCESS, 0, "");
    }

    @Test
    void createGithubRepositoryForCustomerInstallationTest(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(httpHeaders).body("Sample test response");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(), STARTED, 0, "");
        Mockito.when(githubService.createGithubRepositoryFromTemplate(templateInstallationRepo, mockCustomer.getId()+customerInstallationGithubRepoExtension)).thenReturn(responseEntity);
        githubInteractionHandler.createGithubRepositoryForCustomer(mockCustomer, INSTALLATION);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).findTaskStatusByCustomerIdAndTaskName(customerId, CREATE_REPOINSTALLATION.name());
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(), SUCCESS, 0, "");
    }

    @Test
    void createGithubRepositoryForCustomerExceptionTest(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(), STARTED, 0, "");
        Mockito.when(githubService.createGithubRepositoryFromTemplate(templateInstallationRepo, mockCustomer.getId()+customerInstallationGithubRepoExtension))
                .thenThrow(new WebClientResponseException(HttpStatus.UNAUTHORIZED.value(), "Exception",httpHeaders,null, null));
        githubInteractionHandler.createGithubRepositoryForCustomer(mockCustomer, INSTALLATION);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(), FAILED, 3, "401 Exception");
    }

    @Test
    void createGithubRepositoryForCustomerMaxRetryExhaustedTest(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(), STARTED, 0, "");
        Mockito.when(githubService.createGithubRepositoryFromTemplate(templateInstallationRepo, mockCustomer.getId()+customerInstallationGithubRepoExtension))
                .thenThrow(new WebClientResponseException(HttpStatus.UNAUTHORIZED.value(), "Exception",httpHeaders,null, null));
        githubInteractionHandler.createGithubRepositoryForCustomer(mockCustomer, INSTALLATION);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(), FAILED, 3, "401 Exception");
    }

    @Test
    void deleteGithubRepositoryForCustomerInitialTest(){
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, DELETE_GITHUB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        githubInteractionHandler.deleteGithubRepositoryForCustomer(customerId,POLICY);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, DELETE_GITHUB_FOR_POLICY.name(), SUCCESS, 0, "");
    }

    @Test
    void deleteGithubRepositoryForCustomerRetryTest(){
        Mockito.when(mockTaskStatus.getRetry()).thenReturn(1);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, DELETE_GITHUB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        githubInteractionHandler.deleteGithubRepositoryForCustomer(customerId,POLICY);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, DELETE_GITHUB_FOR_POLICY.name(), SUCCESS, 2, "");
    }

    @Test
    void deleteGithubRepositoryForCustomerMaxRetryExhaustedTest(){
        Mockito.when(mockTaskStatus.getRetry()).thenReturn(2);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, DELETE_GITHUB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        githubInteractionHandler.deleteGithubRepositoryForCustomer(customerId,POLICY);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, DELETE_GITHUB_FOR_POLICY.name(), FAILED, 3, "Github repo Deletion encountered exception, Max allowed retry exhausted");
    }

    @Test
    void deleteGithubRepositoryForCustomerExceptionTest(){
        Mockito.when(mockTaskStatus.getRetry()).thenReturn(1);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, DELETE_GITHUB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        Mockito.when(githubService.deleteRepository(customerId,customerId+customerPolicyGithubRepoExtension))
                .thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",null,null, null));
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(), STARTED, 0, "");
        githubInteractionHandler.deleteGithubRepositoryForCustomer(customerId,POLICY);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, DELETE_GITHUB_FOR_POLICY.name(), STARTED, 3, "400 Exception");
    }

}
