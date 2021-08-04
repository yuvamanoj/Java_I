package com.ibm.sec.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jenkins")
@Data
public class JenkinsConfigs {

    private String jenkinsBaseUrl;
    private String configFileUrl;
    private String createJobUrl;
    private String triggerJenkinsJob;
    private String jenkinsTemplateJob;
    private String jenkinsConfigFileName;
    private String deleteJenkinsJob;
}
