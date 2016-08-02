package com.ery.base.support.jndi;

import javax.sql.DataSource;


public interface ILookupHandler {
	public void handle(String name,DataSource dataSource);
}
