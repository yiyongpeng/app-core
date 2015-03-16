package app.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.core.Context;
import app.core.Remote;
import app.util.POJO;

/**
 * 上下文容器默认实现类
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultContext extends POJO implements Remote, Context {
	private Map<Object, Object> context = new HashMap<Object, Object>(1);

	@Override
	public boolean contains(Object key) {
		return context.containsKey(key);
	}

	@Override
	public Object getAttribute(Object key) {
		return context.get(key);
	}

	@Override
	public Object removeAttribute(Object key) {
		return context.remove(key);
	}

	@Override
	public Object setAttribute(Object key, Object value) {
		return context.put(key, value);
	}

	@Override
	public Object getAttribute(String name, Object defaultValue) {
		synchronized (context) {
			if (context.containsKey(name))
				return context.get(name);
		}
		return defaultValue;
	}

	@Override
	public Object getAttribute(String name) {
		// synchronized (context) {
		return context.get(name);
		// }
	}

	@Override
	public String[] getAttributeNames() {
		synchronized (context) {
			List<String> list = new ArrayList<String>();
			for (Object key : context.keySet())
				if (key instanceof String) {
					list.add((String) key);
				}
			String[] names = new String[list.size()];
			return list.toArray(names);
		}
	}

	@Override
	public Object[] getAttributeValues() {
		synchronized (context) {
			return context.values().toArray();
		}
	}

	@Override
	public Object removeAttribute(String name) {
		synchronized (context) {
			return context.remove(name);
		}
	}

	@Override
	public Object setAttribute(String name, Object value) {
		synchronized (context) {
			return context.put(name, value);
		}
	}

	@Override
	public boolean contains(String name) {
		synchronized (context) {
			return context.containsKey(name);
		}
	}

	@Override
	public void clear() {
		synchronized (context) {
			context.clear();
		}
	}
}
