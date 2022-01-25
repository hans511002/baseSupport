package com.sobey.jcg.support.sys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.jcg.support.web.SystemInitListener;
import com.sobey.jcg.support.web.init.SystemVariableInit;

public class SystemVariable {

	public static final String DB_URL_KEY = "db.url";// 任务元数据库url
	public static final String DB_USER_KEY = "db.user";
	public static final String DB_PASS_KEY = "db.pass";
	public static final String DB_MAX_WAIT_KEY = "db.max.wait";// 超时等待
	public static final String DB_MAX_ACTIVE_KEY = "db.max.active";// 最大活动链接数（连接池大小）
	public static final String DB_CHECK_IDLE_KEY = "db.check.idle";// 检查空闲连接间隔
	public static final String DS_ID_KEY = "ds.id";
	public static final String DS_LOAD_DB = "ds.loaddb";
	public static final String DB_RDB_DATASOYRCE_SQL = "db.datasource.query.sql";

	public static final String LOG_FILE_NAME_KEY = "log.fileName";

	protected static Properties conf = new Properties();

	public static String getResourceAbsPath(String fileName) {
		URL url = null;
		if (SystemInitListener.inWebApp && SystemVariableInit.WEB_ROOT_PATH != null) {
			// SystemVariable.class.getClassLoader().getResource("");
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			url = classLoader.getResource(fileName);
			if (url != null)
				fileName = url.toString();
		} else {
			url = ClassLoader.getSystemResource(fileName);
			if (url != null) {
				String fl = url.toString();
				File file = new File(fl);
				if (!file.exists()) {
					url = null;
				} else {
					fileName = url.toString();
				}
			}
			if (url == null) {
				String osName = System.getProperty("os.name").toLowerCase();// 操作系统
				String usrDir = System.getProperty("user.dir");// 当前程序工作目录
				File file = null;
				if (osName.contains("win")) {
					if (fileName.length() > 1 && ":".equals(fileName.substring(1, 1))) { // 绝对路径
						file = new File(fileName);
					} else { // 相对
						file = new File(usrDir, fileName);
					}
				} else {
					if ("/".equals(fileName.substring(0, 1))) {// 绝对路径
						file = new File(fileName);
					} else { // 相对
						file = new File(usrDir, fileName);
					}
				}
				if (file.exists()) {
					fileName = file.getAbsolutePath();
				}
			}
		}
		return fileName;
	}

	public static InputStream getResourceAsStream(String fileName) {
		InputStream in = null;
		if (SystemInitListener.inWebApp && SystemVariableInit.WEB_ROOT_PATH != null) {
			// SystemVariable.class.getClassLoader().getResource("");
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			in = classLoader.getResourceAsStream(fileName);
		} else {
			in = ClassLoader.getSystemResourceAsStream(fileName);
			if (in == null) {
				String osName = System.getProperty("os.name").toLowerCase();// 操作系统
				String usrDir = System.getProperty("user.dir");// 当前程序工作目录
				File file = null;
				if (osName.contains("win")) {
					if (fileName.length() > 1 && ":".equals(fileName.substring(1, 1))) { // 绝对路径
						file = new File(fileName);
					} else { // 相对
						file = new File(usrDir, fileName);
					}
				} else {
					if ("/".equals(fileName.substring(0, 1))) {// 绝对路径
						file = new File(fileName);
					} else { // 相对
						file = new File(usrDir, fileName);
					}
				}
				if (file.exists()) {
					try {
						in = new FileInputStream(file);
					} catch (FileNotFoundException e) {
						throw new RuntimeException("资源文件:[" + file.getAbsolutePath() + "]存在，但不可读!");
					}
				}
			}
		}
		return in;
	}

	static {
		try {
			if (!SystemInitListener.inWebApp) {
				init();
				SystemVariable.DSID = "" + SystemVariable.getDefaultDataSourceID();
			}
		} catch (Exception e) {
			LogUtils.warn("初始系统配置变量:" + e.getMessage());
		}
	}

