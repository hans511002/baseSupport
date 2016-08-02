package com.ery.base.support.utils.bloom;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class CountingBloomFilter extends Filter{

    
    private long[] buckets;
    
    private final static long BUCKET_MAX_VALUE = 15;

    
    public CountingBloomFilter(int n, int k,boolean hasFilter2) {
        super(n, k,hasFilter2);
        try{
            buckets = new long[buckets2words(capacitySize)];
        }catch (Throwable e){
            throw new IllegalArgumentException("初始过滤器失败!",e);//可能发生内存溢出等问题.捕获
        }
    }

    public CountingBloomFilter(int n,boolean hasFilter2) {
        this(n,HASH_KEY_NUM,hasFilter2);
    }

    @Override
    public void put(byte[] data) {
        int[] positions = createHashes(data, hashFunctionNumber);
        for (int position1 : positions) {
            int position = position1 % capacitySize;
            // find the bucket
            int wordNum = position >> 4;          // div 16
            int bucketShift = (position & 0x0f) << 2;  // (mod 16) * 4

            long bucketMask = 15L << bucketShift;
            long bucketValue = (buckets[wordNum] & bucketMask) >>> bucketShift;

            // only increment if the count in the bucket is less than BUCKET_MAX_VALUE
            if(bucketValue < BUCKET_MAX_VALUE) {
                // increment by 1
                buckets[wordNum] = (buckets[wordNum] & ~bucketMask) | ((bucketValue + 1) << bucketShift);
            }
        }
        incrementElement();
    }

    @Override
    public boolean contains(byte[] data) {
        int[] positions = createHashes(data, hashFunctionNumber);
        for(int position1 : positions){
            int position = position1 % capacitySize;
            // find the bucket
            int wordNum = position >> 4;          // div 16
            int bucketShift = (position & 0x0f) << 2;  // (mod 16) * 4
            long bucketMask = 15L << bucketShift;

            if((buckets[wordNum] & bucketMask) == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void remove(byte[] data) {
        int[] positions = createHashes(data, hashFunctionNumber);
        boolean cons = true;
        for (int position1 : positions) {
            int position = position1 % capacitySize;
            // find the bucket
            int wordNum = position >> 4;          // div 16
            int bucketShift = (position & 0x0f) << 2;  // (mod 16) * 4
            long bucketMask = 15L << bucketShift;

            if((buckets[wordNum] & bucketMask) == 0) {
                cons = false;
                break;
            }
        }
        if(cons){
            for (int position1 : positions) {
                int position = position1 % capacitySize;
                // find the bucket
                int wordNum = position >> 4;          // div 16
                int bucketShift = (position & 0x0f) << 2;  // (mod 16) * 4
                long bucketMask = 15L << bucketShift;

                long bucketValue = (buckets[wordNum] & bucketMask) >>> bucketShift;
                // only decrement if the count in the bucket is between 0 and BUCKET_MAX_VALUE
                if(bucketValue >= 1 && bucketValue < BUCKET_MAX_VALUE) {
                    // decrement by 1
                    buckets[wordNum] = (buckets[wordNum] & ~bucketMask) | ((bucketValue - 1) << bucketShift);
                }
            }
            decrementElement();
            removeFilter2(data);
        }
    }

    @Override
    public void clear() {
        buckets = new long[buckets2words(capacitySize)];
        super.reset();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        int sizeInWords = buckets2words(capacitySize);
        for(int i = 0; i < sizeInWords; i++) {
            output.writeLong(buckets[i]);
        }
    }

    @Override
    public boolean read(DataInput input) throws IOException {
        if(super.read(input)){
            int sizeInWords = buckets2words(capacitySize);
            buckets = new long[sizeInWords];
            for(int i = 0; i < sizeInWords; i++) {
                buckets[i] = input.readLong();
            }
            return true;
        }
        return false;
    }

}
