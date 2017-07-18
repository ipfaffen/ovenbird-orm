package com.ipfaffen.ovenbird.model.connection;

import java.sql.Connection;
import java.sql.SQLException;

import com.ipfaffen.ovenbird.model.dialect.SqlDialect;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class ModelDataSource {

	/**
	 * @return
	 * @throws SQLException 
	 */
	public abstract Connection getConnection() throws SQLException;

	/**
	 * @return
	 */
	abstract public String getUrl();

	/**
	 * @return
	 */
	abstract public String getDriver();
	
	/**
	 * @return
	 */
	public Class<?> getDialect() {
		return SqlDialect.getClass(getDriver());
	}
}