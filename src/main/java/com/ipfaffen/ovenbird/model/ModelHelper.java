package com.ipfaffen.ovenbird.model;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import com.ipfaffen.ovenbird.commons.DataList;
import com.ipfaffen.ovenbird.commons.PagingHelper;
import com.ipfaffen.ovenbird.commons.ReflectionUtil;
import com.ipfaffen.ovenbird.commons.exception.ValidationException;
import com.ipfaffen.ovenbird.model.builder.ObjectBuilder;
import com.ipfaffen.ovenbird.model.connection.Database;
import com.ipfaffen.ovenbird.model.exception.ConnectionException;
import com.ipfaffen.ovenbird.model.exception.ModelException;
import com.ipfaffen.ovenbird.model.transaction.ResultTransaction;
import com.ipfaffen.ovenbird.model.transaction.Transaction;
import com.ipfaffen.ovenbird.model.util.FieldList;

/**
 * @author Isaias Pfaffenseller
 */
public class ModelHelper {

	private Database db;

	public ModelHelper(Database db) {
		this.db = db;
	}

	public <D> DataList<D> find(String sql, Class<D> resultClass, Object... parameters) throws ConnectionException, ModelException {
		return find(sql, resultClass, Arrays.asList(parameters));
	}

	public <D> DataList<D> find(String sql, Class<D> resultClass) throws ConnectionException, ModelException {
		return find(sql, resultClass, (List<Object>) null);
	}

	public <D> DataList<D> find(SqlBuilder sql, Class<D> resultClass, Object... parameters) throws ConnectionException, ModelException {
		return find(sql.toString(), resultClass, Arrays.asList(parameters));
	}

	public <D> DataList<D> find(SqlBuilder sql, Class<D> resultClass) throws ConnectionException, ModelException {
		return find(sql.toString(), resultClass, (List<Object>) null);
	}	

	public <D> DataList<D> find(SqlStatement sql, Class<D> resultClass) throws ConnectionException, ModelException {
		return find(sql.toString(), resultClass, sql.getParameters());
	}

	@SuppressWarnings("unchecked")
	public <D> DataList<D> find(String sql, Class<D> resultClass, List<Object> parameters) throws ConnectionException, ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			openConnection();

			statement = buildFindPreparedStatement(sql, parameters);
			resultSet = statement.executeQuery();
			DataList<D> resultList = new DataList<D>();

