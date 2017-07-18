package com.ery.base.support.dwr;

import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.directwebremoting.convert.CollectionConverter;
import org.directwebremoting.dwrp.ParseUtil;
import org.directwebremoting.dwrp.ProtocolConstants;
import org.directwebremoting.extend.ConverterManager;
import org.directwebremoting.extend.InboundContext;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.MarshallException;
import org.directwebremoting.extend.TypeHintContext;
import org.directwebremoting.util.LocalUtil;
import org.directwebremoting.util.Messages;

public class NestedCollectionConverter extends CollectionConverter {

	private ConverterManager converterManager = null;

	@SuppressWarnings("unchecked")
	public Object convertInbound(Class paramType, InboundVariable iv, InboundContext inctx) throws MarshallException {
		String value = iv.getValue();

		// 判断字符串是否是以"["起始，以"]"作为结尾。
		if (value.trim().equals(ProtocolConstants.INBOUND_NULL)) {
			return null;
		}

		if (!value.startsWith(ProtocolConstants.INBOUND_ARRAY_START)) {
			throw new MarshallException(paramType, Messages.getString("CollectionConverter.FormatError",
					ProtocolConstants.INBOUND_ARRAY_START));
		}

		if (!value.endsWith(ProtocolConstants.INBOUND_ARRAY_END)) {
			throw new MarshallException(paramType, Messages.getString("CollectionConverter.FormatError",
					ProtocolConstants.INBOUND_ARRAY_END));
		}

		value = value.substring(1, value.length() - 1);

		try {
			TypeHintContext typeHintContext = inctx.getCurrentTypeHintContext();

			TypeHintContext subTypeHintContext = typeHintContext.createChildContext(0);
			Class<?> subtype = subTypeHintContext.getExtraTypeInfo();

			// 实例化Collection
			Collection<Object> collection;

			// If they want an iterator then just use an array list and fudge
			// at the end.
			if (Iterator.class.isAssignableFrom(paramType)) {
				collection = new ArrayList<Object>();
			} else if (!paramType.isInterface() && !Modifier.isAbstract(paramType.getModifiers())) {
				// If there is a problem creating the type then we have no way
				// of completing this - they asked for a specific type and we
				// can't create that type. I don't know of a way of finding
				// subclasses that might be instaniable so we accept failure.
				collection = (Collection<Object>) paramType.newInstance();
			} else if (SortedSet.class.isAssignableFrom(paramType)) {
				collection = new TreeSet<Object>();
			} else if (Set.class.isAssignableFrom(paramType)) {
				collection = new HashSet<Object>();
			} else if (List.class.isAssignableFrom(paramType)) {
				collection = new ArrayList<Object>();
			} else if (Collection.class.isAssignableFrom(paramType)) {
				collection = new ArrayList<Object>();
			} else {
				throw new MarshallException(paramType);
			}

			// 对已经解析过的数据加入标识，避免重复解析。
			inctx.addConverted(iv, paramType, collection);

			StringTokenizer st = new StringTokenizer(value, ProtocolConstants.INBOUND_ARRAY_SEPARATOR);
			int size = st.countTokens();
			for (int i = 0; i < size; i++) {
				String token = st.nextToken();
				String[] split = ParseUtil.splitInbound(token);
				String splitType = split[LocalUtil.INBOUND_INDEX_TYPE];
				String splitValue = split[LocalUtil.INBOUND_INDEX_VALUE];
				InboundVariable nested = new InboundVariable(iv.getLookup(), null, splitType, splitValue);
				// 新增：张伟

				Class<?> currType = subtype; // 当前正解析的元素的类型，每个元素的类型不见得总是一致。
				if (!nested.getType().equalsIgnoreCase("String")) {
					if (nested.getType().equalsIgnoreCase("Object_Object")) {
						// 如果value的JSON格式是{}样式的，强制定义valType为Map型
						currType = Map.class;
					} else if (nested.getType().equalsIgnoreCase("Array")) {
						// 如果value的JSON格式是[]样式的，强制定义valType为Collection类型型
						currType = Collection.class;
					} else if (nested.getType().equalsIgnoreCase("number")) {
						// 尝试确定number的确切类型
						try {
							NumberFormat numberFormat = NumberFormat.getInstance();
							Object tempValue = numberFormat.parse(nested.getValue());
							currType = tempValue.getClass();
						} catch (Exception e) {
							throw new MarshallException(paramType, e);
						}
					}
				}
				Object output = converterManager.convertInbound(currType, nested, inctx, subTypeHintContext);
				collection.add(output);
			}
			if (Iterator.class.isAssignableFrom(paramType)) {
				return collection.iterator();
			} else {
				return collection;
			}
		} catch (Exception ex) {
			throw new MarshallException(paramType, ex);
		}
	}

	public void setConverterManager(ConverterManager newConfig) {
		super.setConverterManager(newConfig);
		this.converterManager = newConfig;
	}

}
