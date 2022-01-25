package com.sobey.jcg.support.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DBOutParameter implements IDBParameter {
	
	
	private int sqlType;
	
	
	public DBOutParameter(){};
	
	
	public DBOutParameter(int sqlType){
		this.sqlType = sqlType;
	}
	
	
	public int getSqlType() {
		return sqlType;
	}
	
	
	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}
	
	
	public void setParameter(PreparedStatement preparedStatement,int parameterIndex) throws SQLException {
		if(preparedStatement instanceof CallableStatement){
			((CallableStatement)preparedStatement).registerOutParameter(parameterIndex,sqlType);
		}else{
			throw new JdbcException("参数错误,输出参数必须在CallableStatement类中才能注册");
		}
	}
}
