package com.ytsp.entrance.service.exception;

public class ServiceException extends RuntimeException { 

	private static final long serialVersionUID = 1L;
	private int 	errorCode;

	public ServiceException() {
	}

	public ServiceException(String msg) {
		super(msg);
	}

	public ServiceException(Throwable ex) {
		super(ex);
	}

	public ServiceException(String msg, Throwable ex) {
		super(msg, ex);
	}
	
	public ServiceException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}
	
	public ServiceException(int errorCode, String msg) {
		super(msg);
		this.errorCode = errorCode;
	}

	public ServiceException(int errorCode, Throwable ex) {
		super(ex);
		this.errorCode = errorCode;
	}

	public ServiceException(int errorCode, String msg, Throwable ex) {
		super(msg, ex);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	
}
