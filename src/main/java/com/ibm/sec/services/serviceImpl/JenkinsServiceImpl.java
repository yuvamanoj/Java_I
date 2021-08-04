package com.ibm.sec.services.serviceImpl;

import com.ibm.sec.configurations.ApplicationConfigs;
import com.ibm.sec.configurations.JenkinsConfigs;
import com.ibm.sec.exceptions.ApiError;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.exceptions.NoResponseException;
import com.ibm.sec.handlers.RetryExceptionHandler;
import com.ibm.sec.services.CustomerService;
import com.ibm.sec.services.JenkinsService;
import com.ibm.sec.services.CustomerService.GithubRepositoryType;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static com.ibm.sec.configurations.ExternalCallConstants.*;
import static com.ibm.sec.services.CustomerService.GithubRepositoryType.*;

@Component
@NoArgsConstructor
public class JenkinsServiceImpl implements JenkinsService {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsServiceImpl.class);

    @Autowired
    private ErrorMessages errorMessages;

    @Autowired
    private RetryExceptionHandler retryHandler;

    @Autowired
    private ApplicationConfigs applicationConfigs;

    @Autowired
    private JenkinsConfigs jenkinsConfigs;

    @Override
    public ResponseEntity<String> getConfigFile(String... callType) {
        logger.debug(" Executing get jenkins config template");
        return ResponseEntity.ok().body(applicationConfigs.getJenkinsClient()
                .get()
                .uri(jenkinsConfigs.getJenkinsBaseUrl()+jenkinsConfigs.getConfigFileUrl(), jenkinsConfigs.getJenkinsTemplateJob(),jenkinsConfigs.getJenkinsConfigFileName())
                .retrieve().bodyToMono(String.class).blockOptional()
                .orElseThrow(()-> new NoResponseException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getNETWORK_ISSUE(), new RuntimeException()))));
    }

    @Override
    public ResponseEntity<String> pushJenkinsJob(String customerId, String xmlContentPayload, CustomerService.GithubRepositoryType githubRepositoryType, String... callType) {
        logger.debug(" Executing push jenkins job");
        return ResponseEntity.ok().body(applicationConfigs.getJenkinsClient()
                .post()
                .uri(jenkinsConfigs.getJenkinsBaseUrl()+jenkinsConfigs.getCreateJobUrl(), getJobNameByCustomerIdAndType(customerId, githubRepositoryType))
                .bodyValue(xmlContentPayload)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> retryHandler.handle4xxErrorResponse(clientResponse))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> retryHandler.handle5xxErrorResponse(clientResponse))
                .bodyToFlux(String.class)
                .blockFirst());
    }

    @Override
    public ResponseEntity<Void> initiateJenkinsJobBuild(String customerId, CustomerService.GithubRepositoryType githubRepositoryType, String... callType) {
        logger.debug(" Executing initiate jenkins job build");
        return ResponseEntity.ok().body(applicationConfigs.getJenkinsClient()
                .post()
                .uri(jenkinsConfigs.getJenkinsBaseUrl()+jenkinsConfigs.getTriggerJenkinsJob(), getJobNameByCustomerIdAndType(customerId, githubRepositoryType))
                .retrieve().bodyToMono(Void.class).block());
    }

    private String getJobNameByCustomerIdAndType(String customerId, CustomerService.GithubRepositoryType githubRepositoryType){
        String jobName = customerId;
        if (githubRepositoryType == INSTALLATION) jobName = jobName.concat(underscore+INSTALLATION.name());
        if (githubRepositoryType == UNINSTALLATION) jobName = jobName.concat(underscore+UNINSTALLATION.name());
        if (githubRepositoryType == POLICY) jobName = jobName.concat(underscore+POLICY.name());
        if (githubRepositoryType == DASHBOARD_INSTALLATION) jobName = jobName.concat(underscore+DASHBOARD_INSTALLATION.name());
        return jobName;
    }
 
    @Override
    public ResponseEntity<Void> deleteJenkinsJob(String customerId, GithubRepositoryType githubRepositoryType, String... callType) {
    	logger.debug(" Executing delete jenkins job");
    	return ResponseEntity.ok().body(applicationConfigs.getJenkinsClient()
    			.post()
    			.uri(jenkinsConfigs.getJenkinsBaseUrl()+jenkinsConfigs.getDeleteJenkinsJob(), getJobNameByCustomerIdAndType(customerId, githubRepositoryType))
    			.retrieve()
    			.bodyToMono(Void.class).block());
    }
}
