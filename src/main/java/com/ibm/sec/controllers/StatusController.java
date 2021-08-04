package com.ibm.sec.controllers;
import com.ibm.sec.dtos.TaskStatusDto;
import com.ibm.sec.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/status")
public class StatusController {

    @Autowired
    CustomerService customerService;

    @RequestMapping("/user/{ibmId}/customer/all")
    public List<TaskStatusDto> getAllRecentActivityStatusForAllCustomers(@PathVariable("ibmId") String ibmId){
        return customerService.getAllCustomersRecentActivities(ibmId);
    }

    private List<TaskStatusDto> setDummyStatusDtos() {
        TaskStatusDto statusDto1 = new TaskStatusDto("cust1@11223","PCC", "Register new user", "Ongoing", "Jun 1, 2021", "c0ci0ikd0a0s7i0e7ABC",true,0,"");
        TaskStatusDto statusDto2 = new TaskStatusDto("cust1@22324","PCC", "Creating Jenkins Job For installation", "Ongoing", "Jun 2, 2021", "c0ci0ikd0a0s7i0e7123",true, 0, "");
        TaskStatusDto statusDto3 = new TaskStatusDto("cust1@34343","PCC", "Creating Github repository For Policy", "Error", "Jun 3, 2021", "c0ci0ikd0a0s7i0e7XYZ", true, 1, "Some error");
        TaskStatusDto statusDto4 = new TaskStatusDto("cust1@54451","PCC", "Installing customer dashboard", "Completed", "Jun 4, 2021", "c0ci0ikd0a0s7i0e7vm0",true, 0, "");
        return new ArrayList<>(Arrays.asList(statusDto1, statusDto2, statusDto3, statusDto4));
    }//Test method for dummmy response
}
