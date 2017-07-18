package com.ipfaffen.ovenbird.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Isaias Pfaffenseller
 */
public class SqlStatement {
	
	private StringBuilder sb;
	private List<Object> parameters;
	
	public SqlStatement() {
		sb = new StringBuilder();
		parameters = new ArrayList<Object>();
	}
	
	/**
	 * @param str
	 * @return
	 */
	public SqlStatement append(String str) {
		sb.append(str);
		return this;
	}
	
	/**
	 * @param str
	 * @return
	 */
	public SqlStatement $(String str) {
		return append(str);
	}

	/**
	 * @param str
	 * @param params
	 * @return
	 */
	public SqlStatement append(String str, Object... params) {
		sb.append(str);
		for(Object param: params) {
			parameters.add(param);
		}
		return this;
	}
	
	/**
	 * @param str
	 * @param params
	 * @return
	 */
	public SqlStatement $(String str, Object... params) {
		return append(str, params);
	}
	
	/**
	 * @return
	 */
	public List<Object> getParameters() {
		return parameters;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
}