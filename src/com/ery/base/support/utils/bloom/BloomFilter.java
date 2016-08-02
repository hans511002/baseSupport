package com.ery.base.support.utils.bloom;

import com.ery.base.support.log4j.LogUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class BloomFilter extends Filter{
    private BitSet bitSet;

    
    public BloomFilter(int n, int k,boolean hasFilter2) {
        super(n, k,hasFilter2);
        try{
            this.bitSet = new BitSet(this.capacitySize);
        }catch (Throwable e){
            throw new IllegalArgumentException("初始过滤器失败!",e);//可能发生内存溢出等问题.捕获
        }
    }

    public BloomFilter(int n,boolean hasFilter2) {
        this(n, HASH_KEY_NUM,hasFilter2);
    }

    @Override
    public void put(byte[] data) {
        int[] positions = createHashes(data, hashFunctionNumber);
        for (int position1 : positions) {
            int position = position1 % capacitySize;
            bitSet.set(position, true);
        }
        incrementElement();
    }

    @Override
    public boolean contains(byte[] data) {
        int[] positions = createHashes(data, hashFunctionNumber);
        for (int position1 : positions) {
            int position = position1 % capacitySize;
            if (!bitSet.get(position)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void remove(byte[] data){
        removeFilter2(data);
        LogUtils.warn("本实现["+BloomFilter.class.getSimpleName()+"]无法执行删除方法!");
    }

    @Override
    public void clear(){
        bitSet.clear();
        super.reset();
    }

    @Override
    public void write(DataOutput output) throws IOException{
        super.write(output);
        byte[] bytes = new byte[getNBytes()];
        for (int i = 0; i < capacitySize; i++) {
            int index = i / 8;
            int offset = 7 - i % 8;
            bytes[index] |= (bitSet.get(i) ? 1 : 0) << offset;
        }
        output.write(bytes);
    }

    @Override
    public boolean read(DataInput input) throws IOException {
        if(super.read(input)){
            bitSet = new BitSet(capacitySize);
            byte[] bytes = new byte[getNBytes()];
            input.readFully(bytes);
            int index = 0;
            for (byte aByte : bytes) {
                for (int j = 7; j >= 0; j--) {
                    bitSet.set(index++, (aByte & (1 << j)) >> j == 1 ? true : false);
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

