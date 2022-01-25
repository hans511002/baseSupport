package com.sobey.jcg.support.jdbc;

import java.sql.ResultSet;


public interface IRowHandler {

	
	public void handle(ResultSet resultset);
}
