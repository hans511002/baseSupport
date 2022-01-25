package com.sobey.jcg.support.log4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggerRepository;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.ery.base.support.utils.Convert;

public class LogUtils {

	public static final int STACK_TRACE_EXT_NUM = 3;

	public static final int TRACE = 5000;
	public final static int FATAL = 50000;
	public final static int ERROR = 40000;
	public final static int WARN = 30000;
	public final static int INFO = 20000;
	public final static int DEBUG = 10000;

	protected static String CHARSET = null;
	protected static String KEY_PREFIX = "";// classKey 前缀（避免className被第三方工具在log4j内部提前初始,如spring）

	protected static int level = 0;
	protected static Map<String, Logger> loggerMap = new HashMap<String, Logger>();
	protected static LoggerExtFactory factory = new LoggerExtFactory();

	public static Map<String, Logger> getLoggerMap() {
		return loggerMap;
	}

	public static Logger getLogger() {
		return getLogger(2);
	}

	static Logger getLogger(int l) {
		StackTraceElement trace = Thread.currentThread().getStackTrace()[l];
		String className = KEY_PREFIX + trace.getClassName();
		Logger logger = null;
		synchronized (loggerMap) {
			logger = loggerMap.get(className);
			if (logger == null) {
				logger = LoggerExt.getLogger(className, factory);
				if (LogUtils.level > 0) {
					setLevel(logger, LogUtils.level);
				}
				if (!(logger instanceof LoggerExt)) {
					LoggerExt _logger = (LoggerExt) factory.makeNewLoggerInstance(className);
					LoggerRepository rep = (LoggerRepository) logger.getLoggerRepository();
					_logger.setRepository(rep);
					_logger.setParent(logger.getParent());
					if (LogUtils.level > 0) {
						setLevel(_logger, LogUtils.level);
					}
					logger = _logger;
				}
				loggerMap.put(className, logger);
			}
		}
		return logger;
	}

	public static void setLevel(int level) {
		LogUtils.level = level;
		for (String key : loggerMap.keySet()) {
			Logger logger = loggerMap.get(key);
			setLevel(logger, level);
		}
	}

	static void setLevel(Logger logger, int level) {
		switch (level) {
		case DEBUG:
			setLevel(logger, Level.DEBUG);
			break;
		case ERROR:
			setLevel(logger, Level.ERROR);
			break;
		case FATAL:
			setLevel(logger, Level.FATAL);
			break;
		case INFO:
			setLevel(logger, Level.INFO);
			break;
		case WARN:
			setLevel(logger, Level.WARN);
			break;
		case TRACE:
			setLevel(logger, Level.TRACE);
			break;
		}
	}

	static void setLevel(Logger logger, Level level) {
		Category l = logger;
		while (l != null) {
			l.setLevel(level);
			l.getLoggerRepository().setThreshold(level);
			Enumeration<AppenderSkeleton> e = l.getAllAppenders();
			if (e != null) {
				while (e.hasMoreElements()) {
					AppenderSkeleton ap = e.nextElement();
					ap.setThreshold(level);
				}
			}
			l = l.getParent();
		}
	}

	private static void log(int level, Object msgObj) {
		Logger logger = getLogger(STACK_TRACE_EXT_NUM + 1);
		String message = null;
		try {
			if (CHARSET != null) {
				message = new String(Convert.toString(msgObj).getBytes(), CHARSET);
			} else {
				message = Convert.toString(msgObj);
			}
		} catch (UnsupportedEncodingException e) {
			message = Convert.toString(msgObj);
		}
		switch (level) {
		case DEBUG:
			logger.debug(message);
			break;
		case ERROR:
			logger.error(message);
			break;
		case FATAL:
			logger.fatal(message);
			break;
		case INFO:
			logger.info(message);
			break;
		case WARN:
			logger.warn(message);
			break;
		case TRACE:
			logger.trace(message);
			break;
		}
	}

	public static void setCharset(String charset) {
		LogUtils.CHARSET = charset;
	}

	public static void setKeyPrefix(String keyPrefix) {
		LogUtils.KEY_PREFIX = keyPrefix;
	}

	private static void log(int level, Object msgObj, Throwable t) {
		Logger logger = getLogger(STACK_TRACE_EXT_NUM + 1);
		String message = null;
		try {
			if (CHARSET != null) {
				message = new String(Convert.toString(msgObj).getBytes(), CHARSET);
			} else {
				message = Convert.toString(msgObj);
			}
		} catch (UnsupportedEncodingException e) {
			message = Convert.toString(msgObj);
		}
		switch (level) {
		case DEBUG:
			logger.debug(message, t);
			break;
		case ERROR:
			logger.error(message, t);
			break;
		case FATAL:
			logger.fatal(message, t);
			break;
		case INFO:
			logger.info(message, t);
			break;
		case WARN:
			logger.warn(message, t);
			break;
		case TRACE:
			logger.trace(message, t);
			break;
		}
	}

