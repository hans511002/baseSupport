package com.ery.base.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



public class MapColumnMapper extends AbstractMutilColumnMapper<Map<String,Object>> {

	public MapColumnMapper() {
		super();
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractMutilColumnMapper#convert(java.sql.ResultSet)
	 */
	@Override
	public Map<String,Object> convertToObject(ResultSet rs) throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();
        for(int i=0;i<columnHeaders.length;i++){
            map.put(columnHeaders[i], cellCvt.cvt(rs.getObject(i+1),columnHeaders[i],i+1));
        }
		return map;
	}
}
