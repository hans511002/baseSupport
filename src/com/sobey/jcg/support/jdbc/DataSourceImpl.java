package com.sobey.jcg.support.jdbc;

import java.sql.SQLException;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;

public class DataSourceImpl extends DruidDataSource {
	private static final long serialVersionUID = -311617853874073484L;

	public DataSourceImpl(String url, String user, String password) throws SQLException {
		this(null, url, user, password);
	}

	public DataSourceImpl(String driverName, String url, String user, String password) throws SQLException {
		super();
		Properties properties = new Properties();
		if (driverName != null && !driverName.trim().equals(""))
			properties.setProperty(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, driverName);
		properties.setProperty(DruidDataSourceFactory.PROP_URL, url);
		properties.setProperty(DruidDataSourceFactory.PROP_USERNAME, user);
		properties.setProperty(DruidDataSourceFactory.PROP_PASSWORD, password);
		properties.setProperty(DruidDataSourceFactory.PROP_MAXWAIT, "10000");
		properties.setProperty(DruidDataSourceFactory.PROP_MAXACTIVE, "10");
		properties.setProperty(DruidDataSourceFactory.PROP_INITIALSIZE, "2");
		properties.setProperty(DruidDataSourceFactory.PROP_INIT, "false");
		DruidDataSourceFactory.config(this, properties);

	}

}
