package com.ibm.sec.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github")
@Data
public class GithubConfigs {

    private String templateOwner;
    private String githubBaseUrl;
    private String generateRepoUsingTemplate;
    private String deleteRepoApi;
}
