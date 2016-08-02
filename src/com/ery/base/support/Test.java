package com.ery.base.support;

import com.ery.base.support.log4j.LogUtils;
import com.ery.base.support.utils.Bytes;
import com.ery.base.support.utils.Utils;
import com.ery.base.support.utils.hash.HashFunctions;


public class Test {
    
    public static void main(String[] args) throws Exception{
        long st;
        int bucketNum = (int) Utils.getMinDistancePrimeNum(100000, true);//桶数
        int size = 10000000;//数据

        byte[] buckets = new byte[size];
        String str = "测试测试测试adfasdf";
        int hashCode = 0;
        st = System.currentTimeMillis();
        byte[] b = new byte[str.getBytes().length+4];
        System.arraycopy(str.getBytes(),0,b,0,b.length-4);//固定在前
//        System.arraycopy(str.getBytes(),0,b,4,b.length-4);//固定在后
        for(int i=0;i<size;i++){
            System.arraycopy(Bytes.toBytes(i),0,b,b.length-5,4);//固定在前
//            System.arraycopy(Bytes.toBytes(i),0,b,0,4);//固定在后

            hashCode = HashFunctions.BKDRHash(b, 31);
//            hashCode = HashFunctions.APHash(b);
//            hashCode = HashFunctions.ELFHash(b);
            if(hashCode>0){
                buckets[hashCode%bucketNum] ++;
            }else{
                buckets[-hashCode%bucketNum] ++;
            }
        }

        if(LogUtils.infoEnabled()){
            LogUtils.info("加入[" + size + "]数据耗时:" + (System.currentTimeMillis() - st) + " ms，桶数:" + bucketNum);
        }





    }
    
}
