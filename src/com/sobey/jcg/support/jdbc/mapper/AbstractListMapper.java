
package com.sobey.jcg.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sobey.jcg.support.jdbc.JdbcException;


public abstract class AbstractListMapper<T> extends AbstractMutilRowMapper<List<T>,T> {
	
	protected AbstractSingleRowMapper<T> mapper;
	
	
	public AbstractListMapper(Class<T> clazz,AbstractSingleRowMapper<T> mapper) {
		super(clazz);
		this.mapper = mapper;
	}

	public List<T> convert(ResultSet rs){
		List<T> list = new ArrayList<T>();
		try {
            initColMeta(rs);
            mapper.setColumnHeaders(getColumnHeaders());
			while(rs.next()){
				list.add(convertRow(rs));
			}
		} catch (SQLException e) {
			throw new JdbcException(e);
		}
		return list;
	}
	
	public abstract T convertRow(ResultSet rs) throws SQLException;
}
