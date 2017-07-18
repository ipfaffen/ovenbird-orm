package com.ipfaffen.ovenbird.model;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.lang3.StringUtils;

import com.ipfaffen.ovenbird.commons.DataList;
import com.ipfaffen.ovenbird.commons.NameValue;
import com.ipfaffen.ovenbird.commons.PagedList;
import com.ipfaffen.ovenbird.commons.PagingHelper;
import com.ipfaffen.ovenbird.commons.ReflectionUtil;
import com.ipfaffen.ovenbird.model.annotation.Interceptor;
import com.ipfaffen.ovenbird.model.connection.ConnectionHandler;
import com.ipfaffen.ovenbird.model.connection.Database;
import com.ipfaffen.ovenbird.model.criteria.Criteria;
import com.ipfaffen.ovenbird.model.criteria.PagingCriteria;
import com.ipfaffen.ovenbird.model.dialect.SqlDialect;
import com.ipfaffen.ovenbird.model.exception.ConnectionException;
import com.ipfaffen.ovenbird.model.exception.InterceptorException;
import com.ipfaffen.ovenbird.model.exception.ModelException;
import com.ipfaffen.ovenbird.model.transaction.ResultTransaction;
import com.ipfaffen.ovenbird.model.transaction.Transaction;
import com.ipfaffen.ovenbird.model.util.ColumnField;
import com.ipfaffen.ovenbird.model.util.FieldList;
import com.ipfaffen.ovenbird.model.util.JoinColumnField;

/**
 * @author Isaias Pfaffenseller
 */
public abstract class ModelDao<T extends ModelEntity<T>> implements ConnectionHandler {
	
	private Database db;
	private ModelHelper helper;
	private SqlDialect dialect;

	private Class<T> entityClass;
	private ColumnField entityIdField;
	private ModelInterceptor<T> interceptor;

	private String tableName;
	private String basePackage;

	/**
	 * @param db
	 */
	public ModelDao(Database db) {
		initialize(db);
	}

