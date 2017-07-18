package com.ipfaffen.ovenbird.model.criteria;

/**
 * @author Isaias Pfaffenseller
 */
public class CriteriaGroup {
	
	private StringBuilder groupings;

	public CriteriaGroup() {
		groupings = new StringBuilder();
	}
	
	public void clearGroupings() {
		groupings = new StringBuilder();
	}

	/**
	 * @param columnName
	 */
	public void addGrouping(String columnName) {
		if(groupings.length() > 0) {
			groupings.append(", ");
		}
		groupings.append(columnName);
	}

	/**
	 * @return
	 */
	protected StringBuilder getGroupings() {
		return groupings;
	}
}