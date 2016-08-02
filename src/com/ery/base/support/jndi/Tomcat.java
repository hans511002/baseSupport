package com.ery.base.support.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;


public class Tomcat implements IServer{

	/* (non-Javadoc)
	 * @see tydic.frame.jndi.IServer#lookup(java.lang.String, tydic.frame.jndi.ILookupHandler)
	 */
	public void lookup(String namespace, ILookupHandler handler) throws NamingException{
		Context context = new InitialContext();
		Context envContext = (Context) context.lookup("java:/comp/env");
	    NamingEnumeration<NameClassPair> enumeration = envContext.list(namespace);
	    while (enumeration.hasMore()) {
	        NameClassPair pair = enumeration.next();
	        String name = pair.getName();
	        DataSource dataSource = (DataSource) envContext.lookup(namespace + "/" + name);
	        handler.handle(name, dataSource);
	    }
	    
	}

}
