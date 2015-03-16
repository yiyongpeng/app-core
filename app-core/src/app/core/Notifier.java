package app.core;

import java.nio.channels.SelectableChannel;

public interface Notifier<R, S> {

	void addHandler(ServerHandler<R, S> listener);

	void removeHandler(ServerHandler<R, S> listener);

	void fireOnAccept();

	R fireOnAccepted(SelectableChannel sc, R prev) throws Exception;

	boolean fireOnRead(R request);

	boolean fireOnWrite(R request);

	void fireOnClosed(R request);

	void fireOnError(S request, Throwable e);

	boolean isEmpty();

	void init(Connector<R, S> connector);

	void destory();

}
