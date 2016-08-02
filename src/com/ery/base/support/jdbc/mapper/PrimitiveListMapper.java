
package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class PrimitiveListMapper<T> extends AbstractListMapper<T> {

	
	public PrimitiveListMapper(Class<T> clazz) {
		super(clazz,new PrimitiveMapper<T>(clazz));
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractListMapper#convertRow(java.sql.ResultSet)
	 */
	@Override
	public T convertRow(ResultSet rs) throws SQLException {
		return mapper.convertRow(rs);
	}

}
