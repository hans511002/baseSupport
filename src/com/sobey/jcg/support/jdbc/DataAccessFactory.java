package com.sobey.jcg.support.jdbc;

import java.sql.Connection;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.sobey.jcg.support.common.aop.AopFactory;
import com.sobey.jcg.support.common.aop.IAopMethodFilter;
import com.sobey.jcg.support.common.aop.InvokeHandler;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.SystemVariable;


public class DataAccessFactory {
	
	private static boolean isShowSql = SystemVariable.getBoolean("is_show_sql",false);
	private static int queryTimeout = SystemVariable.getInt("db.queryTimeout", 30);

	
	public static DataAccess getInstance(Connection connection) {
		DataAccess dataAccess = new DataAccess(connection);
		dataAccess.setShowSql(isShowSql);
		dataAccess.setQueryTimeout(queryTimeout);
		return dataAccess;
	}

	
	public static DataAccess getSubInstance(
			Class<? extends DataAccess> accessClass, Connection connection) {
		try {
			DataAccess access = (DataAccess) accessClass.newInstance();
			access.setConnection(connection);
			access.setShowSql(isShowSql);
			access.setQueryTimeout(queryTimeout);
			return access;
		} catch (Exception e) {
			 LogUtils.error("获取DataAccess实例失败！", e);
		}
		return null;
	}

	
	public static DataAccess getProxyDataAccess(Connection connection,
			InvokeHandler handler, String[] menthodIncludes,
			IAopMethodFilter filter) {
		DataAccess access = (DataAccess) AopFactory.getInstance(
				DataAccess.class, handler, menthodIncludes, filter);
		access.setConnection(connection);
		access.setShowSql(isShowSql);
		access.setQueryTimeout(queryTimeout);
		return access;
	}
}
