package com.ibm.sec.exceptions;

public class NoResponseException extends RuntimeException{
    private ApiError apiError;

    public NoResponseException(ApiError apiError){
        super();
        this.apiError = apiError;
    }
    public NoResponseException(){
        super("No response received.");
    }

    public ApiError getApiError() {
        return apiError;
    }
}
