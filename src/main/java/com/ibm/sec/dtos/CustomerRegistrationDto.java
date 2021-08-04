package com.ibm.sec.dtos;
import com.ibm.sec.entities.Customer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Base64Utils;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CustomerRegistrationDto {
    private String id;

    @NotNull(message = "Firstname is required")
    private String firstName;

    @NotNull(message = "Lastname is required")
    private String lastName;

    @NotNull(message = "Email is required")
    private String email;

    private Long phoneNumber;

    @NotNull(message = "ApiKey is required")
    private String apiKey;

    @NotNull(message = "Region is required")
    private String region;

    @NotNull(message = "Resource Group is required")
    private String resourceGroup;

    @NotNull(message = "ClusterId is required")
    private String clusterId;

    @NotNull(message = "Platform is required")
    private String platform;

    @NotNull(message = "Toolset is required")
    private String toolset;

    private String policy;

    private boolean tncCheck;

    private boolean isTrialVersion;

    private boolean isFullVersion;

    private String licenseKey;

    private Date createdDateTime;

    private Date updatedDateTime;

    private String createdBy;

    private String updatedBy;

    private String prismaUrl;

    private String prismaUsername;

    private String prismaPassword;

    private String companyName;

    private Long tncId;

    @NotNull(message = "Ibm id is required")
    private String ibmId;

    @NotNull(message = "Ibm Email id is required")
    private String ibmEmailId;

    public CustomerRegistrationDto(Customer customer){
        this.id = customer.getId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
        this.phoneNumber = customer.getPhoneNumber();
        this.apiKey = new String(Base64Utils.decodeFromString(customer.getApiKey()));
        this.region = new String(Base64Utils.decodeFromString(customer.getRegion()));
        this.resourceGroup = new String(Base64Utils.decodeFromString(customer.getResourceGroup()));
        this.clusterId = new String(Base64Utils.decodeFromString(customer.getClusterId()));
        this.platform = customer.getPlatform();
        this.toolset = customer.getToolset();
        this.policy = customer.getPolicy();
        this.tncCheck = customer.isTncCheck();
        this.isTrialVersion = customer.isTrialVersion();
        this.isFullVersion = customer.isFullVersion();
        this.licenseKey = new String(Base64Utils.decodeFromString(customer.getLicenseKey()));
        this.createdDateTime = customer.getCreatedDateTime();
        this.updatedDateTime = customer.getUpdatedDateTime();
        this.createdBy = customer.getCreatedBy();
        this.updatedBy = customer.getUpdatedBy();
        if (customer.getPrismaUrl() != null)
            this.prismaUrl = new String(Base64Utils.decodeFromString(customer.getPrismaUrl()));
        if (customer.getPrismaUsername() != null)
            this.prismaUsername = new String(Base64Utils.decodeFromString(customer.getPrismaUsername()));
        if (customer.getPrismaPassword() != null)
            this.prismaPassword = new String(Base64Utils.decodeFromString(customer.getPrismaPassword()));
        this.companyName = customer.getCompanyName();
        this.tncId = customer.getTncId();
        this.ibmId = customer.getIbmId();
    }
}
