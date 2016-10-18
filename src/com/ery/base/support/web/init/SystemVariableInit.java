package com.ery.base.support.web.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import com.ery.base.support.log4j.LogUtils;
import com.ery.base.support.sys.SystemVariable;
import com.ery.base.support.web.ISystemStart;

public class SystemVariableInit implements ISystemStart {

	private ServletContext servletContext;
	public static String WEB_ROOT_PATH = null;
	public static String CLASS_PATH = null;

	public void destory() {
		Properties conf = SystemVariable.getProperties();
		if (conf != null)
			conf.clear();
	}

	public void init() {
		// 加载webroot路径
		WEB_ROOT_PATH = servletContext.getRealPath("/");
		LogUtils.info("WEB_ROOT_PATH=" + WEB_ROOT_PATH);
		// 加载系统classpath
		// CLASS_PATH = this.getClass().getResource("/").toString();
		// CLASS_PATH = java.net.URLDecoder.decode(CLASS_PATH);
		CLASS_PATH = servletContext.getRealPath("/WEB-INF/classes") + File.separator;
		LogUtils.info("CLASS_PATH=" + CLASS_PATH);
		// 加载properties配置文件
		String proFile = servletContext.getInitParameter("conf_props");
		if (proFile != null) {
			String[] props = proFile.split(",");
			for (String conf : props) {
				File file = new File(CLASS_PATH, conf);
				load(file);
			}
			load(new File(System.getProperty("java.home"), "local.properties"));
		}
		SystemVariable.init();
		SystemVariable.DSID = "" + SystemVariable.getDefaultDataSourceID();// 管理库DSID
	}

	public static void load(File file) {
		if (file.exists() && file.canRead()) {// 判断文件是否存在
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				SystemVariable.load(inputStream);
				inputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						LogUtils.error("load config file error:" + file.getAbsolutePath(), e);
					}
				}
			}
		} else {
			System.err.println("资源文件(" + file.getAbsolutePath() + ") 不存在或不可访问！");
		}
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
