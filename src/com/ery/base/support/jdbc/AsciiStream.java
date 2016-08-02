package com.ery.base.support.jdbc;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class AsciiStream implements IDBStreamParameter {
	
	private InputStream inputStream;
	
	
	public AsciiStream(){}
	
	
	
	public AsciiStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	
	public InputStream getInputStream() {
		return this.inputStream;
	}
	
	
	public void setParameter(PreparedStatement preparedStatement,int parameterIndex) throws SQLException {
		preparedStatement.setAsciiStream(parameterIndex,inputStream);
	}
}
