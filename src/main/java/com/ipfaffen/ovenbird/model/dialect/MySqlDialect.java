package com.ipfaffen.ovenbird.model.dialect;

import static com.ipfaffen.ovenbird.model.ModelConstants.MAIN_TABLE_ALIAS;

import java.util.List;

import com.ipfaffen.ovenbird.commons.StringUtil;
import com.ipfaffen.ovenbird.model.criteria.Criteria;
import com.ipfaffen.ovenbird.model.criteria.PagingCriteria;
import com.ipfaffen.ovenbird.model.util.ColumnField;
import com.ipfaffen.ovenbird.model.util.FieldList;
import com.ipfaffen.ovenbird.model.util.JoinColumnField;

/**
 * @author Isaias Pfaffenseller
 */
public class MySqlDialect extends SqlDialect {

	@Override
	public String buildInsert(String tableName, FieldList fields) {
		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for(ColumnField field: fields) {
			StringUtil.appendTo(columns, ("`" + field.getColumnName() + "`"));
			StringUtil.appendTo(values, "?");
		}
		return new StringBuilder("INSERT INTO `").append(tableName).append("`(").append(columns).append(") VALUES(").append(values).append(")").toString();
	}
	
	@Override
	public String buildUpdate(String tableName, Criteria criteria, FieldList fields) {
		StringBuilder columns = new StringBuilder();
		for(ColumnField field: fields) {
			StringUtil.appendTo(columns, MAIN_TABLE_ALIAS.concat(".").concat(field.getColumnName()).concat(" = ?"));
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE `").append(tableName).append("` ").append(MAIN_TABLE_ALIAS);
		if(criteria.hasJoin()) {
			sql.append(buildJoins(criteria.getJoinFields()));
		}
		sql.append(" SET ").append(columns);
		addClauses(sql, criteria, false);
		return sql.toString();
	}
	
	@Override
	public String buildDelete(String tableName, Criteria criteria) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE ").append(MAIN_TABLE_ALIAS);
		sql.append(" FROM `").append(tableName).append("` ").append(MAIN_TABLE_ALIAS);
		addClauses(sql, criteria);
		return sql.toString();
	}
	
	@Override
	public String buildFind(String tableName, Criteria criteria) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM `").append(tableName).append("` ").append(MAIN_TABLE_ALIAS);
		addClauses(sql, criteria);
		return sql.toString();
	}
	
	@Override
	public String buildGet(String tableName, Criteria criteria, List<String> fields) {
		StringBuilder columns = new StringBuilder();
		for(String field: fields) {
			StringUtil.appendTo(columns, field);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ").append(columns);
		sql.append(" FROM `").append(tableName).append("` ").append(MAIN_TABLE_ALIAS);
		addClauses(sql, criteria);
		return sql.toString();
	}
	
	@Override
	public String buildCount(String tableName, Criteria criteria) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(1) FROM `").append(tableName).append("` ").append(MAIN_TABLE_ALIAS);
		addClauses(sql, criteria);
		return sql.toString();
	}
	
	@Override
	public String buildExists(String tableName, Criteria criteria) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT 1 FROM `").append(tableName).append("` ").append(MAIN_TABLE_ALIAS);
		addClauses(sql, criteria);
		return sql.toString();
	}
	
	@Override
	public void addClauses(StringBuilder sql, Criteria criteria) {
		addClauses(sql, criteria, true);
	}

	@Override
	public void addClauses(StringBuilder sql, Criteria criteria, boolean addJoins) {
		if(addJoins && criteria.hasJoin()) {
			sql.append(buildJoins(criteria.getJoinFields()));
		}
		if(criteria.hasCondition()) {
			sql.append(" WHERE ");
			sql.append(criteria.getConditions());
		}
		if(criteria.hasGrouping()) {
			sql.append(" GROUP BY ");
			sql.append(criteria.getGroupings());
		}
		if(criteria.hasOrder()) {
			sql.append(" ORDER BY ");
			sql.append(criteria.getOrders());
		}
		if(criteria instanceof PagingCriteria) {
			PagingCriteria pagingCriteria = (PagingCriteria) criteria;
			if(pagingCriteria.isPagingEnabled()) {
				sql.append(" LIMIT ");
				sql.append(pagingCriteria.getPaging().getStartIndex());
				sql.append(", ");
				sql.append(pagingCriteria.getPaging().getPageSize());
			}
		}
		else if(criteria.getResultLimit() != null) {
			sql.append(" LIMIT ");
			sql.append(criteria.getResultLimit());
		}
	}
	
	@Override
	public StringBuilder buildJoins(List<JoinColumnField> joinFields) {
		StringBuilder sql = new StringBuilder();
		for(JoinColumnField joinField: joinFields) {
			sql.append(" LEFT JOIN `").append(joinField.getTable().getTableName()).append("`");
			sql.append(" ").append(joinField.getAlias());
			sql.append(" ON ").append(joinField.getAlias());
			sql.append(".").append(joinField.getIdColumn().getColumnName());
			sql.append(" = ").append(joinField.getBaseJoinField().getAlias());
			sql.append(".").append(joinField.getBaseIdColumn().getColumnName());
		}
		return sql;
	}
}