package com.sobey.jcg.support.web;

import javax.servlet.ServletContext;


public interface ISystemStart{

    
    public void setServletContext(ServletContext servletContext);

    
    public void init();

    
    public void destory();

}
