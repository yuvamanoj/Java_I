package com.ibm.sec.entities;

import com.ibm.sec.dtos.CustomerRegistrationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private Long phoneNumber;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "region")
    private String region;

    @Column(name = "resource_group")
    private String resourceGroup;

    @Column(name = "cluster_id")
    private String clusterId;

    @Column(name = "platform")
    private String platform;

    @Column(name = "toolset")
    private String toolset;

    @Column(name = "policy")
    private String policy;

    @Column(name = "t_n_c_check", nullable = false)
    private boolean tncCheck;

    @Column(name = "is_trial_version")
    private boolean isTrialVersion;

    @Column(name = "is_full_version")
    private boolean isFullVersion;

    @Column(name = "license_key")
    private String licenseKey;

    @Column(name = "created_date_time")
    private Date createdDateTime;

    @Column(name = "updated_date_time")
    private Date updatedDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "prisma_url")
    private String prismaUrl;

    @Column(name = "prisma_username")
    private String prismaUsername;

    @Column(name = "prisma_password")
    private String prismaPassword;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tnc_id")
    private Long tncId;

    @Column(name = "ibm_id")
    private String ibmId;

    public Customer(CustomerRegistrationDto registrationDto){
        this.id = registrationDto.getId();
        this.firstName = registrationDto.getFirstName();
        this.lastName = registrationDto.getLastName();
        this.email = registrationDto.getEmail();
        this.phoneNumber = registrationDto.getPhoneNumber();
        this.apiKey = registrationDto.getApiKey();
        this.region = registrationDto.getRegion();
        this.resourceGroup = registrationDto.getResourceGroup();
        this.clusterId = registrationDto.getClusterId();
        this.platform = registrationDto.getPlatform();
        this.toolset = registrationDto.getToolset();
        this.policy = registrationDto.getPolicy();
        this.tncCheck = registrationDto.isTncCheck();
        this.isTrialVersion = registrationDto.isTrialVersion();
        this.isFullVersion = registrationDto.isFullVersion();
        this.licenseKey = registrationDto.getLicenseKey();
        this.createdDateTime = registrationDto.getCreatedDateTime();
        this.updatedDateTime = registrationDto.getUpdatedDateTime();
        this.createdBy = registrationDto.getCreatedBy();
        this.updatedBy = registrationDto.getUpdatedBy();
        this.prismaUrl = registrationDto.getPrismaUrl();
        this.prismaUsername = registrationDto.getPrismaUsername();
        this.prismaPassword = registrationDto.getPrismaPassword();
        this.companyName = registrationDto.getCompanyName();
        this.tncId = registrationDto.getTncId();
        this.ibmId = registrationDto.getIbmId();
    }
}
