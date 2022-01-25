package com.sobey.jcg.support.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArrayMapper extends AbstractArrayMapper<Object[]> {
	protected boolean isColsInited = false;

	public ArrayMapper() {
		super(Object[].class, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tydic.frame.jdbc.mapper.AbstractArrayMapper#convertRow(java.sql.ResultSet)
	 */
	@Override
	protected Object[] convertRow(ResultSet rs) throws SQLException {
		Object[] objects = new Object[columnHeaders.length];
		for (int i = 0, length = columnHeaders.length; i < length; i++) {
			objects[i] = cellCvt.cvt(rs.getObject(i + 1), columnHeaders[i], i + 1);
		}

		return objects;
	}
}
