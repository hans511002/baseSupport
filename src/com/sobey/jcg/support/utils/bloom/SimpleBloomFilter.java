package com.sobey.jcg.support.utils.bloom;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.hash.HashFunctions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.BitSet;


public class SimpleBloomFilter extends Filter{

    //简单hash
    private static int hash(byte[] data,int cap,int seed) {
        return Math.abs(HashFunctions.OTHERHash(data, seed)%cap);
    }

    private BitSet bits;

    
    public SimpleBloomFilter(int n){
        super(n,HASH_KEY_NUM,true);
        try{
            bits = new BitSet(capacitySize);
        }catch (Throwable e){
            throw new IllegalArgumentException("初始过滤器失败!",e);//可能发生内存溢出等问题.捕获
        }
    }

    public SimpleBloomFilter() {
        this(2<<24);//3 kw
    }



    @Override
    public void put(byte[] data) {
        for (int k=0;k<hashFunctionNumber;k++) {
            bits.set(hash(data, capacitySize, SEEDS[k]), true);
        }
        incrementElement();
    }

    @Override
    public boolean contains(byte[] data) {
        for (int k=0;k<hashFunctionNumber;k++) {
            if(!bits.get(hash(data, capacitySize, SEEDS[k]))){
                return false;
            }
        }
        return true;
    }

    @Override
    public void remove(byte[] data) {
        removeFilter2(data);
        LogUtils.warn("本实现["+SimpleBloomFilter.class.getSimpleName()+"]无法执行删除方法!");
    }

    @Override
    public void clear() {
        bits.clear();
        super.reset();
    }

    public void write(DataOutput output) throws IOException{
        super.write(output);
        byte[] bytes = new byte[getNBytes()];
        for (int i = 0; i < capacitySize; i++) {
            int index = i / 8;
            int offset = 7 - i % 8;
            bytes[index] |= (bits.get(i) ? 1 : 0) << offset;
        }
        output.write(bytes);
    }

    @Override
    public boolean read(DataInput input) throws IOException {
        if(super.read(input)){
            bits = new BitSet(capacitySize);
            byte[] bytes = new byte[getNBytes()];
            input.readFully(bytes);
            int index = 0;
            for (byte aByte : bytes) {
                for (int j = 7; j >= 0; j--) {
                    bits.set(index++, (aByte & (1 << j)) >> j == 1 ? true : false);
                }
            }
            return true;
        }
        return false;
    }

    private int getNBytes() {
        return (capacitySize + 7) / 8;//为什么要+7呢，因为int强转会丢掉小数点后
    }
}
