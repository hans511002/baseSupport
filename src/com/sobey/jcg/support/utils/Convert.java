package com.sobey.jcg.support.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Convert {

    public final static String DATE_DEFAULT_FMT = "yyyy-MM-dd HH:mm:ss";//时间默认格式
    
    public static short toShort(byte[] buf){
        if (buf == null){
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 2){
            throw new IllegalArgumentException("byte array size > 2 !");
        }
        return Bytes.toShort(buf);
    }

    public static int toInt(byte[] buf){
        if (buf == null){
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 4){
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        return Bytes.toInt(buf);
    }

    public static long toLong(byte[] buf){
        if (buf == null){
            throw new IllegalArgumentException("byte array is null!");
        }
        if (buf.length > 8){
            throw new IllegalArgumentException("byte array size > 8 !");
        }
        return Bytes.toLong(buf);
    }

    public static double toDouble(byte[] b){
        if (b == null){
            throw new IllegalArgumentException("byte array is null!");
        }
        if (b.length > 8){
            throw new IllegalArgumentException("byte array size > 8 !");
        }
        return Bytes.toDouble(b);
    }

    
    public static byte[] toBytes(Number num){
        if(num instanceof Short){
            return Bytes.toBytes(num.shortValue());
        }else if(num instanceof Integer){
            return Bytes.toBytes(num.intValue());
        }else if(num instanceof Long){
            return Bytes.toBytes(num.longValue());
        }else if(num instanceof Double){
            return Bytes.toBytes(num.doubleValue());
        }else{
            return null;
        }
    }

    
    public static String toHexString(byte[] src){
        if (src == null || src.length <= 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    
    public static byte[] toBytesForHexString(String str){
        if(str==null || "".equals(str)){
            return null;
        }
        str = str.toUpperCase();
        int length = str.length() / 2;
        char[] hexChars = str.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    
    public static double toDouble(Object value){
        return Double.parseDouble(value.toString().trim());
    }

    
    public static double toDouble(Object value,double defaultValue){
        try{
            return Double.parseDouble(value.toString().trim());
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static float toFloat(Object value){
        return Float.parseFloat(value.toString().trim());
    }

    
    public static float toFloat(Object value,float defaultValue){
        try{
            return Float.parseFloat(value.toString().trim());
        }catch(Exception e){
            return defaultValue;
        }
    }


    
    public static int toInt(Object value){
        return Integer.parseInt(value.toString().trim());
    }

    
    public static int toInt(Object value,int defaultValue){
        try{
            return Integer.parseInt(value.toString().trim());
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static int toInt(int radix,Object value,int defaultValue){
        try{
            return Integer.parseInt(value.toString().trim(),radix);
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static int toInt(int radix,Object value){
        return Integer.parseInt(value.toString().trim(),radix);
    }

    
    public static boolean toBool(Object value){
        return Boolean.parseBoolean(value.toString().trim());
    }

    
    public static boolean toBool(Object value,boolean defaultValue){
        try{
            return Boolean.parseBoolean(value.toString().trim());
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static long toLong(Object value){
        return Long.parseLong(value.toString().trim());
    }

    
    public static long toLong(Object value,long defaultValue){
        try{
            return Long.parseLong(value.toString().trim());
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static short toShort(Object value){
        return Short.parseShort(value.toString().trim());
    }

    
    public static short toShort(Object value,short defaultValue){
        try{
            return Short.parseShort(value.toString().trim());
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static byte toByte(Object value){
        return Byte.parseByte(value.toString().trim());
    }

    
    public static byte toByte(Object value,byte defaultValue){
        try{
            return Byte.parseByte(value.toString().trim());
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static char toChar(Object value){
        return Character.class.cast(value);
    }

    
    public static char toChar(Object value,char defaultValue){
        try{
            return Character.class.cast(value);
        }catch(Exception e){
            return defaultValue;
        }
    }

    
    public static String toString(Object value){
        return value == null ? "" : value.toString();
    }

    
    public static String toString(Object value,String defaultValue){
        return value == null ? defaultValue : value.toString();
    }

    
    public static Date toTime(String str,String fmt){
        try{
            Date d = null;
            if(fmt.contains("MMM") || fmt.contains("EEE") || fmt.contains("Z")){
                DateFormat df = new SimpleDateFormat(fmt, Locale.ENGLISH);
                d = df.parse(str);
            }else{
                DateFormat df = new SimpleDateFormat(fmt);
                d = df.parse(str);
            }
            if(!fmt.contains("yyyy") && !fmt.contains("yy")){
                Date currentDate = new Date();
                d.setYear(currentDate.getYear());
                //时间还如未到来，取去年
                if(d.getTime()>currentDate.getTime()){
                    d.setYear(currentDate.getYear()-1);
                }
            }
            return d;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    
    public static String toTimeStr(Date date,String fmt){
        try{
            if(fmt==null){
                fmt = DATE_DEFAULT_FMT;
            }
            if(fmt.contains("MMM") || fmt.contains("EEE") || fmt.contains("Z")){
                //包含英文月，周,时区（GMT=格林尼治标准时间，CST是在GMT上有所偏移,同时代表美国-6h，澳大利亚+9.5h，中国+8h，古巴-4h的标准时间）
                DateFormat df = new SimpleDateFormat(fmt,Locale.ENGLISH);
                return df.format(date);
            }else{
                DateFormat df = new SimpleDateFormat(fmt);
                return df.format(date);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object,Class<T> clazz){
        T t = null;
        if(object==null){

        }else if(clazz==int.class){
            t = (T)Integer.class.cast(Convert.toInt(object));
        }else if(clazz==long.class){
            t = (T)Long.class.cast(Convert.toLong(object));
        }else if(clazz==boolean.class){
            t = (T)Boolean.class.cast(Convert.toBool(object));
        }else if(clazz==double.class){
            t = (T)Double.class.cast(Convert.toDouble(object));
        }else if(clazz==float.class){
            t = (T)Float.class.cast(Convert.toFloat(object));
        }else if(clazz==short.class){
            t = (T)Short.class.cast(Convert.toShort(object));
        }else if(clazz==byte.class){
            t = (T)Byte.class.cast(Convert.toByte(object));
        }else if(clazz==char.class){
            t = (T)Character.class.cast(Convert.toChar(object));
        }else if(clazz==Integer.class){
            t = clazz.cast(Convert.toInt(object));
        }else if(clazz==Long.class){
            t = clazz.cast(Convert.toLong(object));
        }else if(clazz==Boolean.class){
            t = clazz.cast(Convert.toBool(object));
        }else if(clazz==Double.class){
            t = clazz.cast(Convert.toDouble(object));
        }else if(clazz==Float.class){
            t = clazz.cast(Convert.toFloat(object));
        }else if(clazz==Short.class){
            t = clazz.cast(Convert.toShort(object));
        }else if(clazz==Byte.class){
            t = clazz.cast(Convert.toByte(object));
        }else if(clazz==Character.class){
            t = clazz.cast(Convert.toChar(object));
        }else if(clazz == String.class){
            t = clazz.cast(Convert.toString(object));
        }else{
            t = clazz.cast(object);
        }
        return t;
    }

    
    public static <T> T clone(Object obj,Class<T> clazz) {
        try{
            //序列化【效率是比较慢的。可用其他json实现，此处未使用，是避免依赖】
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out= new ObjectOutputStream(byteOut);
            out.writeObject(obj);

            //反序列化
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in =new ObjectInputStream(byteIn);
            return clazz.cast(in.readObject());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
