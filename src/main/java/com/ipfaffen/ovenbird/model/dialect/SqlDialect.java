package com.ipfaffen.ovenbird.model.dialect;

import java.util.List;

import com.ipfaffen.ovenbird.commons.ReflectionUtil;
import com.ipfaffen.ovenbird.model.criteria.Criteria;
import com.ipfaffen.ovenbird.model.util.FieldList;
import com.ipfaffen.ovenbird.model.util.JoinColumnField;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class SqlDialect {

	/**
	 * Build insert sql.
	 * 
	 * @param tableName
	 * @param fields
	 * @return
	 */
	public abstract String buildInsert(String tableName, FieldList fields);
	
	/**
	 * Build update sql.
	 *
	 * @param tableName
	 * @param criteria
	 * @param fields
	 * @return
	 */
	public abstract String buildUpdate(String tableName, Criteria criteria, FieldList fields);
	
	/**
	 * Build delete sql.
	 * 
	 * @param tableName
	 * @param criteria
	 * @return
	 */
	public abstract String buildDelete(String tableName, Criteria criteria);
	
	/**
	 * Build select sql.
	 * 
	 * @param criteria
	 * @return
	 */
	public abstract String buildFind(String tableName, Criteria criteria);
	
	/**
	 * @param tableName
	 * @param criteria
	 * @param fields
	 * @return
	 */
	public abstract String buildGet(String tableName, Criteria criteria, List<String> fields);

	/**
	 * @param tableName
	 * @param criteria
	 * @return
	 */
	public abstract String buildCount(String tableName, Criteria criteria);
	
	/**
	 * @param tableName
	 * @param criteria
	 * @return
	 */
	public abstract String buildExists(String tableName, Criteria criteria);
	
	/**
	 * Add clauses based in criteria.
	 * 
	 * @param sql
	 * @param criteria
	 */
	public abstract void addClauses(StringBuilder sql, Criteria criteria);

	/**
	 * Add clauses based in criteria.
	 * 
	 * @param sql
	 * @param criteria
	 * @param addJoins
	 */
	public abstract void addClauses(StringBuilder sql, Criteria criteria, boolean addJoins);
	
	/**
	 * @param joinFields
	 * @return
	 */
	public abstract StringBuilder buildJoins(List<JoinColumnField> joinFields);
	
	/**
	 * @param driver
	 * @return
	 */
	public static SqlDialect getInstance(String driver) {
		return (SqlDialect) ReflectionUtil.newInstance(getClass(driver));
	}
	
	/**
	 * @param driver
	 * @return
	 */
	public static Class<?> getClass(String driver) {
		if(driver.equalsIgnoreCase("com.mysql.jdbc.Driver")) {
			return MySqlDialect.class;
		}
		/*else if(driver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver")) {
			return OracleDialect.class;
		}*/
		return MySqlDialect.class;
	}
}