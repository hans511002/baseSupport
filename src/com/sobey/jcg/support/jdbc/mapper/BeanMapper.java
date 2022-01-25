package com.sobey.jcg.support.jdbc.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.sobey.jcg.support.jdbc.Column;
import com.sobey.jcg.support.jdbc.JdbcException;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.jcg.support.utils.StringUtils;


public class BeanMapper<T> extends AbstractMutilColumnMapper<T> {
	
	private Map<String,Method> colFields = new HashMap<String,Method>();
    private Map<String,Integer> colFieldIdx = new HashMap<String, Integer>();
	private Class<Column> columnClazz = Column.class;
	private Class<T> clazz = null;
	private boolean isColsInited = false;
	
	public BeanMapper(Class<T> clazz){
		super();
		this.clazz = clazz;
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			try {
				String fieldName = field.getName();
				String methodName = StringUtils.join(new String[] { "set", String.valueOf(fieldName.charAt(0)).toUpperCase(),
						StringUtils.substring(fieldName, 1) });
				Method method = getMethod(methodName, field.getType());
				if (method != null) {
					String columnName = null;
					if (field.isAnnotationPresent(columnClazz)) {
						columnName = field.getAnnotation(columnClazz).value().toUpperCase();
					} else {
						columnName = field.getName().toUpperCase();
					}
					colFields.put(columnName, method);
				}
			} catch (Exception e) {
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see tydic.frame.jdbc.mapper.AbstractMutilColumnMapper#convert(java.sql.ResultSet)
	 */
	public T convertToObject(ResultSet rs) throws SQLException {
		T bean = getNewInstance();
        initCols(rs);
        for(int i=0;i<columnHeaders.length;i++){
        	if(colFields.containsKey(columnHeaders[i])){
        		invokeMethod(bean, colFields.get(columnHeaders[i]), cellCvt.cvt( rs.getObject(i+1),columnHeaders[i],i+1));
        	}
        }
		return bean;
	}
	
	private void initCols(ResultSet rs) throws SQLException {
		if(!isColsInited){
			int colCount = (columnHeaders==null?0:columnHeaders.length);
			Map<String,Method> temp = new HashMap<String,Method>(colFields);
			colFields.clear();
			for(int i=0;i<colCount;i++){
				String columnName = columnHeaders[i];
				if(temp.containsKey(columnName)){
					colFields.put(columnName, temp.get(columnName));
                    colFieldIdx.put(columnName,i+1);
				}
			}
			isColsInited = true;
		}
	}
	
	private T getNewInstance(){
		T obj = null;
		try {
			obj = clazz.newInstance();
		} catch (Exception e) {
			throw new JdbcException(e);
		}
		return obj;
	}
	
	private Method getMethod(String methodName,Class<?> argType){
		Method method  = null;
		try {
			method = clazz.getMethod(methodName, argType);
		} catch (Exception e) {
			throw new JdbcException(e);
		}
		return method;
	}
	
	private void invokeMethod(Object obj,Method method,Object arg){
		try {
			if(method.getParameterTypes().length==1){
				Class<?> clazz = method.getParameterTypes()[0];
				if(clazz.equals(int.class)){
					arg = arg==null?0: Convert.toInt(arg);
				}else if(clazz.equals(long.class)){
					arg = arg==null?0:Convert.toLong(arg);
				}else if(clazz.equals(double.class)){
					arg = arg==null?0:Convert.toDouble(arg);
				}else if(clazz.equals(float.class)){
					arg = arg==null?0:Convert.toFloat(arg);
				}else if(clazz.equals(short.class)){
					arg = arg==null?0:Convert.toShort(arg);
				}else if(clazz.equals(char.class)){
					arg = arg==null?0:Convert.toChar(arg);
				}else if(clazz.equals(byte.class)){
					arg = arg==null?0:Convert.toByte(arg);
				}else if(clazz.equals(boolean.class)){
					arg = arg==null?0:Convert.toBool(arg);
				}else{
					arg = arg==null?null:Convert.convert(arg, clazz);
				}
				method.invoke(obj,arg);
			}
		} catch (Exception e) {
			throw new JdbcException(e);
		}
	}
}
