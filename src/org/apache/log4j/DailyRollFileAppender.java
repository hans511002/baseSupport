package org.apache.log4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

public class DailyRollFileAppender extends FileAppender {
	static final int TOP_OF_TROUBLE = -1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR = 1;
	static final int HALF_DAY = 2;
	static final int TOP_OF_DAY = 3;
	static final int TOP_OF_WEEK = 4;
	static final int TOP_OF_MONTH = 5;
	private String datePattern = "'.'yyyy-MM-dd";
	private String scheduledFilename;
	private long nextCheck = 0L;

	protected long maxFileSize = 10485760L;
	protected int maxBackupFileNum = 0;
	protected int logIndex = 0;

	Date now = new Date();
	SimpleDateFormat sdf;
	RollingCalendar rc = new RollingCalendar();

	static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

	public DailyRollFileAppender() {
	}

	public DailyRollFileAppender(Layout layout, String filename, String datePattern) throws IOException {
		super(layout, filename, true);
		this.datePattern = datePattern;
		activateOptions();
	}

	public int getMaxBackupFileNum() {
		return this.maxBackupFileNum;
	}

	public void setMaxBackupFileNum(int maxBackups) {
		this.maxBackupFileNum = maxBackups;
	}

	public void setMaximumFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public long getMaximumFileSize() {
		return this.maxFileSize;
	}

	// public void setMaximumFileSize(String value) {
	// this.maxFileSize = OptionConverter.toFileSize(value, this.maxFileSize + 1L);
	// }

	// public long getMaxFileSize() {
	// return this.maxFileSize;
	// }
	//
	// public void setMaxFileSize(long maxFileSize) {
	// this.maxFileSize = maxFileSize;
	// }

	public void setMaxFileSize(String value) {
		this.maxFileSize = OptionConverter.toFileSize(value, this.maxFileSize + 1L);
	}

	protected void setQWForFiles(Writer writer) {
		this.qw = new CountingQuietWriter(writer, this.errorHandler);
	}

	public void setDatePattern(String pattern) {
		this.datePattern = pattern;
	}

	public String getDatePattern() {
		return this.datePattern;
	}

	public void activateOptions() {
		super.activateOptions();
		if ((this.datePattern != null) && (this.fileName != null)) {
			this.now.setTime(System.currentTimeMillis());
			this.sdf = new SimpleDateFormat(this.datePattern);
			int type = computeCheckPeriod();
			printPeriodicity(type);
			this.rc.setType(type);
			File file = new File(this.fileName);
			this.scheduledFilename = (this.fileName + this.sdf.format(new Date(file.lastModified())));
		} else {
			LogLog.error("Either File or DatePattern options are not set for appender [" + this.name + "].");
		}
	}

	void printPeriodicity(int type) {
		switch (type) {
		case 0:
			LogLog.debug("Appender [" + this.name + "] to be rolled every minute.");
			break;
		case 1:
			LogLog.debug("Appender [" + this.name + "] to be rolled on top of every hour.");
			break;
		case 2:
			LogLog.debug("Appender [" + this.name + "] to be rolled at midday and midnight.");
			break;
		case 3:
			LogLog.debug("Appender [" + this.name + "] to be rolled at midnight.");
			break;
		case 4:
			LogLog.debug("Appender [" + this.name + "] to be rolled at start of week.");
			break;
		case 5:
			LogLog.debug("Appender [" + this.name + "] to be rolled at start of every month.");
			break;
		default:
			LogLog.warn("Unknown periodicity for appender [" + this.name + "].");
		}
	}

