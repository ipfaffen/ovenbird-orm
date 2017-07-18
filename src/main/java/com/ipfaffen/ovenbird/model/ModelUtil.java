package com.ipfaffen.ovenbird.model;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.ipfaffen.ovenbird.commons.NameValue;
import com.ipfaffen.ovenbird.commons.ReflectionUtil;
import com.ipfaffen.ovenbird.model.annotation.Column;
import com.ipfaffen.ovenbird.model.annotation.JoinColumn;
import com.ipfaffen.ovenbird.model.annotation.Table;
import com.ipfaffen.ovenbird.model.util.ColumnField;
import com.ipfaffen.ovenbird.model.util.FieldList;
import com.ipfaffen.ovenbird.model.util.JoinColumnField;
import com.ipfaffen.ovenbird.model.util.TableEntity;

/**
 * @author Isaias Pfaffenseller
 */
public class ModelUtil {
	
	/**
	 * @param entity
	 * @param joinFields
	 * @param resultSet
	 * @throws SQLException
	 */
	public static void populateJoinFields(ModelEntity<?> entity, Collection<JoinColumnField> joinFields, ResultSet resultSet) throws SQLException {
		ModelEntity<?>[] baseEntity = new ModelEntity<?>[ModelConstants.JOIN_MAX_DEPTH];
		baseEntity[0] = entity;

		for(JoinColumnField joinField: joinFields) {
			ModelEntity<?> referenceEntity = (ModelEntity<?>) ReflectionUtil.newInstance(joinField.getTable().getType());

			populateFields(
					referenceEntity, 
					getEntityFields(referenceEntity), 
					resultSet, 
					joinField.getAlias());

			if(referenceEntity.getId() == null) {
				// If id is null it means that doesn't exist the relationship.
				continue;
			}

			ReflectionUtil.setFieldValue(
					baseEntity[joinField.getDepthLevel()], 
					joinField.getBaseField(), 
					referenceEntity);

			baseEntity[joinField.getDepthLevel() + 1] = referenceEntity;
		}
	}

	/**
	 * @param entity
	 * @param fields
	 * @param resultSet
	 * @throws SQLException
	 */
	public static void populateFields(ModelEntity<?> entity, List<ColumnField> fields, ResultSet resultSet) throws SQLException {
		populateFields(entity, fields, resultSet, ModelConstants.MAIN_TABLE_ALIAS);
	}
	
	/**
	 * @param entity
	 * @param fields
	 * @param resultSet
	 * @param alias
	 * @throws SQLException
	 */
	public static void populateFields(ModelEntity<?> entity, List<ColumnField> fields, ResultSet resultSet, String alias) throws SQLException {
		for(ColumnField field: fields) {
			ReflectionUtil.setFieldValue(entity, field.getDeclaredField(), resultSet.getObject(alias + "." + field.getColumnName()));
		}
	}
	
	/**
	 * @param entity
	 * @return
	 */
	public static FieldList getEntityFields(ModelEntity<?> entity) {
		FieldList fields = new FieldList();
		for(Field declaredField: entity.getClass().getDeclaredFields()) {
			declaredField.setAccessible(true);

			Column column = (Column) declaredField.getAnnotation(Column.class);
			if(column != null) {
				ColumnField field = new ColumnField();
				field.setDeclaredField(declaredField);
				field.setAttributeName(declaredField.getName());
				field.setColumnName(column.name());
				field.setValue(ReflectionUtil.getFieldValue(entity, declaredField));
				field.setType(declaredField.getType());
				field.setGenericType(declaredField.getGenericType().toString());
				field.setIsId(column.isKey());
				fields.add(field);
			}
		}
		return fields;
	}
	
	/**
	 * @param entityClass
	 * @return
	 */
	public static FieldList getEntityFields(Class<? extends ModelEntity<?>> entityClass) {
		FieldList fields = new FieldList();
		for(Field declaredField: entityClass.getDeclaredFields()) {
			declaredField.setAccessible(true);

			Column column = (Column) declaredField.getAnnotation(Column.class);
			if(column != null) {
				ColumnField field = new ColumnField();
				field.setDeclaredField(declaredField);
				field.setAttributeName(declaredField.getName());
				field.setColumnName(column.name());
				field.setType(declaredField.getType());
				field.setGenericType(declaredField.getGenericType().toString());
				field.setIsId(column.isKey());
				fields.add(field);
			}
		}
		return fields;
	}
	
