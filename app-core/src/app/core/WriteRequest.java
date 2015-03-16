package app.core;

public interface WriteRequest extends Runnable {

	void destroy();

	void init(Connection session);

	void flush(Session session);

	int getPacketCount();

	int getByteCount();

	long getNanoTime();

}
