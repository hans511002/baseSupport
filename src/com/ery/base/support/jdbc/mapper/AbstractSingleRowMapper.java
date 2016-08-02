
package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ery.base.support.jdbc.JdbcException;


public abstract class AbstractSingleRowMapper<T> extends AbstractRowMapper<T> {
	protected Class<T> clazz;
	
	public AbstractSingleRowMapper(Class<T> clazz) {
		super(clazz);
		this.clazz = clazz;
	}

	public final T convert(ResultSet rs) {
		T t = null;
		try {
            initColMeta(rs); 
			if(rs.next()){
				t = convertRow(rs);
			}
		} catch (SQLException e) {
			throw new JdbcException(e);
		}
		return t;
	}
	
	public abstract T convertRow(ResultSet rs) throws SQLException;
}
