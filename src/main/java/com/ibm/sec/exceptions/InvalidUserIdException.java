package com.ibm.sec.exceptions;

public class InvalidUserIdException extends RuntimeException {
    private final ApiError apiError;

    public InvalidUserIdException(ApiError apiError){
        super();
        this.apiError = apiError;
    }
    public ApiError getApiError() {
        return apiError;
    }
}
