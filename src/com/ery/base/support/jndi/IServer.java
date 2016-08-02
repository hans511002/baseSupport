package com.ery.base.support.jndi;

import javax.naming.NamingException;


public interface IServer {
	
	public void lookup(String namespace,ILookupHandler handler) throws NamingException;
}
