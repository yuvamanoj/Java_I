package com.ibm.sec.configurations;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import static com.ibm.sec.configurations.ExternalCallConstants.githubApiHeader;
import static com.ibm.sec.configurations.ExternalCallConstants.repositoryGeneratorHeader;

@Configuration
@Data
public class ApplicationConfigs {

    @Value("${jenkins.username}")
    private String jenkinsUserName;

    @Value("${jenkins.token}")
    private String jenkinsToken;

    @Value("${github.template-owner}")
    private String githubTemplateOwner;

    @Value("${github.auth-token}")
    private String githubToken;

    public WebClient getJenkinsClient() {
        return WebClient.builder()
                .defaultHeaders(header -> {
                    header.setBasicAuth(jenkinsUserName, jenkinsToken);
                    header.set("Content-Type", "application/xml");
                })
                .build();
    }

    public WebClient getCreateGithubRepoWebClient(){
        return WebClient.builder()
                .defaultHeaders(header -> {
                    header.set("Authorization","token "+ githubToken);
                    header.set("Accept", repositoryGeneratorHeader);
                })
                .build();
    }

    public WebClient getGithubClient(){
        return WebClient.builder()
                .defaultHeaders(header -> {
                    header.setBasicAuth(githubTemplateOwner, githubToken);
                    header.set("Accept", githubApiHeader);
                })
                .build();
    }

}
