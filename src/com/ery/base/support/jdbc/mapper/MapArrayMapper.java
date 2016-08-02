
package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class MapArrayMapper extends AbstractArrayMapper<Map<String,Object>> {
	
	@SuppressWarnings("unchecked")
	private static Class initClazz = (new HashMap<String,Object>()).getClass();

	public MapArrayMapper() {
		super(initClazz,new MapColumnMapper());
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractArrayMapper#convertRow(java.sql.ResultSet)
	 */
	@Override
	protected Map<String,Object> convertRow(ResultSet rs) throws SQLException {
        return mapper.convertRow(rs);
    }

}
