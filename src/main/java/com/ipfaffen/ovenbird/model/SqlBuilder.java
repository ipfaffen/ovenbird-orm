package com.ipfaffen.ovenbird.model;

/**
 * @author Isaias Pfaffenseller
 */
public class SqlBuilder {
	
	private StringBuilder sb;
	
	public SqlBuilder() {
		sb = new StringBuilder();
	}
	
	/**
	 * @param str
	 * @return
	 */
	public SqlBuilder append(String str) {
		sb.append(str);
		return this;
	}
	
	/**
	 * @param str
	 * @return
	 */
	public SqlBuilder $(String str) {
		return append(str);
	}

	/**
	 * @param str
	 * @param args
	 * @return
	 */
	public SqlBuilder append(String str, Object... args) {
		sb.append(String.format(str, args));
		return this;
	}
	
	/**
	 * @param str
	 * @param args
	 * @return
	 */
	public SqlBuilder $(String str, Object... args) {
		return append(str, args);
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
}