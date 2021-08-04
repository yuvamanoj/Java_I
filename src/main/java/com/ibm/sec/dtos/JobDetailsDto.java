package com.ibm.sec.dtos;

import com.ibm.sec.constraints.JenkinsJobDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@JenkinsJobDetails
public class JobDetailsDto {
    private String jobName;
    @NotNull (message = "status is required")
    private Boolean status;
}
