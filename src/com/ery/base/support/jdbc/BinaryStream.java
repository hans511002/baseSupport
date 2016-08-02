package com.ery.base.support.jdbc;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class BinaryStream implements IDBStreamParameter {
	
	private InputStream inputStream;
	
	private int length=-1;
	
	
	public BinaryStream(){}
	
	
	public BinaryStream(InputStream inputStream,int length) {
		this.inputStream = inputStream;
		this.length = length;
	}
	
	
	public BinaryStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	
	public void setInputStream(InputStream inputStream,int length) {
		this.inputStream = inputStream;
		this.length = length;
	}
	
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	
	public InputStream getInputStream() {
		return this.inputStream;
	}
	
	
	public void setParameter(PreparedStatement preparedStatement,int parameterIndex) throws SQLException {
		if(length!=-1){
			preparedStatement.setBinaryStream(parameterIndex, inputStream,length);
		}else{
			preparedStatement.setBinaryStream(parameterIndex, inputStream,Integer.MAX_VALUE);
		}
	}

}
