package app.msg;

import java.util.Enumeration;

public interface MapMessage extends Message {

	boolean getBoolean(String key);

	byte getByte(String key);

	byte[] getBytes(String key);

	char getChar(String key);

	double getDouble(String key);

	float getFloat(String key);

	int getInt(String key);

	long getLong(String key);

	Enumeration<String> getMapNames();

	Object getObject();

	short getShort(String key);

	String getString(String key);

	boolean itemExists(String key);

	void setBoolean(String key, boolean value);

	void setByte(String key, byte value);

	void setBytes(String key, byte[] value);

	void setBytes(String key, byte[] value, int offset);

	void setChar(String key, char value);

	void setDouble(String key, double value);

	void setFloat(String key, float value);

	void setInt(String key, int value);

	void setLong(String key, long value);

	void setObject(String key, Object value);

	void setShort(String key, short value);

	void setString(String key, String value);

}
