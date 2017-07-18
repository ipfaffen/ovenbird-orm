package com.ipfaffen.ovenbird.model.exception;

/**
 * @author Isaias Pfaffenseller
 */
@SuppressWarnings("serial")
public class InterceptorException extends RuntimeException {
	/**
	 * @param message
	 */
	public InterceptorException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InterceptorException(String message, Throwable cause) {
		super(message, cause);
	}
}
