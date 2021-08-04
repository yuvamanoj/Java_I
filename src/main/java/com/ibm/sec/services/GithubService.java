package com.ibm.sec.services;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface GithubService {
    ResponseEntity<String> createGithubRepositoryFromTemplate(String templateName, String newRepositoryName);
    ResponseEntity<Void> deleteRepository(String customerId, String repositoryType, String... callType);
}
