package com.ipfaffen.ovenbird.model.criteria;

import com.ipfaffen.ovenbird.model.criteria.Criteria.Filter;

/**
 * @author Isaias Pfaffenseller
 */
public class CriteriaOrder {
	
	private StringBuilder orders;

	public CriteriaOrder() {
		orders = new StringBuilder();
	}
	
	public void clearOrders() {
		orders = new StringBuilder();
	}

	/**
	 * @param columnName
	 * @param orderDirection
	 */
	public void addOrder(String columnName, int orderDirection) {
		if(orders.length() > 0) {
			orders.append(", ");
		}
		orders.append(columnName);
		orders.append(" ");
		orders.append(getOrderDirection(orderDirection));
		orders.append(" ");
	}

	/**
	 * @param orderDirection
	 * @return
	 */
	private String getOrderDirection(int orderDirection) {
		if(orderDirection == Filter.DESC) {
			return "DESC";
		}
		return "ASC";
	}

	/**
	 * @return
	 */
	protected StringBuilder getOrders() {
		return orders;
	}
}