package com.ery.base.support.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public interface IDBParameter {
	
	
	public void setParameter(PreparedStatement preparedStatement, int parameterIndex) throws SQLException;
}
