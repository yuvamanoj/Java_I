package com.ibm.sec.exceptions;

public class CustomServiceException extends RuntimeException {
	private static final long serialVersionUID = 11L;
	private ApiError apierror;
    public CustomServiceException(ApiError apierror) {
    	super();
    	this.apierror=apierror;
    }
	public ApiError getApierror() {
		return apierror;
	}
    
}
