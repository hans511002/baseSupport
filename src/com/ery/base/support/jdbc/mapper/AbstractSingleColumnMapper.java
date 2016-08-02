
package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class AbstractSingleColumnMapper<T> extends AbstractSingleRowMapper<T>{

	
	public AbstractSingleColumnMapper(Class<T> clazz) {
		super(clazz);
	}

	public T convertRow(ResultSet rs) throws SQLException {
	 	return convert(cellCvt.cvt(rs.getObject(columnHeaders[0]),columnHeaders[0],1));
	}
	
	public abstract T convert(Object obj);
}
