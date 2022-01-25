package com.sobey.jcg.support.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public interface IParamsSetter {
	
	public void setValues(PreparedStatement preparedStatement, int i) throws SQLException;
	
	
	public int batchSize();
}
