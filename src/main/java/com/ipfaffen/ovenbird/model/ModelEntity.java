package com.ipfaffen.ovenbird.model;

import com.ipfaffen.ovenbird.commons.ReflectionUtil;
import com.ipfaffen.ovenbird.model.util.ColumnField;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class ModelEntity<T extends ModelEntity<T>> implements Comparable<T> {

	/**
	 * @return
	 */
	public abstract Long getId();
	
	/**
	 * @param id
	 */
	public abstract void setId(Long id);

	/**
	 * Return field value by calling its getter method.
	 * 
	 * @param fieldName
	 * @return
	 */
	public final Object getFieldData(String fieldName) {
		try {
			return ReflectionUtil.callFieldGetter(this, fieldName);
		}
		catch(Exception e) {
			return null;
		}
	}

	@Override
	public int hashCode() {
		return new StringBuilder(getClass().getName()).append("#").append(getId()).toString().hashCode();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object object) {
		if(object == null || !(object instanceof ModelEntity)) {
			return false;
		}
		ModelEntity entity = (ModelEntity) object;
		return ((entity.getId() == null) ? (getId() == null) : (entity.getId().compareTo(getId()) == 0));
	}

	@Override
	public String toString() {
		StringBuilder fields = new StringBuilder();
		for(ColumnField field: ModelUtil.getEntityFields(this)) {
			fields.append("[").append(field.getAttributeName()).append("=\'").append(String.valueOf(field.getValue())).append("\']");
		}
		return fields.toString();
	}

	@Override
	public int compareTo(T entity) {
		Long entityAId = this.getId();
		if(entity == null) {
			return -1;
		}
		Long entityBId = entity.getId();
		if(entityAId == null && entityBId == null) {
			return 0;
		}
		else if(entityAId == null && entityBId != null) {
			return -1;
		}
		else if(entityAId != null && entityBId == null) {
			return 1;
		}
		return entityAId.compareTo(entityBId);
	}
}