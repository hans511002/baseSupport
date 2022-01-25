package com.sobey.jcg.support.utils;

import java.security.MessageDigest;

import com.sobey.jcg.support.log4j.LogUtils;

import sun.management.ManagementFactoryHelper;

public class Utils {

	private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String getMD5(String pwd) {
		byte[] source = pwd.getBytes();
		String s = null;
		// 用来将字节转换成 16 进制表示的字符
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
			// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
			// 所以表示成 16 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
				// 转换成 16 进制字符的转换
				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
				// >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	// 挂起当前线程time那么多时间
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			LogUtils.error(null, e);
		}
	}

	// 获取当前程序进程号
	public static int getCurrentProcessId() {
		String name = ManagementFactoryHelper.getRuntimeMXBean().getName();
		return Convert.toInt(name.substring(0, name.indexOf("@")));
	}

	// 扩容
	public static byte[] expandVolume(byte[] arr, int size) {
		if (size < 1024)
			size = 1024;// 最小扩容1kb
		byte[] b1 = new byte[arr.length + size];
		System.arraycopy(arr, 0, b1, 0, arr.length);
		return b1;
	}

	public static long getMinDistancePrimeNum(long num, boolean gl) {
		if (num < 0)
			throw new IllegalArgumentException("num must>0!");
		if (gl) {
			for (;; num++) {
				long k = (long) Math.sqrt(num);
				long j;
				for (j = 2; j <= k; j++) {
					if (num % j == 0) {
						break;
					}
				}
				if (j > k) {
					return num;
				}
			}
		} else {
			for (; num > 0; num--) {
				long k = (long) Math.sqrt(num);
				long j;
				for (j = 2; j <= k; j++) {
					if (num % j == 0) {
						break;
					}
				}
				if (j > k) {
					return num;
				}
			}
		}
		throw new IllegalArgumentException("not found!");// 实际不会走到此步骤
	}

}
