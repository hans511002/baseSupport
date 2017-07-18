package com.ery.base.support.common;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import com.ery.base.support.log4j.LogUtils;

public class FileStreamHandler extends StreamHandler {
	// 希望写入的日志路径
	private String fileUrl;
	// 文件名前缀
	private String filePrefix;
	private long lastTime = 0L;
	private Timer timer = new Timer();

	public FileStreamHandler(String fileUrl, String filePrefix) throws Exception {
		super();
		this.fileUrl = fileUrl;
		this.filePrefix = filePrefix;
		openWriteFiles();
	}

	private synchronized void openWriteFiles() throws IllegalArgumentException {
		if (fileUrl == null) {
			throw new IllegalArgumentException("文件路径不能为null");
		}
		getLastFile();
		setTimer();
	}

	private void openFile(File file, boolean append) throws Exception {
		FileOutputStream fout = new FileOutputStream(file.toString(), append);
		setOutputStream(fout);
	}

	private void getLastFile() {
		try {
			super.close();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String trace = sdf.format(new Date(System.currentTimeMillis()));
			File file = new File(fileUrl);
			if (file.isDirectory()) { // 是个目录
				openFile(new File(fileUrl, filePrefix + "_" + trace + ".log"), true);
			} else {
				openFile(new File(fileUrl, trace + ".log"), true);
			}
			LogUtils.debug("Changed log file.");
		} catch (Exception ex) {
			LogUtils.error("Get log file failed.", ex);
		}
	}

	private void setTimer() {
		Date date = new Date(System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		LogUtils.debug("Next time:" + sdf.format(calendar.getTime()));
		timer.schedule(new TimerTask() {
			public void run() {
				openWriteFiles();
			}
		}, calendar.getTime());
	}

	public synchronized void publish(LogRecord record) {
		lastTime = record.getMillis();
		super.publish(record);
		super.flush();
	}

	public long getLastTime() {
		return lastTime;
	}
}
