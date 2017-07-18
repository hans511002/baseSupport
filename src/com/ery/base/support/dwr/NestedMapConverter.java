package com.ery.base.support.dwr;

import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.directwebremoting.convert.MapConverter;
import org.directwebremoting.dwrp.ParseUtil;
import org.directwebremoting.dwrp.ProtocolConstants;
import org.directwebremoting.extend.ConverterManager;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.TypeHintContext;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.util.Messages;

public class NestedMapConverter extends MapConverter {

	private ConverterManager converterManager = null;

	@SuppressWarnings("unchecked")
	public Object convertInbound(Class paramType, InboundVariable iv, InboundContext inctx) throws MarshallException {
		String value = iv.getValue();

		// If the text is null then the whole bean is null
		if (value.trim().equals(ProtocolConstants.INBOUND_NULL)) {
			return null;
		}

		if (!value.startsWith(ProtocolConstants.INBOUND_MAP_START)) {
			throw new IllegalArgumentException(Messages.getString("MapConverter.FormatError",
					ProtocolConstants.INBOUND_MAP_START));
		}

		if (!value.endsWith(ProtocolConstants.INBOUND_MAP_END)) {
			throw new IllegalArgumentException(Messages.getString("MapConverter.FormatError",
					ProtocolConstants.INBOUND_MAP_END));
		}
		value = value.substring(1, value.length() - 1);

		try {
			Map<String, Object> map;
			// If paramType is concrete then just use whatever we've got.
			if (!paramType.isInterface() && !Modifier.isAbstract(paramType.getModifiers())) {
				// If there is a problem creating the type then we have no way
				// of completing this - they asked for a specific type and we
				// can't create that type. I don't know of a way of finding
				// subclasses that might be instaniable so we accept failure.
				map = (Map<String, Object>) paramType.newInstance();
			} else {
				map = new HashMap<String, Object>();
			}

			TypeHintContext currTypeHintContext = inctx.getCurrentTypeHintContext();
			TypeHintContext keyTypeHintContext = currTypeHintContext.createChildContext(0);
			Class<?> keyType = keyTypeHintContext.getExtraTypeInfo();
			TypeHintContext valTypeHintContext = currTypeHintContext.createChildContext(1);
			Class<?> valType = valTypeHintContext.getExtraTypeInfo();

			// 对已经解析过的数据加入标识，避免重复解析。
			inctx.addConverted(iv, paramType, map);
			InboundContext incx = iv.getLookup();

			StringTokenizer st = new StringTokenizer(value, ",");
			int size = st.countTokens();
			for (int i = 0; i < size; i++) { // 循环MAP
				String token = st.nextToken();
				if (token.trim().length() == 0) {
					continue;
				}

				int colonpos = token.indexOf(ProtocolConstants.INBOUND_MAP_ENTRY);
				if (colonpos == -1) {
					throw new MarshallException(paramType, Messages.getString("MapConverter.MissingSeparator",
							ProtocolConstants.INBOUND_MAP_ENTRY, token));
				}
				// Convert the value part of the token by splitting it into the
				// type and value (as passed in by Javascript)
				String valStr = token.substring(colonpos + 1).trim();
				String[] splitIv = ParseUtil.splitInbound(valStr);
				String splitIvValue = splitIv[LocalUtil.INBOUND_INDEX_VALUE];
				String splitIvType = splitIv[LocalUtil.INBOUND_INDEX_TYPE];
				InboundVariable valIv = new InboundVariable(incx, null, splitIvType, splitIvValue);
				Class<?> currType = valType;
				if (!valIv.getType().equalsIgnoreCase("String")) { // 当JS判断下一个类型不是String时
					if (valIv.getType().equalsIgnoreCase("Object_Object")) {
						// 如果value的JSON格式是{}样式的，强制定义valType为Map型
						currType = Map.class;
					} else if (valIv.getType().equalsIgnoreCase("Array")) {
						// 如果value的JSON格式是[]样式的，强制定义valType为Collection类型型
						currType = Collection.class;
					} else if (valIv.getType().equalsIgnoreCase("number")) {
						// 如果是数字类型
						// 尝试确定number的确切类型
						try {
							NumberFormat numberFormat = NumberFormat.getInstance();
							Object tempValue = numberFormat.parse(valIv.getValue());
							currType = tempValue.getClass();
						} catch (Exception e) {
							throw new MarshallException(paramType, e);
						}
					} else if (valIv.getType().equalsIgnoreCase("boolean")) {
						currType = Boolean.class;
					}
				} else {
					currType = String.class;
				}
				if (currType == Object.class) {
					currType = String.class;
				}
				Object val = converterManager.convertInbound(currType, valIv, inctx, valTypeHintContext);
				// key就直接定义为Stiing类型
				String keyStr = token.substring(0, colonpos).trim();
				InboundVariable keyIv = new InboundVariable(incx, null, ProtocolConstants.TYPE_STRING, keyStr);
				Object key = converterManager.convertInbound(keyType, keyIv, inctx, keyTypeHintContext);
				map.put(key.toString(), val);
			}

			return map;
		} catch (MarshallException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new MarshallException(paramType, ex);
		}
	}

	public void setConverterManager(ConverterManager newConfig) {
		super.setConverterManager(newConfig);
		this.converterManager = newConfig;
	}

}
