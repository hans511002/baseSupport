
package com.sobey.jcg.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class BeanListMapper<T> extends AbstractListMapper<T> {
	
	public BeanListMapper(Class<T> clazz) {
		super(clazz,new BeanMapper<T>(clazz));
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractListMapper#convertRow(java.sql.ResultSet)
	 */
	@Override
	public T convertRow(ResultSet rs) throws SQLException {
        return mapper.convertRow(rs);
    }

}
