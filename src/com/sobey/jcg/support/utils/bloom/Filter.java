package com.sobey.jcg.support.utils.bloom;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.hash.HashFunctions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class Filter {
    
    public static final int HASH_KEY_NUM = 8;

    protected int capacitySize;//过滤器最大容量
    
    protected int hashFunctionNumber = HASH_KEY_NUM;//hash函数个数
    private int estAddElements;//估计放入元素个数
    private AtomicInteger addedElements = new AtomicInteger(0);//已经加入的元素个数
    private boolean hasFilter2 = true;//是否有二次过滤
    private Filter2 filter2;
    
    
    private InitMode mode = InitMode.NEW;
    private String busKey;//业务识别码

    public void setMode(InitMode mode) {
        this.mode = mode;
    }

    public void setBusKey(String busKey) {
        this.busKey = busKey;
    }

    
    public Filter(int n, int k,boolean hasFilter2) {
        int ks = Math.min(2*k,HASH_KEY_NUM);
        if(1l*n*ks>Integer.MAX_VALUE){
            ks = Math.min(k,HASH_KEY_NUM);
            if(1l*n*ks>Integer.MAX_VALUE){
                throw new RuntimeException("预计放入元素量不可超过["+(int)(Integer.MAX_VALUE/ks)+"]");
            }
        }
        this.capacitySize = (int) Math.ceil(n * ks);
        this.estAddElements = n;
        this.hashFunctionNumber = k;
        this.hasFilter2 = hasFilter2;
        if(hasFilter2){
            int n_ = Math.max(n,100000)/100;//将容量缩小100倍，建立一个二次验证区域。辅助去重区域不低于1000
            filter2 = new Filter2(n_);//只有两个去重区域都包含此数据时，才代表包含此数据
        }
    }

    
    public double getFalsePositiveProbability() {
        // (1 - e^(-k * n / m)) ^ k
        return Math.pow((1 - Math.exp(-hashFunctionNumber * (double) estAddElements / capacitySize)),hashFunctionNumber);
    }

    
    public double getCurrentFalsePositiveProbability() {
        // (1 - e^(-k * n / m)) ^ k
        return Math.pow((1 - Math.exp(-hashFunctionNumber * (double) addedElements.get() / capacitySize)),hashFunctionNumber);
    }

    
    public final int getCapacitySize(){
        return capacitySize;
    }
    
    public final int getHashKeyNum(){
        return hashFunctionNumber;
    }
    
    public final int getEstAddElements(){
        return estAddElements;
    }

    
    public final int getAddedElements(){
        return addedElements.get() + (hasFilter2?filter2.addedNum.get():0);
    }
    
    public final int getFilter2AddedNum(){
        return hasFilter2?filter2.addedNum.get():0;
    }

    //是否包含二次过滤
    public boolean hasFilter2() {
        return hasFilter2;
    }

    //添加一个元素
    public final int incrementElement(){
        int cnt = addedElements.incrementAndGet();
        if(cnt>estAddElements){
            LogUtils.warn("["+busKey+"]已存入["+cnt+"],预估["+estAddElements+"],当前错误率:"+getCurrentFalsePositiveProbability());
        }
        return cnt;
    }

    //减少一个元素
    public final int decrementElement(){
        return addedElements.decrementAndGet();
    }

    public abstract void put(byte[] data);
    public abstract boolean contains(byte[] data);
    public abstract void remove(byte[] data);
    public abstract void clear();

    //重置
    public void reset(){
        addedElements.set(0);
        if(hasFilter2){
            filter2.clear();
        }
    }

    public void putFilter2(byte[] data){
        if(hasFilter2){
            filter2.put(data);
        }
    }

    public boolean containsFilter2(byte[] data){
        return hasFilter2 && filter2.contains(data);
    }

    public void removeFilter2(byte[] data){
        if(hasFilter2){
            filter2.remove(data);
        }
    }

    public void writeFilter2(DataOutput output) throws IOException {
        if(hasFilter2){
            filter2.write(output);
        }
    }

    public void readFilter2(DataInput input) throws IOException {
        if(hasFilter2){
            filter2.read(input);
        }
    }

    //写
    public void write(DataOutput output) throws IOException {
        output.writeInt(capacitySize);
        output.writeInt(estAddElements);
        output.writeInt(hashFunctionNumber);
        output.writeBoolean(hasFilter2);
        output.writeInt(addedElements.get());
    }

    
    public boolean read(DataInput input) throws IOException {
        int capacitySize = input.readInt();
        int estAddElements = input.readInt();
        int hashFunctionNumber = input.readInt();
        boolean hasFilter2 = input.readBoolean();
        int adsize = input.readInt();
        if(mode.equals(InitMode.NEW)){
            if(capacitySize==this.capacitySize && estAddElements==this.estAddElements
                && hashFunctionNumber==this.hashFunctionNumber && hasFilter2==this.hasFilter2){
                addedElements.set(adsize);
                return true;
            }
            return false;
        }
        this.capacitySize = capacitySize;
        this.estAddElements = estAddElements;
        this.hashFunctionNumber = hashFunctionNumber;
        this.hasFilter2 = hasFilter2;
        addedElements.set(adsize);
        return true;
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder("[predictNum:"+estAddElements+",totalCap:"+capacitySize);
        sb.append(",occupyOf:").append(getOccupyOf()).append(" kb");
        sb.append(",falsePro:").append(getFalsePositiveProbability());
        sb.append(",currentFalsePro:").append(getCurrentFalsePositiveProbability());
        sb.append("]");
        return sb.toString();
    }

    //本过滤器大致占用内存（单位kb）
    public int getOccupyOf(){
        double of;
        if(this instanceof CountingBloomFilter){
            of = capacitySize/1024.0/2 + (hasFilter2?filter2.getOccupyOf():0);
        }else if(this instanceof CounterBloomFilter){
            of = capacitySize/1024.0 + (hasFilter2?filter2.getOccupyOf():0);
        }else{
            of = capacitySize/1024.0/8 + (hasFilter2?filter2.getOccupyOf():0);
        }
        return (int)of;
    }

    public enum InitMode{
        NEW,//new
        READ//读取datainput
    }

    //根据预计排重数量，估算过滤器占用内存
    public static int predictOccupyOf(Class clazz,int predict,int k){
        int ks = Math.min(2*k,HASH_KEY_NUM);
        if(1l*predict*ks>Integer.MAX_VALUE){
            ks = Math.min(k,HASH_KEY_NUM);
            if(1l*predict*ks>Integer.MAX_VALUE){
                throw new RuntimeException("预估容量过大!");
            }
        }
        int cap = ks*predict;
        double of;
        if(clazz.getSimpleName().equals(CountingBloomFilter.class.getSimpleName())){
            of = cap/1024.0/2;
        }else if(clazz.getSimpleName().equals(CounterBloomFilter.class.getSimpleName())){
            of = cap/1024.0;
        }else{
            of = cap/1024.0/8;
        }
        return (int)of;
    }

    //计算错误率
    public static double getFalsePositiveProbability(int n,int k){
        int ks = Math.min(2*k,HASH_KEY_NUM);
        if(1l*n*ks>Integer.MAX_VALUE){
            ks = Math.min(k,HASH_KEY_NUM);
            if(1l*n*ks>Integer.MAX_VALUE){
                throw new RuntimeException("预估容量过大!");
            }
        }
        int cap = n*ks;
        return Math.pow((1 - Math.exp(-k * (double) n / cap)),k);
    }

    
    public static int[] createHashes(byte[] bytes, int hashNumber) {
        int[] result = new int[hashNumber];
        for (int k = 0 ; k < hashNumber ; k++) {
            result[k] = Math.abs(HashFunctions.hash(bytes, k));
        }
        return result;
    }


    //hash种子,质数
    public final static int[] SEEDS = new int[]{7, 13, 17, 31, 37,47,61,67,131};
    //第二种hash算法
    public static int[] createHashes2(byte[] bytes,int hashNumber){
        int[] result = new int[hashNumber];
        for (int k = 0 ; k < hashNumber ; k++) {
            result[k] = Math.abs(HashFunctions.OTHERHash(bytes, SEEDS[k]));
        }
        return result;
    }

    
    protected static int buckets2words(int vectorSize) {
        return ((vectorSize - 1) >>> 4) + 1;
    }

}


