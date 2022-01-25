package com.sobey.jcg.support.common.aop;

public interface IAopMethodFilter {

	public boolean filter(Class<?> souceClass, String methodName, Class<?>[] paramType);

}
