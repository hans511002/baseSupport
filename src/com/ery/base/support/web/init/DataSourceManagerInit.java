package com.ery.base.support.web.init;

import java.util.List;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import com.ery.base.support.jndi.ILookupHandler;
import com.ery.base.support.jndi.IServer;
import com.ery.base.support.log4j.LogUtils;
import com.ery.base.support.sys.DataSourceManager;
import com.ery.base.support.sys.SystemConstant;
import com.ery.base.support.sys.SystemVariable;
import com.ery.base.support.utils.ClassUtils;
import com.ery.base.support.web.ISystemStart;


public class DataSourceManagerInit implements ISystemStart {
	
	public static final String JNDI_NAMESPACE = SystemVariable.getString("jdbc_namespace", "jdbc");
	private ServletContext servletContext;

	public void destory() {
		DataSourceManager.destroy();
	}

	public void init() {
		// 初始化DataSourceManager
		DataSource dds = null;
		try {
			String dbProfile = servletContext.getInitParameter("db_profile");
			if (dbProfile != null) {
				String[] ddfs = dbProfile.split(",");
				for (String string : ddfs) {
					DataSource _dds = DataSourceManager.dataSourceInit(string);
					if (dds == null)
						dds = _dds;
				}
			} else {
				SystemConstant.setDB_CONF_FILE(dbProfile);
			}
			// 自动获取服务器容器类型
			List<Class<?>> list = ClassUtils
					.getAllClassByInterface(IServer.class, IServer.class.getPackage().getName());
			LogUtils.info("IServers=" + list);
			for (Class class1 : list) {
				try {
					IServer server = (IServer) class1.newInstance();
					server.lookup(JNDI_NAMESPACE, new ILookupHandler() {
						public void handle(String name, DataSource dataSource) {
							DataSourceManager.addDataSource(name, dataSource);
						}
					});
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			LogUtils.error("DataSourceManagerInit", e);
		}
		if (!DataSourceManager.containKey(SystemVariable.DSID) && dds != null) {
			DataSourceManager.addDataSource(SystemVariable.DSID, dds);
		}
		if (DataSourceManager.containKey(SystemVariable.DSID)) {
			DataSourceManager.addDataSource(SystemVariable.getString("currentDataSourceId", "0"),
					DataSourceManager.getDataSource(SystemVariable.DSID));
			DataSourceManager.loadDataSource();
		}
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