	/**
	 * @param dto
	 * @param fields
	 * @param resultSet
	 * @throws SQLException
	 */
	public static void populateFields(ModelDto<?> dto, List<ColumnField> fields, ResultSet resultSet) throws SQLException {
		for(ColumnField field: fields) {
			ReflectionUtil.setFieldValue(dto, field.getDeclaredField(), resultSet.getObject(field.getColumnName()));
		}
	}
	
	/**
	 * @param dtoClass
	 * @return
	 */
	public static FieldList getDtoFields(Class<? extends ModelDto<?>> dtoClass) {
		FieldList fields = new FieldList();
		for(Field declaredField: dtoClass.getDeclaredFields()) {
			declaredField.setAccessible(true);
			
			Column column = (Column) declaredField.getAnnotation(Column.class);
			if(column != null) {
				ColumnField field = new ColumnField();
				field.setDeclaredField(declaredField);
				field.setAttributeName(declaredField.getName());
				field.setColumnName(column.name());
				field.setType(declaredField.getType());
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * Build field list based on the given map.
	 * 
	 * @param entityClass
	 * @param declaredFields
	 * @return
	 */
	public static FieldList buildFieldList(Class<?> entityClass, Map<String, Object> declaredFields) {
		FieldList fields = new FieldList();
		for(String fieldName: declaredFields.keySet()) {
			ColumnField field = getField(entityClass, fieldName);
			field.setValue(declaredFields.get(fieldName));
			fields.add(field);
		}
		return fields;
	}	

	/**
	 * Build field list based on the given name value pair list.
	 * 
	 * @param entityClass
	 * @param declaredFields
	 * @return
	 */
	public static FieldList buildFieldList(Class<?> entityClass, List<NameValue> declaredFields) {
		FieldList fields = new FieldList();
		for(NameValue declaredField: declaredFields) {
			ColumnField field = getField(entityClass, declaredField.getName());
			field.setValue(declaredField.getValue());
			fields.add(field);
		}
		return fields;
	}

	/**
	 * Get respective column field.
	 * 
	 * @param entityClass
	 * @param fieldName
	 * @return
	 */
	public static ColumnField getField(Class<?> entityClass, String fieldName) {
		try {
			return getField(entityClass.getDeclaredField(fieldName));
		}
		catch(Exception e) {
			throw new RuntimeException("Invalid field.");
		}
	}

	/**
	 * Get respective column field.
	 * 
	 * @param declaredField
	 * @return
	 */
	public static ColumnField getField(Field declaredField) {
		try {
			declaredField.setAccessible(true);

			Column column = (Column) declaredField.getAnnotation(Column.class);
			ColumnField field = new ColumnField();
			field.setDeclaredField(declaredField);
			field.setAttributeName(declaredField.getName());
			field.setColumnName(column.name());
			field.setType(declaredField.getType());
			field.setGenericType(declaredField.getGenericType().toString());
			field.setIsId(column.isKey());
			return field;
		}
		catch(Exception e) {
			throw new RuntimeException("Invalid field.");
		}
	}

	/**
	 * Get entity id column field.
	 * 
	 * @param entity
	 * @return
	 */
	public static ColumnField getIdField(ModelEntity<?> entity) {
		for(Field declaredField: entity.getClass().getDeclaredFields()) {
			declaredField.setAccessible(true);

			Column column = (Column) declaredField.getAnnotation(Column.class);
			if(column != null && column.isKey()) {
				ColumnField field = new ColumnField();
				field.setDeclaredField(declaredField);
				field.setAttributeName(declaredField.getName());
				field.setColumnName(column.name());
				field.setValue(ReflectionUtil.getFieldValue(entity, declaredField));
				field.setType(declaredField.getType());
				field.setGenericType(declaredField.getGenericType().toString());
				field.setIsId(true);
				return field;
			}
		}
		return null;
	}

	/**
	 * Get entity id column field.
	 * 
	 * @param entityClass
	 * @return
	 */
	public static ColumnField getIdField(Class<?> entityClass) {
		for(Field declaredField: entityClass.getDeclaredFields()) {
			declaredField.setAccessible(true);

			Column column = (Column) declaredField.getAnnotation(Column.class);
			if(column != null && column.isKey()) {
				ColumnField field = new ColumnField();
				field.setDeclaredField(declaredField);
				field.setAttributeName(declaredField.getName());
				field.setColumnName(column.name());
				field.setType(declaredField.getType());
				field.setGenericType(declaredField.getGenericType().toString());
				field.setIsId(true);
				return field;
			}
		}
		return null;
	}

	/**
	 * Get class table entity.
	 * 
	 * @param entity
	 * @return
	 */
	public static TableEntity getTableEntity(Class<?> entityClass) {
		Table table = (Table) entityClass.getAnnotation(Table.class);
		TableEntity tableEntity = new TableEntity();
		tableEntity.setTableName(table.name());
		tableEntity.setType(entityClass);
		return tableEntity;
	}

	/**
	 * Get class table name.
	 * 
	 * @param entityClass
	 * @return
	 */
	public static String getTableName(Class<?> entityClass) {
		return ((Table) entityClass.getAnnotation(Table.class)).name();
	}

	/**
	 * Get field column name.
	 * 
	 * @param entityClass
	 * @param fieldName
	 * @return
	 */
	public static String getColumnName(Class<?> entityClass, String fieldName) {
		try {
			return ((Column) entityClass.getDeclaredField(fieldName).getAnnotation(Column.class)).name();
		}
		catch(Exception e) {
			throw new RuntimeException("Invalid field.");
		}
	}

	/**
	 * Get join column field.
	 * 
	 * @param baseClass
	 * @param fieldName
	 * @return
	 */
	public static JoinColumnField getJoinField(Class<?> entityClass, String fieldName) {
		try {
			Field baseField = entityClass.getDeclaredField(fieldName);
			baseField.setAccessible(true);
			
			JoinColumn joinColumn = (JoinColumn) baseField.getAnnotation(JoinColumn.class);

			TableEntity baseTable = getTableEntity(entityClass);
			ColumnField baseIdColumn = getField(entityClass, joinColumn.name());

			TableEntity referenceTable = getTableEntity(baseField.getType());
			ColumnField referenceIdColumn = getIdField(baseField.getType());

			JoinColumnField joinColumnField = new JoinColumnField();
			joinColumnField.setBaseTable(baseTable);
			joinColumnField.setBaseIdColumn(baseIdColumn);
			joinColumnField.setBaseField(baseField);
			joinColumnField.setTable(referenceTable);
			joinColumnField.setIdColumn(referenceIdColumn);
			return joinColumnField;
		}
		catch(Exception e) {
			throw new RuntimeException("Invalid join field.");
		}
	}

	/**
	 * List all join fields.
	 */
	public static List<JoinColumnField> getJoinFields(ModelEntity<?> entity, Class<?> fieldType) {
		List<JoinColumnField> joinFields = new ArrayList<JoinColumnField>();

		for(Field declaredField: entity.getClass().getDeclaredFields()) {
			JoinColumn join = (JoinColumn) declaredField.getAnnotation(JoinColumn.class);
			if(join != null) {
				if(declaredField.getType() != fieldType) {
					continue;
				}
				joinFields.add(getJoinField(entity.getClass(), declaredField.getName()));
			}
		}
		return joinFields;
	}
	
	/**
	 * Sort list by given field.
	 * 
	 * @param entitys
	 * @param fieldName
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void sort(List entityList, final String fieldName) {
		Collections.sort(entityList, new Comparator<ModelEntity>() {
			public int compare(ModelEntity entity1, ModelEntity entity2) {
				Comparable entity1Field = (Comparable) entity1.getFieldData(fieldName);
				Comparable entity2Field = (Comparable) entity2.getFieldData(fieldName);
				return entity1Field.compareTo(entity2Field);
			}
		});
	}

	/**
	 * Sort list by given fields.
	 * 
	 * @param entityList
	 * @param fieldsNames
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void sort(List entityList, final String[] fieldsNames) {
		Collections.sort(entityList, new Comparator<ModelEntity>() {
			public int compare(ModelEntity entity1, ModelEntity entity2) {
				int compare = 0;
				for(String fieldName: fieldsNames) {
					Comparable entity1Field = (Comparable) entity1.getFieldData(fieldName);
					Comparable entity2Field = (Comparable) entity2.getFieldData(fieldName);
					if((compare = entity1Field.compareTo(entity2Field)) != 0) {
						break;
					}
				}
				return compare;
			}
		});
	}
}