class Filter2 {
    int capacitySize;//过滤器最大容量
    int estSize;
    int ks = Filter.HASH_KEY_NUM;
    AtomicInteger addedNum = new AtomicInteger(0);
    long[] buckets;
    final static long BUCKET_MAX_VALUE = 15;

    public Filter2(int n) {
        capacitySize = (int)Math.ceil(n * Math.min(2*ks,Filter.HASH_KEY_NUM));
        estSize = n;
        buckets = new long[Filter.buckets2words(capacitySize)];
    }

    public void put(byte[] data) {
        int[] positions = Filter.createHashes2(data, ks);
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
        addedNum.incrementAndGet();
    }

    public boolean contains(byte[] data) {
        int[] positions = Filter.createHashes2(data, ks);
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

    public void remove(byte[] data) {
        int[] positions = Filter.createHashes2(data, ks);
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
            //包含再删除
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
            addedNum.decrementAndGet();
        }
    }

    public void clear() {
        buckets = new long[Filter.buckets2words(capacitySize)];
    }

    //写
    public void write(DataOutput output) throws IOException {
        output.writeInt(capacitySize);
        output.writeInt(estSize);
        output.writeInt(ks);
        output.writeInt(addedNum.get());
        int sizeInWords = Filter.buckets2words(capacitySize);
        for(int i = 0; i < sizeInWords; i++) {
            output.writeLong(buckets[i]);
        }
    }

    //读
    public void read(DataInput input) throws IOException {
        capacitySize = input.readInt();
        estSize = input.readInt();
        ks = input.readInt();
        addedNum.set(input.readInt());
        int sizeInWords = Filter.buckets2words(capacitySize);
        buckets = new long[sizeInWords];
        for(int i = 0; i < sizeInWords; i++) {
            buckets[i] = input.readLong();
        }
    }

    public int getOccupyOf(){
        return (int)(capacitySize/1024.0/8);
    }
}