	/**
	 * @param db
	 */
	@SuppressWarnings("unchecked")
	private void initialize(Database db) {
		this.db = db;

		Interceptor interceptorAnnotation = getClass().getAnnotation(Interceptor.class);
		if(interceptorAnnotation != null) {
			Class<? extends ModelInterceptor<? extends ModelEntity<?>>> interceptorClass = interceptorAnnotation.value();
			interceptor = (ModelInterceptor<T>) ReflectionUtil.newInstance(interceptorClass, Database.class, db);
		}
		
		try {
			entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		catch(ClassCastException e) {
			// This is necessary when transaction proxy is been used.
			entityClass = (Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		entityIdField = ModelUtil.getIdField(entityClass);
		tableName = ModelUtil.getTableName(entityClass);
		basePackage = StringUtils.substringBeforeLast(entityClass.getPackage().getName(), ".");
	}

	/**
	 * @return
	 */
	protected Class<?>[] getRelatedEntityClass() {
		return new Class<?>[]{};
	}

	/**
	 * Insert record in the database.
	 * 
	 * @param entity
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T insert(T entity) throws ConnectionException, ModelException, InterceptorException {
		boolean success = true;
		try {
			openTransaction();

			if(interceptor != null) {
				interceptor.setEntity(entity);
				interceptor.beforeSave();
				interceptor.beforeInsert();
			}
			
			insertRecord(entity);
			return entity;
		}
		catch(Exception e) {
			success = false;
			throw e;
		}
		finally {
			if(interceptor != null) {
				interceptor.afterSave(success);
				interceptor.afterInsert(success);
			}
			closeTransaction(success);
		}
	}

	/**
	 * @param entity
	 * @throws ModelException
	 */
	private void insertRecord(T entity) throws ModelException {
		PreparedStatement statement = null;
		ResultSet generatedKeys = null;
		try {
			FieldList fields = ModelUtil.getEntityFields(entity);

			statement = buildInsertPreparedStatement(fields);
			statement.executeUpdate();

			generatedKeys = statement.getGeneratedKeys();
			if(generatedKeys.next()) {
				ReflectionUtil.callFieldSetter(entity, entityIdField.getAttributeName(), Long.class, generatedKeys.getLong(1));
			}
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in insertion: %s", e.getMessage()), e);
		}
		finally {
			helper().close(generatedKeys);
			helper().close(statement);
		}
	}

	/**
	 * @param fields
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildInsertPreparedStatement(FieldList fields) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(dialect().buildInsert(tableName, fields), Statement.RETURN_GENERATED_KEYS);
		for(int i = 1; i <= fields.size(); i++) {
			statement.setObject(i, fields.get(i - 1).getValue());
		}
		return statement;
	}

	/**
	 * Update record in the database.
	 * 
	 * @param entity
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T update(T entity) throws ConnectionException, ModelException, InterceptorException {
		if(updateByCriteria(idCriteria(entity.getId()), entity) <= 0) {
			throw new ModelException("No record was updated.");
		}
		return entity;
	}

	/**
	 * @param criteria
	 * @param entity
	 * @return number of records updated.
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	private int updateByCriteria(Criteria criteria, T entity) throws ConnectionException, ModelException, InterceptorException {
		boolean success = true;
		int updatedRecords = 0;
		try {
			openTransaction();
			criteria.setResultLimit(null);

			if(interceptor != null) {
				interceptor.setEntity(entity);
				interceptor.beforeSave();
				interceptor.beforeUpdate();
			}

			updatedRecords = updateRecord(criteria, ModelUtil.getEntityFields(entity));
		}
		catch(Exception e) {
			success = false;
			throw e;
		}
		finally {
			if(interceptor != null) {
				interceptor.afterSave(success);
				interceptor.afterUpdate(success);
			}
			closeTransaction(success);
		}
		return updatedRecords;
	}

	/**
	 * @param fieldName
	 * @param fieldValue
	 * @return number of records updated.
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateAll(String fieldName, Object fieldValue) throws ConnectionException, ModelException {
		return updateAll(new NameValue(fieldName, fieldValue));
	}
	
	/**
	 * @param field
	 * @return number of records updated.
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateAll(final NameValue field) throws ConnectionException, ModelException {
		return updateAll(new ArrayList<NameValue>(){{add(field);}});
	}
	
	/**
	 * Update all records.
	 * 
	 * @param fields - field to be updated [fieldName, fieldValue].
	 * @return number of records updated.
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateAll(List<NameValue> fields) throws ConnectionException, ModelException {
		return updateByCriteria(fields, criteria());
	}
	
	/**
	 * @param fieldName
	 * @param fieldValue
	 * @param id
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateById(String fieldName, Object fieldValue, Object id) throws ConnectionException, ModelException {
		return updateById(new NameValue(fieldName, fieldValue), id);
	}

	/**
	 * @param field
	 * @param id
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateById(final NameValue field, Object id) throws ConnectionException, ModelException {
		return updateById(new ArrayList<NameValue>(){{add(field);}}, id);
	}

	/**
	 * @param fields
	 * @param id
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateById(List<NameValue> fields, Object id) throws ConnectionException, ModelException {
		return updateByCriteria(fields, idCriteria(id));
	}

	/**
	 * @param fieldName
	 * @param fieldValue
	 * @param condition
	 * @param conditionValues
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateByCondition(String fieldName, Object fieldValue, String condition, Object... conditionValues) throws ConnectionException, ModelException {
		return updateByCondition(new NameValue(fieldName, fieldValue), condition, conditionValues);
	}

	/**
	 * @param field
	 * @param condition
	 * @param conditionValues
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateByCondition(final NameValue field, String condition, Object... conditionValues) throws ConnectionException, ModelException {
		return updateByCondition(new ArrayList<NameValue>(){{add(field);}}, condition, conditionValues);
	}
	
	/**
	 * Update records filtering by condition (not pass through the interceptor).
	 * 
	 * @param fields - field to be updated [fieldName, fieldValue].
	 * @param condition - structure: \@company.name = ? AND \@recordDate = ?<br>
	 * @param conditionValues
	 * @return number of records updated.
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateByCondition(List<NameValue> fields, String condition, Object... conditionValues) throws ConnectionException, ModelException {
		return updateByCriteria(fields, conditionCriteria(condition, conditionValues));
	}
	
	/**
	 * @param fieldName
	 * @param fieldValue
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateByCriteria(String fieldName, Object fieldValue, Criteria criteria) throws ConnectionException, ModelException {
		return updateByCriteria(new NameValue(fieldName, fieldValue), criteria);
	}

	/**
	 * @param field
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateByCriteria(final NameValue field, Criteria criteria) throws ConnectionException, ModelException {
		return updateByCriteria(new ArrayList<NameValue>(){{add(field);}}, criteria);
	}
	
	/**
	 * Update records filtering by criteria (not pass through the interceptor).
	 * 
	 * @param fields - field to be updated [fieldName, fieldValue].
	 * @param criteria
	 * @return number of records updated.
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int updateByCriteria(List<NameValue> fields, Criteria criteria) throws ConnectionException, ModelException {
		return updateByCriteria(criteria, ModelUtil.buildFieldList(entityClass, fields));
	}

	/**
	 * Update given fields by criteria (not pass through interceptor).
	 * 
	 * @param criteria
	 * @param fields
	 * @return number of records updated.
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	private int updateByCriteria(Criteria criteria, FieldList fields) throws ConnectionException, ModelException {
		boolean success = true;
		int updatedRecords = 0;
		try {
			openTransaction();

			criteria.setResultLimit(null);
			updatedRecords = updateRecord(criteria, fields);
		}
		catch(Exception e) {
			success = false;
			throw e;
		}
		finally {
			closeTransaction(success);
		}
		return updatedRecords;
	}

	/**
	 * @param criteria
	 * @param fields
	 * @return
	 * @throws ModelException
	 */
	private int updateRecord(Criteria criteria, FieldList fields) throws ModelException {
		PreparedStatement statement = null;
		try {
			statement = buildUpdatePreparedStatement(criteria, fields);
			return statement.executeUpdate();
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the update: %s", e.getMessage()), e);
		}
		finally {
			helper().close(statement);
		}
	}

	/**
	 * @param criteria
	 * @param fields
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildUpdatePreparedStatement(Criteria criteria, FieldList fields) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(dialect().buildUpdate(tableName, criteria, fields));
		for(int i = 1; i <= fields.size(); i++) {
			statement.setObject(i, fields.get(i - 1).getValue());
		}
		helper().addParameters(statement, criteria.getConditionsValues(), (fields.size() + 1));
		return statement;
	}

	/**
	 * If the entity has a filled id then update the record otherwise insert a new one.
	 * 
	 * @param entity
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T save(T entity) throws ConnectionException, ModelException, InterceptorException {
		if(entity.getId() == null) {
			return insert(entity);
		}
		else {
			return update(entity);
		}
	}

	/**
	 * Delete the record from the database (do not remove the relationships).
	 * 
	 * @param entity
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T delete(T entity) throws ConnectionException, ModelException, InterceptorException {
		if(interceptor != null) {
			interceptor.setEntity(entity);
		}
		if(deleteByCriteria(idCriteria(entity.getId())) <= 0) {
			throw new ModelException("No record was deleted.");
		}
		return entity;
	}

	/**
	 * Delete the record with given id from the database (do not remove the relationships).
	 * 
	 * @param id
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public void deleteById(Object id) throws ConnectionException, ModelException, InterceptorException {
		if(deleteByCriteria(idCriteria(id)) <= 0) {
			throw new ModelException("No record was deleted.");
		}
	}

	/**
	 * Delete records from the database filtering by criteria (do not remove the relationships).
	 * 
	 * @param criteria
	 * @return number of records deleted.
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public int deleteByCriteria(Criteria criteria) throws ConnectionException, ModelException, InterceptorException {
		return deleteByCriteria(criteria, false);
	}

	/**
	 * Delete records from the database filtering by condition (do not remove the relationships).
	 * 
	 * @param condition - structure: \@company.name = ? AND \@recordDate = ?<br>
	 * @param conditionValues
	 * @return number of records deleted.
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public int deleteByCondition(String condition, Object... conditionValues) throws ConnectionException, ModelException, InterceptorException {
		return deleteByCriteria(conditionCriteria(condition, conditionValues), false);
	}

	/**
	 * Delete the record from the database but first delete its relationships.
	 * 
	 * @param entity
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T deleteCascade(T entity) throws ConnectionException, ModelException, InterceptorException {
		if(interceptor != null) {
			interceptor.setEntity(entity);
		}
		if(deleteCascadeByCriteria(idCriteria(entity.getId())) <= 0) {
			throw new ModelException("No record was deleted.");
		}
		return entity;
	}

	/**
	 * Delete the record with given id from the database but first delete its relationships.
	 * 
	 * @param id
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public void deleteCascadeById(Object id) throws ConnectionException, ModelException, InterceptorException {
		if(deleteCascadeByCriteria(idCriteria(id)) <= 0) {
			throw new ModelException("No record was deleted.");
		}
	}

	/**
	 * Delete records from the database filtering by criteria but first delete its relationships.
	 * 
	 * @param criteria
	 * @return number of records deleted.
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public int deleteCascadeByCriteria(Criteria criteria) throws ConnectionException, ModelException, InterceptorException {
		return deleteByCriteria(criteria, true);
	}

	/**
	 * Delete records from the database filtering by condition but first delete its relationships.
	 * 
	 * @param condition - structure: \@company.name = ? AND \@recordDate = ?<br>
	 * @param conditionValues
	 * @return number of records deleted.
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public int deleteCascadeByCondition(String condition, Object... conditionValues) throws ConnectionException, ModelException, InterceptorException {
		return deleteByCriteria(conditionCriteria(condition, conditionValues), true);
	}

	/**
	 * @param criteria
	 * @param cascadeDelete
	 * @return number of records deleted.
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	private int deleteByCriteria(Criteria criteria, boolean cascadeDelete) throws ConnectionException, ModelException, InterceptorException {
		boolean success = true;
		int deletedRecords = 0;
		try {
			openTransaction();
			criteria.setResultLimit(null);

			if(cascadeDelete) {
				deleteRelated(findByCriteria(criteria));
			}
			if(interceptor != null) {
				interceptor.beforeDelete();
			}
			
			deletedRecords = deleteRecord(criteria);
		}
		catch(Exception e) {
			success = false;
			throw e;
		}
		finally {
			if(interceptor != null) {
				interceptor.afterDelete(success);
			}
			closeTransaction(success);
		}
		return deletedRecords;
	}

	/**
	 * @param criteria
	 * @return
	 * @throws ModelException
	 */
	private int deleteRecord(Criteria criteria) throws ModelException {
		PreparedStatement statement = null;
		try {
			statement = buildDeletePreparedStatement(criteria);
			return statement.executeUpdate();
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the deletion: %s", e.getMessage()), e);
		}
		finally {
			helper().close(statement);
		}
	}

	/**
	 * Delete the relationships.
	 * 
	 * @param entity
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	private void deleteRelated(List<T> entityList) throws ConnectionException, ModelException, InterceptorException {
		if(entityList.isEmpty()) {
			return;
		}

		String relatedDaoClassName;
		ModelEntity<?> relatedEntity;
		ModelDao<?> relatedDao;
		List<JoinColumnField> joinFields;
		Criteria criteria;

		for(Class<?> relatedEntityClass: getRelatedEntityClass()) {
			relatedDaoClassName = basePackage.concat(".").concat(relatedEntityClass.getSimpleName()).concat("Dao");
			relatedEntity = (ModelEntity<?>) ReflectionUtil.newInstance(relatedEntityClass);
			relatedDao = (ModelDao<?>) ReflectionUtil.newInstance(relatedDaoClassName, Database.class, db);
			joinFields = ModelUtil.getJoinFields(relatedEntity, entityClass);

			for(JoinColumnField joinField: joinFields) {
				for(T entity: entityList) {
					criteria = new Criteria(relatedEntity.getClass());
					criteria.addFilter(joinField.getBaseIdColumn().getAttributeName(), entity.getId());
					relatedDao.deleteByCriteria(criteria, true);
				}
			}
		}
	}

	/**
	 * @param fields
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildDeletePreparedStatement(Criteria criteria) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(dialect().buildDelete(tableName, criteria));
		helper().addParameters(statement, criteria.getConditionsValues());
		return statement;
	}

	/**
	 * Load record with given id.
	 * 
	 * @param id
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T loadById(Object id) throws ConnectionException, ModelException, InterceptorException {
		if(id == null) {
			return null;
		}
		return loadByCriteria(idCriteria(id));
	}

	/**
	 * Load record filtering by condition.
	 * 
	 * @param condition - structure: \@company.name = ? AND \@recordDate = ?<br>
	 * @param conditionValues
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T loadByCondition(String condition, Object... conditionValues) throws ConnectionException, ModelException, InterceptorException {
		return loadByCriteria(conditionCriteria(condition, conditionValues));
	}

	/**
	 * Load record filtering by criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public T loadByCriteria(Criteria criteria) throws ConnectionException, ModelException, InterceptorException {
		boolean success = true;
		try {
			openConnection();

			if(interceptor != null) {
				interceptor.beforeLoad();
			}
			
			T entity = loadRecord(criteria);

			if(interceptor != null && success) {
				interceptor.setEntity(entity);
			}
			return entity;
		}
		catch(Exception e) {
			success = false;
			throw e;
		}
		finally {
			if(interceptor != null) {
				interceptor.afterLoad(success);
			}
			closeConnection();
		}
	}

	/**
	 * @param criteria
	 * @return
	 * @throws ModelException
	 */
	private T loadRecord(Criteria criteria) throws ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			criteria.setResultLimit(1);

			statement = buildFindPreparedStatement(criteria);
			resultSet = statement.executeQuery();
			
			T entity = null;
			FieldList fields = ModelUtil.getEntityFields(entityClass);
			
			if(resultSet.next()) {
				entity = buildEntity();

				ModelUtil.populateFields(entity, fields, resultSet);
				if(criteria.hasJoin()) {
					ModelUtil.populateJoinFields(entity, criteria.getJoinFields(), resultSet);
				}
			}

			return entity;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the load: %s", e.getMessage()), e);
		}
		finally {
			helper().close(resultSet);
			helper().close(statement);
		}
	}

	/**
	 * Find all records.
	 * 
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public DataList<T> findAll() throws ConnectionException, ModelException, InterceptorException {
		return findByCriteria(criteria());
	}

	/**
	 * Find records filtering by condition.
	 * 
	 * @param condition - structure: \@company.name = ? AND \@recordDate = ?<br>
	 * @param conditionValues
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public DataList<T> findByCondition(String condition, Object... conditionValues) throws ConnectionException, ModelException, InterceptorException {
		return findByCriteria(conditionCriteria(condition, conditionValues));
	}

	/**
	 * Find records filtering by criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 * @throws InterceptorException
	 */
	public DataList<T> findByCriteria(Criteria criteria) throws ConnectionException, ModelException, InterceptorException {
		try {
			openConnection();
			return findRecords(criteria);
		}
		finally {
			closeConnection();
		}
	}

	/**
	 * Find records filtering by criteria and paging.
	 * 
	 * @param pagingCriteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public PagedList<T> findByCriteria(PagingCriteria pagingCriteria) throws ConnectionException, ModelException {
		try {
			openConnection();

			PagedList<T> entityList = new PagedList<T>(findRecords(pagingCriteria));
			PagingHelper paging = pagingCriteria.getPaging();

			// Set find total rows to paging helper.
			if(!entityList.isEmpty() && (paging.getTotalRows() == 0)) {
				pagingCriteria.disablePaging();
				paging.setTotalRows(countByCriteria(pagingCriteria));
				pagingCriteria.enablePaging();
			}

			entityList.setPaging(paging);
			return entityList;
		}
		finally {
			closeConnection();
		}
	}

	/**
	 * @param criteria
	 * @return
	 * @throws ModelException
	 */
	private DataList<T> findRecords(Criteria criteria) throws ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = buildFindPreparedStatement(criteria);
			resultSet = statement.executeQuery();
			
			DataList<T> entityList = new DataList<T>();
			FieldList fields = ModelUtil.getEntityFields(entityClass);
			
			while(resultSet.next()) {
				T entity = buildEntity();

				ModelUtil.populateFields(entity, fields, resultSet);
				if(criteria.hasJoin()) {
					ModelUtil.populateJoinFields(entity, criteria.getJoinFields(), resultSet);
				}

				entityList.add(entity);
			}
			return entityList;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the find: %s", e.getMessage()), e);
		}
		finally {
			helper().close(resultSet);
			helper().close(statement);
		}
	}

	/**
	 * @param criteria
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildFindPreparedStatement(Criteria criteria) throws SQLException {
		return helper().buildFindPreparedStatement(dialect().buildFind(tableName, criteria), criteria.getConditionsValues());
	}
	
	/**
	 * @param fieldName
	 * @param id
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public <X> X getById(String fieldName, Object id) throws ConnectionException, ModelException {
		return getByCriteria(fieldName, idCriteria(id));
	}

	/**
	 * @param fieldName
	 * @param condition
	 * @param conditionValues
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public <X> X getByCondition(String fieldName, String condition, Object... conditionValues) throws ConnectionException, ModelException {
		return getByCriteria(fieldName, conditionCriteria(condition, conditionValues));
	}
	
	/**
	 * @param fieldName
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	@SuppressWarnings("unchecked")
	public <X> X getByCriteria(String fieldName, Criteria criteria) throws ConnectionException, ModelException {
		Object[] result = getByCriteria(new String[]{fieldName}, criteria);
		if(result == null) {
			return null;
		}
		return (X) result[0];
	}
	
	/**
	 * @param fieldsNames
	 * @param id
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public Object[] getById(String[] fieldsNames, Object id) throws ConnectionException, ModelException {
		return getByCriteria(fieldsNames, idCriteria(id));
	}

	/**
	 * @param fieldsNames
	 * @param condition
	 * @param conditionValues
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public Object[] getByCondition(String[] fieldsNames, String condition, Object... conditionValues) throws ConnectionException, ModelException {
		return getByCriteria(fieldsNames, conditionCriteria(condition, conditionValues));
	}
	
	/**
	 * @param fieldsNames
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public Object[] getByCriteria(String[] fieldsNames, Criteria criteria) throws ConnectionException, ModelException {
		try {
			openConnection();
			return get(fieldsNames, criteria);
		}
		finally {
			closeConnection();
		}
	}
	
	/**
	 * @param fieldsNames
	 * @param criteria
	 * @return
	 * @throws ModelException
	 */
	private Object[] get(String[] fieldsNames, Criteria criteria) throws ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			criteria.setResultLimit(1);
			
			List<String> fields = new ArrayList<String>();
			for(String fieldName: fieldsNames) {
				fields.add(criteria.replaceWithAlias(fieldName));
			}
			
			statement = buildGetPreparedStatement(criteria, fields);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				Object[] result = new Object[fieldsNames.length];
				for(int i = 1; i <= fieldsNames.length; i++) {
					result[i - 1] = resultSet.getObject(i);
				}
				return result;
			}
			return null;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the get: %s", e.getMessage()), e);
		}
		finally {
			helper().close(resultSet);
			helper().close(statement);
		}
	}	
	
	/**
	 * @param criteria
	 * @param fields
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildGetPreparedStatement(Criteria criteria, List<String> fields) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(dialect().buildGet(tableName, criteria, fields), TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		helper().addParameters(statement, criteria.getConditionsValues());
		return statement;
	}

	/**
	 * Get the number of records.
	 * 
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int countAll() throws ConnectionException, ModelException {
		return countByCriteria(criteria());
	}

	/**
	 * Get the number of records filtering by condition.
	 * 
	 * @param condition - structure: \@company.name = ? AND \@recordDate = ?<br>
	 * @param conditionValues
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int countByCondition(String condition, Object... conditionValues) throws ConnectionException, ModelException {
		return countByCriteria(conditionCriteria(condition, conditionValues));
	}

	/**
	 * Get the number of records filtering by criteria.
	 * 
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int countByCriteria(Criteria criteria) throws ConnectionException, ModelException {
		try {
			openConnection();
			return countRecords(criteria);
		}
		finally {
			closeConnection();
		}
	}

	/**
	 * @param criteria
	 * @return
	 * @throws ModelException
	 */
	private int countRecords(Criteria criteria) throws ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = buildCountPreparedStatement(criteria);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
			return 0;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the count: %s", e.getMessage()), e);
		}
		finally {
			helper().close(resultSet);
			helper().close(statement);
		}
	}

	/**
	 * @param criteria
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildCountPreparedStatement(Criteria criteria) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(dialect().buildCount(tableName, criteria), TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		helper().addParameters(statement, criteria.getConditionsValues());
		return statement;
	}

	/**
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public boolean exists() throws ConnectionException, ModelException {
		return existsByCriteria(criteria());
	}

	/**
	 * @param condition - structure: \@company.name = ? AND \@recordDate = ?<br>
	 * @param conditionValues
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public boolean existsByCondition(String condition, Object... conditionValues) throws ConnectionException, ModelException {
		return existsByCriteria(conditionCriteria(condition, conditionValues));
	}

	/**
	 * @param criteria
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public boolean existsByCriteria(Criteria criteria) throws ConnectionException, ModelException {
		try {
			openConnection();
			return existsRecord(criteria);
		}
		finally {
			closeConnection();
		}
	}

	/**
	 * @param criteria
	 * @return
	 * @throws ModelException
	 */
	private boolean existsRecord(Criteria criteria) throws ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			criteria.setResultLimit(1);
			statement = buildExistsPreparedStatement(criteria);
			resultSet = statement.executeQuery();
			return resultSet.next();
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the exists: %s", e.getMessage()), e);
		}
		finally {
			helper().close(resultSet);
			helper().close(statement);
		}
	}

	/**
	 * @param criteria
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement buildExistsPreparedStatement(Criteria criteria) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(dialect().buildExists(tableName, criteria), TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		helper().addParameters(statement, criteria.getConditionsValues());
		return statement;
	}

	/**
	 * @return
	 */
	public Criteria criteria() {
		return new Criteria(entityClass);
	}
	
	/**
	 * @param resultLimit
	 * @return
	 */
	public Criteria criteria(int resultLimit) {
		return new Criteria(entityClass, resultLimit);
	}
	
	/**
	 * Build criteria that filters by id.
	 * 
	 * @param id
	 * @return
	 */
	public Criteria idCriteria(Object id) {
		return criteria().addFilter(entityIdField.getAttributeName(), id);
	}

	/**
	 * Build criteria with condition.
	 * 
	 * @param condition
	 * @param conditionValues
	 * @return
	 */
	public Criteria conditionCriteria(String condition, Object... conditionValues) {
		return criteria().addCondition(condition, conditionValues);
	}
	
	/**
	 * @param pageSize
	 * @return
	 */
	public PagingCriteria pagingCriteria(int pageSize) {
		return new PagingCriteria(entityClass, pageSize);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private T buildEntity() {
		return (T) ReflectionUtil.newInstance(entityClass);
	}

	/**
	 * @param sql
	 * @param dtoClass
	 * @param parameters
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public <D extends ModelDto<D>> DataList<D> find(String sql, Class<D> dtoClass, Object... parameters) throws ConnectionException, ModelException {
		return find(sql, dtoClass, Arrays.asList(parameters));
	}
	
	/**
	 * @param sql
	 * @param dtoClass
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public <D extends ModelDto<D>> DataList<D> find(String sql, Class<D> dtoClass) throws ConnectionException, ModelException {
		return find(sql, dtoClass, (List<Object>)null);
	}
	
	/**
	 * @param sql
	 * @param dtoClass
	 * @param parameters
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public <D extends ModelDto<D>> DataList<D> find(SqlBuilder sql, Class<D> dtoClass, Object... parameters) throws ConnectionException, ModelException {
		return find(sql.toString(), dtoClass, Arrays.asList(parameters));
	}

	/**
	 * @param sql
	 * @param dtoClass
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public <D extends ModelDto<D>> DataList<D> find(SqlBuilder sql, Class<D> dtoClass) throws ConnectionException, ModelException {
		return find(sql.toString(), dtoClass, (List<Object>)null);
	}
	
	/**
	 * @param sql
	 * @param dtoClass
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public <D extends ModelDto<D>> DataList<D> find(SqlStatement sql, Class<D> dtoClass) throws ConnectionException, ModelException {
		return find(sql.toString(), dtoClass, sql.getParameters());
	}

	/**
	 * @param sql
	 * @param dtoClass
	 * @param parameters
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	private <D extends ModelDto<D>> DataList<D> find(String sql, Class<D> dtoClass, List<Object> parameters) throws ConnectionException, ModelException {
		return helper().find(sql, dtoClass, (List<Object>)parameters);
	}

	/**
	 * @param sql
	 * @param parameters
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public CachedRowSet find(String sql, Object... parameters) throws ConnectionException, ModelException {
		return find(sql, Arrays.asList(parameters));
	}
	
	/**
	 * @param sql
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public CachedRowSet find(String sql) throws ConnectionException, ModelException {
		return find(sql, (List<Object>)null);
	}
	
	/**
	 * @param sql
	 * @param parameters
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public CachedRowSet find(SqlBuilder sql, Object... parameters) throws ConnectionException, ModelException {
		return find(sql.toString(), Arrays.asList(parameters));
	}
	
	/**
	 * @param sql
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public CachedRowSet find(SqlBuilder sql) throws ConnectionException, ModelException {
		return find(sql.toString(), (List<Object>)null);
	}

	/**
	 * @param sql
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public CachedRowSet find(SqlStatement sql) throws ConnectionException, ModelException {
		return find(sql.toString(), sql.getParameters());
	}

	/**
	 * @param sql
	 * @param parameters
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	private CachedRowSet find(String sql, List<Object> parameters) throws ConnectionException, ModelException {
		return helper().find(sql, parameters);
	}

	/**
	 * @param countSql
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public PagingHelper buildPaging(String countSql, int pageSize, int pageNumber) throws ConnectionException, ModelException {
		return helper().buildPaging(countSql, pageSize, pageNumber);
	}
	
	/**
	 * @param statement
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
	public int execute(SqlStatement statement) throws ConnectionException, ModelException {
		return helper().execute(statement);
	}
	
	/**
	 * @param transaction
	 * @throws ModelException
	 */
	public void start(Transaction transaction) throws ModelException {
		helper().start(transaction);
	}
	
	/**
	 * @param transaction
	 * @return
	 * @throws ModelException
	 */
	public <C> C start(ResultTransaction<C> transaction) throws ModelException {
		return helper().start(transaction);
	}

	/**
	 * @return
	 */
	public ModelHelper helper() {
		if(helper == null) {
			helper = new ModelHelper(db);
		}
		return helper;
	}
	
	/**
	 * @return
	 */
	public SqlDialect dialect() {
		if(dialect == null) {
			dialect = (SqlDialect) ReflectionUtil.newInstance(db.getDataSource().getDialect());
		}
		return dialect;
	}

	@Override
	public void openTransaction() throws ConnectionException {
		helper().openTransaction();
	}

	@Override
	public void openConnection() throws ConnectionException {
		helper().openConnection();
	}

	@Override
	public void closeTransaction(boolean commit) throws ConnectionException {
		helper().closeTransaction(commit);
	}

	@Override
	public void closeConnection() throws ConnectionException {
		helper().closeConnection();
	}
	
	@Override
	public Connection getConnection() {
		return helper().getConnection();
	}
	
	/**
	 * @return
	 */
	public Database getDatabase() {
		return db;
	}
}