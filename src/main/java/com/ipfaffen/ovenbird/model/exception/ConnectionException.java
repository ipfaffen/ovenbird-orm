package com.ipfaffen.ovenbird.model.exception;

/**
 * @author Isaias Pfaffenseller
 */
@SuppressWarnings("serial")
public class ConnectionException extends RuntimeException {
	/**
	 * @param message
	 */
	public ConnectionException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