	public synchronized static void init() {
		String sysConfFile = SystemConstant.getSYS_CONF_FILE();
		InputStream in = getResourceAsStream(sysConfFile);
		if (in != null) {
			try {
				conf.load(in);
				in.close();
				System.getProperties().putAll(SystemVariable.getConf());
				LogUtils.info("加载[" + sysConfFile + "]文件OK!");
			} catch (Exception e) {
				LogUtils.error("加载[" + sysConfFile + "]文件出错!" + e.getMessage());
				throw new RuntimeException(e);
			}
		} else {
			LogUtils.warn("资源文件不存在：" + sysConfFile);
		}
		if (System.getProperty("logFileName") == null) {
			System.setProperty("logFileName", getLogFileName());
		}
		if ("true".equals(conf.get(DS_LOAD_DB))) {
			SystemConstant.setLOAD_DB_DATA_SOURCE(true);
		} else if ("false".equals(conf.get(DS_LOAD_DB))) {
			SystemConstant.setLOAD_DB_DATA_SOURCE(false);
		}
	}

	public static Properties getConf() {
		return conf;
	}

	public static String getDataSourceQuerySql() {
		return Convert.toString(conf.getProperty(DB_RDB_DATASOYRCE_SQL),
				"SELECT DATA_SOURCE_ID,DATA_SOURCE_NAME,DATA_SOURCE_TYPE,DATA_SOURCE_URL,DATA_SOURCE_USER,DATA_SOURCE_PASS,"
						+ "DATA_SOURCE_DESC,DATA_SOURCE_CFG FROM ST_DATA_SOURCE");
	}

	public static String getDbUrl() {
		return conf.getProperty(DB_URL_KEY);
	}

	public static String getDbUser() {
		return conf.getProperty(DB_USER_KEY);
	}

	public static String getDbPass() {
		return conf.getProperty(DB_PASS_KEY);
	}

	public static String getDbMaxwait() {
		return conf.getProperty(DB_MAX_WAIT_KEY, "30000");
	}

	public static String getDbMaxActive() {
		return conf.getProperty(DB_MAX_ACTIVE_KEY, "50");
	}

	public static String getDbCheckIdle() {
		return conf.getProperty(DB_CHECK_IDLE_KEY, "120000");// 2分钟
	}

	public static String getDefaultDataSourceID() {
		return conf.getProperty(DS_ID_KEY, "1");
	}

	public static String DSID = "1";// 管理库DS
									// ID

	public static String getLogFileName() {
		return conf.getProperty(LOG_FILE_NAME_KEY, "system");
	}

	public static String getCurrentWorkDir() {
		return System.getProperty("user.dir");
	}

	public static boolean containKey(String key) {
		return conf.containsKey(key);
	}

	public static int getInt(String key, int defaultValue) {
		return containKey(key) ? Integer.parseInt(conf.getProperty(key)) : defaultValue;
	}

	public static int getInt(String key) {
		return Integer.parseInt(conf.getProperty(key));
	}

	public static long getLong(String key, long defaultValue) {
		return containKey(key) ? Long.parseLong(conf.getProperty(key)) : defaultValue;
	}

	public static long getLong(String key) {
		return Long.parseLong(conf.getProperty(key));
	}

	public static short getShort(String key, short defaultValue) {
		return containKey(key) ? Short.parseShort(conf.getProperty(key)) : defaultValue;
	}

	public static short getShort(String key) {
		return Short.parseShort(conf.getProperty(key));
	}

	public static double getDouble(String key, double defaultValue) {
		return containKey(key) ? Double.parseDouble(conf.getProperty(key)) : defaultValue;
	}

	public static double getDouble(String key) {
		return Double.parseDouble(conf.getProperty(key));
	}

	public static float getFloat(String key, float defaultValue) {
		return containKey(key) ? Float.parseFloat(conf.getProperty(key)) : defaultValue;
	}

	public static float getFloat(String key) {
		return Float.parseFloat(conf.getProperty(key));
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		return containKey(key) ? Boolean.parseBoolean(conf.getProperty(key)) : defaultValue;
	}

	public static boolean getBoolean(String key) {
		return Boolean.parseBoolean(conf.getProperty(key));
	}

	public static String getString(String key, String defaultValue) {
		return containKey(key) ? conf.getProperty(key).trim() : defaultValue;
	}

	public static String getString(String key) {
		return conf.getProperty(key);
	}

	public static Properties getProperties() {
		return conf;
	}

	public synchronized static void load(FileInputStream inputStream) throws IOException {
		conf.load(inputStream);
	}

}
