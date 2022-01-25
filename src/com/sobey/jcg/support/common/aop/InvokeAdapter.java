package com.sobey.jcg.support.common.aop;

public class InvokeAdapter extends InvokeHandler {

	public boolean beforeInvoke(Object source, String methodName, Object[] args) {
		return true;
	}

	public Object afterInvoke(Object source, Object result, String methodName, Object[] args) {
		return result;
	}

	public void exceptionInvoke(Object source, String methodName, Throwable exception, Object[] args) throws Throwable {
		throw exception;
	}

}
