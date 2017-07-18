package com.ipfaffen.ovenbird.model.util;

import java.lang.reflect.Field;

/**
 * @author Isaias Pfaffenseller
 */
public class JoinColumnField {

	private String identifier;
	private Integer depthLevel;
	private JoinColumnField baseJoinField;
	private TableEntity baseTable;
	private ColumnField baseIdColumn;
	private Field baseField;
	private Integer[] aliasHandler;
	private String alias;
	private TableEntity table;
	private ColumnField idColumn;

	private Integer aliasIndex = -1;

	/**
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return
	 */
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * @param depthLevel
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/**
	 * @return
	 */
	public JoinColumnField getBaseJoinField() {
		return baseJoinField;
	}

	/**
	 * @param baseJoinField
	 */
	public void setBaseJoinField(JoinColumnField baseJoinField) {
		this.baseJoinField = baseJoinField;
	}

	/**
	 * @return
	 */
	public TableEntity getBaseTable() {
		return baseTable;
	}

	/**
	 * @param baseTable
	 */
	public void setBaseTable(TableEntity baseTable) {
		this.baseTable = baseTable;
	}

	/**
	 * @return
	 */
	public ColumnField getBaseIdColumn() {
		return baseIdColumn;
	}

	/**
	 * @param baseIdColumn
	 */
	public void setBaseIdColumn(ColumnField baseIdColumn) {
		this.baseIdColumn = baseIdColumn;
	}

	/**
	 * @return
	 */
	public Field getBaseField() {
		return baseField;
	}

	/**
	 * @param baseField
	 */
	public void setBaseField(Field baseField) {
		this.baseField = baseField;
	}

	/**
	 * @return
	 */
	public Integer[] getAliasHandler() {
		return aliasHandler;
	}

	/**
	 * @param aliasHandler
	 */
	public void setAliasHandler(Integer[] aliasHandler) {
		this.aliasHandler = aliasHandler;
	}

	/**
	 * @return
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return
	 */
	public TableEntity getTable() {
		return table;
	}

	/**
	 * @param table
	 */
	public void setTable(TableEntity table) {
		this.table = table;
	}

	/**
	 * @return
	 */
	public ColumnField getIdColumn() {
		return idColumn;
	}

	/**
	 * @param idColumn
	 */
	public void setIdColumn(ColumnField idColumn) {
		this.idColumn = idColumn;
	}

	/**
	 * @return
	 */
	public Integer getAliasIndex() {
		return aliasIndex;
	}

	/**
	 * @param aliasIndex
	 */
	public void setAliasIndex(Integer aliasIndex) {
		this.aliasIndex = aliasIndex;
	}
}