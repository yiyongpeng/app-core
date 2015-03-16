package app.core;

public interface Context {

	Object setAttribute(Object key, Object value);

	Object setAttribute(String name, Object value);

	Object getAttribute(String name);

	Object getAttribute(Object key);

	Object getAttribute(String name, Object defaultValue);

	Object removeAttribute(String name);

	Object removeAttribute(Object key);

	boolean contains(String name);

	boolean contains(Object key);

	String[] getAttributeNames();

	Object[] getAttributeValues();

	void clear();
}
