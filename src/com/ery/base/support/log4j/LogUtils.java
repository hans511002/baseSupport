package com.ery.base.support.log4j;

import com.ery.base.support.utils.Convert;
import org.apache.log4j.*;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class LogUtils {

    
    public static final int STACK_TRACE_EXT_NUM = 3;

    private static final int TRACE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int FATAL = 6;
    private static String CHARSET = null;
    private static String KEY_PREFIX = "";//classKey 前缀（避免className被第三方工具在log4j内部提前初始,如spring）

    private static Map<String,Logger> loggerMap = new HashMap<String, Logger>();
    private static LoggerExtFactory factory = new LoggerExtFactory();

    private static void log(int level,Object msgObj){
        StackTraceElement trace = Thread.currentThread().getStackTrace()[STACK_TRACE_EXT_NUM];
        String className = KEY_PREFIX+trace.getClassName();
        Logger logger = loggerMap.get(className);
        if(logger==null){
            logger = LoggerExt.getLogger(className,factory);
            loggerMap.put(className, logger);
        }
        String message = null;
        try {
            if(CHARSET!=null){
                message = new String(Convert.toString(msgObj).getBytes(),CHARSET);
            }else{
                message = Convert.toString(msgObj);
            }
        } catch (UnsupportedEncodingException e) {
            message = Convert.toString(msgObj);
        }
        switch (level){
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

    public static void setCharset(String charset){
        LogUtils.CHARSET = charset;
    }

    public static void setKeyPrefix(String keyPrefix) {
        LogUtils.KEY_PREFIX = keyPrefix;
    }

    private static void log(int level,Object msgObj,Throwable t){
        StackTraceElement trace = Thread.currentThread().getStackTrace()[STACK_TRACE_EXT_NUM];
        String className = KEY_PREFIX+trace.getClassName();
        Logger logger = loggerMap.get(className);
        if(logger==null){
            logger = LoggerExt.getLogger(className,factory);
            loggerMap.put(className,logger);
        }
        String message = null;
        try {
            if(CHARSET!=null){
                message = new String(Convert.toString(msgObj).getBytes(),CHARSET);
            }else{
                message = Convert.toString(msgObj);
            }
        } catch (UnsupportedEncodingException e) {
            message = Convert.toString(msgObj);
        }
        switch (level){
            case DEBUG:
                logger.debug(message,t);
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

    //判断指定类位置打印日志可用否
    private static boolean enabledFor(int level){
        StackTraceElement trace = Thread.currentThread().getStackTrace()[STACK_TRACE_EXT_NUM];
        String className = KEY_PREFIX+trace.getClassName();
        Logger logger = loggerMap.get(className);
        if(logger==null){
            logger = LoggerExt.getLogger(className,factory);
            loggerMap.put(className,logger);
        }
        switch (level){
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

    public static boolean debugEnabled(){
        return enabledFor(DEBUG);
    }

    public static boolean errorEnabled(){
        return enabledFor(ERROR);
    }

    public static boolean fatalEnabled(){
        return enabledFor(FATAL);
    }

    public static boolean infoEnabled(){
        return enabledFor(INFO);
    }

    public static boolean warnEnabled(){
        return enabledFor(WARN);
    }

    public static boolean traceEnabled(){
        return enabledFor(TRACE);
    }

    public static void debug(java.lang.Object message) {
        log(DEBUG,message);
    }
    public static void debug(java.lang.Object message, java.lang.Throwable t) {
        log(DEBUG, message, t);
    }
    public static void error(java.lang.Object message) {
        log(ERROR, message);
    }
    public static void error(java.lang.Object message, java.lang.Throwable t) {
        log(ERROR, message, t);
    }
    public static void fatal(java.lang.Object message) {
        log(FATAL, message);
    }
    public static void fatal(java.lang.Object message, java.lang.Throwable t) {
        log(FATAL, message, t);
    }
    public static void info(java.lang.Object message) {
        log(INFO, message);
    }
    public static void info(java.lang.Object message, java.lang.Throwable t) {
        log(INFO, message, t);
    }
    public static void warn(java.lang.Object message) {
        log(WARN, message);
    }
    public static void warn(java.lang.Object message, java.lang.Throwable t) {
        log(WARN, message, t);
    }
    public static void trace(java.lang.Object message) {
        log(TRACE, message);
    }
    public static void trace(java.lang.Object message, java.lang.Throwable t) {
        log(TRACE, message, t);
    }

    
    private static Properties log4jProp;
    public synchronized static Properties getLog4jConfig(){
        if(log4jProp!=null){
            return log4jProp;
        }
        
        String override = OptionConverter.getSystemProperty(LogManager.DEFAULT_INIT_OVERRIDE_KEY,null);
        // if there is no default init override, then get the resource
        // specified by the user or the default config file.
        if(override == null || "false".equalsIgnoreCase(override)) {
            URL url = Loader.getResource(LogManager.DEFAULT_CONFIGURATION_FILE);
            if(url!=null){
                log4jProp = new Properties();
                InputStream istream = null;
                URLConnection uConn = null;
                try {
                    uConn = url.openConnection();
                    uConn.setUseCaches(false);
                    istream = uConn.getInputStream();
                    log4jProp.load(istream);
                }
                catch (Exception e) {
                    if (e instanceof InterruptedIOException || e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    log4jProp.clear();
                }
                finally {
                    if (istream != null) {
                        try {
                            istream.close();
                        } catch(InterruptedIOException ignore) {
                            Thread.currentThread().interrupt();
                        } catch(IOException ignore) {
                        } catch(RuntimeException ignore) {
                        }
                    }
                }
            }else{
                //无log4j配置
            }
        }
        return log4jProp;
    }
}