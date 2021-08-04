package com.ibm.sec.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface JenkinsService {
    ResponseEntity<String> getConfigFile(String... callType);
    ResponseEntity<String> pushJenkinsJob(String customerId, String xmlContentPayload, CustomerService.GithubRepositoryType githubRepositoryType,String... callType);
    ResponseEntity<Void> initiateJenkinsJobBuild(String customerId, CustomerService.GithubRepositoryType githubRepositoryType, String... callType);
    ResponseEntity<Void> deleteJenkinsJob(String customerId, CustomerService.GithubRepositoryType githubRepositoryType, String... callType);
    enum BuildInitializerType {
        INITIAL, RETRY
    }
}
