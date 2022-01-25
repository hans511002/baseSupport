package com.sobey.jcg.support.sys.podo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sobey.jcg.support.jdbc.DataAccess;
import com.sobey.jcg.support.jdbc.DataAccessFactory;
import com.sobey.jcg.support.jdbc.JdbcException;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.utils.Convert;

public abstract class BaseDAO {
	private Boolean isMysql = null;

	public static Map<Long, Boolean> IS_TRANSACTION_MAP = new HashMap<Long, Boolean>();

	private List<Connection> connections = new ArrayList<Connection>();

	private Map<Object, DataAccess> dataAccesses = new HashMap<Object, DataAccess>();

	public boolean isMysql() {
		if (isMysql == null) {
			isMysql = "mysql".equals(getDataAccess().getDatabaseName());
		}
		return isMysql;
	}

	public Connection getConnection(Object key) {
		Connection conn = DataSourceManager.getConnection(Convert.toString(key));
		long threadId = Thread.currentThread().getId();
		if (BaseDAO.IS_TRANSACTION_MAP.containsKey(threadId) && BaseDAO.IS_TRANSACTION_MAP.get(threadId)) {
			try {
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				LogUtils.error("设置连接不自动提交事务失败：" + conn, e);
				throw new JdbcException(e);
			}
		}
		connections.add(conn);// 添加到可被关闭的数据库连接链表中
		return conn;
	}

	public Connection getConnection() {
		return getConnection(SystemVariable.DSID);
	}

	public Connection getConnection(String user, String passwd, String url) throws SQLException {
		Connection conn = DataSourceManager.getConnection(user, passwd, url);
		long threadId = Thread.currentThread().getId();
		if (BaseDAO.IS_TRANSACTION_MAP.containsKey(threadId) && BaseDAO.IS_TRANSACTION_MAP.get(threadId)) {
			try {
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				LogUtils.error("设置连接不自动提交事务失败：" + conn, e);
				throw new JdbcException(e);
			}
		}
		connections.add(conn);// 添加到可被关闭的数据库连接链表中
		return conn;
	}

	public boolean isConnectionClosed(Connection connection) {
		if (connection == null) {
			return true;
		}
		try {
			if (connection.isClosed()) {
				return true;
			} else {
				Statement stmt = connection.createStatement();
				stmt.close();
			}
		} catch (SQLException e) {
			return true;
		}
		return false;
	}

	protected DataAccess getDataAccess(String user, String passwd, String url) throws SQLException {
		String key = user + "/" + passwd + "@" + url;
		DataAccess dataAccess = null;
		if (dataAccesses.containsKey(key)) {
			dataAccess = dataAccesses.get(key);
			long threadId = Thread.currentThread().getId();
			try {
				Connection conn = dataAccess.getConnection();
				if (isConnectionClosed(conn)) {
					dataAccess.setConnection(getConnection(user, passwd, url));
				} else if (BaseDAO.IS_TRANSACTION_MAP.containsKey(threadId) &&
						BaseDAO.IS_TRANSACTION_MAP.get(threadId) && dataAccess.getConnection().getAutoCommit()) {
					dataAccess.getConnection().setAutoCommit(false);
				} else {

				}
			} catch (SQLException e) {
				LogUtils.error("设置连接不自动提交事务失败：" + dataAccess.getConnection(), e);
				throw new JdbcException(e);
			}
		} else {
			dataAccess = getDataAccessInstance(getConnection(user, passwd, url));
			dataAccesses.put(key, dataAccess);
		}
		return dataAccess;
	}

	protected DataAccess getDataAccess(Object key) {
		DataAccess dataAccess = null;
		if (dataAccesses.containsKey(key)) {
			dataAccess = dataAccesses.get(key);
			long threadId = Thread.currentThread().getId();
			try {
				Connection conn = dataAccess.getConnection();
				if (conn == null || conn.isClosed()) {
					dataAccess.setConnection(getConnection(key));
				} else if (BaseDAO.IS_TRANSACTION_MAP.containsKey(threadId) &&
						BaseDAO.IS_TRANSACTION_MAP.get(threadId) && dataAccess.getConnection().getAutoCommit()) {
					dataAccess.getConnection().setAutoCommit(false);
				} else {

				}
				// 如果当前进程存在事物，将链接加入到事物控制中
				if (BaseDAO.IS_TRANSACTION_MAP.containsKey(threadId)) {
					DataSourceManager.pushUsedConnection(Convert.toString(key), dataAccess.getConnection());
				}
			} catch (SQLException e) {
				LogUtils.error("设置连接不自动提交事务失败：" + dataAccess.getConnection(), e);
				throw new JdbcException(e);
			}
		} else {
			dataAccess = getDataAccessInstance(getConnection(key));
			dataAccesses.put(key, dataAccess);
		}
		return dataAccess;
	}

	protected DataAccess getDataAccess() {
		return getDataAccess(SystemVariable.DSID);
	}

	protected DataAccess getDataAccessInstance(Connection connection) {
		return DataAccessFactory.getInstance(connection);
	}

	public static void beginTransaction() {

		long threadId = Thread.currentThread().getId();
		BaseDAO.IS_TRANSACTION_MAP.put(threadId, true);
		Connection conns[] = DataSourceManager.getUsedConnection();
		if (conns != null && conns.length > 0) {
			for (Connection conn : conns) {
				try {
					if (conn.getAutoCommit()) {
						conn.setAutoCommit(false);
					}
				} catch (SQLException e) {
					LogUtils.error("设置连接不自动提交事务失败：" + conn, e);
					throw new JdbcException(e);
				}
			}

		}
	}

	public static void rollback() {
		long threadId = Thread.currentThread().getId();
		if (BaseDAO.IS_TRANSACTION_MAP.containsKey(threadId) && BaseDAO.IS_TRANSACTION_MAP.get(threadId)) {
			Connection[] usedDruidPooledConnections = DataSourceManager.getUsedConnection();
			for (Connection conn : usedDruidPooledConnections) {
				try {
					conn.rollback();
				} catch (SQLException e) {
					LogUtils.error("事务回滚失败：" + conn, e);
					throw new JdbcException(e);
				}
			}
			BaseDAO.IS_TRANSACTION_MAP.remove(threadId);
		}
	}

	public static void commit() {
		long threadId = Thread.currentThread().getId();
		if (BaseDAO.IS_TRANSACTION_MAP.containsKey(threadId) && BaseDAO.IS_TRANSACTION_MAP.get(threadId)) {
			Connection[] usedDruidPooledConnections = DataSourceManager.getUsedConnection();
			for (Connection conn : usedDruidPooledConnections) {
				try {
					conn.commit();
				} catch (SQLException e) {
					LogUtils.error("事务提交失败：" + conn, e);
					throw new JdbcException(e);
				}
			}
			BaseDAO.IS_TRANSACTION_MAP.remove(threadId);
		}
	}

	public final void close() {
		long threadId = Thread.currentThread().getId();
		BaseDAO.IS_TRANSACTION_MAP.remove(threadId);
		dataAccesses.clear();
		DataSourceManager.destroy(connections);
		connections.clear();
	}

}
