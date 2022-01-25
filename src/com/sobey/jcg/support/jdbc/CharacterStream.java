package com.sobey.jcg.support.jdbc;

import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class CharacterStream implements IDBStreamParameter {
	
	
	private Reader reader;
	
	
	public CharacterStream(){}
	
	
	public CharacterStream(Reader reader){
		this.reader = reader;
	}
	
	
	public Reader getReader() {
		return reader;
	}
	
	
	public void setReader(Reader reader) {
		this.reader = reader;
	}
	
	
	public void setParameter(PreparedStatement preparedStatement,int parameterIndex) throws SQLException {
		preparedStatement.setCharacterStream(parameterIndex, reader);
	}
}
