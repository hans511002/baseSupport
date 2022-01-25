package com.sobey.jcg.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.sobey.jcg.support.jdbc.JdbcException;
import com.sobey.jcg.support.log4j.LogUtils;


public class MapMapMapper extends AbstractMutilRowMapper<Map<String,Map<String,Object>>, Map<String,Object>> {
	
	private String keyColName;
	
	
	public MapMapMapper(String keyColName) {
		super(null);
		this.keyColName = keyColName.toUpperCase();
        if(!keyColName.equals(this.keyColName))
            LogUtils.warn("传入的keyColName有小写字符,已被转换,可正确使用");
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.IRowMapper#convert(java.sql.ResultSet)
	 */
	public Map<String,Map<String,Object>> convert(ResultSet rs) {
		Map<String,Map<String,Object>> map = new HashMap<String, Map<String,Object>>();
		try {
			initColMeta(rs);
			while(rs.next()){
				Map<String,Object> record = new HashMap<String,Object>();
				for(int i=0;i<columnHeaders.length;i++){
					record.put(columnHeaders[i], cellCvt.cvt( rs.getObject(i+1),columnHeaders[i],i+1));
				}
				String key = rs.getString(keyColName);
				if(map.containsKey(key)){
					throw new JdbcException("被用作键值的列有重复值");
				}else{
					map.put(rs.getString(keyColName), record);
				}
			}
		} catch (SQLException e) {
            throw new JdbcException(e);
		}
		return map;
	}

}
