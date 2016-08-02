/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ery.base.support.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.SortedMap;

import com.ery.base.support.log4j.LogUtils;



public class MapUtils {
    
    
    private MapUtils() {
    }    
    
    // Type safe getters
    //-------------------------------------------------------------------------
    
    public static Object getObject(final Map<?,?> map, final Object key) {
        if (map != null) {
            return map.get(key);
        }
        return null;
    }

    
    public static String getString(final Map<?,?> map, final Object key) {
        if (map != null) {
            Object answer = map.get(key);
            if (answer != null) {
                return answer.toString();
            }
        }
        return null;
    }

    
    public static Boolean getBoolean(final Map<?,?> map, final Object key) {
        if (map != null) {
            Object answer = map.get(key);
            if (answer != null) {
                if (answer instanceof Boolean) {
                    return (Boolean) answer;
                    
                } else if (answer instanceof String) {
                    return new Boolean((String) answer);
                    
                } else if (answer instanceof Number) {
                    Number n = (Number) answer;
                    return (n.intValue() != 0) ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
        return null;
    }

    
    public static Number getNumber(final Map<?,?> map, final Object key) {
        if (map != null) {
            Object answer = map.get(key);
            if (answer != null) {
                if (answer instanceof Number) {
                    return (Number) answer;
                    
                } else if (answer instanceof String) {
                    try {
                        String text = (String) answer;
                        return NumberFormat.getInstance().parse(text);
                    } catch (ParseException e) {
                    	LogUtils.error("Number parse Error!",e);
                    }
                }
            }
        }
        return null;
    }

    
    public static Byte getByte(final Map<?,?> map, final Object key) {
        Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        } else if (answer instanceof Byte) {
            return (Byte) answer;
        }
        return new Byte(answer.byteValue());
    }

    
    public static Short getShort(final Map<?,?> map, final Object key) {
        Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        } else if (answer instanceof Short) {
            return (Short) answer;
        }
        return new Short(answer.shortValue());
    }

    
    public static Integer getInteger(final Map<?,?> map, final Object key) {
        Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        } else if (answer instanceof Integer) {
            return (Integer) answer;
        }
        return new Integer(answer.intValue());
    }

    
    public static Long getLong(final Map<?,?> map, final Object key) {
        Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        } else if (answer instanceof Long) {
            return (Long) answer;
        }
        return new Long(answer.longValue());
    }

    
    public static Float getFloat(final Map<?,?> map, final Object key) {
        Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        } else if (answer instanceof Float) {
            return (Float) answer;
        }
        return new Float(answer.floatValue());
    }

    
    public static Double getDouble(final Map<?,?> map, final Object key) {
        Number answer = getNumber(map, key);
        if (answer == null) {
            return null;
        } else if (answer instanceof Double) {
            return (Double) answer;
        }
        return new Double(answer.doubleValue());
    }

    
    public static Map<?,?>  getMap(final Map<?,?> map, final Object key) {
        if (map != null) {
            Object answer = map.get(key);
            if (answer != null && answer instanceof Map<?,?>) {
                return (Map<?,?>) answer;
            }
        }
        return null;
    }

    // Type safe getters with default values
    //-------------------------------------------------------------------------
    
    public static Object getObject( Map<?,?> map, Object key, Object defaultValue ) {
        if ( map != null ) {
            Object answer = map.get( key );
            if ( answer != null ) {
                return answer;
            }
        }
        return defaultValue;
    }

    
    public static String getString( Map<?,?> map, Object key, String defaultValue ) {
        String answer = getString( map, key );
        if ( answer == null ) {
            answer = defaultValue;
        }
        return answer;
    }

    
    public static Boolean getBoolean( Map<?,?> map, Object key, Boolean defaultValue ) {
        Boolean answer = getBoolean( map, key );
        if ( answer == null ) {
            answer = defaultValue;
        }
        return answer;
    }

    
    public static Number getNumber( Map<?,?> map, Object key, Number defaultValue ) {
    	if (map != null) {
            Object answer = map.get(key);
            if (answer != null) {
                if (answer instanceof Number) {
                    return (Number) answer;
                    
                } else if (answer instanceof String) {
                    try {
                        String text = (String) answer;
                        return NumberFormat.getInstance().parse(text);
                    } catch (ParseException e) {
                        LogUtils.debug("Number parse Error! Use the default value.");
                    }
                }
            }
        }
    	return defaultValue;
    }

    
    public static Byte getByte( Map<?,?> map, Object key, Byte defaultValue ) {
        return getNumber(map, key, defaultValue).byteValue();
    }

    
    public static Short getShort( Map<?,?> map, Object key, Short defaultValue ) {
    	return getNumber(map, key, defaultValue).shortValue();
    }

    
    public static Integer getInteger( Map<?,?> map, Object key, Integer defaultValue ) {
    	return getNumber(map, key, defaultValue).intValue();
    }

    
    public static Long getLong( Map<?,?> map, Object key, Long defaultValue ) {
    	return getNumber(map, key, defaultValue).longValue();
    }

    
    public static Float getFloat( Map<?,?> map, Object key, Float defaultValue ) {
    	return getNumber(map, key, defaultValue).floatValue();
    }

    
    public static Double getDouble( Map<?,?> map, Object key, Double defaultValue ) {
    	return getNumber(map, key, defaultValue).doubleValue();
    }

    
    public static Map<?,?>  getMap( Map<?,?> map, Object key, Map<?,?> defaultValue ) {
        Map<?,?>  answer = getMap( map, key );
        if ( answer == null ) {
            answer = defaultValue;
        }
        return answer;
    }
    

    // Type safe primitive getters
    //-------------------------------------------------------------------------
    
    public static boolean getBooleanValue(final Map<?,?> map, final Object key) {
        Boolean booleanObject = getBoolean(map, key);
        if (booleanObject == null) {
            return false;
        }
        return booleanObject.booleanValue();
    }

    
    public static byte getByteValue(final Map<?,?> map, final Object key) {
        Byte byteObject = getByte(map, key);
        if (byteObject == null) {
            return 0;
        }
        return byteObject.byteValue();
    }

    
    public static short getShortValue(final Map<?,?> map, final Object key) {
        Short shortObject = getShort(map, key);
        if (shortObject == null) {
            return 0;
        }
        return shortObject.shortValue();
    }

    
    public static int getIntValue(final Map<?,?> map, final Object key) {
        Integer integerObject = getInteger(map, key);
        if (integerObject == null) {
            return 0;
        }
        return integerObject.intValue();
    }

    
    public static long getLongValue(final Map<?,?> map, final Object key) {
        Long longObject = getLong(map, key);
        if (longObject == null) {
            return 0L;
        }
        return longObject.longValue();
    }

    
    public static float getFloatValue(final Map<?,?> map, final Object key) {
        Float floatObject = getFloat(map, key);
        if (floatObject == null) {
            return 0f;
        }
        return floatObject.floatValue();
    }

    
    public static double getDoubleValue(final Map<?,?> map, final Object key) {
        Double doubleObject = getDouble(map, key);
        if (doubleObject == null) {
            return 0d;
        }
        return doubleObject.doubleValue();
    }

    // Type safe primitive getters with default values
    //-------------------------------------------------------------------------
    
    public static boolean getBooleanValue(final Map<?,?> map, final Object key, boolean defaultValue) {
        Boolean booleanObject = getBoolean(map, key);
        if (booleanObject == null) {
            return defaultValue;
        }
        return booleanObject.booleanValue();
    }

    
    public static byte getByteValue(final Map<?,?> map, final Object key, byte defaultValue) {
    	return getNumber(map,key,defaultValue).byteValue();
    }

    
    public static short getShortValue(final Map<?,?> map, final Object key, short defaultValue) {
    	return getNumber(map,key,defaultValue).shortValue();
    }

    
    public static int getIntValue(final Map<?,?> map, final Object key, int defaultValue) {
    	return getNumber(map,key,defaultValue).intValue();
    }

    
    public static long getLongValue(final Map<?,?> map, final Object key, long defaultValue) {
    	return getNumber(map,key,defaultValue).longValue();
    }

    
    public static float getFloatValue(final Map<?,?> map, final Object key, float defaultValue) {
    	return getNumber(map,key,defaultValue).floatValue();
    }

    
    public static double getDoubleValue(final Map<?,?> map, final Object key, double defaultValue) {
        return getNumber(map,key,defaultValue).doubleValue();
    }

    //-----------------------------------------------------------------------
    
    public static boolean isEmpty(Map<?,?> map) {
        return (map == null || map.isEmpty());
    }

    
    public static boolean isNotEmpty(Map<?,?> map) {
        return !MapUtils.isEmpty(map);
    }
    
    
    public static String[] getStringArray(Map<?,?> map,Object key,String separator){
    	String[] array = null;
    	if(map!=null&&map.containsValue(key)&&getString(map, key)!=null){
    		String str = getString(map, key);
    		array = str.split(separator);
    	}
    	return array;
    }
    
    
    public static String[] getStringArray(Map<?,?> map,Object key){
    	return getStringArray(map,key,",");
    }
}
