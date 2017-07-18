package com.ipfaffen.ovenbird.model.util;

import java.util.ArrayList;

/**
 * @author Isaias Pfaffenseller
 */
@SuppressWarnings("serial")
public class FieldList extends ArrayList<ColumnField> {
	
	/**
	 * Find id field (isId = true).
	 * 
	 * @return
	 */
	public ColumnField getIdField() {
		for(ColumnField field: this) {
			if(field.isId()) {
				return field;
			}
		}
		return null;
	}
}