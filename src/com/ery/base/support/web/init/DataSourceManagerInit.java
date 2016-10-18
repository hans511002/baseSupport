package com.ery.base.support.web.init;

import java.util.List;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import com.ery.base.support.common.aop.AopFactory;
import com.ery.base.support.common.aop.IAopMethodFilter;
import com.ery.base.support.common.aop.InvokeAdapter;
import com.ery.base.support.jdbc.DataAccess;
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

	void aopTest() {
		try {
			DataAccess access = (DataAccess) AopFactory.getInstance(DataAccess.class, new InvokeAdapter() {
				public boolean beforeInvoke(Object source, String methodName, Object[] args) {
					System.out.println("public boolean beforeInvoke(" + source + ", " + methodName + ",   " + args
							+ ")");
					return true;
				}

				public Object afterInvoke(Object source, Object result, String methodName, Object[] args) {
					System.out
							.println("public boolean afterInvoke(" + source + ", " + methodName + ",   " + args + ")");
					System.out.println("result=" + result);
					return result;
				}

				public void exceptionInvoke(Object source, String methodName, Throwable exception, Object[] args)
						throws Throwable {
					System.out.println("public boolean exceptionInvoke(" + source + ", " + methodName + ",   " + args
							+ ")");
					exception.printStackTrace();
					throw exception;
				}
			}, new String[] { "\\w*" },// 要进行拦截的方法正则表达式。
					new IAopMethodFilter() {
						/**
						 * 过滤器，过滤某个方法，返回true表示可以进行AOP拦截，返回false表示不能进行AOP拦截，与AopFactory联合使用
						 * 
						 * @param souceClass
						 * @param methodName
						 * @param paramType
						 * @return
						 */
						public boolean filter(Class<?> souceClass, String methodName, Class<?>[] paramType) {
							return true;
						}
					});
			// System.err.println(access.testLong());
			// System.err.println(access.testLong(2l, 3l, 1l, null));

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void init() {
		// aopTest()
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
