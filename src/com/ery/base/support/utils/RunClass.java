package com.ery.base.support.utils;

import java.lang.reflect.Method;


public class RunClass {
    
    public static void main(String[] args){
        if(args!=null && args.length>=1){
            try {
                String className = args[0];
                Class c = Class.forName(className);
                Method m = c.getMethod("main",String[].class);
                String[] pars = new String[args.length-1];
                System.arraycopy(args, 1, pars, 0, pars.length);
                m.invoke(null,new Object[]{pars});//重要的地方
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        }else{
            System.out.println("请传入MainClass");
        }
    }
    
}
