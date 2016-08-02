package com.ery.base.support.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;



public class LogFormatter extends java.util.logging.Formatter {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	
	public String format(LogRecord record) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("\n");
		strBuffer.append(sdf.format(new Date(record.getMillis())));
		strBuffer.append(" ");
		strBuffer.append(record.getSourceClassName());
		strBuffer.append(".");
		strBuffer.append(record.getSourceMethodName());
		strBuffer.append("\n");
		strBuffer.append(record.getLevel().getName());
		strBuffer.append(":");
		strBuffer.append(record.getMessage());
		strBuffer.append("\n");
		return strBuffer.toString();
	}
}
