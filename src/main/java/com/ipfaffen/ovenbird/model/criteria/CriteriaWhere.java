package com.ipfaffen.ovenbird.model.criteria;

import java.util.ArrayList;
import java.util.List;

import com.ipfaffen.ovenbird.model.criteria.Criteria.Filter;

/**
 * @author Isaias Pfaffenseller
 */
public class CriteriaWhere {
	
	private StringBuilder conditions;
	private List<Object> conditionsValues;

	public CriteriaWhere() {
		conditions = new StringBuilder();
		conditionsValues = new ArrayList<Object>();
	}
	
	public void clearConditions() {
		conditions = new StringBuilder();
		conditionsValues.clear();
	}

	/**
	 * @param logicalOperator
	 */
	public void addLogicalOperator(int logicalOperator) {
		if(conditions.length() > 0) {
			conditions.append(" ");
			conditions.append(getLogicalOperator(logicalOperator));
			conditions.append(" ");
		}
	}

	/**
	 * @param columnName
	 * @param operator
	 */
	public void addCondition(String columnName, int operator) {
		addCondition(buildCondition(columnName, operator));
	}

	/**
	 * @param filter
	 */
	public void addCondition(String filter) {
		conditions.append(filter);
	}

	/**
	 * @param conditionValues
	 */
	public void addConditionValue(Object[] conditionValues) {
		for(Object conditionValue: conditionValues) {
			addConditionValue(conditionValue);
		}
	}

	/**
	 * @param conditionValue
	 */
	public void addConditionValue(Object conditionValue) {
		conditionsValues.add(conditionValue);
	}

	/**
	 * @param columnName
	 * @param operator
	 * @return
	 */
	private String buildCondition(String columnName, int operator) {
		StringBuilder condition = new StringBuilder();
		condition.append(columnName);
		condition.append(" ");

		switch(operator) {
			case Filter.EQUAL_TO:
				condition.append("=");
				break;
			case Filter.NOT_EQUAL_TO:
				condition.append("<>");
				break;
			case Filter.GREATER_THAN:
				condition.append(">");
				break;
			case Filter.GREATER_OR_EQUAL_TO:
				condition.append(">=");
				break;
			case Filter.LESS_THAN:
				condition.append("<");
				break;
			case Filter.LESS_OR_EQUAL_TO:
				condition.append("<=");
				break;
			case Filter.IN:
				condition.append("IN");
				break;
			case Filter.LIKE:
				condition.append("LIKE");
				break;
			default:
				condition.append("=");
				break;
		}

		condition.append(" ");
		condition.append("?");
		return condition.toString();
	}

	/**
	 * @param logicalOperator
	 * @return
	 */
	private String getLogicalOperator(int logicalOperator) {
		if(logicalOperator == Filter.OR) {
			return "OR";
		}
		return "AND";
	}

	/**
	 * @return
	 */
	protected StringBuilder getConditions() {
		return conditions;
	}

	/**
	 * @return
	 */
	protected List<Object> getConditionsValues() {
		return conditionsValues;
	}
}