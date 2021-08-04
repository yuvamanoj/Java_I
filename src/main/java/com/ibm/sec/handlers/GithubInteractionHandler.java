package com.ibm.sec.handlers;

import static com.ibm.sec.configurations.ExternalCallConstants.*;
import static com.ibm.sec.entities.Status.Statuses.FAILED;
import static com.ibm.sec.entities.Status.Statuses.STARTED;
import static com.ibm.sec.entities.Status.Statuses.SUCCESS;
import static com.ibm.sec.entities.Task.Tasks.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.INSTALLATION;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.POLICY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ibm.sec.entities.Customer;
import com.ibm.sec.entities.Status;
import com.ibm.sec.services.CustomerService;
import com.ibm.sec.services.GithubService;


@Component
public class GithubInteractionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GithubInteractionHandler.class);
    @Autowired
    GithubService githubService;

    @Autowired
    DataPersistenceHandler dataPersistenceHandler;

    int retryCount = 0;

    /*public void createGithubRepositoryForCustomer(Customer customer, CustomerService.GithubRepositoryType repositoryType) {
        logger.debug(" Entering create github repository for customer");
        try{
            if (repositoryType == POLICY){
                githubService.createGithubRepositoryFromTemplate(templatePolicyRepo, customer.getId()+customerPolicyGithubRepoExtension);
            }
            if(repositoryType == INSTALLATION){
                githubService.createGithubRepositoryFromTemplate(templateInstallationRepo, customer.getId()+customerInstallationGithubRepoExtension);
            }
            logger.debug("Github Repository creation successful, Updating status in database");
            updateCreateRepositoryTaskStatusForCustomer(customer.getId(),repositoryType,STARTED, retryCount, "");
        }
        catch (WebClientResponseException e){
            if (retryCount < maxAllowedRetryCount){
                retryCount++;
                updateCreateRepositoryTaskStatusForCustomer(customer.getId(),repositoryType,FAILED, retryCount, e.getMessage());
                logger.error("Github repository creation encountered error system will retry now, retry count: {} status code: {} Message: {}", retryCount, e.getRawStatusCode(), e.getMessage());
                createGithubRepositoryForCustomer(customer,repositoryType);
            }else{
                updateCreateRepositoryTaskStatusForCustomer(customer.getId(),repositoryType,FAILED, retryCount, e.getMessage());
                logger.error("Github repository creation encountered error, retry count: {} status code: {} Message: {}", retryCount,e.getRawStatusCode(), e.getMessage());
            }
            logger.error("Github repository creation encountered error");
        }
        retryCount = 0;
        logger.debug(" Exiting create github repository for customer");
    }*/

    public void createGithubRepositoryForCustomer(Customer customer, CustomerService.GithubRepositoryType repositoryType) {
        logger.debug(" Entering create github repository for customer");
        String task = (repositoryType == POLICY)?CREATE_REPOPOLICY.name():CREATE_REPOINSTALLATION.name();
        try{
            dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customer.getId(), task).ifPresent(taskStatus -> {
                if (taskStatus.getRetry()>0)
                    retryCount = taskStatus.getRetry() + 1;
                else
                    retryCount=0;
            });
            if (repositoryType == POLICY){
                githubService.createGithubRepositoryFromTemplate(templatePolicyRepo, customer.getId()+customerPolicyGithubRepoExtension);
            }
            if(repositoryType == INSTALLATION){
                githubService.createGithubRepositoryFromTemplate(templateInstallationRepo, customer.getId()+customerInstallationGithubRepoExtension);
            }
            logger.debug("Github Repository creation successful, Updating status in database");
            updateCreateRepositoryTaskStatusForCustomer(customer.getId(),repositoryType,SUCCESS, retryCount, "");
        }
        catch (WebClientResponseException e){

            dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customer.getId(), task).ifPresent(taskStatus -> {
                retryCount = taskStatus.getRetry();
            });

            if (retryCount < maxAllowedRetryCount){
                retryCount++;
                updateCreateRepositoryTaskStatusForCustomer(customer.getId(),repositoryType,STARTED, retryCount, e.getMessage());
                logger.error("Github repository creation encountered error system will retry now, retry count: {} status code: {} Message: {}", retryCount, e.getRawStatusCode(), e.getMessage());
                createGithubRepositoryForCustomer(customer,repositoryType);
            }else{
                updateCreateRepositoryTaskStatusForCustomer(customer.getId(),repositoryType,FAILED, retryCount, e.getMessage());
                logger.error("Github repository creation encountered error, retry count: {} status code: {} Message: {}", retryCount,e.getRawStatusCode(), e.getMessage());
            }
            logger.error("Github repository creation encountered error");
        }
        retryCount = 0;
        logger.debug(" Exiting create github repository for customer");
    }


    private void updateCreateRepositoryTaskStatusForCustomer(String customerId, CustomerService.GithubRepositoryType githubRepositoryType, Status.Statuses status , int retryCount, String errorMessage){
        if (githubRepositoryType == POLICY)
            dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_REPOPOLICY.name(),status, retryCount, errorMessage);
        if(githubRepositoryType == INSTALLATION)
            dataPersistenceHandler.updateTaskStatusForCustomer(customerId, CREATE_REPOINSTALLATION.name(),status, retryCount, errorMessage);
    }
    
    public void deleteGithubRepositoryForCustomer(String customerId, CustomerService.GithubRepositoryType repositoryType, String... callType) {
        logger.debug(" Entering delete github repository for customer");
        String taskName=(repositoryType == POLICY)?DELETE_GITHUB_FOR_POLICY.name():DELETE_GITHUB_FOR_INSTALLATION.name();
        try{
            dataPersistenceHandler.findTaskStatusByCustomerIdAndTaskName(customerId, taskName).ifPresent(taskStatus -> {
                if (taskStatus.getRetry()>0)
                    retryCount = taskStatus.getRetry() + 1;
                else
                	retryCount=0;
            });  
            String repoName=(repositoryType == POLICY)?customerId+customerPolicyGithubRepoExtension:customerId+customerInstallationGithubRepoExtension;
            if (retryCount < maxAllowedRetryCount){
                githubService.deleteRepository(customerId,repoName, callType);
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId,taskName,SUCCESS,retryCount,"");
               }else{
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,FAILED, maxAllowedRetryCount, "Github repo Deletion encountered exception, Max allowed retry exhausted");
               }
        }
        catch (WebClientResponseException e){
            if (retryCount < maxAllowedRetryCount){
                retryCount++;
                logger.error("github repo deletion encountered exception, system will retry now retry count : {} Message : {}", retryCount, e.getMessage());
                dataPersistenceHandler.updateTaskStatusForCustomer(customerId, taskName,STARTED, retryCount, e.getMessage());
                deleteGithubRepositoryForCustomer(customerId, repositoryType, callTypeRecursive);
            }
            logger.error("Github repository deletion encountered error");
        }
        logger.debug(" Exiting deletion github repository for customer");
    } 
}
