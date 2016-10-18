package com.ery.base.support.jdbc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ery.base.support.jdbc.mapper.ArrayMapper;
import com.ery.base.support.jdbc.mapper.BeanArrayMapper;
import com.ery.base.support.jdbc.mapper.BeanListMapper;
import com.ery.base.support.jdbc.mapper.BeanMapper;
import com.ery.base.support.jdbc.mapper.MapArrayMapper;
import com.ery.base.support.jdbc.mapper.MapColumnMapper;
import com.ery.base.support.jdbc.mapper.MapListMapMapper;
import com.ery.base.support.jdbc.mapper.MapListMapper;
import com.ery.base.support.jdbc.mapper.MapMapMapper;
import com.ery.base.support.jdbc.mapper.PrimitiveArrayMapper;
import com.ery.base.support.jdbc.mapper.PrimitiveListMapper;
import com.ery.base.support.jdbc.mapper.PrimitiveMapper;
import com.ery.base.support.log4j.LogUtils;

public class DataAccess {

	private Connection connection = null;

	private boolean isShowSql = false;

	private int queryTimeout = 10;

	public DataAccess(Connection conn) {
		this.connection = conn;
	}

	public DataAccess() {
	}

	public int execUpdate(String sql) {
		logDebug(sql);
		int result = -1;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(queryTimeout);
			result = statement.executeUpdate(sql);
		} catch (Exception e) {
			logError(sql, e);
			throw new JdbcException(e);
		} finally {
			close(statement);
		}
		return result;
	}

	public boolean execNoQuerySql(String sql) {
		logDebug(sql);
		boolean result = false;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(queryTimeout);
			statement.execute(sql);
			result = true;
		} catch (Exception ex) {
			logError(sql, ex);
			result = false;
		} finally {
			close(statement);
		}
		return result;
	}

	public int execUpdate(String sql, Object... params) {
		// 转换当前的SQL语句
		logDebug(sql, params);
		int result = -1;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setQueryTimeout(queryTimeout);
			bindParams(preparedStatement, params);
			result = preparedStatement.executeUpdate();
		} catch (Exception ex) {
			logError(sql, ex, params);
			throw new JdbcException(ex);
		} finally {
			close(preparedStatement);
		}
		return result;
	}

	public boolean execNoQuerySql(String sql, Object... params) {
		logDebug(sql, params);
		boolean result = false;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setQueryTimeout(queryTimeout);
			bindParams(preparedStatement, params);
			preparedStatement.execute();
			result = true;
		} catch (Exception ex) {
			logError(sql, ex, params);
			result = false;
		} finally {
			close(preparedStatement);
		}
		return result;
	}

	public ResultSet execQuerySql(String sql) {
		logDebug(sql);
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(queryTimeout);
			rs = statement.executeQuery(sql);
		} catch (Exception ex) {
			close(statement);
			logError(sql, ex);
			throw new JdbcException(ex);
		}
		return rs;
	}

	public ResultSet execQuerySql(String sql, int resultSetType, int resultSetConcurrency) {
		logDebug(sql);
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = connection.createStatement(resultSetType, resultSetConcurrency);
			statement.setQueryTimeout(queryTimeout);
			rs = statement.executeQuery(sql);
		} catch (Exception ex) {
			close(statement);
			logError(sql, ex);
			throw new JdbcException(ex);
		}
		return rs;
	}

	public void execCall(String sql, Object... params) {
		logDebug(sql, params);
		CallableStatement callableStatement = null;
		try {
			callableStatement = connection.prepareCall(sql);
			callableStatement.setQueryTimeout(queryTimeout);
			bindParams(callableStatement, params);
			callableStatement.execute();
		} catch (Exception ex) {
			logError(sql, ex, params);
			throw new JdbcException(ex);
		} finally {
			close(callableStatement);
		}
	}

	public CallableStatement execQueryCall(String sql, Object... params) {
		logDebug(sql, params);
		CallableStatement callableStatement = null;
		try {
			callableStatement = connection.prepareCall(sql);
			callableStatement.setQueryTimeout(queryTimeout);
			bindParams(callableStatement, params);
			callableStatement.execute();
		} catch (Exception ex) {
			close(callableStatement);
			logError(sql, ex, params);
			throw new JdbcException(ex);
		}
		return callableStatement;
	}

	public ResultSet execQuerySql(String sql, Object... params) {
		ResultSet rs = null;
		if (params == null || params.length == 0) {
			rs = execQuerySql(sql);
		} else {
			logDebug(sql, params);
			PreparedStatement preparedStatement = null;
			try {
				preparedStatement = connection.prepareStatement(sql);
				preparedStatement.setQueryTimeout(queryTimeout);
				bindParams(preparedStatement, params);
				rs = preparedStatement.executeQuery();
			} catch (Exception ex) {
				close(preparedStatement);
				logError(sql, ex, params);
				throw new JdbcException(ex);
			}
		}

		return rs;
	}

	public ResultSet execQuerySql(String sql, int resultSetType, int resultSetConcurrency, Object... params) {

		ResultSet rs = null;
		if (params == null || params.length == 0) {
			try {
				rs = execQuerySql(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			} catch (Exception ex) {
				throw new JdbcException(ex);
			}
		} else {
			logDebug(sql, params);
			PreparedStatement preparedStatement = null;
			try {
				preparedStatement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
				preparedStatement.setQueryTimeout(queryTimeout);
				bindParams(preparedStatement, params);
				rs = preparedStatement.executeQuery();
			} catch (Exception ex) {
				close(preparedStatement);
				logError(sql, ex, params);
				throw new JdbcException(ex);
			}
		}

		return rs;
	}

	public int[] execUpdateBatch(String sql, int buffSize, IParamsSetter paramsSetter) {
		logDebug(sql);
		PreparedStatement preparedStatement = null;
		try {
			int[] rtn = new int[paramsSetter.batchSize()];
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setQueryTimeout(queryTimeout);
			int batchSize = paramsSetter.batchSize();
			for (int i = 0; i < batchSize; i++) {
				paramsSetter.setValues(preparedStatement, i);
				preparedStatement.addBatch();
				if (i != 0 && (i + 1) % buffSize == 0) {
					int[] temp = preparedStatement.executeBatch();
					System.arraycopy(temp, 0, rtn, i + 1 - buffSize, temp.length);
				}
			}
			int[] temp = preparedStatement.executeBatch();
			System.arraycopy(temp, 0, rtn, batchSize - temp.length, temp.length);
			return rtn;
		} catch (Exception e) {
			logError(sql, e);
			throw new JdbcException(e);
		} finally {
			close(preparedStatement);
		}
	}

	public int[] execUpdateBatch(String sql, IParamsSetter paramsSetter) {
		return execUpdateBatch(sql, 20000, paramsSetter);
	}

	public int[] execUpdateBatch(String sql, final Object[][] params) {
		return execUpdateBatch(sql, 20000, new IParamsSetter() {

			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
				bindParams(preparedStatement, params[i]);
			}

			public int batchSize() {
				return params.length;
			}
		});
	}

	public int[] execUpdateBatch(String[] sqls) {
		logDebug(Arrays.deepToString(sqls));
		Statement statement = null;
		try {
			statement = connection.createStatement();
			for (String sql : sqls) {
				statement.addBatch(sql);
			}
			return statement.executeBatch();
		} catch (Exception e) {
			logError(Arrays.deepToString(sqls), e);
			throw new JdbcException(e);
		} finally {
			close(statement);
		}
	}

	public Object[][] queryForArray(String sql, boolean includeColNames, Object... params) {
		ResultSet rs = null;
		Object[][] rtn = null;
		try {
			ArrayMapper mapper = new ArrayMapper();
			Object[][] temp = queryByRowMapper(sql, mapper, params);
			if (includeColNames) {
				String[] headers = mapper.getColumnHeaders();
				rtn = new Object[temp.length + 1][headers.length];
				rtn[0] = headers;
				System.arraycopy(temp, 0, rtn, 1, temp.length);
			} else {
				rtn = temp;
			}
		} catch (Exception e) {
			throw new JdbcException(e);
		} finally {
			close(rs);
		}
		return rtn;
	}

	public <T> T[] queryForPrimitiveArray(String sql, Class<T> clazz, Object... params) {
		return queryByRowMapper(sql, new PrimitiveArrayMapper<T>(clazz), params);
	}

	public <T> T[] queryForBeanArray(String sql, Class<T> clazz, Object... params) {
		return queryByRowMapper(sql, new BeanArrayMapper<T>(clazz), params);
	}

	public Map<String, Object>[] queryForArrayMap(String sql, Object... params) {
		return queryByRowMapper(sql, new MapArrayMapper(), params);
	}

	public List<Map<String, Object>> queryForList(String sql, Object... params) {
		return queryByRowMapper(sql, new MapListMapper(), params);
	}

	public <T> List<T> queryForPrimitiveList(String sql, Class<T> clazz, Object... params) {
		return queryByRowMapper(sql, new PrimitiveListMapper<T>(clazz), params);
	}

	public <T> List<T> queryForBeanList(String sql, Class<T> clazz, Object... params) {
		return queryByRowMapper(sql, new BeanListMapper<T>(clazz), params);
	}

	public Long queryForLong(String sql, Object... params) {
		return queryForObject(sql, Long.class, params);
	}

	public Long queryForLongByNvl(String sql, Long defaultVal, Object... params) {
		return queryForObjectByNvl(sql, Long.class, defaultVal, params);
	}

	public int queryForInt(String sql, Object... params) {
		return queryForObject(sql, int.class, params);
	}

	public int queryForIntByNvl(String sql, int defaultVal, Object... params) {
		return queryForObjectByNvl(sql, int.class, defaultVal, params);
	}

	public String queryForString(String sql, Object... params) {
		return queryForObject(sql, String.class, params);
	}

	public <T> T queryForObject(String sql, Class<T> clazz, Object... params) {
		return queryByRowMapper(sql, new PrimitiveMapper<T>(clazz), params);
	}

	public <T> T queryForObjectByNvl(String sql, Class<T> clazz, T defaultVal, Object... params) {
		T t = queryByRowMapper(sql, new PrimitiveMapper<T>(clazz, defaultVal), params);
		return t == null ? defaultVal : t;
	}

	public Map<String, Object> queryForMap(String sql, Object... params) {
		return queryByRowMapper(sql, new MapColumnMapper(), params);
	}

	public <T> T queryForBean(String sql, Class<T> clazz, Object... params) {
		return queryByRowMapper(sql, new BeanMapper<T>(clazz), params);
	}

	public Map<String, Map<String, Object>> queryForMapMap(String sql, String keyColName, Object... params) {
		return queryByRowMapper(sql, new MapMapMapper(keyColName), params);
	}

	public Map<String, List<Map<String, Object>>> queryForMapListMap(String sql, String keyColName, Object... params) {
		return queryByRowMapper(sql, new MapListMapMapper(keyColName), params);
	}

	public <T> T queryByRowMapper(String sql, IRowMapper<T> rowMapper, Object... params) {
		ResultSet rs = null;
		T t = null;
		try {
			rs = this.execQuerySql(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, params);
			t = rowMapper.convert(rs);
		} catch (Exception e) {
			throw new JdbcException(e);
		} finally {
			close(rs);
		}
		return t;
	}

	public void queryByRowHandler(String sql, IRowHandler rowHandler, Object... params) {
		ResultSet rs = null;
		try {
			rs = this.execQuerySql(sql, params);
			if (rs != null) {
				while (rs.next()) {
					rowHandler.handle(rs);
				}
			}
		} catch (Exception e) {
			throw new JdbcException(e);
		} finally {
			close(rs);
		}
	}

	public DataTable queryForDataTable(String sql, Object... params) {
		ResultSet rs = null;
		try {
			rs = this.execQuerySql(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, params);
			return new DataTable(rs);
		} catch (Exception e) {
			throw new JdbcException(e);
		} finally {
			close(rs);
		}
	}

	public DataTable queryForDataTableByPage(String sql, int posStart, int count, Object... params) {
		ResultSet rs = null;
		try {
			rs = this.execQuerySql(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, params);
			return new DataTable(rs, posStart, count);
		} catch (Exception e) {
			throw new JdbcException(e);
		} finally {
			close(rs);
		}
	}

	public DataTable queryForDataTableByPageCount(String sql, int posStart, int count, int totalCount, Object... params) {
		ResultSet rs = null;
		try {
			rs = this.execQuerySql(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, params);
			return new DataTable(rs, posStart, count, totalCount);
		} catch (Exception e) {
			throw new JdbcException(e);
		} finally {
			close(rs);
		}
	}

	public DataTable queryForDataTableByPageTotal(String sql, int posStart, int count, boolean isCalTotal,
			Object... params) {
		ResultSet rs = null;
		try {
			rs = this.execQuerySql(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, params);
			return new DataTable(rs, posStart, count, isCalTotal);
		} catch (Exception e) {
			throw new JdbcException(e);
		} finally {
			close(rs);
		}
	}

	public void beginTransaction() throws Exception {
		connection.setAutoCommit(false);
	}

	public void commit() throws Exception {
		connection.commit();
	}

	public void rollback() {
		if (connection != null) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				LogUtils.error(null, e);
				throw new JdbcException(e);
			}
		}
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public boolean isShowSql() {
		return isShowSql;
	}

	public void setShowSql(boolean isShowSql) {
		this.isShowSql = isShowSql;
	}

	public int getQueryTimeout() {
		return queryTimeout;
	}

	public int setQueryTimeout(int queryTimeout) {
		int oldQueryTimeout = this.queryTimeout;
		this.queryTimeout = queryTimeout;
		return oldQueryTimeout;
	}

	public void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (Throwable t) {
				LogUtils.debug("关闭Statement失败", t);
			}
		}
	}

	public void close(ResultSet rs) {
		if (rs != null) {
			Statement statement = null;
			try {
				statement = rs.getStatement();
			} catch (Throwable t) {
				LogUtils.debug("获取需要关闭的Statement失败", t);
			}

			try {
				rs.close();
			} catch (Throwable t) {
				LogUtils.debug("关闭ResultSet失败", t);
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (Throwable t) {
					LogUtils.debug("关闭Statement失败", t);
				}
			}
		}
	}

	private void logDebug(String sql, Object... params) {
		if (isShowSql) {
			StackTraceElement[] traces = Thread.currentThread().getStackTrace();
			for (int i = 1; i < traces.length; i++) {
				StackTraceElement trace = traces[i];
				if (!trace.getClassName().equals(this.getClass().getName())) {
					StringBuilder msgBuffer = new StringBuilder();
					msgBuffer.append("替换参数后SQL语句:");
					msgBuffer.append(replaceParam(sql, params));
					msgBuffer.append("\n");
					msgBuffer.append("调试SQL语句:");
					msgBuffer.append(sql);
					msgBuffer.append("\n");
					if (params != null && params.length > 0) {
						msgBuffer.append("参数：");
						msgBuffer.append(Arrays.deepToString(params));
						msgBuffer.append("\n");
					}
					LogUtils.debug(msgBuffer.toString());
					break;
				}
			}
		}
	}

	private void logError(String sql, Throwable t, Object... params) {
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		for (int i = 1; i < traces.length; i++) {
			StackTraceElement trace = traces[i];
			if (!trace.getClassName().equals(this.getClass().getName())) {
				StringWriter stringWriter = new StringWriter();// 将异常先输出到String
																// Writer中
				t.printStackTrace(new PrintWriter(stringWriter));
				StringBuilder msgBuffer = new StringBuilder();
				msgBuffer.append("替换参数后SQL语句:");
				msgBuffer.append(replaceParam(sql, params));
				msgBuffer.append("\n");
				msgBuffer.append("出错SQL语句:");
				msgBuffer.append(sql);
				msgBuffer.append("\n");
				if (params != null && params.length > 0) {
					msgBuffer.append("参数：");
					msgBuffer.append(Arrays.deepToString(params));
					msgBuffer.append("\n错误信息：\n");
					msgBuffer.append(stringWriter.toString());
				} else {
					msgBuffer.append(stringWriter.toString());
				}
				LogUtils.error(msgBuffer.toString());
				break;
			}
		}
	}

	private String replaceParam(String sql, Object[] params) {
		StringBuffer buffer = new StringBuffer();
		if (params != null && params.length != 0) {
			String[] strArray = sql.split("\\?");
			for (int i = 0; i < strArray.length; i++) {
				buffer.append(strArray[i]);
				if (params.length > i) {
					Object param = params[i];
					if (param == null) {
						buffer.append("null");
					} else if (param.getClass().equals(String.class)) {
						buffer.append("'" + param.toString() + "'");
					} else {
						buffer.append(param.toString());
					}
				}
			}
		} else {
			buffer.append(sql);
		}
		return buffer.toString();
	}

	private void bindParams(PreparedStatement preparedStatement, Object[] params) throws SQLException {
		if (params != null && params.length != 0) {
			for (int i = 0; i < params.length; i++) {
				if (params[i] == null) {
					preparedStatement.setNull(i + 1, java.sql.Types.NULL);
				} else if (params[i] instanceof IDBParameter) {
					IDBParameter parameter = (IDBParameter) params[i];
					parameter.setParameter(preparedStatement, i + 1);
				} else if (params[i] instanceof java.util.Date) {
					Date temp = (Date) params[i];
					if (params[i] instanceof java.sql.Date || params[i] instanceof java.sql.Time) {
						preparedStatement.setObject(i + 1, params[i]);
					} else {
						preparedStatement.setTimestamp(i + 1, new java.sql.Timestamp(temp.getTime()));
					}
				} else {
					preparedStatement.setObject(i + 1, params[i]);
				}
			}
		}
	}
}
