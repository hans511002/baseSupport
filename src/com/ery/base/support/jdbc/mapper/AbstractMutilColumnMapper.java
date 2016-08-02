
package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class AbstractMutilColumnMapper<T> extends AbstractSingleRowMapper<T> {
	public AbstractMutilColumnMapper() {
		super(null);
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractSingleRowMapper#convertRow(java.sql.ResultSet)
	 */
	@Override
	public final T convertRow(ResultSet rs) throws SQLException {
		return convertToObject(rs);
	}
	
	public abstract T convertToObject(ResultSet rs) throws SQLException;

}
