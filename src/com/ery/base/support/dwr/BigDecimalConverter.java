package com.ery.base.support.dwr;

import org.directwebremoting.convert.BigNumberConverter;
import org.directwebremoting.dwrp.SimpleOutboundVariable;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;

import java.math.BigDecimal;


public class BigDecimalConverter extends BigNumberConverter {


    public OutboundVariable convertOutbound(Object object, OutboundContext outctx) {
        if (object == null) {
            return new SimpleOutboundVariable("null", outctx, true);
        }
        String value = object.toString();
        try {
            if (new BigDecimal(value).compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0) {
                value = '\"' + value + '\"';
            }
        } catch (Exception e) {
        }
        return new SimpleOutboundVariable(value, outctx, true);
    }
}
