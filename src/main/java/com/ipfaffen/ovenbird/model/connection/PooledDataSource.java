package com.ipfaffen.ovenbird.model.connection;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Isaias Pfaffenseller
 */
public class PooledDataSource extends ModelDataSource {

	private ComboPooledDataSource dataSource;
	
	/**
	 * @param url
	 * @param user
	 * @param password
	 * @param driver
	 */
	public PooledDataSource(String url, String user, String password, String driver) {
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(driver);
		}
		catch(PropertyVetoException e) {
			throw new RuntimeException(e);
		}
		cpds.setJdbcUrl(url);
		cpds.setUser(user);
		cpds.setPassword(password);

		// Configuring Connection Testing:
		cpds.setAutomaticTestTable("C3P0_TEST_TABLE");
		cpds.setTestConnectionOnCheckin(true);
		cpds.setIdleConnectionTestPeriod(60);

		// Basic Pool Configuration:
		Integer minPoolSize = getMinPoolSize();
		if(minPoolSize != null) {
			cpds.setMinPoolSize(minPoolSize);
		}
		Integer maxPoolSize = getMaxPoolSize();
		if(maxPoolSize != null) {
			cpds.setMaxPoolSize(maxPoolSize);
		}
		Integer acquireIncrement = getAcquireIncrement();
		if(acquireIncrement != null) {
			cpds.setAcquireIncrement(acquireIncrement);
		}
		Integer maxIdleTime = getMaxIdleTime();
		if(maxIdleTime != null) {
			cpds.setMaxIdleTime(maxIdleTime);
		}
		Boolean testConnectionOnCheckin = getTestConnectionOnCheckin();
		if(testConnectionOnCheckin != null) {
			cpds.setTestConnectionOnCheckin(testConnectionOnCheckin);
		}
		Integer idleConnectionTestPeriod = getIdleConnectionTestPeriod();
		if(idleConnectionTestPeriod != null) {
			cpds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
		}
		Integer maxIdleTimeExcessConnections = getMaxIdleTimeExcessConnections();
		if(maxIdleTimeExcessConnections != null) {
			cpds.setMaxIdleTimeExcessConnections(maxIdleTimeExcessConnections);
		}
		dataSource = cpds;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	/**
	 * Minimum pool size.
	 * 
	 * @return
	 */
	public Integer getMinPoolSize() {
		return null;
	}

	/**
	 * Maximum pool size.
	 * 
	 * @return
	 */
	public Integer getMaxPoolSize() {
		return null;
	}

	/**
	 * Determines how many connections at a time it will try to acquire when the
	 * pool is exhausted.
	 * 
	 * @return
	 */
	public Integer getAcquireIncrement() {
		return null;
	}

	/**
	 * Seconds a Connection can remain pooled but unused before being discarded.
	 * Zero means idle connections never expire.
	 * 
	 * @return
	 */
	public Integer getMaxIdleTime() {
		return null;
	}

	/**
	 * If true, an operation will be performed asynchronously at every
	 * connection checkin to verify that the connection is valid. Use in
	 * combination with idleConnectionTestPeriod for quite reliable, always
	 * asynchronous Connection testing. Also, setting an automaticTestTable or
	 * preferredTestQuery will usually speed up all connection tests.
	 * 
	 * @return
	 */
	public Boolean getTestConnectionOnCheckin() {
		return null;
	}

	/**
	 * If this is a number greater than 0, c3p0 will test all idle, pooled but
	 * unchecked-out connections, every this number of seconds.
	 * 
	 * @return
	 */
	public Integer getIdleConnectionTestPeriod() {
		return null;
	}

	/**
	 * Number of seconds that Connections in excess of minPoolSize should be
	 * permitted to remain idle in the pool before being culled. Intended for
	 * applications that wish to aggressively minimize the number of open
	 * Connections, shrinking the pool back towards minPoolSize if, following a
	 * spike, the load level diminishes and Connections acquired are no longer
	 * needed. If maxIdleTime is set, maxIdleTimeExcessConnections should be
	 * smaller if the parameter is to have any effect. Zero means no
	 * enforcement, excess Connections are not idled out.
	 * 
	 * @return
	 */
	public Integer getMaxIdleTimeExcessConnections() {
		return null;
	}

	@Override
	public String getUrl() {
		return dataSource.getJdbcUrl();
	}
	
	@Override
	public String getDriver() {
		return dataSource.getDriverClass();
	}
}