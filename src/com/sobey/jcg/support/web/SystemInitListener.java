package com.sobey.jcg.support.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sobey.jcg.support.log4j.LogUtils;


public class SystemInitListener implements ServletContextListener {
	private ISystemStart[] systemStarts = null;
	public static boolean inWebApp = false;

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		inWebApp = true;
		// 获取servlet
		ServletContext servletContext = servletContextEvent.getServletContext();
		String sysStartInitClass = servletContext.getInitParameter("sysStartInitClass").replaceAll("\\n", "")
				.replaceAll("\\s", "");
		String[] initClasses = null;
		if (sysStartInitClass.contains(",")) {
			initClasses = sysStartInitClass.split(",");
		} else {
			initClasses = new String[] { sysStartInitClass };
		}
		systemStarts = new ISystemStart[initClasses.length];
		// 依次初始化类
		int i = 0;
		for (String initClass : initClasses) {
			try {
				systemStarts[i++] = (ISystemStart) Class.forName(initClass).newInstance();
				systemStarts[i - 1].setServletContext(servletContext);
				systemStarts[i - 1].init();
			} catch (Exception e) {
				LogUtils.error(null, e);
			}
		}
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		if (systemStarts != null) {
			// 依次调用其销毁方法
			for (ISystemStart systemStart : systemStarts) {
				if (systemStart != null)
					systemStart.destory();
			}
		}
	}
}
