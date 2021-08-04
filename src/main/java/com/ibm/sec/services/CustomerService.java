package com.ibm.sec.services;

import com.ibm.sec.dtos.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService {
    CustomerRegistrationDto registerUser(CustomerRegistrationDto registrationDto);
    CustomerRegistrationDto updateCustomerPrismaDetails(String customerId, PrismaDetailsDto prismaDetailsDto);
    CustomerClusterDetailsDto getClusterDetailsByCustomerId(String customerId);
    PrismaDetailsDto getCustomerPrismaDetails(String customerId);
    String updateJobStatusForCustomer(String customerId, JobDetailsDto jobDetailsDto);
    ResponseEntity<Void> initiatePrismaConsoleInstallation(InstallationDto dto);

    List<TaskStatusDto> getAllCustomersRecentActivities(String ibmId);

    enum GithubRepositoryType{
        POLICY, INSTALLATION, UNINSTALLATION , DASHBOARD_INSTALLATION
    }

    enum TaskStatusResponses{
        Done, Error, Ongoing
    }
}
