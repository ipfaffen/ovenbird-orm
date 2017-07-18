package com.ipfaffen.ovenbird.model.connection;

import java.sql.Connection;

import com.ipfaffen.ovenbird.model.exception.ConnectionException;


/**
 * @author Isaias Pfaffenseller
 */
public interface ConnectionHandler {
	
	/**
	 * @throws ConnectionException
	 */
	public void openTransaction() throws ConnectionException;

	/**
	 * @throws ConnectionException
	 */
	void openConnection() throws ConnectionException;

	/**
	 * @param commit
	 * @throws ConnectionException
	 */
	void closeTransaction(boolean commit) throws ConnectionException;

	/**
	 * @throws ConnectionException
	 */
	void closeConnection() throws ConnectionException;
	
	/**
	 * @return
	 */
	public Connection getConnection();
}