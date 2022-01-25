
package com.sobey.jcg.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class PrimitiveArrayMapper<T> extends AbstractArrayMapper<T> {

	
	public PrimitiveArrayMapper(Class<T> clazz) {
		super(clazz, new PrimitiveMapper<T>(clazz));
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractArrayMapper#convertRow(java.sql.ResultSet)
	 */
	@Override
	protected T convertRow(ResultSet rs) throws SQLException {
		return mapper.convertRow(rs);
	}

}
