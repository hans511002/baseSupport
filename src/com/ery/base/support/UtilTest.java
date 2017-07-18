package com.ery.base.support;

import com.ery.base.support.log4j.LogUtils;
import com.ery.base.support.utils.Bytes;
import com.ery.base.support.utils.RemotingUtils;
import com.ery.base.support.utils.Utils;
import com.ery.base.support.utils.hash.HashFunctions;

public class UtilTest {

	public static void main(String[] args) throws Exception {
		long st = System.currentTimeMillis();
		LogUtils.info(RemotingUtils.isAvaUrl("http://192.168.10.101:9080/bdfetch_res"));
		LogUtils.info(System.currentTimeMillis() - st);

		if (true)
			return;
		LogUtils.info(Utils.getMinDistancePrimeNum(112, false));
		int bucketNum = (int) Utils.getMinDistancePrimeNum(100000, true);// 桶数
		int size = 4000000;// 数据

		int[] buckets = new int[bucketNum];
		String str = "测试测试测试adfasdf撒旦法撒打发斯蒂芬dsdsdddddddddddddddssd";
		int hashCode = 0;
		st = System.currentTimeMillis();
		byte[] b = new byte[str.getBytes().length + 4];
		System.arraycopy(str.getBytes(), 0, b, 0, b.length - 4);// 固定在前
		// System.arraycopy(str.getBytes(),0,b,4,b.length-4);//固定在后
		for (int i = 0; i < size; i++) {
			System.arraycopy(Bytes.toBytes(i), 0, b, b.length - 5, 4);// 固定在前
			// System.arraycopy(Bytes.toBytes(i),0,b,0,4);//固定在后

			hashCode = HashFunctions.BKDRHash(b, 31);
			// hashCode = HashFunctions.APHash(b);
			// hashCode = HashFunctions.ELFHash(b);
			if (hashCode > 0) {
				buckets[hashCode % bucketNum]++;
			} else {
				buckets[-hashCode % bucketNum]++;
			}
		}
		LogUtils.info("加入[" + size + "]数据耗时:" + (System.currentTimeMillis() - st) + " ms，桶数:" + bucketNum);

		int zero = 0;
		int max = 0;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < bucketNum; i++) {
			if (buckets[i] == 0) {
				zero++;
			} else {
				max = Math.max(max, buckets[i]);
				min = Math.min(min, buckets[i]);
			}
		}
		double avg = size * 1.0 / bucketNum;

		double allfc = 0;
		for (int i = 0; i < bucketNum; i++) {
			allfc += Math.pow(buckets[i] - avg, 2);
		}
		allfc /= bucketNum;

		double fc = 0;
		for (int i = 0; i < bucketNum; i++) {
			if (buckets[i] != 0) {
				fc += Math.pow(buckets[i] - avg, 2);
			}
		}
		fc /= (bucketNum - zero);

		StringBuilder sb = new StringBuilder();
		sb.append("空桶:").append(zero).append("(" + zero * 1.0 / bucketNum + ")");
		sb.append(", 最大桶:").append(max);
		sb.append(", 最小桶:").append(min);
		sb.append(", 平均值:").append(avg);
		sb.append(", 全局方差:").append(allfc);
		sb.append(", 排出空桶方差:").append(fc);
		// 方差可衡量数据集离平均值偏移程度，越小说明偏移越小。分布也越均衡。反之则表示分布不均衡

		LogUtils.info(sb.toString());
	}
}
