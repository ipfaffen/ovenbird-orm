package com.ipfaffen.ovenbird.model.criteria;

import com.ipfaffen.ovenbird.commons.PagingHelper;
import com.ipfaffen.ovenbird.model.ModelConstants;

/**
 * @author Isaias Pfaffenseller
 */
public class PagingCriteria extends Criteria {

	private PagingHelper pagingHelper;
	private boolean isPagingEnabled = true;

	/**
	 * @param modelEntityClass
	 * @param pageSize - number of records for page.
	 */
	public PagingCriteria(Class<?> modelEntityClass, int pageSize) {
		super(modelEntityClass);
		pagingHelper = new PagingHelper(pageSize);
	}

	/**
	 * Size default of 10 records for page.
	 * 
	 * @param modelEntityClass
	 */
	public PagingCriteria(Class<?> modelEntityClass) {
		this(modelEntityClass, ModelConstants.DEFAULT_PAGE_SIZE);
	}

	/**
	 * Enable paging.
	 */
	public void enablePaging() {
		isPagingEnabled = true;
	}

	/**
	 * Disable paging.
	 */
	public void disablePaging() {
		isPagingEnabled = false;
	}

	/**
	 * @return
	 */
	public boolean isPagingEnabled() {
		return isPagingEnabled;
	}

	/**
	 * @return
	 */
	public PagingHelper getPaging() {
		return pagingHelper;
	}
}