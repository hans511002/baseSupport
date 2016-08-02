package com.ery.base.support.log4j;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;


public class PatternParserExt extends PatternParser {

    private final static char THREAD_ID_ch = 'T';//线程ID

    public PatternParserExt(String pattern) {
        super(pattern);
    }

    
    @Override
    protected void finalizeConverter(char c) {
        switch (c){
            case THREAD_ID_ch:
                addConverter(new ThreadIDConverter(this.formattingInfo));
                currentLiteral.setLength(0);
                break;
            default:
                super.finalizeConverter(c);
        }
    }

    //线程ID
    private static class ThreadIDConverter extends PatternConverter {
        public ThreadIDConverter(FormattingInfo fi) {
            super(fi);
        }
        @Override
        protected String convert(LoggingEvent event) {
            return String.valueOf(Thread.currentThread().getId());
        }
    }
}
