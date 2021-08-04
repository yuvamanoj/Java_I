package com.ibm.sec.exceptions;

public class ClientDataException  extends RuntimeException {
	
	private ApiError apierror;
	private static final long serialVersionUID = 1L;

    public ClientDataException(ApiError apierror) {
    	super();
    	this.apierror=apierror;
    }

	public ApiError getApierror() {
		return apierror;
	}

}
