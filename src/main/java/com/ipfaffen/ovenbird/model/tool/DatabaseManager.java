package com.ipfaffen.ovenbird.model.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.ipfaffen.ovenbird.model.connection.Database;

/**
 * @author Isaias Pfaffenseller
 */
public class DatabaseManager {
	
	protected Database db;
	protected Statement statement;

	protected PrintWriter databaseSQLFile;

	/**
	 * @param database
	 */
	public DatabaseManager(Database database) {
		this.db = database;
	}

	/**
	 * @param databaseConnection
	 * @param outputSqlFile
	 * @throws IOException
	 */
	public DatabaseManager(File outputSqlFile) throws IOException {
		outputSqlFile.createNewFile();
		this.databaseSQLFile = new PrintWriter(new FileWriter(outputSqlFile));
	}

	/**
	 * @param databaseName
	 */
	public void createDatabase(String databaseName) {
		try {
			String sql = "CREATE DATABASE " + databaseName + " DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci";
			if(databaseSQLFile == null) {
				getStatement().executeUpdate(sql);
			}
			else {
				databaseSQLFile.println(sql + ";");
			}

			log("Database '" + databaseName + "' created.");
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to create the database '" + databaseName + "'.");
			logError("--> [" + e.getMessage() + "]");
		}
	}

	/**
	 * @param tableName
	 * @throws SQLException
	 */
	public void dropTable(String tableName) {
		try {
			String sql = "DROP TABLE `" + tableName + "`";
			if(databaseSQLFile == null) {
				getStatement().executeUpdate(sql);
			}
			else {
				databaseSQLFile.println(sql + ";");
			}

			log("Table '" + tableName + "' dropped.");
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to drop the table '" + tableName + "'.");
			logError("--> [" + e.getMessage() + "]");
		}
	}

	/**
	 * @param table
	 * @throws Exception
	 */
	public void createTable(TableSQLMaker table) {
		try {
			String sql = table.getSql();
			if(databaseSQLFile == null) {
				getStatement().executeUpdate(sql);
			}
			else {
				databaseSQLFile.println("\r\n" + sql + ";");
			}

			log("Table '" + table.getTableName() + "' created (" + table.getColumnCount() + " columns).");
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to create the table '" + table.getTableName() + "'.");
			logError("--> [" + e.getMessage() + "]");
		}
	}

	/**
	 * @param sql
	 * @throws Exception
	 */
	public void insertRecord(String sql) {
		try {
			if(databaseSQLFile == null) {
				getStatement().executeUpdate(sql);
			}
			else {
				databaseSQLFile.println(sql + ";");
			}
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to insert a record.");
			logError("--> [" + e.getMessage() + "]");
		}
	}

	/**
	 * @param tableName
	 * @param outputDirectory
	 */
	public void backupTable(String tableName, String outputDirectory) {
		try {
			File file = new File(outputDirectory.concat("\\").concat(tableName).concat(".txt"));
			file.createNewFile();
			PrintWriter backupTableFile = new PrintWriter(new FileWriter(file));

			Statement statement = db.getConnection().createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM `" + tableName + "`");
			ResultSetMetaData rsmd = resultSet.getMetaData();

			for(int i = 1; i <= rsmd.getColumnCount(); i++) {
				if(i > 1) {
					backupTableFile.print("|");
				}

				String columnName = rsmd.getColumnName(i);
				int columnSize = rsmd.getColumnDisplaySize(i);
				backupTableFile.print(String.format("%-" + (columnSize > 1000 ? 1000 : columnSize) + "s", columnName));
			}

			while(resultSet.next()) {
				backupTableFile.println();

				for(int i = 1; i <= rsmd.getColumnCount(); i++) {
					if(i > 1) {
						backupTableFile.print("|");
					}

					String columnName = rsmd.getColumnName(i);
					int columnSize = rsmd.getColumnDisplaySize(i);
					backupTableFile.print(String.format("%-" + (columnSize > 1000 ? 1000 : columnSize) + "s", resultSet.getObject(columnName)));
				}
			}

			backupTableFile.close();
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to backup the table '" + tableName + "'.");
			logError("--> [" + e.getMessage() + "]");
		}
	}

