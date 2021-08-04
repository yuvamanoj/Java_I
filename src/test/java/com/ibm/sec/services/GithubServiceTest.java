package com.ibm.sec.services;

import com.ibm.sec.configurations.ApplicationConfigs;
import com.ibm.sec.configurations.GithubConfigs;
import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.services.serviceImpl.GithubServiceImpl;
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
public class GithubServiceTest {
    @Mock
    private ErrorMessages errorMessages;

    private final MockWebServer mockWebServer  = new MockWebServer();

    @Spy
    private GithubServiceImpl githubServiceImpl;

    @Mock
    private ApplicationConfigs applicationConfigs;

    @Mock
    private GithubConfigs githubConfigs;

    private WebClient client;

    @BeforeEach
    void setUp(){
        client = WebClient.create(mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(githubServiceImpl, "errorMessages", errorMessages);
        ReflectionTestUtils.setField(githubServiceImpl,"applicationConfigs", applicationConfigs);
        ReflectionTestUtils.setField(githubServiceImpl,"githubConfigs", githubConfigs);
    }

    @AfterEach
    void cleanUp() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void createGithubRepositoryFromTemplate(){
        Mockito.when(applicationConfigs.getCreateGithubRepoWebClient()).thenReturn(client);
        Mockito.when(githubConfigs.getGithubBaseUrl()).thenReturn("");
        Mockito.when(githubConfigs.getGenerateRepoUsingTemplate()).thenReturn("repos/{template_owner}/{template_repo}/generate");
        Mockito.when(githubConfigs.getTemplateOwner()).thenReturn("testOwner");

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("Sample response"));

        ResponseEntity<String> responseEntity = githubServiceImpl.createGithubRepositoryFromTemplate("test_template", "new_test_repo");
        Assertions.assertEquals(200 , responseEntity.getStatusCode().value());
    }

    @Test
    void deleteRepositoryTest(){
        Mockito.when(applicationConfigs.getGithubClient()).thenReturn(client);
        Mockito.when(githubConfigs.getGithubBaseUrl()).thenReturn("");
        Mockito.when(githubConfigs.getDeleteRepoApi()).thenReturn("repos/{owner}/{repo}");
        Mockito.when(githubConfigs.getTemplateOwner()).thenReturn("testOwner");

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("Sample response"));

        ResponseEntity<Void> responseEntity = githubServiceImpl.deleteRepository("test_customer", "new_test_repo");
        Assertions.assertEquals(200 , responseEntity.getStatusCode().value());

    }
}
