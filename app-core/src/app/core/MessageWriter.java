package app.core;

import java.nio.channels.SelectionKey;

public interface MessageWriter<R, S> {

	void processRequest(SelectionKey key);

	void init(Connector<R, S> connector);

	void destory();

}
