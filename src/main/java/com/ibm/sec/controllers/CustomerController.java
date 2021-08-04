package com.ibm.sec.controllers;

import com.ibm.sec.dtos.*;
import com.ibm.sec.producer.MessageKafkaProducer;
import com.ibm.sec.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @Autowired
    MessageKafkaProducer producer;

    @PostMapping("/register")
    public ResponseEntity<String> registerNewUser(@Valid @RequestBody CustomerRegistrationDto registrationDto){
        producer.send("new-user", registrationDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Published To 'new-user' topic");
    }

    @PutMapping("/{customerId}/prismaDetails/update")
    public ResponseEntity<CustomerRegistrationDto> updateUserPrismaDetails(@PathVariable("customerId") String customerId, @Valid @RequestBody PrismaDetailsDto prismaDetailsDto){
        return ResponseEntity.ok(customerService.updateCustomerPrismaDetails(customerId, prismaDetailsDto));
    }

    @GetMapping("/{customerId}/prismaDetails")
    public ResponseEntity<PrismaDetailsDto> getPrismaDetailsForCustomer(@PathVariable("customerId") String customerId){
        return ResponseEntity.ok(customerService.getCustomerPrismaDetails(customerId));
    }

    @GetMapping("/{customerId}/clusterDetails")
    public ResponseEntity<CustomerClusterDetailsDto> getClusterDetailsByCustomerId(@PathVariable("customerId") String customerId){
        return ResponseEntity.ok(customerService.getClusterDetailsByCustomerId(customerId));
    }

    @PutMapping("/{customerId}/job")
    public ResponseEntity<String> updateJobStatus(@PathVariable("customerId") String customerId, @Valid @RequestBody JobDetailsDto jobDetailsDto){
        return ResponseEntity.ok(customerService.updateJobStatusForCustomer(customerId, jobDetailsDto));
    }

    @GetMapping("/{customerId}/status")
    public ResponseEntity<CustomerStatusDto> getCustomersTaskStatus(@PathVariable("customerId") String customerId){
        return dummyCustomerTaskStatus(customerId);
    }

    private ResponseEntity<CustomerStatusDto> dummyCustomerTaskStatus(String customerId) {
        List<ActivityStatusDto> activities = new ArrayList<>();
        activities.add(new ActivityStatusDto(1L,"Creating Jenkins Job For installation","Ongoing", "11:02 UTC Jun 1, 2021"));
        activities.add(new ActivityStatusDto(2L,"Creating Jenkins Job For Policy","Completed", "11:02 UTC Jun 1, 2021"));
        activities.add(new ActivityStatusDto(3L,"Creating Github repository For Policy","Completed", "11:04 UTC Jun 1, 2021"));
        activities.add(new ActivityStatusDto(4L,"Installing customer dashboard","Error", "11:10 UTC Jun 1, 2021"));

        CustomerStatusDto customerStatusDto = new CustomerStatusDto(customerId,"PCC", "Register new user", "Ongoing", "Jun 1, 2021", "fghdgd45453453Akl",true, 0, "", activities);

        return ResponseEntity.ok(customerStatusDto);
    }
}
