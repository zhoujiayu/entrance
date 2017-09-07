/*
 * $Id: SystemException.java 287 2011-08-07 09:35:56Z louis $
 * All rights reserved
 */
package com.ytsp.entrance.system; 

/**
 * 系统异常，该异常继承自RuntimeException。
 * 
 * @author Louis
 */
public class SystemException extends RuntimeException {

	public SystemException() {
		super();
	}

	public SystemException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemException(String message) {
		super(message);
	}

	public SystemException(Throwable cause) {
		super(cause);
	}

}