	int computeCheckPeriod() {
		RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.getDefault());
		Date epoch = new Date(0L);
		if (this.datePattern != null) {
			for (int i = 0; i <= 5; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.datePattern);
				simpleDateFormat.setTimeZone(gmtTimeZone);
				String r0 = simpleDateFormat.format(epoch);
				rollingCalendar.setType(i);
				Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
				String r1 = simpleDateFormat.format(next);
				if ((r0 != null) && (r1 != null) && (!r0.equals(r1))) {
					return i;
				}
			}
		}
		return -1;
	}

	boolean zipFile(String filePath) {
		try {
			writeZip(filePath, filePath);
			return true;
		} catch (IOException e) {
			LogLog.debug("gzip file failed:" + filePath, e);
		}
		return false;
	}

	private static void handlerFile(String zip, ZipOutputStream zipOut, File srcFile, String path) throws IOException {
		if (!"".equals(path) && !path.endsWith(File.separator)) {
			path += File.separator;
		}
		if (srcFile.getPath().equals(zip) || !srcFile.exists()) {
			return;
		}
		if (srcFile.isDirectory()) {
			File[] _files = srcFile.listFiles();
			if (_files.length == 0) {
				zipOut.putNextEntry(new ZipEntry(path + srcFile.getName() + File.separator));
				zipOut.closeEntry();
			} else {
				for (File _f : _files) {
					handlerFile(zip, zipOut, _f, path + srcFile.getName());
				}
			}
		} else {
			InputStream _in = new FileInputStream(srcFile);
			zipOut.putNextEntry(new ZipEntry(path + srcFile.getName()));
			int len = 0;
			byte[] _byte = new byte[1024];
			while ((len = _in.read(_byte)) > 0) {
				zipOut.write(_byte, 0, len);
			}
			_in.close();
			zipOut.closeEntry();
		}
	}

	public static void writeZip(String zipname, String... files) throws IOException {
		OutputStream os = new BufferedOutputStream(new FileOutputStream(zipname + ".zip"));
		ZipOutputStream zos = new ZipOutputStream(os);
		for (String _f : files) {
			handlerFile(zipname + ".zip", zos, new File(_f), "");
		}
		zos.close();
		for (int i = 0; i < files.length; i++) {
			File file = new File(files[i]);
			file.delete();
		}
	}

	void checkFileNums() {
		final File file = new File(this.fileName);
		File dir = file.getParentFile();
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.equals(file.getName()))
					return false;
				return name.startsWith(file.getName());
			}
		});
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (o1.equals(file)) {
					return -1;
				} else if (o2.equals(file)) {
					return 1;
				}
				String[] name1 = o1.getName().split("\\.|-");
				String[] name2 = o2.getName().split("\\.|-");
				int i = 0;
				while (true) {
					if (name1.length > i && name2.length > i) {
						long l1 = -1;
						long l2 = -1;
						try {
							l1 = Long.parseLong(name1[i]);
							l2 = Long.parseLong(name2[i]);
							i++;
							if (l1 >= 0 && l2 >= 0) {
								if (l1 == l2)
									continue;
								else
									return (int) (l1 - l2);
							}
						} catch (Throwable e) {
							int res = name1[i].compareTo(name2[i]);
							i++;
							if (res == 0) {
								continue;
							} else {
								return res;
							}
						}
					} else if (name1.length > i) {
						return 1;
					} else if (name2.length > i) {
						return -1;
					} else {
						return 0;
					}
				}
			}
		});
		for (int i = 0; i < files.length - maxBackupFileNum; i++) {
			files[i].delete();
		}
	}

	private void getLogIndex() {
		final File file = new File(this.scheduledFilename);
		File dir = file.getParentFile();
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String fn = file.getName();
				if (name.equals(fn))
					return false;
				if (name.length() > fn.length())
					return name.startsWith(fn);
				else
					return false;
			}
		});
		for (File file2 : files) {
			String fileName = file2.getName();
			fileName = fileName.substring(file.getName().length() + 1);
			// deployactor.log.2018-09-20.1.zip 1.zip
			if (fileName.endsWith(".zip")) {
				fileName = fileName.substring(0, fileName.length() - 4);
			}
			try {
				int idx = Integer.parseInt(fileName);
				if (idx > this.logIndex)
					this.logIndex = idx;
			} catch (Throwable e) {
			}
		}
	}

	void rollOver() throws IOException {
		if (System.currentTimeMillis() >= this.nextCheck) {
			if (this.datePattern == null) {
				this.errorHandler.error("Missing DatePattern option in rollOver().");
				return;
			}
			String datedFilename = this.fileName + this.sdf.format(this.now);
			if (this.scheduledFilename.equals(datedFilename)) {
				return;
			}
			closeFile();
			getLogIndex();
			logIndex++;
			String idx = (logIndex > 1 ? "." + logIndex : "");
			File target = new File(this.scheduledFilename + idx);
			if (target.exists()) {
				target.delete();
			}
			File file = new File(this.fileName);
			boolean result = file.renameTo(target);
			if (result)
				LogLog.debug(this.fileName + " -> " + this.scheduledFilename + idx);
			else {
				LogLog.error("Failed to rename [" + this.fileName + "] to [" + this.scheduledFilename + idx + "].");
			}
			zipFile(target.getCanonicalPath());
			checkFileNums();
			try {
				logIndex = 0;
				setFile(this.fileName, true, this.bufferedIO, this.bufferSize);
			} catch (IOException e) {
				this.errorHandler.error("setFile(" + this.fileName + ", true) call failed.");
			}
			this.scheduledFilename = datedFilename;
		} else {// roll
			if (this.qw == null) {
				return;
			}
			long size = ((CountingQuietWriter) this.qw).getCount();
			if ((size < this.maxFileSize)) {
				return;
			}
			closeFile();
			getLogIndex();
			String datedFilename = this.fileName + this.sdf.format(this.now);
			logIndex++;
			String idx = "." + logIndex;
			if (!this.scheduledFilename.equals(datedFilename)) {
				idx = (logIndex > 1 ? "." + logIndex : "");
			}
			File target = new File(this.scheduledFilename + idx);
			if (target.exists()) {
				target.delete();
			}
			File file = new File(this.fileName);
			file.renameTo(target);
			zipFile(target.getCanonicalPath());
			checkFileNums();
			try {
				this.scheduledFilename = datedFilename;
				setFile(this.fileName, false, this.bufferedIO, this.bufferSize);
			} catch (IOException e) {
				if ((e instanceof InterruptedIOException)) {
					Thread.currentThread().interrupt();
				}
				LogLog.error("setFile(" + this.fileName + ", false) call failed.", e);
			}
		}
	}

	protected void subAppend(LoggingEvent event) {
		long n = System.currentTimeMillis();
		if (n >= this.nextCheck && this.nextCheck > 0) {
			this.now.setTime(n);
			try {
				rollOver();
			} catch (IOException ioe) {
				if (ioe instanceof InterruptedIOException) {
					Thread.currentThread().interrupt();
				}
				LogLog.error("rollOver() failed.", ioe);
			}
			this.nextCheck = this.rc.getNextCheckMillis(this.now);
		} else if ((this.fileName != null) && (this.qw != null)) {
			this.now.setTime(n);
			if (this.nextCheck == 0) {
				String datedFilename = this.fileName + this.sdf.format(this.now);
				this.nextCheck = this.rc.getNextCheckMillis(this.now);
				this.scheduledFilename = datedFilename;
				File file = new File(this.fileName);
				if (file.exists() && file.length() > 0) {
					((CountingQuietWriter) this.qw).setCount(file.length());
				}
			}
			long size = ((CountingQuietWriter) this.qw).getCount();
			if ((size >= this.maxFileSize)) {
				try {
					rollOver();
				} catch (IOException ioe) {
					if ((ioe instanceof InterruptedIOException)) {
						Thread.currentThread().interrupt();
					}
					LogLog.error("rollOver() failed.", ioe);
				}
			}
		}
		super.subAppend(event);
	}

	public static void main(String[] argsv) {
		final File file = new File("/sobeyhive/logs/installer/deployactor.log");
		File dir = file.getParentFile();
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.equals(file.getName()))
					return false;
				return name.startsWith(file.getName());
			}
		});
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i]);
		}
		System.out.println("==========");
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (o1.equals(file)) {
					return -1;
				} else if (o2.equals(file)) {
					return 1;
				}
				String[] name1 = o1.getName().split("\\.|-");
				String[] name2 = o2.getName().split("\\.|-");
				int i = 0;
				while (true) {
					if (name1.length > i && name2.length > i) {
						long l1 = -1;
						long l2 = -1;
						try {
							l1 = Long.parseLong(name1[i]);
							l2 = Long.parseLong(name2[i]);
							i++;
							if (l1 >= 0 && l2 >= 0) {
								if (l1 == l2)
									continue;
								else
									return (int) (l1 - l2);
							}
						} catch (Throwable e) {
							int res = name1[i].compareTo(name2[i]);
							i++;
							if (res == 0) {
								continue;
							} else {
								return res;
							}
						}
					} else if (name1.length > i) {
						return 1;
					} else if (name2.length > i) {
						return -1;
					} else {
						return 0;
					}
				}
			}
		});
		for (int i = 0; i < files.length - 5; i++) {
			System.out.println(files[i]);
		}
	}

}