	public static void main(String[] args) {
		System.out.println(String.format("%-10s", "test").replace(' ', '-'));
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	protected Statement getStatement() throws Exception {
		if(statement == null) {
			try {
				statement = db.getConnection().createStatement();
			}
			catch(Exception e) {
				logError("Occurred a problem in trying to open connection with database (" + db.getDataSource().getUrl() + ").");
				throw e;
			}
		}
		return statement;
	}

	/**
	 * @param message
	 */
	protected void log(String message) {
		System.out.println(message);
	}

	/**
	 * @param message
	 */
	protected void logError(String message) {
		System.err.println(message);
	}

	/**
	 * Abre conexão com o banco de dados.
	 * 
	 * @throws Exception
	 */
	protected void openConnection() throws Exception {
		log("Establishing connection.");
		try {
			db.openConnection();
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to open connection with database (" + db.getDataSource().getUrl() + ").");
			throw e;
		}
		log("Connection established.");
	}

	/**
	 * Fecha conexão com o banco de dados.
	 * 
	 * @throws Exception
	 */
	protected void closeConnection() {
		log("Closing connection.");
		try {
			db.closeConnection();
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to close connection with database (" + db.getDataSource().getUrl() + ").");
		}
		log("Connection closed.");
	}

	/**
	 * Fecha o arquivo.
	 * 
	 * @throws Exception
	 */
	protected void closeFile() {
		try {
			databaseSQLFile.close();
		}
		catch(Exception e) {
			logError("Occurred a problem in trying to close the file.");
		}
		log("Connection closed.");
	}

	/**
	 * @author Isaias Pfaffenseller
	 */
	public class TableSQLMaker {
		private static final String ID_COLUMN = "id";

		private String tableName;
		private StringBuilder sql;

		private int columnCount = 0;
		private int ukCount = 0;
		private int fkCount = 0;

		private boolean addPrimaryKey = false;

		/**
		 * @param tableName
		 */
		public TableSQLMaker(String tableName) {
			this.tableName = tableName;
			sql = new StringBuilder();
		}

		public void beginTable() {
			beginTable(false, false);
		}

		/**
		 * @param addIdColumnAndPK
		 */
		public void beginTable(boolean addIdColumnAndPK) {
			beginTable(addIdColumnAndPK, false);
		}

		/**
		 * @param addIdColumnAndPK
		 * @param addRecordDateColumn
		 */
		public void beginTable(boolean addIdColumnAndPK, boolean addRecordDateColumn) {
			sql.append("CREATE TABLE `" + tableName + "`").append("\r\n");
			sql.append("(").append("\r\n");

			if(addIdColumnAndPK) {
				addIdColumnAndPk();
			}

			if(addRecordDateColumn) {
				addRecordDateColumn();
			}
		}

		public void endTable() {
			addPrimaryKey();
			sql.append("\r\n");
			sql.append(")").append("\r\n");
			sql.append("ENGINE=InnoDB").append("\r\n");
			sql.append("CHARACTER SET=utf8").append("\r\n");
			sql.append("COLLATE=utf8_general_ci");
		}

		/**
		 * @param name
		 * @param type
		 * @param nullable
		 * @param extra
		 */
		public void addColumn(String name, String type, boolean nullable, String extra) {
			columnCount += 1;
			if(columnCount > 1) {
				sql.append(",").append("\r\n");
			}

			sql.append("\t").append("`" + name + "` " + type + (!nullable ? " NOT NULL" : ""));

			if(extra != null && !extra.equals("")) {
				sql.append(" " + extra);
			}
		}

		/**
		 * @param name
		 * @param type
		 * @param nullable
		 */
		public void addColumn(String name, String type, boolean nullable) {
			addColumn(name, type, nullable, "");
		}

		/**
		 * @param name
		 * @param type
		 */
		public void addColumn(String name, String type) {
			addColumn(name, type, true, "");
		}

		/**
		 * @param name
		 * @param length
		 * @param nullable
		 */
		public void addVarchar(String name, int length, boolean nullable) {
			addColumn(name, "VARCHAR(" + length + ")", nullable);
		}

		/**
		 * @param name
		 * @param length
		 */
		public void addVarchar(String name, int length) {
			addVarchar(name, length, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addText(String name, boolean nullable) {
			addColumn(name, "TEXT", nullable);
		}

		/**
		 * @param name
		 */
		public void addText(String name) {
			addText(name, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addInt(String name, boolean nullable, String extra) {
			addColumn(name, "INT", nullable, extra);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addInt(String name, boolean nullable) {
			addInt(name, nullable, null);
		}

		/**
		 * @param name
		 */
		public void addInt(String name) {
			addInt(name, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addBigInt(String name, boolean nullable, String extra) {
			addColumn(name, "BIGINT", nullable, extra);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addBigInt(String name, boolean nullable) {
			addBigInt(name, nullable, null);
		}

		/**
		 * @param name
		 */
		public void addBigInt(String name) {
			addBigInt(name, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addTinyInt(String name, boolean nullable) {
			addColumn(name, "TINYINT", nullable);
		}

		/**
		 * @param name
		 */
		public void addTinyInt(String name) {
			addTinyInt(name, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addBoolean(String name, boolean nullable) {
			addColumn(name, "BOOLEAN", nullable);
		}

		/**
		 * @param name
		 */
		public void addBoolean(String name) {
			addBoolean(name, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addDateTime(String name, boolean nullable) {
			addColumn(name, "DATETIME", nullable);
		}

		/**
		 * @param name
		 */
		public void addDateTime(String name) {
			addDateTime(name, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addDate(String name, boolean nullable) {
			addColumn(name, "DATE", nullable);
		}

		/**
		 * @param name
		 */
		public void addDate(String name) {
			addDate(name, true);
		}

		/**
		 * @param name
		 * @param nullable
		 */
		public void addTime(String name, boolean nullable) {
			addColumn(name, "TIME", nullable);
		}

		/**
		 * @param name
		 */
		public void addTime(String name) {
			addTime(name, true);
		}

		/**
		 * Adiciona coluna id (see ID_COLUMN) (BIGINT, NOT NULL, auto_increment) e primary key.
		 */
		public void addIdColumnAndPk() {
			addBigInt(ID_COLUMN, false, "auto_increment");
			addPrimaryKey = true;
		}

		/**
		 * Adiciona coluna record_date (DATETIME, NOT NULL).
		 */
		public void addRecordDateColumn() {
			addDateTime("record_date", false);
		}

		/**
		 * @param columnName
		 */
		public void addPrimaryKey(String columnName) {
			sql.append(",").append("\r\n");
			sql.append("\t" + "PRIMARY KEY (`" + columnName + "`)");
		}

		/**
		 * Adiciona primary key (id).
		 */
		private void addPrimaryKey() {
			if(addPrimaryKey) {
				addPrimaryKey(ID_COLUMN);
				addPrimaryKey = false;
			}
		}

		/**
		 * @param columns
		 */
		public void addUniqueKey(String... columns) {
			StringBuilder columnName = new StringBuilder();
			for(String column: columns) {
				if(columnName.length() > 0) {
					columnName.append(", ");
				}
				columnName.append("`" + column + "`");
			}

			ukCount++;
			String uniqueKeyName = tableName.toLowerCase().concat("_uk_").concat((ukCount < 10 ? "0" : "") + ukCount);

			addPrimaryKey();
			sql.append(",").append("\r\n");
			sql.append("\t" + "UNIQUE KEY `" + uniqueKeyName + "` (" + columnName + ")");
		}

		/**
		 * @param columnName
		 * @param referenceTable
		 */
		public void addForeignKey(String columnName, String referenceTable) {
			fkCount++;
			String foreignKeyName = tableName.toLowerCase().concat("_fk_").concat((fkCount < 10 ? "0" : "") + fkCount);
			String referenceColumn = ID_COLUMN;

			addPrimaryKey();
			sql.append(",").append("\r\n");
			sql.append("\t" + "CONSTRAINT `" + foreignKeyName + "` FOREIGN KEY (`" + columnName + "`) REFERENCES `" + referenceTable + "` (`" + referenceColumn + "`) ON DELETE RESTRICT ON UPDATE RESTRICT");
		}

		/**
		 * @return
		 */
		public String getSql() {
			return sql.toString();
		}

		/**
		 * @return
		 */
		public String getTableName() {
			return tableName;
		}

		/**
		 * @return
		 */
		public Integer getColumnCount() {
			return columnCount;
		}
	}
}