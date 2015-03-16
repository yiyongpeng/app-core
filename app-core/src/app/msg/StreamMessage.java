package app.msg;

public interface StreamMessage extends Message {

	boolean readBoolean();

	byte readByte();

	int readBytes(byte[] bts);

	char readChar();

	double readDouble();

	float readFloat();

	int readInt();

	long readLong();

	short readShort();

	String readString();

	void reset();

	void writeBoolean(boolean value);

	void writeByte(byte value);

	void writeBytes(byte[] value);

	void writeBytes(byte[] value, int offset, int length);

	void writeChar(char value);

	void writeDouble(double value);

	void writeFloat(float value);

	void writeInt(int value);

	void writeLong(long value);

	void writeObject(Object value);

	void writeShort(short value);

	void writeString(String value);
}
