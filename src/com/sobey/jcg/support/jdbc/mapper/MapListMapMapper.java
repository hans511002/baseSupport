package com.sobey.jcg.support.jdbc.mapper;

import com.sobey.jcg.support.jdbc.JdbcException;
import com.sobey.jcg.support.utils.Convert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapListMapMapper extends AbstractRowMapper<Map<String, List<Map<String, Object>>>> {

    String columnNameKey = "";

    public MapListMapMapper(String columnNameKey) {
        super(null);
        this.columnNameKey = columnNameKey;
    }

    @Override
    public Map<String, List<Map<String, Object>>> convert(ResultSet rs) {
        Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
        try {
            initColMeta(rs);
            while (rs.next()) {
                Map<String, Object> record = new HashMap<String, Object>();
                for (int i=0;i<columnHeaders.length;i++) {
                    record.put(columnHeaders[i], cellCvt.cvt(rs.getObject(i+1),columnHeaders[i],i+1));
                }
                String key = Convert.toString(record.get(columnNameKey), "");
                List<Map<String, Object>> list = map.get(key);
                if (list == null) {
                    list = new ArrayList<Map<String, Object>>();
                }
                list.add(record);
                map.put(key, list);
            }
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
        return map;
    }

}
