package com.ery.base.support.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.ery.base.support.sys.SystemVariable;



public class Weblogic implements IServer {

	/* (non-Javadoc)
	 * @see tydic.frame.jndi.IServer#lookup(java.lang.String, tydic.frame.jndi.ILookupHandler)
	 */
	public void lookup(String namespace, ILookupHandler handler) throws NamingException {
		Hashtable<String, String> envTable = new Hashtable<String, String>(); 
		envTable.put(Context.INITIAL_CONTEXT_FACTORY, SystemVariable.getString("weblogic.jndi.context.factory","weblogic.jndi.WLInitialContextFactory")); 
		envTable.put(Context.PROVIDER_URL, SystemVariable.getString("weblogic.jndi.provider.url")); 
		Context context = new InitialContext(envTable);
		NamingEnumeration<NameClassPair> enumeration = context.list(namespace);
	    while (enumeration.hasMore()) {
	        NameClassPair pair = enumeration.next();
	        String name = pair.getName();
	        DataSource dataSource = (DataSource) context.lookup(namespace + "/" + name);
	        handler.handle(name, dataSource);
	    }
	}

}
