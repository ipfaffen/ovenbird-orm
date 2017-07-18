package com.ipfaffen.ovenbird.model;

import com.ipfaffen.ovenbird.model.connection.Database;
import com.ipfaffen.ovenbird.model.exception.InterceptorException;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class ModelInterceptor<T extends ModelEntity<T>> {
	
	private Database db;
	private T entity;

	/**
	 * @param database
	 */
	public ModelInterceptor(Database database) {
		this.db = database;
	}

	/**
	 * Called before insert or update.
	 * 
	 * @throws InterceptorException
	 */
	public void beforeSave() throws InterceptorException {
	}

	/**
	 * Called after insert or update.
	 * 
	 * @param saved
	 * @throws InterceptorException
	 */
	public void afterSave(boolean saved) throws InterceptorException {
	}

	/**
	 * @throws InterceptorException
	 */
	public void beforeInsert() throws InterceptorException {
	}

	/**
	 * @param inserted
	 * @throws InterceptorException
	 */
	public void afterInsert(boolean inserted) throws InterceptorException {
	}

	/**
	 * @throws InterceptorException
	 */
	public void beforeUpdate() throws InterceptorException {
	}

	/**
	 * @param updated
	 * @throws InterceptorException
	 */
	public void afterUpdate(boolean updated) throws InterceptorException {
	}

	/**
	 * @throws InterceptorException
	 */
	public void beforeDelete() throws InterceptorException {
	}

	/**
	 * @param deleted
	 * @throws InterceptorException
	 */
	public void afterDelete(boolean deleted) throws InterceptorException {
	}

	/**
	 * @throws InterceptorException
	 */
	public void beforeLoad() throws InterceptorException {
	}

	/**
	 * @param loaded
	 * @throws InterceptorException
	 */
	public void afterLoad(boolean loaded) throws InterceptorException {
	}

	/**
	 * @return
	 */
	protected Database getDatabase() {
		return db;
	}

	/**
	 * @param entity
	 */
	protected void setEntity(T entity) {
		this.entity = entity;
	}

	/**
	 * @return
	 */
	public T getEntity() {
		return entity;
	}
}