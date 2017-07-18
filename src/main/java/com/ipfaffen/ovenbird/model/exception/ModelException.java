package com.ipfaffen.ovenbird.model.exception;

/**
 * @author Isaias Pfaffenseller
 */
@SuppressWarnings("serial")
public class ModelException extends RuntimeException {
	/**
	 * @param message
	 */
	public ModelException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ModelException(String message, Throwable cause) {
		super(message, cause);
	}
}
