package com.sobey.jcg.support.jdbc;

import java.sql.ResultSet;


public interface IRowMapper<T> {

	
	public T convert(ResultSet resultset);
}
