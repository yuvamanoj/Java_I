package com.ibm.sec.exceptions;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "error-messages")
@Data
public class ErrorMessages {
    private String INVALID_URL;
    private String NETWORK_ISSUE;
    private String DATABASE_UNAVAILABLE;
    private String PARAMETER_IS_MISSING;
    private String MEDIA_TYPE_NOT_SUPPORTED;
    private String INVALID_USER_ID;
    private String PRISMA_URL_UNAVAILABLE;
    private String PRISMA_USERNAME_UNAVAILABLE;
    private String PRISMA_PASSWORD_UNAVAILABLE;
    private String CLUSTER_ID_ALREADY_AVAILABLE;
}
