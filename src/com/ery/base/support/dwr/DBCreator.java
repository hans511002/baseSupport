package com.ery.base.support.dwr;

import java.lang.reflect.Proxy;

import org.directwebremoting.create.NewCreator;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.util.Logger;
import org.directwebremoting.util.Messages;

import com.ery.base.support.common.aop.ActionClassAdapterFactory;
import com.ery.base.support.common.aop.AopHandler;

public class DBCreator extends NewCreator {

	private static final Logger log = Logger.getLogger(DBCreator.class);

	private Class<?> clazz;

	private Class<?> interfaceClazz;

	private Class<?> interfaceImplClazz;

	public Object getInstance() throws InstantiationException {
		try {
			Object object = Proxy.newProxyInstance(interfaceImplClazz.getClassLoader(), new Class[] { interfaceClazz },
					new AopHandler(interfaceImplClazz.newInstance()));
			return object;
		} catch (IllegalAccessException ex) {
			throw new InstantiationException(Messages.getString("Creator.IllegalAccess"));
		}
	}

	// ----------------------------------------------------setter/getter-----------------------------------------------------

	public void setClassName(String className) {
		setClass(className);
	}

	public String getClassName() {
		return getType().getName();
	}

	public void setClass(String classname) {
		try {
			clazz = LocalUtil.classForName(classname);
			interfaceClazz = ActionClassAdapterFactory.getInstance().getInterface(clazz);
			interfaceImplClazz = ActionClassAdapterFactory.getInstance().getAdapter(clazz,
					interfaceClazz.getCanonicalName());
		} catch (ExceptionInInitializerError ex) {
			log.warn("Class load error", ex);
			throw new IllegalArgumentException(Messages.getString("Creator.ClassLoadError", classname));
		} catch (ClassNotFoundException e) {
			log.warn("ClassNotFoundException：" + classname, e);
		}
	}

	public Class<?> getType() {
		return interfaceClazz;
	}
}
