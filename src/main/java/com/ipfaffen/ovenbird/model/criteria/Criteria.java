package com.ipfaffen.ovenbird.model.criteria;

import java.util.List;

import com.ipfaffen.ovenbird.model.ModelConstants;
import com.ipfaffen.ovenbird.model.ModelUtil;
import com.ipfaffen.ovenbird.model.util.JoinColumnField;

/**
 * @author Isaias Pfaffenseller
 */
public class Criteria {

	public static final class Filter {
		/**
		 * Logical Operator And (AND).
		 */
		public static final int AND = 0;
		/**
		 * Logical Operator Or (OR).
		 */
		public static final int OR = 1;

		/**
		 * Operator Equal To (=).
		 */
		public static final int EQUAL_TO = 0;
		/**
		 * Operator Not Equal To (<>).
		 */
		public static final int NOT_EQUAL_TO = 1;
		/**
		 * Operator Greater Than (>).
		 */
		public static final int GREATER_THAN = 2;
		/**
		 * Operator Greater Than or Equal To (>=).
		 */
		public static final int GREATER_OR_EQUAL_TO = 3;
		/**
		 * Operator Less Than (<).
		 */
		public static final int LESS_THAN = 4;
		/**
		 * Operator Less Than or Equal To (<=).
		 */
		public static final int LESS_OR_EQUAL_TO = 5;
		/**
		 * Operator In (IN).
		 */
		public static final int IN = 6;
		/**
		 * Operator Like (LIKE).
		 */
		public static final int LIKE = 7;

		/**
		 * Order Ascendent (ASC).
		 */
		public static final int ASC = 0;
		/**
		 * Order Descendant (ASC).
		 */
		public static final int DESC = 1;
	};

	private Class<?> modelEntityClass;
	private CriteriaWhere criteriaWhere;
	private CriteriaOrder criteriaOrder;
	private CriteriaGroup criteriaGroup;
	private CriteriaJoin criteriaJoin;
	private Integer resultLimit;

	/**
	 * @param modelEntityClass
	 */
	public Criteria(Class<?> modelEntityClass) {
		this.modelEntityClass = modelEntityClass;
	}

	/**
	 * @param modelEntityClass
	 * @param resultLimit - max limit of records.
	 */
	public Criteria(Class<?> modelEntityClass, int resultLimit) {
		this.modelEntityClass = modelEntityClass;
		this.resultLimit = resultLimit;
	}

	/**
	 * Structure: AND [fieldName] = [value]
	 * 
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public Criteria addFilter(String fieldName, Object value) {
		addFilter(fieldName, Filter.EQUAL_TO, value);
		return this;
	}

	/**
	 * Structure: AND [fieldName] [operator] [value]
	 * 
	 * @param fieldName
	 * @param operator
	 * @param value
	 * @return criteria
	 */
	public Criteria addFilter(String fieldName, int operator, Object value) {
		addFilter(Filter.AND, fieldName, operator, value);
		return this;
	}

	/**
	 * Structure: [logicalOperator] [fieldName] [operator] [value]
	 * 
	 * @param logicalOperator
	 * @param fieldName
	 * @param operator
	 * @param value
	 * @return
	 */
	public Criteria addFilter(int logicalOperator, String fieldName, int operator, Object value) {
		if(value == null) {
			throw new RuntimeException("Value cannot be null.");
		}
		getCriteriaWhere().addLogicalOperator(logicalOperator);
		getCriteriaWhere().addCondition(replaceWithAlias(fieldName), operator);
		getCriteriaWhere().addConditionValue(value);		
		return this;
	}

	/**
	 * Fields must contain the prefix \@.<br>
	 * Eg: \@name = ? AND \@client.age > ?<br>
	 * Structure: AND [condition]
	 * 
	 * @param condition
	 * @param values
	 * @return
	 */
	public Criteria addCondition(String condition, Object... values) {
		addCondition(Filter.AND, condition, values);
		return this;
	}

