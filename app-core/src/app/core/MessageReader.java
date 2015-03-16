package app.core;

import java.nio.channels.SelectionKey;

public interface MessageReader<R, S> {

	void processRequest(SelectionKey key);

	void init(Connector<R, S> connector);

	void destory();

}
