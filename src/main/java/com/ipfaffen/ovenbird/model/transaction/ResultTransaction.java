package com.ipfaffen.ovenbird.model.transaction;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class ResultTransaction<T> {
	private boolean readOnly;
	
	public ResultTransaction() {
		this(false);
	}

	/**
	 * @param readOnly
	 */
	public ResultTransaction(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public abstract T $() throws Exception;

	/**
	 * @return
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
}