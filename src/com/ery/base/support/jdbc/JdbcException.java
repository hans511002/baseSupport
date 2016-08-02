package com.ery.base.support.jdbc;

import java.sql.SQLException;


public class JdbcException extends RuntimeException {
	
	private Exception baseException;

	
	private static final long serialVersionUID = 8303668503481795345L;

	public JdbcException(Exception e) {
		super(e.getMessage());
		if(e instanceof JdbcException){
			JdbcException je = (JdbcException)e;
			baseException = je.getException();
		}else{
			baseException = e;
		}
		
	}
	
	public JdbcException(String message) {
		super(message);
		baseException = this;
	}
	
	
	public Exception getException(){
		return baseException;
	}
	
	
	public boolean isSQLException(){
		return baseException==null?false:baseException instanceof SQLException;
	}
}
