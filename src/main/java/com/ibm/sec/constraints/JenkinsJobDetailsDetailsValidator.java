package com.ibm.sec.constraints;

import com.ibm.sec.dtos.JobDetailsDto;
import com.ibm.sec.exceptions.ApiError;
import com.ibm.sec.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.ibm.sec.services.CustomerService.GithubRepositoryType.*;


public class JenkinsJobDetailsDetailsValidator implements ConstraintValidator<JenkinsJobDetails, JobDetailsDto> {
    @Override
    public void initialize(JenkinsJobDetails constraintAnnotation) {
    }

    @Override
    public boolean isValid(JobDetailsDto detailsDto, ConstraintValidatorContext constraintValidatorContext) {
        if (detailsDto.getJobName() == null || detailsDto.getJobName().isEmpty())
            throw new InvalidInputException(new ApiError(HttpStatus.BAD_REQUEST, "Job Name cannot be null", new RuntimeException()));
        if (!(detailsDto.getJobName().equalsIgnoreCase(INSTALLATION.name()) || detailsDto.getJobName().equalsIgnoreCase(POLICY.name()) || detailsDto.getJobName().equalsIgnoreCase(UNINSTALLATION.name()) || detailsDto.getJobName().equalsIgnoreCase(DASHBOARD_INSTALLATION.name()) ))
            throw new InvalidInputException(new ApiError(HttpStatus.BAD_REQUEST, "Only 'installation','policy','dashboard_installation' or 'uninstallation' values allowed in job name", new RuntimeException()));
        return true;
    }
}
