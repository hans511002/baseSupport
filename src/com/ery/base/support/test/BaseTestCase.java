package com.ery.base.support.test;

import junit.framework.TestCase;

import com.ery.base.support.jdbc.TestDB;
import com.ery.base.support.sys.DataSourceManager;

public class BaseTestCase extends TestCase {

	/**
	 * 测试用数据源信息配置
	 */
	private TestDB testDB = new TestDB();

	/**
	 * 构造函数,需要传入测试用的数据库信息
	 * 
	 * @param user
	 *            用户名
	 * @param passwd
	 *            密码
	 * @param url
	 *            地址
	 * @param driverClass
	 *            驱动类
	 */
	public BaseTestCase(String user, String passwd, String url, String driverClass) {
		super();
		testDB.setUser(user);
		testDB.setPasswd(passwd);
		testDB.setUrl(url);
		testDB.setClassName(driverClass);
	}

	/**
	 * 执行测试方法,重写TestCase中的runTest方法,实现了动态分配和回收数据库连接
	 */
	protected void runTest() throws Throwable {
		super.runTest();
		DataSourceManager.destroy();
	}
}
