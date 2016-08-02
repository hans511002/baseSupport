
package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class BeanArrayMapper<T> extends AbstractArrayMapper<T> {
	
	
	public BeanArrayMapper(Class<T> clazz) {
		super(clazz,new BeanMapper<T>(clazz));
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractArrayMapper#convertRow(java.sql.ResultSet)
	 */
	@Override 
	protected T convertRow(ResultSet rs) throws SQLException {
        return mapper.convertRow(rs);
    }
	
}
