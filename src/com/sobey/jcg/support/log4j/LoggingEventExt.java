package com.sobey.jcg.support.log4j;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

public class LoggingEventExt extends LoggingEvent {

	protected LocationInfo locationInfo;

	public LoggingEventExt(String fqnOfCategoryClass, Category logger, Priority level, Object message, Throwable throwable) {
		super(fqnOfCategoryClass, logger, level, message, throwable);
	}

	@Override
	public LocationInfo getLocationInformation() {
		if (locationInfo == null) {
			StackTraceElement[] traces = Thread.currentThread().getStackTrace();
			String fileName = null;
			String className = null;
			String methodName = null;
			String lineNumber = null;
			for (int i = traces.length - 1; i >= 0; i--) {
				StackTraceElement trace = traces[i];
				String thisClass = trace.getClassName();
				if (fqnOfCategoryClass.equals(thisClass)) {
					int caller = 0;
					if (traces[i + 1].getClassName().equals(LogUtils.class.getCanonicalName())) {
						caller = i + LogUtils.STACK_TRACE_EXT_NUM;
					} else {
						caller = i + 1;
					}
					if (caller < traces.length) {
						fileName = traces[caller].getFileName();
						if (fileName == null) {
							fileName = LocationInfo.NA;
						}
						className = traces[caller].getClassName();
						methodName = traces[caller].getMethodName();
						int line = traces[caller].getLineNumber();
						if (line < 0) {
							lineNumber = LocationInfo.NA;
						} else {
							lineNumber = String.valueOf(line);
						}
					}
					locationInfo = new LocationInfo(fileName, className, methodName, lineNumber);
					break;
				}
			}
			if (locationInfo == null) {
				locationInfo = super.getLocationInformation();
			}
		}
		return locationInfo;
	}
}
