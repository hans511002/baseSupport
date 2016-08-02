
package com.ery.base.support.jdbc.mapper;

import com.ery.base.support.utils.Convert;


public class PrimitiveMapper<T> extends AbstractSingleColumnMapper<T>{
	private T defaultValue = null;
	
	public PrimitiveMapper(Class<T> clazz) {
		super(clazz);
	}
	
	public PrimitiveMapper(Class<T> clazz,T defaultValue) {
		super(clazz);
		this.defaultValue = defaultValue;
	}

	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractSingleColumnMapper#convert(java.lang.Object)
	 */
	@Override
	public T convert(Object object) {
		T t = null;
		if(object==null){
			t = defaultValue;
		}else{
			t = Convert.convert(object, clazz);
		}
		return t;
	}
	
}
