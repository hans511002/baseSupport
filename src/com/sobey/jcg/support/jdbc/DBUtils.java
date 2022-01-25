package com.sobey.jcg.support.jdbc;

import com.sobey.jcg.support.utils.RemotingUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


public class DBUtils {

    private final static long TIME_LONG = 10000000000000l;
    private static String ipLastStr ;
    private static Map<String,AtomicLong> primaryKeyMap = new HashMap<String, AtomicLong>();
    static {
        ipLastStr = RemotingUtils.getLocalAddress().split("\\.")[3];
    }
    
    public static void initKey(String key,long id){
        primaryKeyMap.put(key,new AtomicLong(id));
    }

    
    public static void initKeyForIP(String key,long lastMaxId){
        if(lastMaxId<TIME_LONG && (lastMaxId+"").length()==13){
            long id = lastMaxId ;
            id += Long.parseLong(ipLastStr) * TIME_LONG;
            primaryKeyMap.put(key,new AtomicLong(id));

        }else{
            throw new IllegalArgumentException("lastMaxId 不是一个时间戳值,他的值应该是一个13位数长整形!");
        }
    }

    
    public static long getPrimaryKeyID(String key){
        AtomicLong idKey = primaryKeyMap.get(key);
        if(idKey==null){
            long id = Long.parseLong(ipLastStr+System.currentTimeMillis());
            idKey = new AtomicLong(id);
            primaryKeyMap.put(key,idKey);
        }
        return idKey.incrementAndGet();
    }

    
    public static long getMultiplePrimaryKeyID(String key,int multiple){
        if(multiple<1)multiple = 1;//保证不会因为参数问题将key重置
        AtomicLong idKey = primaryKeyMap.get(key);
        if(idKey==null){
            long id = Long.parseLong(ipLastStr+System.currentTimeMillis());
            idKey = new AtomicLong(id);
            primaryKeyMap.put(key,idKey);
        }
        return idKey.addAndGet(multiple);
    }

}
