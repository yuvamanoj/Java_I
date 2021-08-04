package com.ibm.sec.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiError {

	@JsonIgnore
	private HttpStatus status;
	private String statusMsg;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;
	private String message;
	private String customMessage;


	private ApiError() {
		timestamp = LocalDateTime.now();
	}

	public ApiError(HttpStatus status) {
		this();
		this.status = status;
		this.statusMsg= ErrorMessages.ERROR.message + status.value()+" "+ status.name();
	}

	public ApiError(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.statusMsg= ErrorMessages.ERROR.message + status.value()+" "+ status.name();
		this.message = ErrorMessages.GENERIC.getMessage();
		this.customMessage = ex.getLocalizedMessage();
	}

	public ApiError(HttpStatus status, String message, Throwable ex) {
		this();
		this.status = status;
		this.statusMsg= ErrorMessages.ERROR.message + status.value()+" "+ status.name();
		this.message = message;
		this.customMessage = ex.getLocalizedMessage();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public enum ErrorMessages{
		INVALID_URL("Invalid Url/Network issue, unable to connect"),
		NETWORK_ISSUE("Unable to connect due to network issue"),
		ERROR("ERROR: "),
		GENERIC("Unexpected error"),
		NO_WEB_CLIENT_FOUND("No Web Client Found");
		private String message;
		ErrorMessages(String message){
			this.message = message;
		}
		public String getMessage(){
			return message;
		}
	}
}


