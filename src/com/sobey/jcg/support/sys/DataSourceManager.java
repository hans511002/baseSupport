package com.sobey.jcg.support.sys;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.nordb.FTPDataSource;
import com.sobey.jcg.support.nordb.HDFSDataSource;
import com.sobey.jcg.support.nordb.HbaseDataSource;
import com.sobey.jcg.support.sys.podo.BaseDAO;
import com.sobey.jcg.support.sys.podo.DataSrcDAO;
import com.sobey.jcg.support.sys.podo.DataSrcPO;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.jcg.support.utils.Utils;
import com.sobey.jcg.support.web.SystemInitListener;

public class DataSourceManager {

	private static Map<String, Object> dataSource = new HashMap<String, Object>();
	private static Map<String, DataSrcPO> dataSrcPOMap = new HashMap<String, DataSrcPO>();

	private static Hashtable<Long, Hashtable<String, Connection>> usingConnections = new Hashtable<Long, Hashtable<String, Connection>>();
	static boolean loadDataSource = false;

	public synchronized static void loadDataSource() {
		if (loadDataSource || !SystemConstant.isLOAD_DB_DATA_SOURCE())
			return;
		// 查询数据库配置的数据源，加载到内存
		DataSrcDAO srcDAO = new DataSrcDAO();
		try {
			loadDataSource = true;
			List<DataSrcPO> allDS = srcDAO.getAllDataSrc();
			for (DataSrcPO ds : allDS) {
				String dsId = Convert.toString(ds.getDATA_SOURCE_ID());
				if (SystemVariable.DSID.equals(dsId)) {
					continue;
				}
				try {
					dataSrcPOMap.put(dsId, ds);
					if (ds.getDATA_SOURCE_TYPE() == DataSrcPO.DS_TYPE_MYSQL ||
							ds.getDATA_SOURCE_TYPE() == DataSrcPO.DS_TYPE_ORACLE) {
						Properties pro = new Properties();
						pro.setProperty(DruidDataSourceFactory.PROP_URL, ds.getDATA_SOURCE_URL());
						pro.setProperty(DruidDataSourceFactory.PROP_USERNAME, ds.getDATA_SOURCE_USER());
						pro.setProperty(DruidDataSourceFactory.PROP_PASSWORD, ds.getDATA_SOURCE_PASS());
						pro.setProperty(DruidDataSourceFactory.PROP_MAXWAIT, SystemVariable.getDbMaxwait());
						pro.setProperty(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS,
								SystemVariable.getDbCheckIdle());
						if (!pro.containsKey(DruidDataSourceFactory.PROP_MAXACTIVE)) {
							pro.setProperty(DruidDataSourceFactory.PROP_MAXACTIVE, "16");
						}

						DruidDataSource dds = (DruidDataSource) DruidDataSourceFactory.createDataSource(pro);
						dataSource.put(dsId, dds);
					} else if (ds.getDATA_SOURCE_TYPE() == DataSrcPO.DS_TYPE_HBASE) {
						// Hbase 连接初始
						dataSource.put(dsId, new HbaseDataSource(ds));
					} else if (ds.getDATA_SOURCE_TYPE() == DataSrcPO.DS_TYPE_FTP) {
						// 文件连接
						dataSource.put(dsId, new FTPDataSource(ds));
					} else if (ds.getDATA_SOURCE_TYPE() == DataSrcPO.DS_TYPE_HDFS) {
						// 文件连接
						dataSource.put(dsId, new HDFSDataSource(ds));
					}
				} catch (Exception e) {
					LogUtils.error("初始配置数据源[" + ds.getDATA_SOURCE_ID() + "]出错!" + e.getMessage());
				}
			}
		} finally {
			srcDAO.close();
		}

	}

	public synchronized static DataSource dataSourceInit() {
		String dbConfFile = SystemConstant.getDB_CONF_FILE();
		return dataSourceInit(dbConfFile);
	}

