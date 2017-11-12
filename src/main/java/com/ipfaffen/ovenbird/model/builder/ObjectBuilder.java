package com.ipfaffen.ovenbird.model.builder;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;

import com.ipfaffen.ovenbird.commons.ReflectionUtil;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class ObjectBuilder<T> {
	
	private Class<T> objectClass;

	@SuppressWarnings("unchecked")
	public ObjectBuilder() {
		try {
			objectClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		catch(ClassCastException e) {
			// This is necessary when transaction proxy is been used.
			objectClass = (Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
	}
	
	public final T build(ResultSet resultSet) throws Exception {
		T object = buildObject();
		build(resultSet, object);
		return object;
	}

	public abstract void build(ResultSet resultSet, T object) throws Exception;

	@SuppressWarnings("unchecked")
	private T buildObject() {
		return (T) ReflectionUtil.newInstance(objectClass);
	}
}