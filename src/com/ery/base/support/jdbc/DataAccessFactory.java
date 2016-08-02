package com.ery.base.support.jdbc;

import java.sql.Connection;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.ery.base.support.common.aop.AopFactory;
import com.ery.base.support.common.aop.IAopMethodFilter;
import com.ery.base.support.common.aop.InvokeHandler;
import com.ery.base.support.log4j.LogUtils;
import com.ery.base.support.sys.SystemVariable;


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
