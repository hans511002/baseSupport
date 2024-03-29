package com.sobey.jcg.support.dwr;

import java.math.BigDecimal;

import org.directwebremoting.convert.BigNumberConverter;
import org.directwebremoting.dwrp.SimpleOutboundVariable;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;

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