	public synchronized static DataSource dataSourceInit(String dbConfFile) {
		if (dbConfFile == null)
			return null;
		Properties pro = new Properties();
		InputStream in = SystemVariable.getResourceAsStream(dbConfFile);
		boolean fileExists = false;
		if (in != null) {
			try {
				pro.load(in);
				in.close();
				fileExists = true;
				System.out.println("加载[" + dbConfFile + "]文件OK!");
			} catch (Exception e) {
				LogUtils.warn("加载[" + dbConfFile + "]文件出错!" + e.getMessage());
				return null;
			}
			if (!pro.containsKey(DruidDataSourceFactory.PROP_MAXWAIT)) {
				pro.setProperty(DruidDataSourceFactory.PROP_MAXWAIT, SystemVariable.getDbMaxwait());
			}
		} else {
			if (dataSource.size() > 0) {
				System.out.println("未能加载数据源配置[" + dbConfFile + "]文件,已经有数据源，不再使用默认配置!");
				return null;
			}
			System.out.println("未能加载数据源配置[" + dbConfFile + "]文件,使用默认配置!");
			if (SystemVariable.getDbUrl() == null) {
				System.out.println("未能加载数据源配置[" + dbConfFile + "]文件,默认配置也错误!");
				return null;
			}
			pro.setProperty(DruidDataSourceFactory.PROP_URL, SystemVariable.getDbUrl());
			pro.setProperty(DruidDataSourceFactory.PROP_USERNAME, SystemVariable.getDbUser());
			pro.setProperty(DruidDataSourceFactory.PROP_PASSWORD, SystemVariable.getDbPass());
			pro.setProperty(DruidDataSourceFactory.PROP_MAXWAIT, SystemVariable.getDbMaxwait());
			pro.setProperty("DataSourceId", SystemVariable.DSID);
		}
		if (!pro.containsKey(DruidDataSourceFactory.PROP_MAXACTIVE)) {
			pro.setProperty(DruidDataSourceFactory.PROP_MAXACTIVE, SystemVariable.getDbMaxActive());
		}
		if (!pro.containsKey(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS)) {
			pro.setProperty(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, SystemVariable.getDbCheckIdle());
		}
		String dsId = pro.getProperty("DataSourceId", SystemVariable.DSID);
		if (!dataSource.containsKey(dsId)) {
			pro.putAll(SystemVariable.getConf());
			try {
				if (pro.containsKey("config.decrypt") && pro.getProperty("config.decrypt", "false").equals("true")) {
					String connectPros = pro.getProperty(DruidDataSourceFactory.PROP_CONNECTIONPROPERTIES, "");
					if (connectPros.equals("")) {
						pro.put(DruidDataSourceFactory.PROP_CONNECTIONPROPERTIES, "config.decrypt=true");
					} else {
						if (connectPros.indexOf("config.decrypt=") == -1) {
							pro.put(DruidDataSourceFactory.PROP_CONNECTIONPROPERTIES, connectPros +
									";config.decrypt=true");
						}
					}
				}
				System.out.println(pro);
				DataSource dds = DruidDataSourceFactory.createDataSource(pro);
				dataSource.put(dsId, dds);
				if (SystemVariable.DSID.equals(dsId) && SystemConstant.isLOAD_DB_DATA_SOURCE()) {
					loadDataSource();
				}
				return dds;
			} catch (Exception e) {
				LogUtils.error("初始连接池出错!" + e.getMessage());
				return null;
			}
		} else {
			LogUtils.error("已经存在名称为:" + dsId + " 的数据库连接源，请确认数据源名称唯一[DataSourceId]：" + dataSource.get(dsId));
			return null;
		}
	}

	static {
		try {
			if (!SystemInitListener.inWebApp)
				dataSourceInit();
		} catch (Exception e) {
			LogUtils.error("初始化数据源", e);
		}
	}

	public static DataSrcPO getSrcPO(String dsId) {
		return dataSrcPOMap.get(dsId);
	}

	// 获取连接
	public static Connection getConnection() {
		return getConnection(SystemVariable.DSID);
	}

	public static Connection getConnection(String dsId, int waitTimeMs) {
		long l = System.currentTimeMillis();
		while (true) {
			try {
				return getConnection(dsId);
			} catch (Exception e) {
				Utils.sleep(1000);
				if (waitTimeMs <= 0) {
					throw new RuntimeException("获取数据库连接出错!" + e.getMessage());
				}
				if (System.currentTimeMillis() - l >= waitTimeMs) {
					throw new RuntimeException("获取数据库连接出错!" + e.getMessage());
				}
			}
		}
	}