	/**
	 * Fields must contain the prefix \@.<br>
	 * Eg: \@name = ? AND \@client.age > ?<br>
	 * Structure: [logicalOperator] [condition]
	 * 
	 * @param logicalOperator
	 * @param condition
	 * @param values
	 * @return
	 */
	public Criteria addCondition(int logicalOperator, String condition, Object... values) {
		if(condition == null) {
			throw new RuntimeException("Condition cannot be null.");
		}
		getCriteriaWhere().addLogicalOperator(logicalOperator);
		getCriteriaWhere().addCondition(resolveSql(condition));
		getCriteriaWhere().addConditionValue(values);
		return this;
	}

	/**
	 * Structure: [columnName] ASC
	 * 
	 * @param columnName
	 * @return
	 */
	public Criteria addOrder(String columnName) {
		addOrder(columnName, Filter.ASC);
		return this;
	}

	/**
	 * Structure: [columnName] ASC
	 * 
	 * @param columnName
	 * @return
	 */
	public Criteria addOrderAsc(String columnName) {
		addOrder(columnName, Filter.ASC);
		return this;
	}
	
	/**
	 * Structure: [columnName] DESC
	 * 
	 * @param columnName
	 * @return
	 */
	public Criteria addOrderDesc(String columnName) {
		addOrder(columnName, Filter.DESC);
		return this;
	}
	
	/**
	 * Structure: [columnName] [orderDirection]
	 * 
	 * @param columnName
	 * @param orderDirection
	 * @return
	 */
	public Criteria addOrder(String columnName, int orderDirection) {
		getCriteriaOrder().addOrder(replaceWithAlias(columnName), orderDirection);
		return this;
	}

	/**
	 * @param columnName
	 * @return
	 */
	public Criteria addGrouping(String columnName) {
		getCriteriaGroup().addGrouping(replaceWithAlias(columnName));
		return this;
	}

	/**
	 * @param fetches
	 * @return
	 */
	public Criteria addFetches(String... fetches) {
		for(String fetch: fetches) {
			addFetch(fetch);
		}
		return this;
	}

	/**
	 * Fields are joined with dot divisor.
	 * 
	 * @param fetch
	 * @return
	 */
	public Criteria addFetch(String... fetch) {
		getCriteriaJoin().addFetch(modelEntityClass, fetch);
		return this;
	}

	/**
	 * @param fetch
	 * @return
	 */
	public Criteria addFetch(String fetch) {
		getCriteriaJoin().addFetch(modelEntityClass, fetch.split("\\."));
		return this;
	}

	/**
	 * @return
	 */
	public Criteria clearConditions() {
		if(hasCondition()) {
			getCriteriaWhere().clearConditions();
		}
		return this;
	}

	/**
	 * @return
	 */
	public Criteria clearOrders() {
		if(hasOrder()) {
			getCriteriaOrder().clearOrders();
		}
		return this;
	}

	/**
	 * @return
	 */
	public Criteria clearGroupings() {
		if(hasGrouping()) {
			getCriteriaGroup().clearGroupings();
		}
		return this;
	}

	/**
	 * @return
	 */
	public Criteria clearJoins() {
		if(hasJoin()) {
			getCriteriaJoin().clearJoins();
		}
		return this;
	}

	/**
	 * @return
	 */
	public Criteria setResultLimit(Integer resultLimit) {
		this.resultLimit = resultLimit;
		return this;
	}

	/**
	 * @return
	 */
	public Integer getResultLimit() {
		return resultLimit;
	}

	/**
	 * @return
	 */
	public String getConditions() {
		return getCriteriaWhere().getConditions().toString();
	}

	/**
	 * @return
	 */
	public List<Object> getConditionsValues() {
		return getCriteriaWhere().getConditionsValues();
	}

	/**
	 * @return
	 */
	public String getOrders() {
		return getCriteriaOrder().getOrders().toString();
	}

	/**
	 * @return
	 */
	public String getGroupings() {
		return getCriteriaGroup().getGroupings().toString();
	}

	/**
	 * @return
	 */
	public List<JoinColumnField> getJoinFields() {
		return getCriteriaJoin().getJoinFields();
	}

