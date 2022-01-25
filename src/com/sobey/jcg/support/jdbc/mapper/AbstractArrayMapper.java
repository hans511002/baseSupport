package com.sobey.jcg.support.jdbc.mapper;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sobey.jcg.support.jdbc.JdbcException;

public abstract class AbstractArrayMapper<B> extends AbstractMutilRowMapper<B[], B> {

	protected AbstractSingleRowMapper<B> mapper;

	public AbstractArrayMapper(Class<B> clazz, AbstractSingleRowMapper<B> mapper) {
		super(clazz);
		this.mapper = mapper;
	}

	@SuppressWarnings("unchecked")
	public B[] convert(ResultSet rs) {
		B[] array = null;
		try {
			initColMeta(rs);
			if (mapper != null) {
				mapper.setColumnHeaders(getColumnHeaders());
			}
			rs.last();
			int rowsCount = rs.getRow();
			rs.beforeFirst();
			array = (B[]) Array.newInstance(clazz, rowsCount);
			int i = 0;
			while (rs.next()) {
				array[i] = convertRow(rs);
				i++;
			}
		} catch (SQLException e) {
			throw new JdbcException(e);
		}
		return array;
	}

	protected abstract B convertRow(ResultSet rs) throws SQLException;
}
