package com.ipfaffen.ovenbird.model.connection;

import java.sql.Connection;

import com.ipfaffen.ovenbird.model.exception.ConnectionException;

/**
 * @author Isaias Pfaffenseller
 */
public class Database implements ConnectionHandler {
	
	private ModelDataSource dataSource;
	private Connection connection;

	/**
	 * Count how many times the open connection method was called to make sure that the connection is not closed when it
	 * should not be. For example if the open connection method was called three times the the close connection method
	 * must be called three times either.
	 */
	private int openConnectionCallCounter;

	/**
	 * @param dataSource
	 */
	public Database(ModelDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @param url
	 * @param user
	 * @param password
	 * @param driver
	 */
	public Database(String url, String user, String password, String driver) {
		this.dataSource = new PooledDataSource(url, user, password, driver);
	}
	
	/**
	 * @param url
	 * @param driver
	 */
	public Database(String url, String driver) {
		this.dataSource = new FileDataSource(url, driver);
	}

	/**
	 * Open database connection.
	 * 
	 * @throws ConnectionException
	 */
	@Override
	public void openConnection() throws ConnectionException {
		openConnectionCallCounter++;

		try {
			if(isConnectionOpen()) {
				return;
			}
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
		}
		catch(Exception e) {
			throw new ConnectionException(e.getMessage(), e);
		}
	}

	/**
	 * Open database transaction.
	 * 
	 * @throws ConnectionException
	 */
	@Override
	public void openTransaction() throws ConnectionException {
		openConnectionCallCounter++;

		try {
			if(isConnectionOpen()) {
				return;
			}
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch(Exception e) {
			throw new ConnectionException(e.getMessage(), e);
		}
	}

	/**
	 * Close database connection.
	 */
	@Override
	public void closeConnection() {
		openConnectionCallCounter--;

		try {
			if(openConnectionCallCounter > 0 || !isConnectionOpen()) {
				return;
			}
			connection.close();
		}
		catch(Exception e) {
			throw new ConnectionException(e.getMessage(), e);
		}
	}

	/**
	 * Close database transaction.
	 * 
	 * @param commit - <code>true:</code> commit<br/>
	 * <code>false</code>: rollback
	 * @throws ConnectionException
	 */
	@Override
	public void closeTransaction(boolean commit) throws ConnectionException {
		openConnectionCallCounter--;

		try {
			if(openConnectionCallCounter > 0) {
				return;
			}
			if(isConnectionOpen()) {
				// Only commit or rollback and close when is last close call.
				if(commit) {
					connection.commit();
				}
				else {
					connection.rollback();
				}
				connection.close();
			}
		}
		catch(Exception e) {
			throw new ConnectionException(e.getMessage(), e);
		}
	}

	/**
	 * Check if database connection is open.
	 * 
	 * @return
	 * @throws ConnectionException
	 */
	public boolean isConnectionOpen() throws ConnectionException {
		try {
			return !(connection == null || connection.isClosed());
		}
		catch(Exception e) {
			throw new ConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * @return
	 */
	public ModelDataSource getDataSource() {
		return dataSource;
	}
}