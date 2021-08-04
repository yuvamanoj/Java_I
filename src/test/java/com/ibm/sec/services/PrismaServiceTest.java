package com.ibm.sec.services;

import com.ibm.sec.exceptions.ErrorMessages;
import com.ibm.sec.exceptions.NoResponseException;
import com.ibm.sec.services.serviceImpl.PrismaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PrismaServiceTest {

    @Mock
    private ErrorMessages errorMessages;

    @Spy
    private PrismaService prismaService;

    @InjectMocks
    private PrismaServiceImpl prismaServiceImpl;


    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(prismaServiceImpl, "errorMessages", errorMessages);
    }

    @Test
    void getClientTest(){
        WebClient client = ReflectionTestUtils.invokeMethod(prismaServiceImpl, "getClient", "TestUrl","TestUserName","TestPassword");
        //Base64Utils.decodeFromString(((DefaultWebClient) client).defaultHeaders.get("Authorization").get(0).split(" ")[1])
        /*new String(Base64Utils.decodeFromString(
                ((DefaultWebClient) client).defaultHeaders.get("Authorization").get(0).split(" ")[1]))*/
        //System.out.println(((DefaultWebClientTests) client).defaultHeaders.get("Authorization"));

        assertNotNull(client);
    }

    @Test
    void refreshVulnerabilityExceptionTest(){
        when(errorMessages.getNETWORK_ISSUE()).thenReturn("Test error");
        NoResponseException exception = assertThrows(NoResponseException.class,
                () -> ReflectionTestUtils.invokeMethod(prismaServiceImpl, "refreshVulnerability","TestUrl","TestUserName","TestPassword"));
        assertEquals(400, exception.getApiError().getStatus().value());
        assertEquals("Test error", exception.getApiError().getMessage());
    }

    @Test
    void refreshComplianceExceptionTest(){
        when(errorMessages.getNETWORK_ISSUE()).thenReturn("Test error");
        NoResponseException exception = assertThrows(NoResponseException.class,
                () -> ReflectionTestUtils.invokeMethod(prismaServiceImpl, "refreshCompliance","TestUrl","TestUserName","TestPassword"));
        assertEquals(400, exception.getApiError().getStatus().value());
        assertEquals("Test error", exception.getApiError().getMessage());
    }

    @Test
    void refreshAPITest(){
        prismaService.refreshAPI("TestUrl","TestUserName","TestPassword");
        verify(prismaService, times(1)).refreshAPI("TestUrl","TestUserName","TestPassword");
    }
}
