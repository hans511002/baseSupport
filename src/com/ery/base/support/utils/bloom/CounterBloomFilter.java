package com.ery.base.support.utils.bloom;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class CounterBloomFilter extends Filter{

    private volatile byte[] counter;//用字节计数，因此每个位最多允许冲突127次

    
    public CounterBloomFilter( int n, int k,boolean hasFilter2) {
        super(n, k,hasFilter2);
        try{
            counter = new byte[capacitySize];
            for(int i=0;i<capacitySize;i++){
                counter[i] = 0;
            }
        }catch (Throwable e){
            throw new IllegalArgumentException("初始过滤器失败!",e);//可能发生内存溢出等问题.捕获
        }
    }

    public CounterBloomFilter(int n,boolean hasFilter2) {
        this(n,HASH_KEY_NUM,hasFilter2);
    }

    @Override
    public void put(byte[] data) {
        int[] positions = createHashes(data, hashFunctionNumber);
        for (int position1 : positions) {
            int position = position1 % capacitySize;
            if(counter[position]<127){
                counter[position] ++;
            }
        }
        incrementElement();
    }

    @Override
    public boolean contains(byte[] data) {
        int[] positions = createHashes(data, hashFunctionNumber);
        for (int position1 : positions) {
            int position = position1 % capacitySize;
            if (counter[position]<=0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void remove(byte[] data){
        int[] positions = createHashes(data, hashFunctionNumber);
        boolean cons = true;
        for (int position1 : positions) {
            int position = position1 % capacitySize;
            if (counter[position]<=0) {
                cons = false;
                break;
            }
        }
        if(cons){
            for (int position1 : positions) {
                int position = position1 % capacitySize;
                if(counter[position]>0){
                    counter[position] --;
                }
            }
            decrementElement();
            removeFilter2(data);
        }
    }

    @Override
    public void clear() {
        for(int i=0;i<capacitySize;i++){
            counter[i] = 0;
        }
        super.reset();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        output.write(counter);
    }

    @Override
    public boolean read(DataInput input) throws IOException {
        if(super.read(input)){
            counter = new byte[capacitySize];
            input.readFully(counter);
            return true;
        }
        return false;
    }
}
