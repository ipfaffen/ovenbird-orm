package com.ipfaffen.ovenbird.model;

/**
 * @author Isaias Pfaffenseller
 */
public final class ModelConstants {
	
	/**
	 * Alias of main table in sql query.
	 */
	public static String MAIN_TABLE_ALIAS = "t_main";

	/**
	 * Prefix for joins alias.
	 */
	public static String ALIAS_PREFFIX = "t_";

	/**
	 * Max fetch depth.
	 */
	public static int JOIN_MAX_DEPTH = 20;

	/**
	 * Pagination default page size.
	 */
	public static int DEFAULT_PAGE_SIZE = 10;
}