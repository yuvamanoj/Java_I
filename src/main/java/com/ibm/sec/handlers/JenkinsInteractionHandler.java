package com.ibm.sec.handlers;

import com.ibm.sec.services.CustomerService;
import com.ibm.sec.services.JenkinsService;

import com.ibm.sec.services.JenkinsService.BuildInitializerType;
import com.ibm.sec.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

import static com.ibm.sec.configurations.ExternalCallConstants.callTypeRecursive;
import static com.ibm.sec.configurations.ExternalCallConstants.maxAllowedRetryCount;
import static com.ibm.sec.entities.Status.Statuses.*;
import static com.ibm.sec.entities.Task.Tasks.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.*;
import static com.ibm.sec.services.JenkinsService.BuildInitializerType.RETRY;


@Component
public class JenkinsInteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsInteractionHandler.class);

    @Autowired
    JenkinsService jenkinsService;

    @Autowired
    DataPersistenceHandler dataPersistenceHandler;

    int retryCount = 0;

    public String getConfigFile(String customerId, String... callType){
        logger.debug("Entering get config file from jenkins template");
        ResponseEntity<String> getJenkinsConfigTemplateFileResp = null;
        try {
            getJenkinsConfigTemplateFileResp = jenkinsService.getConfigFile(callType);
            logger.debug("Fetched config file from source template , updating database");
            dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_POLICY.name(), STARTED, retryCount, "");
            dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_INSTALLATION.name(), STARTED, retryCount, "");
            dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_UNINSTALLATION.name(), STARTED, retryCount, "");
        }catch (WebClientResponseException e){
            if (retryCount < maxAllowedRetryCount){
                retryCount++;
                logger.error("System encountered error while fetching template it will try to fetch again now , retry count {}",retryCount);
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_POLICY.name(), FAILED, retryCount, e.getMessage());
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_INSTALLATION.name(), FAILED, retryCount, e.getMessage());
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_UNINSTALLATION.name(), FAILED, retryCount, e.getMessage());
                getConfigFile(customerId, callTypeRecursive);
            }
        }
        retryCount = 0;
        logger.debug("Exiting get config file from jenkins template");
        return Objects.requireNonNull(getJenkinsConfigTemplateFileResp).getBody();
    }

    public ResponseEntity<String> createJenkinsJob(String customerId, String xmlContentPayload,CustomerService.GithubRepositoryType githubRepositoryType, String... callType){
        logger.debug("Entering create jenkins Job");
        ResponseEntity<String> createJenkinsJobResp = null;
        try {
            String updatedXmlContentPayload = XmlUtils.updateCustomerDetailsInXml(customerId, xmlContentPayload, githubRepositoryType);
            createJenkinsJobResp = jenkinsService.pushJenkinsJob(customerId, updatedXmlContentPayload, githubRepositoryType, callType);
            logger.debug("Jenkins job pushed successfully , updating database with status of task");
            if (githubRepositoryType == POLICY)
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_POLICY.name(),SUCCESS, retryCount, "");
            if (githubRepositoryType == INSTALLATION)
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_INSTALLATION.name(),SUCCESS, retryCount, "");
            if (githubRepositoryType == UNINSTALLATION)
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_UNINSTALLATION.name(),SUCCESS, retryCount, "");
            if (githubRepositoryType == DASHBOARD_INSTALLATION)
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_PRISMA_DASHBOARD_INSTALLATION.name(),SUCCESS, retryCount, "");
            logger.debug("Jenkins job creation successful status : {} for payload :{}",createJenkinsJobResp.getStatusCodeValue(),updatedXmlContentPayload);
        }catch (WebClientResponseException e){
            if (retryCount < maxAllowedRetryCount){
                retryCount++;
                logger.error("Jenkins job creation encountered exception, system will retry now status code : {} retry count : {} Message {}: ",e.getRawStatusCode(),retryCount,e.getMessage());
                if (githubRepositoryType == POLICY)
                    dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_POLICY.name(),FAILED, retryCount, e.getMessage());
                if (githubRepositoryType == INSTALLATION)
                    dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_INSTALLATION.name(),FAILED, retryCount, e.getMessage());
                if (githubRepositoryType == UNINSTALLATION)
                    dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_UNINSTALLATION.name(),FAILED, retryCount, e.getMessage());
                if (githubRepositoryType == DASHBOARD_INSTALLATION)
                    dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_JENKINSJOB_FOR_PRISMA_DASHBOARD_INSTALLATION.name(),FAILED, retryCount, e.getMessage());
                createJenkinsJob(customerId, xmlContentPayload, githubRepositoryType, callTypeRecursive);
            }
            logger.error("Jenkins job creation encountered exception, Max allowed retry exhausted : {} retry count : {} Message : {}",e.getRawStatusCode(),retryCount,e.getMessage());
        }
        retryCount = 0;
        logger.debug("Exiting create jenkins Job");
        return createJenkinsJobResp;
    }

    public ResponseEntity<Void> initiateJenkinsJobBuild(String customerId, CustomerService.GithubRepositoryType githubRepositoryType, BuildInitializerType buildInitializerType, String... callType){
        logger.debug("Entering initiate jenkins job build");
        ResponseEntity<Void> initiateJenkinsJobResp = null;
        //Updating retryCount from db
        String taskName = getBuildTaskName(githubRepositoryType.name());
        dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, taskName).ifPresent(taskStatus -> {
            if (buildInitializerType == RETRY)
                retryCount = taskStatus.getRetry() + 1;
        });
        try{
            if (retryCount < maxAllowedRetryCount){
                initiateJenkinsJobResp = jenkinsService.initiateJenkinsJobBuild(customerId,githubRepositoryType,callType);
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,STARTED, retryCount, "");
            }else{
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,FAILED, retryCount, "Jenkins job trigger build encountered exception, Max allowed retry exhausted");
            }
        }catch (WebClientResponseException e){
            if (retryCount < maxAllowedRetryCount){
                retryCount++;
                logger.error("Jenkins job trigger build encountered exception, system will retry now status code : {} retry count : {} Message : {}",e.getRawStatusCode(), retryCount,e.getMessage());
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,FAILED, retryCount, e.getMessage());
                initiateJenkinsJobBuild(customerId, githubRepositoryType, RETRY, callTypeRecursive);
            }
            logger.error("Jenkins job trigger build encountered exception, Max allowed retry exhausted : {} retry count : {} Message : {}",e.getRawStatusCode(),retryCount,e.getMessage());
        }
        retryCount = 0;
        logger.debug("Exiting create jenkins Job");
        return initiateJenkinsJobResp;
    }

    public String getBuildTaskName(String jobName) {
        if (jobName.equalsIgnoreCase(POLICY.name())) return BUILD_JENKINSJOB_FOR_POLICY.name();
        if (jobName.equalsIgnoreCase(INSTALLATION.name())) return BUILD_JENKINSJOB_FOR_INSTALLATION.name();
        if (jobName.equalsIgnoreCase(DASHBOARD_INSTALLATION.name())) return PRISMA_DASHBOARD_INSTALLATION.name();
        return BUILD_JENKINSJOB_FOR_UNINSTALLATION.name();
    }
    public String getDeleteTaskName(String jobName) {
        if (jobName.equalsIgnoreCase(POLICY.name())) return DELETE_JENKINSJOB_FOR_POLICY.name();
        if (jobName.equalsIgnoreCase(INSTALLATION.name())) return DELETE_JENKINSJOB_FOR_INSTALLATION.name();
        if (jobName.equalsIgnoreCase(DASHBOARD_INSTALLATION.name())) return DELETE_JENKINSJOB_FOR_PRISMA_DASHBOARD.name();
        return DELETE_JENKINSJOB_FOR_UNINSTALLATION.name();
    }
    
    public ResponseEntity<Void> deleteJenkinsJob(String customerId,CustomerService.GithubRepositoryType githubRepositoryType, BuildInitializerType buildInitializerType, String... callType){
        logger.debug("Entering delete jenkins Job");
    	String taskName = getDeleteTaskName(githubRepositoryType.name());
        ResponseEntity<Void> deleteJenkinsJobResp = null;
        try {
            dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, taskName).ifPresent(taskStatus -> {
                if (buildInitializerType == RETRY)
                    retryCount = taskStatus.getRetry() + 1;
                else
                	retryCount=0;
            });
            if (retryCount < maxAllowedRetryCount){
                 deleteJenkinsJobResp = jenkinsService.deleteJenkinsJob(customerId,githubRepositoryType, callType);
                 dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,SUCCESS, retryCount, "");
            }else{
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,FAILED, maxAllowedRetryCount, "Jenkins job Deletion encountered exception, Max allowed retry exhausted");
            }
        }catch (WebClientResponseException e){
            if (retryCount < maxAllowedRetryCount){
                retryCount++;
                logger.error("Jenkins job deletion encountered exception, system will retry now retry count : {} Message : {}",retryCount,e.getMessage());
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,STARTED, retryCount, e.getMessage());
                deleteJenkinsJob(customerId, githubRepositoryType, RETRY, callTypeRecursive);
            }    
        }
        retryCount = 0;
        logger.debug("Exiting delete jenkins Job");
        return deleteJenkinsJobResp;
    }    
}
