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

	/**
	 * @param db
	 */
	public ModelHelper(Database db) {
		this.db = db;
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
		return find(sql, dtoClass, (List<Object>) null);
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
		return find(sql.toString(), dtoClass, (List<Object>) null);
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
	@SuppressWarnings("unchecked")
	public <D extends ModelDto<D>> DataList<D> find(String sql, Class<D> dtoClass, List<Object> parameters) throws ConnectionException, ModelException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			openConnection();

			statement = buildFindPreparedStatement(sql, parameters);
			resultSet = statement.executeQuery();

			DataList<D> dtoList = new DataList<D>();
			FieldList fields = ModelUtil.getDtoFields(dtoClass);
			
			while(resultSet.next()) {
				D dto = (D) ReflectionUtil.newInstance(dtoClass);
				ModelUtil.populateFields(dto, fields, resultSet);
				dtoList.add(dto);
			}
			return dtoList;
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
		return find(sql, (List<Object>) null);
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
		return find(sql.toString(), (List<Object>) null);
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
	
	/**
	 * @param sql
	 * @param parameters
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement buildFindPreparedStatement(String sql, List<Object> parameters) throws SQLException {
		PreparedStatement statement = getConnection().prepareStatement(sql, TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		if(parameters != null) {
			addParameters(statement, parameters);
		}
		return statement;
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
	
	/**
	 * @param sql
	 * @return
	 * @throws ConnectionException
	 * @throws ModelException
	 */
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

	/**
	 * @param transaction
	 * @throws ModelException
	 */
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
	
	/**
	 * @param transaction
	 * @throws ModelException
	 */
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
	 * 
	 * @param preparedStatement
	 * @param parameters
	 * @param indexFrom
	 * @throws SQLException
	 */
	public void addParameters(PreparedStatement preparedStatement, List<Object> parameters, int indexFrom) throws SQLException {
		for(int i = 0; i < parameters.size(); i++) {
			preparedStatement.setObject(indexFrom + i, parameters.get(i));
		}
	}

	/**
	 * Add parameters to prepared statement.
	 * 
	 * @param preparedStatement
	 * @param parameters
	 * @throws SQLException
	 */
	public void addParameters(PreparedStatement preparedStatement, List<Object> parameters) throws SQLException {
		addParameters(preparedStatement, parameters, 1);
	}

	/**
	 * @param statement
	 */
	public void close(PreparedStatement statement) {
		try {
			if(statement != null)
				statement.close();
		}
		catch(Exception e) {
		}
	}

	/**
	 * @param resultSet
	 */
	public void close(ResultSet resultSet) {
		try {
			if(resultSet != null)
				resultSet.close();
		}
		catch(Exception e) {
		}
	}

	/**
	 * @throws ConnectionException
	 */
	public void openTransaction() throws ConnectionException {
		db.openTransaction();
	}

	/**
	 * @throws ConnectionException
	 */
	public void openConnection() throws ConnectionException {
		db.openConnection();
	}

	/**
	 * @param commit
	 * @throws ConnectionException
	 */
	public void closeTransaction(boolean commit) throws ConnectionException {
		db.closeTransaction(commit);
	}

	/**
	 * @throws ConnectionException
	 */
	public void closeConnection() throws ConnectionException {
		db.closeConnection();
	}
	
	/**
	 * @return
	 */
	public Connection getConnection() {
		return db.getConnection();
	}

	/**
	 * @return
	 */
	public Database getDatabase() {
		return db;
	}
}