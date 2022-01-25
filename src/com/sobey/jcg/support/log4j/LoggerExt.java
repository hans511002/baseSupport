package com.sobey.jcg.support.log4j;

import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;

public class LoggerExt extends Logger {

	protected LoggerExt(String name) {
		super(name);
	}

	public void setRepository(LoggerRepository repository) {
		this.repository = repository;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}

	public static Logger getLogger(String name, LoggerFactory factory) {
		return LogManager.getLogger(name, factory);
	}

	protected void forcedLog(String fqcn, Priority level, Object message, Throwable t) {
		callAppenders(new LoggingEventExt(fqcn, this, level, message, t));
	}

}
