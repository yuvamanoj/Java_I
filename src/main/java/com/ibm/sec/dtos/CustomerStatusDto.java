package com.ibm.sec.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerStatusDto extends TaskStatusDto{
    private List<ActivityStatusDto> activities;

    public CustomerStatusDto(String customerId, String platform, String taskName, String status, String updatedDateTime, String clusterId,boolean isTrialVersion, int retry, String error, List<ActivityStatusDto> activities) {
        super(customerId, platform, taskName, status, updatedDateTime, clusterId, isTrialVersion, retry, error);
        this.activities = activities;
    }
}