			if(ReflectionUtil.newInstance(resultClass) instanceof ModelDto) {
				Class<? extends ModelDto<?>> dtoClass = (Class<? extends ModelDto<?>>) resultClass;
				FieldList fields = ModelUtil.getDtoFields(dtoClass);
				while(resultSet.next()) {
					D dto = (D) ReflectionUtil.newInstance(dtoClass);
					ModelUtil.populateFields((ModelDto<?>) dto, fields, resultSet);
					resultList.add(dto);
				}
				return resultList;
			}
			while(resultSet.next()) {
				resultList.add(resultSet.getObject(1, resultClass));
			}
			return resultList;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the dto find: %s", e.getMessage()), e);
		}
		finally {
			close(resultSet);
			close(statement);
			closeConnection();
		}
	}
	
	public <D> DataList<D> find(String sql, ObjectBuilder<D> builder, Object... parameters) throws ConnectionException, ModelException {
		return find(sql, builder, Arrays.asList(parameters));
	}

	public <D> DataList<D> find(String sql, ObjectBuilder<D> builder) throws ConnectionException, ModelException {
		return find(sql, builder, (List<Object>) null);
	}

	public <D> DataList<D> find(SqlBuilder sql, ObjectBuilder<D> builder, Object... parameters) throws ConnectionException, ModelException {
		return find(sql.toString(), builder, Arrays.asList(parameters));
	}

	public <D> DataList<D> find(SqlBuilder sql, ObjectBuilder<D> builder) throws ConnectionException, ModelException {
		return find(sql.toString(), builder, (List<Object>) null);
	}	

	public <D> DataList<D> find(SqlStatement sql, ObjectBuilder<D> builder) throws ConnectionException, ModelException {
		return find(sql.toString(), builder, sql.getParameters());
	}

	public <D> DataList<D> find(String sql, ObjectBuilder<D> builder, List<Object> parameters) throws ConnectionException, ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			openConnection();
			statement = buildFindPreparedStatement(sql, parameters);
			resultSet = statement.executeQuery();
			DataList<D> resultList = new DataList<D>();
			while(resultSet.next()) {
				resultList.add(builder.build(resultSet));
			}
			return resultList;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the dto find: %s", e.getMessage()), e);
		}
		finally {
			close(resultSet);
			close(statement);
			closeConnection();
		}
	}

	public CachedRowSet find(String sql, Object... parameters) throws ConnectionException, ModelException {
		return find(sql, Arrays.asList(parameters));
	}

	public CachedRowSet find(String sql) throws ConnectionException, ModelException {
		return find(sql, (List<Object>) null);
	}

	public CachedRowSet find(SqlBuilder sql, Object... parameters) throws ConnectionException, ModelException {
		return find(sql.toString(), Arrays.asList(parameters));
	}

	public CachedRowSet find(SqlBuilder sql) throws ConnectionException, ModelException {
		return find(sql.toString(), (List<Object>) null);
	}
	
	public CachedRowSet find(SqlStatement sql) throws ConnectionException, ModelException {
		return find(sql.toString(), sql.getParameters());
	}

	public CachedRowSet find(String sql, List<Object> parameters) throws ConnectionException, ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			openConnection();
			
			statement = buildFindPreparedStatement(sql, parameters);
			resultSet = statement.executeQuery();

			// Create and populate cached row set so the result set and connection can be closed.
			CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
			rowSet.populate(resultSet);
			return rowSet;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the sql find: %s", e.getMessage()), e);
		}
		finally {
			close(resultSet);
			close(statement);
			closeConnection();
		}
	}
	
	protected PreparedStatement buildFindPreparedStatement(String sql, List<Object> parameters) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(sql, TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		if(parameters != null) {
			addParameters(statement, parameters);
		}
		return statement;
	}

	public PagingHelper buildPaging(String countSql, int pageSize, int pageNumber) throws ConnectionException, ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			openConnection();
			
			statement = getConnection().prepareStatement(countSql, TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY);
			statement.setFetchSize(Integer.MIN_VALUE);

			resultSet = statement.executeQuery();
			if(!resultSet.next()) {
				return null;
			}

			PagingHelper paging = new PagingHelper(pageSize);
			paging.setPage(pageNumber);
			paging.setTotalRows(resultSet.getInt(1));
			return paging;
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the paging sql: %s", e.getMessage()), e);
		}
		finally {
			close(resultSet);
			close(statement);
			closeConnection();
		}
	}

	public int execute(SqlStatement sql) throws ConnectionException, ModelException {
		PreparedStatement statement = null;
		try {
			openConnection();
			
			statement = getConnection().prepareStatement(sql.toString());
			addParameters(statement, sql.getParameters());
			return statement.executeUpdate();
		}
		catch(Exception e) {
			throw new ModelException(String.format("Occurred a problem in the sql update: %s", e.getMessage()), e);
		}
		finally {
			close(statement);
			closeConnection();
		}
	}

	public void start(Transaction transaction) throws ModelException {
		try {
			boolean commit = true;
			try {
				openTransaction();
				transaction.$();
			}
			catch(Exception e) {
				commit = false;
				throw e;
			}
			finally {
				closeTransaction(commit);
			}
		}
		catch(Exception e) {
			if(e instanceof ValidationException) {
				throw (ValidationException) e;
			}
			throw new ModelException(e.getMessage(), e);
		}
	}

	public <T> T start(ResultTransaction<T> transaction) throws ModelException {
		try {
			if(transaction.isReadOnly()){
				try {
					openConnection();
					return transaction.$();
				}
				finally {
					closeConnection();
				}
			}
			else {
				boolean commit = true;
				try {
					openTransaction();
					return transaction.$();
				}
				catch(Exception e) {
					commit = false;
					throw e;
				}
				finally {
					closeTransaction(commit);
				}
			}
		}
		catch(Exception e) {
			if(e instanceof ValidationException) {
				throw (ValidationException) e;
			}
			throw new ModelException(e.getMessage(), e);
		}
	}

	/**
	 * Add parameters to prepared statement.
	 */
	public void addParameters(PreparedStatement preparedStatement, List<Object> parameters, int indexFrom) throws SQLException {
		for(int i = 0; i < parameters.size(); i++) {
			preparedStatement.setObject(indexFrom + i, parameters.get(i));
		}
	}

	/**
	 * Add parameters to prepared statement.
	 */
	public void addParameters(PreparedStatement preparedStatement, List<Object> parameters) throws SQLException {
		addParameters(preparedStatement, parameters, 1);
	}

	public void close(PreparedStatement statement) {
		try {
			if(statement != null)
				statement.close();
		}
		catch(Exception e) {
		}
	}

	public void close(ResultSet resultSet) {
		try {
			if(resultSet != null)
				resultSet.close();
		}
		catch(Exception e) {
		}
	}

	public void openTransaction() throws ConnectionException {
		db.openTransaction();
	}

	public void openConnection() throws ConnectionException {
		db.openConnection();
	}

	public void closeTransaction(boolean commit) throws ConnectionException {
		db.closeTransaction(commit);
	}

	public void closeConnection() throws ConnectionException {
		db.closeConnection();
	}

	public Connection getConnection() {
		return db.getConnection();
	}

	public Database getDatabase() {
		return db;
	}
}