	// 判断指定类位置打印日志可用否
	private static boolean enabledFor(int level) {
		Logger logger = getLogger(STACK_TRACE_EXT_NUM + 1);
		switch (level) {
		case DEBUG:
			return logger.isDebugEnabled();
		case ERROR:
			return logger.isEnabledFor(Priority.ERROR);
		case FATAL:
			return logger.isEnabledFor(Priority.FATAL);
		case INFO:
			return logger.isInfoEnabled();
		case WARN:
			return logger.isEnabledFor(Priority.WARN);
		case TRACE:
			return logger.isTraceEnabled();
		}
		return false;
	}

	public static boolean debugEnabled() {
		return enabledFor(DEBUG);
	}

	public static boolean errorEnabled() {
		return enabledFor(ERROR);
	}

	public static boolean fatalEnabled() {
		return enabledFor(FATAL);
	}

	public static boolean infoEnabled() {
		return enabledFor(INFO);
	}

	public static boolean warnEnabled() {
		return enabledFor(WARN);
	}

	public static boolean traceEnabled() {
		return enabledFor(TRACE);
	}

	public static boolean isDebugEnabled() {
		return enabledFor(DEBUG);
	}

	public static boolean isErrorEnabled() {
		return enabledFor(ERROR);
	}

	public static boolean isFatalEnabled() {
		return enabledFor(FATAL);
	}

	public static boolean isInfoEnabled() {
		return enabledFor(INFO);
	}

	public static boolean isWarnEnabled() {
		return enabledFor(WARN);
	}

	public static boolean isTraceEnabled() {
		return enabledFor(TRACE);
	}

	public static void debug(Object message) {
		log(DEBUG, message);
	}

	public static void debug(String message, java.lang.Throwable t) {
		log(DEBUG, message, t);
	}

	public static void debug(String format, Object arg) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(DEBUG, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void debug(String format, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(DEBUG, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void debug(String format, Object... arguments) {
		if (isDebugEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
			log(DEBUG, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void error(Object message) {
		log(ERROR, message);
	}

	public static void error(String message, java.lang.Throwable t) {
		log(ERROR, message, t);
	}

	public static void error(String format, Object arg) {
		if (isErrorEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(ERROR, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void error(String format, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(ERROR, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void error(String format, Object... arguments) {
		if (isErrorEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
			log(ERROR, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void fatal(Object message) {
		log(FATAL, message);
	}

	public static void fatal(String message, java.lang.Throwable t) {
		log(FATAL, message, t);
	}

	public static void fatal(String format, Object arg) {
		if (isFatalEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(FATAL, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void fatal(String format, Object arg1, Object arg2) {
		if (isFatalEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(FATAL, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void fatal(String format, Object... arguments) {
		if (isFatalEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
			log(FATAL, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void info(Object message) {
		log(INFO, message);
	}

	public static void info(String message, java.lang.Throwable t) {
		log(INFO, message, t);
	}

	public static void info(String format, Object arg) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(INFO, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void info(String format, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(INFO, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void info(String format, Object... arguments) {
		if (isInfoEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
			log(INFO, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void warn(Object message) {
		log(WARN, message);
	}

	public static void warn(String message, java.lang.Throwable t) {
		log(WARN, message, t);
	}

	public static void warn(String format, Object arg) {
		if (isWarnEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(WARN, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void warn(String format, Object arg1, Object arg2) {
		if (isWarnEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(WARN, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void warn(String format, Object... arguments) {
		if (isWarnEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
			log(WARN, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void trace(Object message) {
		log(TRACE, message);
	}

	public static void trace(String message, java.lang.Throwable t) {
		log(TRACE, message, t);
	}

	public static void trace(String format, Object arg) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg);
			log(TRACE, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
			log(TRACE, ft.getMessage(), ft.getThrowable());
		}
	}

	public static void trace(String format, Object... arguments) {
		if (isTraceEnabled()) {
			FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
			log(TRACE, ft.getMessage(), ft.getThrowable());
		}
	}

	private static Properties log4jProp;

	public synchronized static Properties getLog4jConfig() {
		if (log4jProp != null) {
			return log4jProp;
		}

		String override = OptionConverter.getSystemProperty(LogManager.DEFAULT_INIT_OVERRIDE_KEY, null);
		// if there is no default init override, then get the resource
		// specified by the user or the default config file.
		if (override == null || "false".equalsIgnoreCase(override)) {
			URL url = Loader.getResource(LogManager.DEFAULT_CONFIGURATION_FILE);
			if (url != null) {
				log4jProp = new Properties();
				InputStream istream = null;
				URLConnection uConn = null;
				try {
					uConn = url.openConnection();
					uConn.setUseCaches(false);
					istream = uConn.getInputStream();
					log4jProp.load(istream);
				} catch (Exception e) {
					if (e instanceof InterruptedIOException || e instanceof InterruptedException) {
						Thread.currentThread().interrupt();
					}
					log4jProp.clear();
				} finally {
					if (istream != null) {
						try {
							istream.close();
						} catch (InterruptedIOException ignore) {
							Thread.currentThread().interrupt();
						} catch (IOException ignore) {
						} catch (RuntimeException ignore) {
						}
					}
				}
			} else {
				// 无log4j配置
			}
		}
		return log4jProp;
	}

	public static void main(String[] argv) {
		LogUtils.info("test");
	}
}