	/**
	 * @return
	 */
	public boolean hasCondition() {
		return criteriaWhere != null && (criteriaWhere.getConditions().length() > 0);
	}

	/**
	 * @return
	 */
	public boolean hasOrder() {
		return criteriaOrder != null && (criteriaOrder.getOrders().length() > 0);
	}

	/**
	 * @return
	 */
	public boolean hasGrouping() {
		return criteriaGroup != null && (criteriaGroup.getGroupings().length() > 0);
	}

	/**
	 * @return
	 */
	public boolean hasJoin() {
		return criteriaJoin != null && (!criteriaJoin.joinFields.isEmpty());
	}
	
	/**
	 * @param sql
	 * @return
	 */
	protected String resolveSql(String sql) {
		String resolvedSql = sql.replaceAll("@from", ModelConstants.MAIN_TABLE_ALIAS);
		resolvedSql = replaceFieldsWithAlias(resolvedSql);
		return resolvedSql;
	}
	
	/**
	 * @param sql
	 * @return
	 */
	protected String replaceFieldsWithAlias(String sql) {
		String replacedSql = sql;

		int startField = -1;
		int endField = -1;

		int emptyIndex;
		int parenthesisIndex;
		
		String signedFieldName;
		String fieldName;

		while((startField = sql.indexOf("@", startField + 1)) >= 0) {
			// Field ends in the first empty space or close bracket after the @.
			emptyIndex = sql.indexOf(" ", startField);
			parenthesisIndex = sql.indexOf(")", startField);
			
			if(emptyIndex > 0 && (parenthesisIndex < 0 || emptyIndex < parenthesisIndex)) {
				endField = emptyIndex;
			}
			else if(parenthesisIndex > 0) {
				endField = parenthesisIndex;
			}
			else {
				throw new RuntimeException("Invalid criteria. Check the conditions syntax.");
			}

			signedFieldName = sql.substring(startField, endField);
			fieldName = signedFieldName.substring(1, signedFieldName.length());
			replacedSql = replacedSql.replaceFirst(signedFieldName, replaceWithAlias(fieldName));
		}
		return replacedSql;
	}

	/**
	 * 1. Add fetches if necessary;<br>
	 * 2. Replace join class path for alias if necessary;<br>
	 * 3. Replace field name for respective column name.
	 * 
	 * @param fieldName
	 * @return
	 */
	public String replaceWithAlias(String fieldName) {
		Integer lastIndexOf = fieldName.lastIndexOf(".");
		if(lastIndexOf > 0) {
			String fetch = fieldName.substring(0, lastIndexOf);
			String name = fieldName.substring(lastIndexOf + 1);

			JoinColumnField joinField = getCriteriaJoin().addFetch(modelEntityClass, fetch.split("\\."));
			String columnName = ModelUtil.getColumnName(joinField.getTable().getType(), name);

			fieldName = fieldName.replaceFirst(fetch, joinField.getAlias());
			fieldName = fieldName.replaceFirst(name, columnName);
		}
		else {
			fieldName = ModelConstants.MAIN_TABLE_ALIAS.concat(".").concat(ModelUtil.getColumnName(modelEntityClass, fieldName));
		}
		return fieldName;
	}

	/**
	 * @return
	 */
	public CriteriaWhere getCriteriaWhere() {
		if(criteriaWhere == null) {
			criteriaWhere = new CriteriaWhere();
		}
		return criteriaWhere;
	}

	/**
	 * @return
	 */
	public CriteriaOrder getCriteriaOrder() {
		if(criteriaOrder == null) {
			criteriaOrder = new CriteriaOrder();
		}
		return criteriaOrder;
	}

	/**
	 * @return
	 */
	public CriteriaGroup getCriteriaGroup() {
		if(criteriaGroup == null) {
			criteriaGroup = new CriteriaGroup();
		}
		return criteriaGroup;
	}

	/**
	 * @return
	 */
	public CriteriaJoin getCriteriaJoin() {
		if(criteriaJoin == null) {
			criteriaJoin = new CriteriaJoin();
		}
		return criteriaJoin;
	}
}