package com.ery.base.support.jdbc.mapper;


public abstract class AbstractMutilRowMapper<A,B> extends AbstractRowMapper<A> {

	
	public AbstractMutilRowMapper(Class<B> clazz) {
		super(clazz); 
	}
	
}
