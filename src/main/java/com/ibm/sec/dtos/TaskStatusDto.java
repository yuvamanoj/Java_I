package com.ibm.sec.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskStatusDto {
    private String customerId;
    private String platform;
    private String taskName;
    private String status;
    private String initiatedDateTime;
    private String clusterId;
    private boolean isTrialVersion;
    private int retry;
    private String error;
}
