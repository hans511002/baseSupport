package com.ery.base.support.common.aop;


public abstract class InvokeHandler {


    public final boolean beforeHanderInvoke(Object obj,Object...args){
        StackTraceElement lastTrace= Thread.currentThread().getStackTrace()[2]; //方法调用轨迹，上个调用此方法信息
        return beforeInvoke(obj, lastTrace.getMethodName(), args);
    }

    
    public final Object afterHanderInvoke(Object obj,Object result,Object...args){
        StackTraceElement lastTrace= Thread.currentThread().getStackTrace()[2]; //方法调用轨迹，上个调用此方法信息
        return afterInvoke(obj,result,lastTrace.getMethodName(),args);
    }

    
    public final void exceptionHanderInvoke(Object obj,Throwable exception,Object...args) throws Throwable{
        StackTraceElement lastTrace= Thread.currentThread().getStackTrace()[2]; //方法调用轨迹，上个调用此方法信息
        exceptionInvoke(obj,lastTrace.getMethodName(),exception,args);
    }

    
    public abstract boolean beforeInvoke(Object source,String methodName,Object[] args);

    
    public abstract Object afterInvoke(Object source,Object result,String methodName,Object[] args);

    
    public abstract void exceptionInvoke(Object source,String methodName,Throwable exception,Object[] args) throws Throwable;

}
