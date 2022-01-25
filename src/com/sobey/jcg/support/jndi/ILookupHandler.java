package com.sobey.jcg.support.jndi;

import javax.sql.DataSource;


public interface ILookupHandler {
	public void handle(String name,DataSource dataSource);
}
