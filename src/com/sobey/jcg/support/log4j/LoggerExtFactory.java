package com.sobey.jcg.support.log4j;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class LoggerExtFactory implements LoggerFactory {

	@Override
	public Logger makeNewLoggerInstance(String name) {
		return new LoggerExt(name);
	}
}
