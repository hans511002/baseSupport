
package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


public class MapListMapper extends AbstractListMapper<Map<String,Object>> {

	public MapListMapper() {
		super(null, new MapColumnMapper());
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractListMapper#convertRow(java.sql.ResultSet)
	 */
	@Override
	public Map<String,Object> convertRow(ResultSet rs) throws SQLException {
		return mapper.convertRow(rs);
	}

}
