package com.ibm.sec.exceptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import reactor.netty.http.client.PrematureCloseException;
import java.net.ConnectException;
import java.net.UnknownHostException;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice extends ResponseEntityExceptionHandler {
    @Autowired
    private ErrorMessages errorMessages;

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required' request parameter is missing.
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        StringBuilder errorBuilder = new StringBuilder();
        errorBuilder.append(ex.getParameterName()).append(errorMessages.getPARAMETER_IS_MISSING());
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, errorBuilder.toString(), ex));
    }


    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when apart from JSON will triggered.
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(errorMessages.getMEDIA_TYPE_NOT_SUPPORTED());
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        return buildResponseEntity(new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, builder.substring(0, builder.length() - 2), ex));
    }

    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody
    ResponseEntity<Object> handleInvalidInputException(final InvalidInputException exception) {
        return buildResponseEntity(exception.getApiError());
    }

    @ExceptionHandler(InvalidUserIdException.class)
    public @ResponseBody
    ResponseEntity<Object> handleInvalidUserIdException(final InvalidUserIdException exception) {
        return buildResponseEntity(exception.getApiError());
    }

    @ExceptionHandler(NoResponseException.class)
    public @ResponseBody
    ResponseEntity<Object> handleNoResponseException(final NoResponseException exception) {
        return buildResponseEntity(exception.getApiError());
    }

    @ExceptionHandler(ClientDataException.class)
    public @ResponseBody ResponseEntity<Object> handleClientDataException(final ClientDataException exception) {
        return buildResponseEntity(exception.getApierror());
    }

    @ExceptionHandler(java.net.ConnectException.class)
    public @ResponseBody ResponseEntity<Object> handleConnectionException(final ConnectException exception) {
        logger.error("The url is invalid received exception : {}",exception);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, errorMessages.getINVALID_URL(), new RuntimeException()));
    }

    @ExceptionHandler(java.net.UnknownHostException.class)
    public @ResponseBody ResponseEntity<Object> handleUnknownHostException(final UnknownHostException exception) {
        logger.error("The hostname is invalid received exception : {}",exception);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, errorMessages.getNETWORK_ISSUE(), new RuntimeException()));
    }

    @ExceptionHandler(PrematureCloseException.class)
    public @ResponseBody ResponseEntity<Object> handlePrematureCloseException(final PrematureCloseException exception) {
        logger.error("Connection prematurely closed BEFORE response : {}",exception);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, errorMessages.getNETWORK_ISSUE(), new RuntimeException()));
    }
}
