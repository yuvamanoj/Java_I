package com.ibm.sec.handlers;

import com.ibm.sec.entities.Customer;
import com.ibm.sec.repositories.CustomerRepository;
import com.ibm.sec.repositories.TaskStatusRepository;
import com.ibm.sec.services.PrismaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Base64Utils;

import java.util.Date;

import static com.ibm.sec.services.CustomerService.GithubRepositoryType.*;
import static com.ibm.sec.services.JenkinsService.BuildInitializerType.INITIAL;

@ExtendWith(SpringExtension.class)
public class CSSSchedulerHandlerTest {
    @Mock
    PrismaService prismaService;

    @Mock
    JenkinsInteractionHandler jenkinsHandler;

    @Mock
    CustomerRepository customerRepository;


    @Mock
    TaskStatusRepository taskStatusRepository;

    @Mock
    GithubInteractionHandler githubInteractionHandler;

    @InjectMocks
    CSSSchedulerHandler cssSchedulerHandler;

    private Customer mockCustomer = new Customer();

    @BeforeEach
    void setUp(){
        mockCustomer.setId("cust_1234");
        mockCustomer.setFirstName("MockCustomer");
        mockCustomer.setLastName("User");
        mockCustomer.setPhoneNumber(12312123L);
        mockCustomer.setEmail("Some@mail.com");
        mockCustomer.setApiKey(Base64Utils.encodeToString("mockApiKey".getBytes()));
        mockCustomer.setRegion("mockRegion");
        mockCustomer.setResourceGroup("default");
        mockCustomer.setClusterId("c0ci0ikd0a0s7i0e7vm0");
        mockCustomer.setPlatform("mockPlatform");
        mockCustomer.setToolset("mockToolSet");
        mockCustomer.setPolicy("mockPolicy");
        mockCustomer.setTncCheck(true);
        mockCustomer.setTrialVersion(true);
        mockCustomer.setFullVersion(false);
        mockCustomer.setLicenseKey("mockLicenseKey");
        mockCustomer.setCreatedDateTime(new Date());
        mockCustomer.setUpdatedDateTime(new Date());
        mockCustomer.setCreatedBy("mockUser");
        mockCustomer.setUpdatedBy("mockUser");
        mockCustomer.setPrismaUrl("wwwMockPrismaUrlcom");
        mockCustomer.setPrismaUsername("mockPrismaUserName");
        mockCustomer.setPrismaPassword("mockPrismaPassword");
        mockCustomer.setCompanyName("mockCompany");
        mockCustomer.setTncId(12L);
    }

    @Test
    void refreshAPICallTest(){
        cssSchedulerHandler.refreshAPICall(mockCustomer);
        String prismaUrl = new String(Base64Utils.decodeFromString(mockCustomer.getPrismaUrl()));
        String prismaUsername = new String(Base64Utils.decodeFromString(mockCustomer.getPrismaUsername()));
        String prismaPassword = new String(Base64Utils.decodeFromString(mockCustomer.getPrismaPassword()));
        Mockito.verify(prismaService, Mockito.times(1)).refreshAPI(prismaUrl,prismaUsername,prismaPassword);
    }

    @Test
    void applyUnInstallationStepsTest(){
        cssSchedulerHandler.applyUnInstallationSteps(mockCustomer.getId());
        Mockito.doNothing().when(taskStatusRepository).deleteByUserId(Mockito.anyString());
        Mockito.doNothing().when(customerRepository).deleteById(Mockito.anyString());
        Mockito.verify(jenkinsHandler, Mockito.times(1)).initiateJenkinsJobBuild(mockCustomer.getId(), UNINSTALLATION, INITIAL);
        Mockito.verify(jenkinsHandler, Mockito.times(1)).deleteJenkinsJob(mockCustomer.getId(), DASHBOARD_INSTALLATION, INITIAL);
        Mockito.verify(jenkinsHandler, Mockito.times(1)).deleteJenkinsJob(mockCustomer.getId(), POLICY, INITIAL);
        Mockito.verify(jenkinsHandler, Mockito.times(1)).deleteJenkinsJob(mockCustomer.getId(), INSTALLATION, INITIAL);
        Mockito.verify(githubInteractionHandler, Mockito.times(1)).deleteGithubRepositoryForCustomer(mockCustomer.getId(), POLICY);
    }
}
