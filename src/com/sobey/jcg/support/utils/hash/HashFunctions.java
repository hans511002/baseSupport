package com.sobey.jcg.support.utils.hash;


public class HashFunctions {

    public static int hash(byte[] bytes, int k) {
        switch (k) {
            case 0:
                
                return BKDRHash(bytes,131);
            case 1:
                return BKDRHash(bytes,127);
            case 2:
                return BKDRHash(bytes,109);
            case 3:
                return APHash(bytes);
            case 4:
                return ELFHash(bytes);
            case 5:
                return JSHash(bytes);
            case 6:
                return RSHash(bytes);
            case 7:
                return DJBHash(bytes);
            case 8:
                return SDBMHash(bytes);
            case 9:
                return PJWHash(bytes);
            default:
                return OTHERHash(bytes,k);
        }
    }
    public static int RSHash(byte[] bytes) {
        int hash = 0;
        int magic = 63689;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            hash = hash * magic + bytes[i];
            magic = magic * 378551;
        }
        return hash;
    }
    public static int JSHash(byte[] bytes) {
        int hash = 1315423911;
        for (byte aByte : bytes) {
            hash ^= ((hash << 5) + aByte + (hash >> 2));
        }
        return hash;
    }
    public static int ELFHash(byte[] bytes) {
        int hash = 0;
        int x = 0;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            hash = (hash << 4) + bytes[i];
            if ((x = hash & 0xF0000000) != 0) {
                hash ^= (x >> 24);
                hash &= ~x;
            }
        }
        return hash;
    }
    //seed： 31 131 1313 13131 131313
    public static int BKDRHash(byte[] bytes,int seed) {
        int hash = 0;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            hash = (hash * seed) + bytes[i];
        }
        return hash;
    }
    public static int APHash(byte[] bytes) {
        int hash = 0;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            if ((i & 1) == 0) {
                hash ^= ((hash << 7) ^ bytes[i] ^ (hash >> 3));
            } else {
                hash ^= (~((hash << 11) ^ bytes[i] ^ (hash >> 5)));
            }
        }
        return hash;
    }
    public static int DJBHash(byte[] bytes) {
        int hash = 5381;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            hash = ((hash << 5) + hash) + bytes[i];
        }
        return hash;
    }
    public static int SDBMHash(byte[] bytes) {
        int hash = 0;
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            hash = bytes[i] + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    public static int PJWHash(byte[] bytes) {
        long BitsInUnsignedInt = (4 * 8);
        long ThreeQuarters = ((BitsInUnsignedInt * 3) / 4);
        long OneEighth = (BitsInUnsignedInt / 8);
        long HighBits = (long) (0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
        int hash = 0;
        long test = 0;
        for (byte aByte : bytes) {
            hash = (hash << OneEighth) + aByte;
            if ((test = hash & HighBits) != 0) {
                hash = (int) ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
            }
        }
        return hash;
    }

    //实现来自 org.apache.hadoop.util.hash.MurmurHash
    public static int OTHERHash(byte[] data,int seed){
        int length = data.length;
        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ length;
        int len_4 = length >> 2;
        for (int i = 0; i < len_4; i++) {
            int i_4 = i << 2;
            int k = data[i_4 + 3];
            k = k << 8;
            k = k | (data[i_4 + 2] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 1] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 0] & 0xff);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // avoid calculating modulo
        int len_m = len_4 << 2;
        int left = length - len_m;

        if (left != 0) {
            if (left >= 3) {
                h ^= (int) data[length - 3] << 16;
            }
            if (left >= 2) {
                h ^= (int) data[length - 2] << 8;
            }
            if (left >= 1) {
                h ^= (int) data[length - 1];
            }
            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }

}
