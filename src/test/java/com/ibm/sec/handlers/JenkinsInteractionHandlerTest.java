package com.ibm.sec.handlers;

import com.ibm.sec.entities.TaskStatus;
import com.ibm.sec.services.CustomerService;
import com.ibm.sec.services.JenkinsService;
import com.ibm.sec.services.serviceImpl.JenkinsServiceImpl;
import com.ibm.sec.utils.XmlUtils;
import org.junit.jupiter.api.Assertions;
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

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Date;
import java.util.Optional;

import static com.ibm.sec.configurations.ExternalCallConstants.callTypeRecursive;
import static com.ibm.sec.entities.Status.Statuses.*;
import static com.ibm.sec.entities.Task.Tasks.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.*;

@ExtendWith(SpringExtension.class)
class JenkinsInteractionHandlerTest {

    @Mock
    JenkinsService jenkinsService = new JenkinsServiceImpl();

    @Mock
    DataPersistenceHandler dataPersistenceHandler;

    @InjectMocks
    JenkinsInteractionHandler jenkinsInteractionHandler;

    private final String customerId = "cust_1234";

    private int retryCount = 0;

    private String mockXmlString = "<?xml version='1.1' encoding='UTF-8'?><project><actions/><description></description><keepDependencies>false</keepDependencies><properties><hudson.plugins.jira.JiraProjectProperty plugin=\"jira@3.0.17\"/></properties><scm class=\"hudson.plugins.git.GitSCM\" plugin=\"git@4.2.2\"><configVersion>2</configVersion><userRemoteConfigs><hudson.plugins.git.UserRemoteConfig><url>git@github.ibm.com:Cloud-Security/newcustomer_1621582202914_installation_prisma.git</url><credentialsId>gituser</credentialsId></hudson.plugins.git.UserRemoteConfig></userRemoteConfigs><branches><hudson.plugins.git.BranchSpec><name>*/master</name></hudson.plugins.git.BranchSpec></branches><doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations><submoduleCfg class=\"list\"/><extensions/></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers/><concurrentBuild>false</concurrentBuild><builders><jenkins.plugins.publish__over__ssh.BapSshBuilderPlugin plugin=\"publish-over-ssh@1.22\"><delegate><consolePrefix>SSH: </consolePrefix><delegate plugin=\"publish-over@0.22\"><publishers><jenkins.plugins.publish__over__ssh.BapSshPublisher plugin=\"publish-over-ssh@1.22\"><configName>RemoteServer</configName><verbose>true</verbose><transfers><jenkins.plugins.publish__over__ssh.BapSshTransfer><remoteDirectory>newcustomer_1621582202914_installation_prisma</remoteDirectory><sourceFiles>**/*</sourceFiles><excludes></excludes><removePrefix></removePrefix><remoteDirectorySDF>false</remoteDirectorySDF><flatten>false</flatten><cleanRemote>false</cleanRemote><noDefaultExcludes>false</noDefaultExcludes><makeEmptyDirs>false</makeEmptyDirs><patternSeparator>[, ]+</patternSeparator><execCommand>ansible-playbook ./newcustomer_1621582202914_installation_prisma/ansible_install.yml -e 'ansible_python_interpreter=/usr/bin/python3'</execCommand><execTimeout>120000</execTimeout><usePty>false</usePty><useAgentForwarding>false</useAgentForwarding><useSftpForExec>false</useSftpForExec></jenkins.plugins.publish__over__ssh.BapSshTransfer></transfers><useWorkspaceInPromotion>false</useWorkspaceInPromotion><usePromotionTimestamp>false</usePromotionTimestamp></jenkins.plugins.publish__over__ssh.BapSshPublisher></publishers><continueOnError>false</continueOnError><failOnError>false</failOnError><alwaysPublishFromMaster>false</alwaysPublishFromMaster><hostConfigurationAccess class=\"jenkins.plugins.publish_over_ssh.BapSshPublisherPlugin\" reference=\"../..\"/></delegate></delegate></jenkins.plugins.publish__over__ssh.BapSshBuilderPlugin></builders><publishers/><buildWrappers/></project>";

    @Mock
    private TaskStatus mockTaskStatus = new TaskStatus();

