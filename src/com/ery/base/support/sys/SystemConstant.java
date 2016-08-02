package com.ery.base.support.sys;


public class SystemConstant {

	private static String SYS_CONF_FILE = "support.properties";// 应用系统资源配置
	private static String DB_CONF_FILE = "db.properties";// 数据库连接池资源配置
	private static String LOG4J_CONF_FILE = "log4j.properties";// log4j配置
	private static boolean IS_LOCAL_TEST = false;// 是否是开发ide环境
	private static String LOG_FILE_SUFFIX = "";
	private static boolean LOAD_DB_DATA_SOURCE = false;// 是否加载db配置的数据源

	static {
		System.setProperty("log4j.configuration", LOG4J_CONF_FILE);
	}

	public static String getSYS_CONF_FILE() {
		return SYS_CONF_FILE;
	}

	public static void setSYS_CONF_FILE(String SYS_CONF_FILE) {
		SystemConstant.SYS_CONF_FILE = SYS_CONF_FILE;
	}

	public static String getDB_CONF_FILE() {
		return DB_CONF_FILE;
	}

	public static void setDB_CONF_FILE(String DB_CONF_FILE) {
		SystemConstant.DB_CONF_FILE = DB_CONF_FILE;
	}

	public static String getLOG4J_CONF_FILE() {
		return LOG4J_CONF_FILE;
	}

	public static void setLOG4J_CONF_FILE(String LOG4J_CONF_FILE) {
		if (!SystemConstant.LOG4J_CONF_FILE.equals(LOG4J_CONF_FILE)) {
			SystemConstant.LOG4J_CONF_FILE = LOG4J_CONF_FILE;
			System.setProperty("log4j.configuration", LOG4J_CONF_FILE);
		}
	}

	public static boolean isIS_LOCAL_TEST() {
		return IS_LOCAL_TEST;
	}

	public static void setIS_LOCAL_TEST(boolean IS_LOCAL_TEST) {
		SystemConstant.IS_LOCAL_TEST = IS_LOCAL_TEST;
	}

	public static String getLOG_FILE_SUFFIX() {
		return LOG_FILE_SUFFIX;
	}

	public static void setLOG_FILE_SUFFIX(String LOG_FILE_SUFFIX) {
		SystemConstant.LOG_FILE_SUFFIX = LOG_FILE_SUFFIX;
	}

	public static boolean isLOAD_DB_DATA_SOURCE() {
		return LOAD_DB_DATA_SOURCE;
	}

	public static void setLOAD_DB_DATA_SOURCE(boolean LOAD_DB_DATA_SOURCE) {
		SystemConstant.LOAD_DB_DATA_SOURCE = LOAD_DB_DATA_SOURCE;
	}

}
