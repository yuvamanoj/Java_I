package com.ibm.sec.services;
import com.ibm.sec.configurations.ApplicationConfigs;
import com.ibm.sec.configurations.JenkinsConfigs;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.services.serviceImpl.JenkinsServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
class JenkinsServiceTest {

    @Mock
    private ErrorMessages errorMessages;

    @Spy
    private JenkinsServiceImpl jenkinsServiceImpl;

    private final MockWebServer mockWebServer  = new MockWebServer();

    @Mock
    private ApplicationConfigs applicationConfigs;

    @Mock
    private JenkinsConfigs jenkinsConfigs;

    private WebClient client;

    @BeforeEach
    void setUp(){
        client = WebClient.create(mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(jenkinsServiceImpl, "errorMessages", errorMessages);
        ReflectionTestUtils.setField(jenkinsServiceImpl,"applicationConfigs", applicationConfigs);
        ReflectionTestUtils.setField(jenkinsServiceImpl,"jenkinsConfigs", jenkinsConfigs);
    }

    @AfterEach
    void cleanUp() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getConfigFileTest(){
        Mockito.when(applicationConfigs.getJenkinsClient()).thenReturn(client);
        Mockito.when(jenkinsConfigs.getJenkinsBaseUrl()).thenReturn("");
        Mockito.when(jenkinsConfigs.getConfigFileUrl()).thenReturn("/job/{template_job_name}/{config_file_name}");
        Mockito.when(jenkinsConfigs.getJenkinsTemplateJob()).thenReturn("test");
        Mockito.when(jenkinsConfigs.getJenkinsConfigFileName()).thenReturn("config.xml");
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("Sample response"));
        ResponseEntity<String> responseEntity = jenkinsServiceImpl.getConfigFile();
        Assertions.assertEquals(200 , responseEntity.getStatusCode().value());
    }

    @Test
    void pushJenkinsJobTest(){
        Mockito.when(applicationConfigs.getJenkinsClient()).thenReturn(client);
        Mockito.when(jenkinsConfigs.getJenkinsBaseUrl()).thenReturn("");
        Mockito.when(jenkinsConfigs.getCreateJobUrl()).thenReturn("/createItem?name={job_name}");
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("Sample response"));
        ResponseEntity<String> responseEntity = jenkinsServiceImpl.pushJenkinsJob("testCustomer", "", CustomerService.GithubRepositoryType.INSTALLATION);
        Assertions.assertEquals(200 , responseEntity.getStatusCode().value());
    }

    @Test
    void initiateJenkinsJobBuildTest(){
        Mockito.when(applicationConfigs.getJenkinsClient()).thenReturn(client);
        Mockito.when(jenkinsConfigs.getJenkinsBaseUrl()).thenReturn("");
        Mockito.when(jenkinsConfigs.getTriggerJenkinsJob()).thenReturn("/job/{job_name}/build");
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("Sample response"));
        ResponseEntity<Void> responseEntity = jenkinsServiceImpl.initiateJenkinsJobBuild("testCustomer", CustomerService.GithubRepositoryType.INSTALLATION);
        Assertions.assertEquals(200 , responseEntity.getStatusCode().value());
    }

    @Test
    void deleteJenkinsJobTest(){
        Mockito.when(applicationConfigs.getJenkinsClient()).thenReturn(client);
        Mockito.when(jenkinsConfigs.getJenkinsBaseUrl()).thenReturn("");
        Mockito.when(jenkinsConfigs.getDeleteJenkinsJob()).thenReturn("/job/{job_name}/doDelete");
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("Sample response"));
        ResponseEntity<Void> responseEntity = jenkinsServiceImpl.deleteJenkinsJob("testCustomer", CustomerService.GithubRepositoryType.INSTALLATION);
        Assertions.assertEquals(200 , responseEntity.getStatusCode().value());
    }
}
