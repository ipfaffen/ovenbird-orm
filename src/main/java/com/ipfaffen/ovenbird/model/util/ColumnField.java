package com.ipfaffen.ovenbird.model.util;

import java.lang.reflect.Field;

/**
 * @author Isaias Pfaffenseller
 */
public class ColumnField {
	
	private Field declaredField;
	private String attributeName;
	private String columnName;
	private Object value;
	private Class<?> type;
	private String genericType;
	private boolean isId;

	/**
	 * @return
	 */
	public Field getDeclaredField() {
		return declaredField;
	}
	
	/**
	 * @param declaredField
	 */
	public void setDeclaredField(Field declaredField) {
		this.declaredField = declaredField;
	}
	
	/**
	 * @return
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @param attributeName
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * @return
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
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

	/**
	 * @return
	 */
	public String getGenericType() {
		return genericType;
	}

	/**
	 * @param genericType
	 */
	public void setGenericType(String genericType) {
		this.genericType = genericType;
	}

	/**
	 * @param isId
	 */
	public void setIsId(boolean isId) {
		this.isId = isId;
	}

	/**
	 * @return
	 */
	public boolean isId() {
		return isId;
	}
}