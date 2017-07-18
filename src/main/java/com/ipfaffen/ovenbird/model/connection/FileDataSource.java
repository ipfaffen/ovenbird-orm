package com.ipfaffen.ovenbird.model.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Isaias Pfaffenseller
 */
public class FileDataSource extends ModelDataSource {

	private String url;
	private String driver;
	
	/**
	 * @param url
	 * @param driver
	 * @throws ClassNotFoundException 
	 */
	public FileDataSource(String url, String driver) {
		this.url = url;
		this.driver = driver;
		try {
			Class.forName(driver);
		}
		catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url);
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String getDriver() {
		return driver;
	}
}