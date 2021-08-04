package com.ibm.sec.exceptions;

public class InvalidInputException extends RuntimeException{
    private final ApiError apiError;

    public InvalidInputException(ApiError apiError){
        super();
        this.apiError = apiError;
    }
    public ApiError getApiError() {
        return apiError;
    }
}
