package com.ipfaffen.ovenbird.model.util;

/**
 * @author Isaias Pfaffenseller
 */
public class TableEntity {
	
	private String tableName;
	private Class<?> type;

	/**
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(Class<?> type) {
		this.type = type;
	}
}