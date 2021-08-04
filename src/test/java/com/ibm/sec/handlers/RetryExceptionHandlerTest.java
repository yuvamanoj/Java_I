package com.ibm.sec.handlers;

import com.ibm.sec.exceptions.ClientDataException;
import com.ibm.sec.exceptions.CustomServiceException;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.support.ClientResponseWrapper;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RetryExceptionHandlerTest {

    @Autowired
    @Spy
    @InjectMocks
    private RetryExceptionHandler retryHandler;

    ClientResponse clientResponse = Mockito.mock(ClientResponse.class);

    @Before
    void setUp(){
        clientResponse = new ClientResponseWrapper(clientResponse);
    }

    @Test
    void handle4xxErrorResponseTest(){
        Mockito.when(clientResponse.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        Mockito.when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just("Test error message"));
        ClientDataException exception = assertThrows(ClientDataException.class,
                ()-> retryHandler.handle4xxErrorResponse(clientResponse).block());
        assertEquals(400, exception.getApierror().getStatus().value());
        assertEquals("Test error message", exception.getApierror().getMessage());
    }

    @Test
    void handle5xxErrorResponseTest(){
        Mockito.when(clientResponse.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just("Test error message"));
        CustomServiceException exception = assertThrows(CustomServiceException.class,
                ()-> retryHandler.handle5xxErrorResponse(clientResponse).block());
        assertEquals(500, exception.getApierror().getStatus().value());
        assertEquals("Test error message", exception.getApierror().getMessage());
    }
}
