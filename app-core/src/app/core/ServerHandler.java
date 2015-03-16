package app.core;

import java.nio.channels.SelectableChannel;

public interface ServerHandler<R, S> extends ServerContext {

	void onError(S session, Throwable e);

	void onClosed(R request);

	boolean onWrite(R request, boolean prev) throws Exception;

	boolean onRead(R request, boolean prev) throws Exception;

	R onAccepted(SelectableChannel sc, R prev) throws Exception;

	void onAccept() throws Throwable;

	void destory();

	void init(Connector<R, S> connector);

}
