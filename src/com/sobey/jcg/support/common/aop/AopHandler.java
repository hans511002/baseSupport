package com.sobey.jcg.support.common.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.sobey.jcg.support.common.BusinessException;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.jcg.support.sys.podo.BaseDAO;

public class AopHandler implements InvocationHandler {

	private Object target;

	public AopHandler(Object target) {
		this.target = target;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object obj = null;
		try {
			initObject(target);
			obj = method.invoke(target, args);
		} catch (Exception e) {
			Throwable throwable = e.getCause();
			if (throwable instanceof BusinessException) {
				LogUtils.error("Action抛出BusinessException：" + method, throwable);
				throw new RuntimeException(throwable.getMessage());
			} else {
				LogUtils.error("Action抛出异常：" + method, e);
			}
		} finally {
			DataSourceManager.destroy();
		}
		return obj;
	}

	private void initObject(Object obj) {
		Method[] methods = obj.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String name = method.getName();
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (name.startsWith("set") && parameterTypes.length == 1 && isInstanceof(parameterTypes[0], BaseDAO.class)) {
				if (method.getModifiers() == Modifier.PUBLIC) {
					try {
						BaseDAO baseDAO = (BaseDAO) parameterTypes[0].newInstance();
						method.invoke(obj, baseDAO);
					} catch (Exception e) {
						LogUtils.warn(this.getClass().getCanonicalName(), e);
					}
				}
			}
		}
	}

	private boolean isInstanceof(Class<?> child, Class<?> parent) {
		boolean returnVal = false;
		try {
			child.asSubclass(parent);
			returnVal = true;
		} catch (Exception e) {
			returnVal = false;
		}
		return returnVal;
	}
}