    @BeforeEach
    void setUp(){
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
    void getConfigFileTest(){
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body("Sample test response");
        Mockito.when(jenkinsService.getConfigFile()).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_POLICY.name(), STARTED, retryCount, "");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_INSTALLATION.name(), STARTED, retryCount, "");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_UNINSTALLATION.name(), STARTED, retryCount, "");
        Assertions.assertEquals("Sample test response", jenkinsInteractionHandler.getConfigFile(customerId));
    }

    @Test
    void getConfigFileExceptionTest(){
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body("Sample test response");
        HttpHeaders httpHeaders = new HttpHeaders();
        Mockito.when(jenkinsService.getConfigFile()).thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",httpHeaders,null, null));
        Mockito.when(jenkinsService.getConfigFile(callTypeRecursive)).thenReturn(responseEntity);

        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_POLICY.name(), STARTED, retryCount, "");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_INSTALLATION.name(), STARTED, retryCount, "");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_UNINSTALLATION.name(), STARTED, retryCount, "");

        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_POLICY.name(), FAILED, retryCount, "");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_INSTALLATION.name(), FAILED, retryCount, "");
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_UNINSTALLATION.name(), FAILED, retryCount, "");

        NullPointerException exception = Assertions.assertThrows(NullPointerException.class, () -> {
            jenkinsInteractionHandler.getConfigFile(customerId);
        });
        Assertions.assertNull(exception.getMessage());

    }

    @Test
    void createJenkinsJobPolicyTest(){
        CustomerService.GithubRepositoryType repositoryType = POLICY;
        String taskName = CREATE_JENKINSJOB_FOR_POLICY.name();
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body("Sample test response");

        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);
        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,SUCCESS, retryCount, "");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Sample test response", response.getBody());
    }

    @Test
    void createJenkinsJobInstallationTest(){
        CustomerService.GithubRepositoryType repositoryType = INSTALLATION;
        String taskName = CREATE_JENKINSJOB_FOR_INSTALLATION.name();
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body("Sample test response");

        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);
        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,SUCCESS, retryCount, "");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Sample test response", response.getBody());
    }

    @Test
    void createJenkinsJobUNINSTALLATIONTest(){
        CustomerService.GithubRepositoryType repositoryType = UNINSTALLATION;
        String taskName = CREATE_JENKINSJOB_FOR_UNINSTALLATION.name();
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body("Sample test response");

        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);
        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,SUCCESS, retryCount, "");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Sample test response", response.getBody());
    }

    @Test
    void createJenkinsJobDASHBOARD_INSTALLATIONTest(){
        CustomerService.GithubRepositoryType repositoryType = DASHBOARD_INSTALLATION;
        String taskName = CREATE_JENKINSJOB_FOR_PRISMA_DASHBOARD_INSTALLATION.name();
        ResponseEntity<String> responseEntity = ResponseEntity.ok().body("Sample test response");
        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);

        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,SUCCESS, retryCount, "");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Sample test response", response.getBody());
    }


    @Test
    void createJenkinsJobPolicyExceptionTest(){
        CustomerService.GithubRepositoryType repositoryType = POLICY;
        String taskName = CREATE_JENKINSJOB_FOR_POLICY.name();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(httpHeaders).body("Sample test response");
        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);

        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType))
                 .thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",null,null, null));
        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType,callTypeRecursive)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");

    }

    @Test
    void createJenkinsJobINSTALLATIONExceptionTest(){
        CustomerService.GithubRepositoryType repositoryType = INSTALLATION;
        String taskName = CREATE_JENKINSJOB_FOR_INSTALLATION.name();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(httpHeaders).body("Sample test response");
        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);

        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType))
                 .thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",null,null, null));
        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType,callTypeRecursive)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");

    }

    @Test
    void createJenkinsJobUNINSTALLATIONExceptionTest(){
        CustomerService.GithubRepositoryType repositoryType = UNINSTALLATION;
        String taskName = CREATE_JENKINSJOB_FOR_UNINSTALLATION.name();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(httpHeaders).body("Sample test response");
        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);

        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType))
                 .thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",null,null, null));
        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType,callTypeRecursive)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");

    }

    @Test
    void createJenkinsJobDASHBOARD_INSTALLATIONExceptionTest(){
        CustomerService.GithubRepositoryType repositoryType = DASHBOARD_INSTALLATION;
        String taskName = CREATE_JENKINSJOB_FOR_PRISMA_DASHBOARD_INSTALLATION.name();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(httpHeaders).body("Sample test response");
        String updatedMockXmlString = XmlUtils.updateCustomerDetailsInXml(customerId, mockXmlString, repositoryType);

        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType))
                 .thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",null,null, null));
        Mockito.when(jenkinsService.pushJenkinsJob(customerId, updatedMockXmlString, repositoryType,callTypeRecursive)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");
        ResponseEntity<String> response = jenkinsInteractionHandler.createJenkinsJob(customerId, mockXmlString, repositoryType);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");

    }

    @Test
    void getBuildTaskNamePOLICYTest(){
        Assertions.assertEquals(BUILD_JENKINSJOB_FOR_POLICY.name(), jenkinsInteractionHandler.getBuildTaskName(POLICY.name()));
    }

    @Test
    void getBuildTaskNameINSTALLATIONTest(){
        Assertions.assertEquals(BUILD_JENKINSJOB_FOR_INSTALLATION.name(), jenkinsInteractionHandler.getBuildTaskName(INSTALLATION.name()));
    }

    @Test
    void getBuildTaskNameDASHBOARD_INSTALLATIONTest(){
        Assertions.assertEquals(PRISMA_DASHBOARD_INSTALLATION.name(), jenkinsInteractionHandler.getBuildTaskName(DASHBOARD_INSTALLATION.name()));
    }

    @Test
    void getBuildTaskNameDefaultTest(){
        Assertions.assertEquals(BUILD_JENKINSJOB_FOR_UNINSTALLATION.name(), jenkinsInteractionHandler.getBuildTaskName(UNINSTALLATION.name()));
    }

    @Test
    void getDeleteTaskNamePOLICYTest(){
        Assertions.assertEquals(DELETE_JENKINSJOB_FOR_POLICY.name(), jenkinsInteractionHandler.getDeleteTaskName(POLICY.name()));
    }

    @Test
    void getDeleteTaskNameINSTALLATIONTest(){
        Assertions.assertEquals(DELETE_JENKINSJOB_FOR_INSTALLATION.name(), jenkinsInteractionHandler.getDeleteTaskName(INSTALLATION.name()));
    }

    @Test
    void getDeleteTaskNameDASHBOARD_INSTALLATIONTest(){
        Assertions.assertEquals(DELETE_JENKINSJOB_FOR_PRISMA_DASHBOARD.name(), jenkinsInteractionHandler.getDeleteTaskName(DASHBOARD_INSTALLATION.name()));
    }

    @Test
    void getDeleteTaskNameDefaultTest(){
        Assertions.assertEquals(DELETE_JENKINSJOB_FOR_UNINSTALLATION.name(), jenkinsInteractionHandler.getDeleteTaskName(UNINSTALLATION.name()));
    }

    @Test
    void initiateJenkinsInitialJobBuildTest(){
        String taskName = BUILD_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,BUILD_JENKINSJOB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().headers(httpHeaders).build();
        Mockito.when(jenkinsService.initiateJenkinsJobBuild(customerId,POLICY)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,STARTED, 0, "");
        ResponseEntity<Void> response = jenkinsInteractionHandler.initiateJenkinsJobBuild(customerId, POLICY, JenkinsService.BuildInitializerType.INITIAL);
        Assertions.assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void initiateJenkinsRetryJobBuildTest(){
        String taskName = BUILD_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,BUILD_JENKINSJOB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().headers(httpHeaders).build();
        Mockito.when(jenkinsService.initiateJenkinsJobBuild(customerId,POLICY)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,STARTED, 1, "");
        ResponseEntity<Void> response = jenkinsInteractionHandler.initiateJenkinsJobBuild(customerId, POLICY, JenkinsService.BuildInitializerType.RETRY);
        Assertions.assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void initiateJenkinsJobBuildRetryMaxRetryExceededTest(){
        ReflectionTestUtils.setField(jenkinsInteractionHandler, "retryCount", 5);
        String taskName = BUILD_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        mockTaskStatus.setRetry(4);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,BUILD_JENKINSJOB_FOR_POLICY.name()))
                .thenReturn(Optional.of(mockTaskStatus));
        ResponseEntity<Void> response = jenkinsInteractionHandler.initiateJenkinsJobBuild(customerId, POLICY, JenkinsService.BuildInitializerType.INITIAL);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).findTaskStatusByCustomerIdAndTaskName(customerId,BUILD_JENKINSJOB_FOR_POLICY.name());
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, taskName,FAILED, 5, "Jenkins job trigger build encountered exception, Max allowed retry exhausted");
    }

    @Test
    void initiateJenkinsInitialJobBuildExceptionTest(){
        String taskName = BUILD_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,BUILD_JENKINSJOB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        Mockito.when(jenkinsService.initiateJenkinsJobBuild(customerId, POLICY))
                .thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",null,null, null));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().headers(httpHeaders).build();
        Mockito.when(jenkinsService.initiateJenkinsJobBuild(customerId,POLICY,callTypeRecursive)).thenReturn(responseEntity);
        ResponseEntity<Void> response = jenkinsInteractionHandler.initiateJenkinsJobBuild(customerId, POLICY, JenkinsService.BuildInitializerType.INITIAL);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, taskName,FAILED, 1, "400 Exception");

    }

    @Test
    void deleteJenkinsJobInitialTest(){
        String taskName = DELETE_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,BUILD_JENKINSJOB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().headers(httpHeaders).build();
        Mockito.when(jenkinsService.deleteJenkinsJob(customerId,POLICY)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,STARTED, 0, "");
        ResponseEntity<Void> response = jenkinsInteractionHandler.deleteJenkinsJob(customerId, POLICY, JenkinsService.BuildInitializerType.INITIAL);
        Assertions.assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void deleteJenkinsJobRetryTest(){
        String taskName = DELETE_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,BUILD_JENKINSJOB_FOR_POLICY.name())).thenReturn(Optional.of(mockTaskStatus));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().headers(httpHeaders).build();
        Mockito.when(jenkinsService.deleteJenkinsJob(customerId,POLICY)).thenReturn(responseEntity);
        Mockito.doNothing().when(dataPersistenceHandler).updateTaskStatusForCustomer(customerId, taskName,STARTED, 1, "");
        ResponseEntity<Void> response = jenkinsInteractionHandler.deleteJenkinsJob(customerId, POLICY, JenkinsService.BuildInitializerType.RETRY);
        Assertions.assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void deleteJenkinsJobRetryMaxRetryExceededTest(){
        String taskName = DELETE_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        mockTaskStatus.setRetry(4);
        Mockito.when(mockTaskStatus.getRetry()).thenReturn(4);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,DELETE_JENKINSJOB_FOR_POLICY.name()))
                .thenReturn(Optional.of(mockTaskStatus));
        ResponseEntity<Void> response = jenkinsInteractionHandler.deleteJenkinsJob(customerId, POLICY, JenkinsService.BuildInitializerType.RETRY);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).findTaskStatusByCustomerIdAndTaskName(customerId,taskName);
    }

    @Test
    void deleteJenkinsJobExceptionTest(){
        String taskName = DELETE_JENKINSJOB_FOR_POLICY.name();
        mockTaskStatus.setTaskName(taskName);
        Mockito.when(dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId,taskName))
                .thenReturn(Optional.of(mockTaskStatus));
        Mockito.when(jenkinsService.deleteJenkinsJob(customerId, POLICY))
                .thenThrow(new WebClientResponseException(HttpStatus.BAD_REQUEST.value(), "Exception",null,null, null));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "Application/Json");
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().headers(httpHeaders).build();
        Mockito.when(jenkinsService.initiateJenkinsJobBuild(customerId,POLICY,callTypeRecursive)).thenReturn(responseEntity);
        ResponseEntity<Void> response = jenkinsInteractionHandler.deleteJenkinsJob(customerId, POLICY, JenkinsService.BuildInitializerType.INITIAL);
        Mockito.verify(dataPersistenceHandler, Mockito.times(1)).updateTaskStatusForCustomer(customerId, taskName,STARTED, 1, "400 Exception");

    }
}