	public static Connection getConnection(String dsId) {
		long threadId = Thread.currentThread().getId();
		if (dataSource.get(dsId) != null && dataSource.get(dsId) instanceof DataSource) {
			Connection connection = null;
			try {
				Hashtable<String, Connection> currentUserConn = usingConnections.get(threadId);
				if (currentUserConn == null) {
					currentUserConn = new Hashtable<String, Connection>();
					usingConnections.put(threadId, currentUserConn);
				}
				connection = currentUserConn.get(dsId);
				if (connection != null) {
					if (connection.isClosed()) {
						currentUserConn.remove(dsId);
						connection = null;
					} else if (connection instanceof DruidPooledConnection) {
						DruidPooledConnection con = (DruidPooledConnection) connection;
						if (con.isDisable() || con.isClosed() || con.isAbandonded()) {
							currentUserConn.remove(dsId);
							connection = null;
						}
					}
				}
				if (connection == null) {
					DataSource ds = (DataSource) dataSource.get(dsId);
					// ds = (DruidDataSource) dataSource.get(dsId);
					connection = ds.getConnection();
					currentUserConn.put(dsId, connection);
				}
			} catch (SQLException e) {
				LogUtils.error("获取连接出错!" + e.getMessage());
				throw new RuntimeException("获取数据库连接出错!" + e.getMessage());
			}
			return connection;
		} else {
			throw new RuntimeException("数据源[" + dsId + "]不是数据库类型，无法获取数据库连接!");
		}
	}

	public static Connection getConnection(final String user, final String passwd, final String url)
			throws SQLException {
		String key = user + "/" + passwd + "@" + url;
		return getConnection(key, user, passwd, url);
	}

	public static Connection getConnection(final String key, final String user, final String passwd, final String url)
			throws SQLException {
		if (dataSource.containsKey(key)) {
			return getConnection(key);
		} else {
			DruidDataSource ds = new DruidDataSource() {
				{
					setUrl(url);
					setUsername(user);
					setPassword(passwd);
					setMaxActive(10);
				}
			};
			dataSource.put(key, ds);
			return getConnection(key);
		}
	}

	public static Connection[] getUsedConnection() {
		long threadId = Thread.currentThread().getId();
		Connection[] connections = new Connection[] {};
		if (usingConnections.containsKey(threadId)) {
			connections = usingConnections.get(threadId).values().toArray(connections);
		}
		return connections;
	}

	public static void pushUsedConnection(String key, Connection connection) {
		long threadId = Thread.currentThread().getId();
		// 如果不存在线程，新增用户链接
		if (!usingConnections.containsKey(threadId)) {
			usingConnections.put(threadId, new Hashtable<String, Connection>());
		}
		if (!usingConnections.get(threadId).containsKey(key)) {
			usingConnections.get(threadId).put(key, connection);
		}
	}

	public static void destroy() {
		long threadId = Thread.currentThread().getId();
		if (usingConnections.containsKey(threadId)) {
			Set<Map.Entry<String, Connection>> entrySet = usingConnections.get(threadId).entrySet();
			for (Iterator<Map.Entry<String, Connection>> iterator = entrySet.iterator(); iterator.hasNext();) {
				Map.Entry<String, Connection> entry = iterator.next();
				Connection conn = entry.getValue();
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					LogUtils.error("数据库连接关闭异常：" + conn, e);

				}
			}
			usingConnections.remove(threadId);
			BaseDAO.IS_TRANSACTION_MAP.remove(threadId);
		}
	}

	public static void destroy(List<Connection> conns) {
		long threadId = Thread.currentThread().getId();
		if (usingConnections.containsKey(threadId)) {
			Set<Map.Entry<String, Connection>> entrySet = usingConnections.get(threadId).entrySet();
			for (Iterator<Map.Entry<String, Connection>> iterator = entrySet.iterator(); iterator.hasNext();) {
				Map.Entry<String, Connection> entry = iterator.next();
				Connection conn = entry.getValue();
				try {
					if (conns.contains(conn) && conn != null) {
						conn.close();
						iterator.remove();
					}
				} catch (SQLException e) {
					LogUtils.error("数据库连接关闭异常：" + conn, e);

				}
			}
			// 如果该线程当下已经没有任何连接在时候
			if (entrySet.isEmpty()) {
				usingConnections.remove(threadId);
				BaseDAO.IS_TRANSACTION_MAP.remove(threadId);
			}
		}
	}

	public static Map<String, Object> getAllDataSource() {
		return dataSource;
	}

	public static void addDataSource(String key, Object ds) {
		if (!dataSource.containsKey(key))
			dataSource.put(key, ds);
	}

	public static Object getDataSource(String key) {
		return dataSource.get(key);
	}

	public static void removeDataSource(String key) {
		dataSource.remove(key);
	}

	public static boolean containKey(String key) {
		return dataSource.containsKey(key);
	}

}
