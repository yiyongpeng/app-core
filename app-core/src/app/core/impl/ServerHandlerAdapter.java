package app.core.impl;

import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import app.core.Connection;
import app.core.Connector;
import app.core.MessageQueue;
import app.core.Notifier;
import app.core.ServerHandler;
import app.core.Session;
import app.core.SessionFactory;
import app.core.WriteRequest;
import app.filter.IFilterChain;

public class ServerHandlerAdapter<R, S> extends DefaultContext implements
		ServerHandler<R, S> {

	@Override
	public void init(Connector<R, S> connector) {
	}

	@Override
	public void destory() {
	}

	@Override
	public void onAccept() throws Exception {
	}

	@Override
	public R onAccepted(SelectableChannel sc, R prev) throws Exception {
		return prev;
	}

	@Override
	public void onClosed(R request) {
	}

	@Override
	public void onError(S request, Throwable e) {
		e.printStackTrace();
	}

	@Override
	public boolean onRead(R request, boolean prev) throws Exception {
		return false;
	}

	@Override
	public boolean onWrite(R request, boolean prev) throws Exception {
		return false;
	}

	@Override
	public int getSessionCount() {

		return 0;
	}

	@Override
	public String[] getSessionIds() {

		return null;
	}

	@Override
	public Collection<Connection> getConnections() {

		return null;
	}

	@Override
	public Connection getConnection(ByteChannel sc) {

		return null;
	}

	@Override
	public Connector<Connection, Session> getConnector() {

		return null;
	}

	@Override
	public Session createSession(Connection conn, Object sid) {

		return null;
	}

	@Override
	public Session addSession(Session session) {

		return null;
	}

	@Override
	public Session getSession(String sid) {

		return null;
	}

	@Override
	public boolean hasSessionId(String sessionId) {

		return false;
	}

	@Override
	public Session removeSession(String sid) {

		return null;
	}

	@Override
	public MessageQueue createMessageQueue() {

		return null;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable runnable, long delay) {

		return null;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable runnable, long delay,
			long period) {

		return null;
	}

	@Override
	public void execute(Runnable run, long delay) {

	}

	@Override
	public void execute(Runnable runnable) {

	}

	@Override
	public Notifier<Connection, Session> getNotifier() {

		return null;
	}

	@Override
	public IFilterChain getFilterChain() {

		return null;
	}

	@Override
	public WriteRequest createWriteRequest(Connection session) {

		return null;
	}

	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {

	}

	@Override
	public SessionFactory getSessionFactory() {

		return null;
	}

}
