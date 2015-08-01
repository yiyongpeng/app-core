package app.core.impl;

import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;

import app.core.AccessException;
import app.core.Connection;
import app.core.MessageOutput;
import app.core.MessageQueue;
import app.core.ServerContext;
import app.core.Session;
import app.filter.IFilterChain;
import app.filter.IProtocolEncodeFilter;
import app.filter.IFilterChain.IChain;

public class DefaultSession extends DefaultContext implements Session {

	private boolean closed;

	private ServerContext server;
	protected Connection conn;

	protected String sessionId;

	public DefaultSession(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public void close() {
		setClosed(true);
		if (conn != null && isDefault())
			conn.close();
	}

	@Override
	public boolean isDefault() {
		return conn.getSession() == this;
	}

	@Override
	public void flush() {
		conn.getWriteRequest().flush(this);
	}

	public void init(ServerContext server) {
		this.server = server;
	}

	public void init(Connection conn) {
		this.conn = conn;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(Object message) {
		if (server.getConnector().isRuning() == false) {
			throw new AccessException(this.getInetAddress()
					+ " send msg failure,  Server is stoped!");
		}

		// socket 已经被关闭
		if (!conn.isClosed()) {
			if (conn.getSocketChannel() instanceof SocketChannel
					&& ((SocketChannel) conn.getSocketChannel()).socket()
							.isClosed()) {
				conn.close();
				throw new AccessException(this+" send msg failed, Connection is closed.");
			}
		}
		IChain<IProtocolEncodeFilter> chain = (IChain<IProtocolEncodeFilter>) server
				.getFilterChain().getFirstChain(
						IFilterChain.FILTER_PROTOCOL_ENCODE);

		if (chain == null) {
			throw new IllegalStateException(
					"No configuration protocol encode filter.");
		}
		try {
			MessageOutput output = getMessageOutputQueue();

			chain.getFilter().messageEncode(conn, message, output, chain);

		} catch (Throwable e) {
			server.getNotifier().fireOnError(this, e);
		}finally{
			flush();
		}
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	public synchronized void setSessionId(String sessionId) {
		if (sessionId.equals(getSessionId()))
			return;

		// 移除旧Session
		if (getSessionId() != null) {
			getServerHandler().removeSession(getSessionId());
		}
		// 设置新Session
		this.sessionId = sessionId;

		getServerHandler().addSession(this);
	}

	@Override
	public MessageQueue getMessageOutputQueue() {
		MessageQueue queue = (MessageQueue) getAttribute(MESSAGE_QUEUE_OUT);
		if (queue == null)
			synchronized (this) {
				queue = (MessageQueue) getAttribute(MESSAGE_QUEUE_OUT);
				if(queue==null){
					queue = server.createMessageQueue();
					setAttribute(MESSAGE_QUEUE_OUT, queue);
				}
			}
		return queue;
	}

	@Override
	public MessageQueue getMessageInputQueue() {
		MessageQueue queue = (MessageQueue) getAttribute(MESSAGE_QUEUE_IN);
		if (queue == null)
			synchronized (this) {
				queue = (MessageQueue) getAttribute(MESSAGE_QUEUE_IN);
				if(queue==null){
					queue = server.createMessageQueue();
					setAttribute(MESSAGE_QUEUE_IN, queue);
				}
			}
		return queue;
	}

	@Override
	public Object getCoverAttributeOfUser(Object key, Object def) {
		// Session
		if (contains(key))
			return getAttribute(key);
		// App
		if (getServerHandler().contains(key))
			return getServerHandler().getAttribute(key);
		return def;
	}

	@Override
	public Object getCoverAttributeOfApp(Object key, Object def) {
		// App
		if (getServerHandler().contains(key))
			return getServerHandler().getAttribute(key);
		// Session
		if (contains(key))
			return getAttribute(key);
		return def;
	}

	public ByteChannel getSocketChannel() {
		return conn.getSocketChannel();
	}

	@Override
	public ServerContext getServerHandler() {
		return server;
	}

	@Override
	public Connection getConnection() {
		return conn;
	}

	@Override
	public String getInetAddress() {
		return conn.getInetAddress();
	}

	@Override
	public String getRemoteAddress() {
		return conn.getRemoteAddress();
	}

	@Override
	public int getRemotePort() {
		return conn.getRemotePort();
	}

	@Override
	public String getLocalAddress() {
		return conn.getLocalAddress();
	}

	@Override
	public int getLocalPort() {
		return conn.getLocalPort();
	}

	public boolean isClosed() {
		return closed;
	}

	protected void setClosed(boolean bool) {
		this.closed = bool;
	}

	public void destory() {
		if (!isClosed()) {
			close();
		}
		MessageQueue out = (MessageQueue) removeAttribute(MESSAGE_QUEUE_OUT);
		if (out != null) {
			out.destory();
		}
		MessageQueue in = (MessageQueue) removeAttribute(MESSAGE_QUEUE_IN);
		if (in != null) {
			in.destory();
		}
	}

}
