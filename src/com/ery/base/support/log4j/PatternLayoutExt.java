package com.ery.base.support.log4j;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;


public class PatternLayoutExt extends PatternLayout {

    public PatternLayoutExt(String pattern) {
        super(pattern);
    }

    public PatternLayoutExt() {
        super();
    }

    
    @Override
    protected PatternParser createPatternParser(String pattern) {
        return new PatternParserExt(pattern);
    }

}
