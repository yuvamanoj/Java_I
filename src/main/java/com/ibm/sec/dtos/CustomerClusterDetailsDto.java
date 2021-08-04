package com.ibm.sec.dtos;

import com.ibm.sec.entities.Customer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Base64Utils;

@Getter
@Setter
@NoArgsConstructor
public class CustomerClusterDetailsDto {
    private String customerId;

    private String apiKey;

    private String region;

    private String resourceGroup;

    private String clusterId;

    private String licenseKey;

    public CustomerClusterDetailsDto(Customer customer){
        this.customerId = customer.getId();
        this.apiKey = new String(Base64Utils.decodeFromString(customer.getApiKey()));
        this.region = new String(Base64Utils.decodeFromString(customer.getRegion()));
        this.resourceGroup = new String(Base64Utils.decodeFromString(customer.getResourceGroup()));
        this.clusterId = new String(Base64Utils.decodeFromString(customer.getClusterId()));
        this.licenseKey = new String(Base64Utils.decodeFromString(customer.getLicenseKey()));
    }
}
