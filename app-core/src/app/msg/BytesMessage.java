package app.msg;

public interface BytesMessage extends Message {

	long getBodyLength();

	boolean readBoolean();

	byte readByte();

	int readBytes(byte[] bts);

	int readBytes(byte[] bts, int offset);

	char readChar();

	short readShort();

	int readInt();

	long readLong();

	float readFloat();

	double readDouble();

	int readUnsignedByte();

	int readUnsignedShort();

	String readUTF();

	void reset();

	void writeBoolean(boolean value);

	void writeByte(byte value);

	void writeBytes(byte[] value);

	void writeBytes(byte[] value, int offset);

	void writeChar(char value);

	void writeShort(short value);

	void writeInt(int value);

	void writeLong(long value);

	void writeFloat(float value);

	void writeDouble(double value);

	void writeObject(Object value);

	void writeUTF(String value);

	void writeBytes(byte[] array, int offset, int length);

}
