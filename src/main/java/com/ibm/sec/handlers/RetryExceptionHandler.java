package com.ibm.sec.handlers;

import com.ibm.sec.exceptions.ApiError;
import com.ibm.sec.exceptions.ClientDataException;
import com.ibm.sec.exceptions.CustomServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.function.Predicate;

@Component
public class RetryExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(RetryExceptionHandler.class);
	@Value("${maxRetry}")
	private long maxRetry;

    @Value("${custom_error_message}")
    private String customErrorMessage;

	private RetryExceptionHandler() {};
	
	protected Predicate<Throwable> is401 =
            throwable -> throwable instanceof WebClientResponseException.Unauthorized;

	protected Predicate<Throwable> is404 =
            throwable -> throwable instanceof WebClientResponseException.NotFound;

    protected  Predicate<Throwable> is5xx =
            throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable;

    protected  Predicate<Throwable> is429 =
            throwable -> throwable instanceof WebClientResponseException.TooManyRequests;

    public Retry retry401 = Retry.max(maxRetry)
                .filter(is401)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());

    public Retry retry404 = Retry.max(maxRetry)
                .filter(is404)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());

    public Retry retry5xx = Retry.max(maxRetry)
                .filter(is5xx)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());

    public  Retry retry429 = Retry.max(maxRetry)
                .filter(is429)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
    
    public Mono<ClientDataException> handle4xxErrorResponse(ClientResponse clientResponse) {
        Mono<String> errorResponse = clientResponse.bodyToMono(String.class);
        return errorResponse.flatMap((message) -> {
            ApiError apierror=new ApiError(clientResponse.statusCode());
         		   apierror.setMessage(message);
         		   apierror.setCustomMessage(customErrorMessage);
         		   logger.error(message);
            throw new ClientDataException(apierror);
        });
    }

    public Mono<CustomServiceException> handle5xxErrorResponse(ClientResponse clientResponse) {
    	  Mono<String> errorResponse = clientResponse.bodyToMono(String.class);
          return errorResponse.flatMap((message) -> {
           ApiError apierror=new ApiError(clientResponse.statusCode());
        		   apierror.setMessage(message);
                  apierror.setCustomMessage(customErrorMessage);
              throw new CustomServiceException(apierror);
          });

    }
}
