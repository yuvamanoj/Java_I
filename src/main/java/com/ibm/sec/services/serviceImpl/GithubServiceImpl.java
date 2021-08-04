package com.ibm.sec.services.serviceImpl;
import com.ibm.sec.configurations.ApplicationConfigs;
import com.ibm.sec.configurations.GithubConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.ibm.sec.dtos.CreateRepositoryRequestDto;
import com.ibm.sec.exceptions.ApiError;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.exceptions.NoResponseException;
import com.ibm.sec.services.GithubService;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class GithubServiceImpl implements GithubService {
    private static final Logger logger = LoggerFactory.getLogger(GithubServiceImpl.class);

    @Autowired
    private ErrorMessages errorMessages;

    @Autowired
    private ApplicationConfigs applicationConfigs;

    @Autowired
    private GithubConfigs githubConfigs;

    @Override
    public ResponseEntity<String> createGithubRepositoryFromTemplate(String templateName, String newRepositoryName) {
        logger.debug(" Executing create github repository from template");
        return ResponseEntity.ok().body(applicationConfigs.getCreateGithubRepoWebClient()
                .post()
                .uri(githubConfigs.getGithubBaseUrl()+githubConfigs.getGenerateRepoUsingTemplate(), githubConfigs.getTemplateOwner(),templateName)
                .bodyValue(new CreateRepositoryRequestDto(newRepositoryName, githubConfigs.getTemplateOwner()))
                .retrieve().bodyToMono(String.class).blockOptional()
                .orElseThrow(()-> new NoResponseException(new ApiError(HttpStatus.BAD_REQUEST, errorMessages.getNETWORK_ISSUE(), new RuntimeException()))));
    }

    @Override
	public ResponseEntity<Void> deleteRepository(String customerId, String repository, String... callType) {
		logger.debug("delete repo type: {}",repository);
		return ResponseEntity.ok().body(applicationConfigs.getGithubClient()
			.delete()
			.uri(githubConfigs.getGithubBaseUrl()+githubConfigs.getDeleteRepoApi(), githubConfigs.getTemplateOwner(), repository)
			.retrieve()
			.bodyToMono(Void.class).block());

	}    
}
