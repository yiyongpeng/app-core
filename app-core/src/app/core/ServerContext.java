package app.core;

import java.nio.channels.ByteChannel;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import app.filter.IFilterChain;

public interface ServerContext extends Context {
	/** 获取在线Session总数 */
	int getSessionCount();

	/** 获取在线SessionId集合 */
	String[] getSessionIds();

	/** 获取在线连接集合 */
	Collection<Connection> getConnections();

	/** 获取连接对象 */
	Connection getConnection(ByteChannel sc);

	/** 获取连接器 */
	Connector<Connection, Session> getConnector();

	/** 创建新的Session */
	Session createSession(Connection conn, Object sid);

	Session addSession(Session session);

	Session getSession(String sid);

	boolean hasSessionId(String sessionId);

	Session removeSession(String sid);

	MessageQueue createMessageQueue();

	ScheduledFuture<?> schedule(Runnable runnable, long delay);

	ScheduledFuture<?> schedule(Runnable runnable, long delay, long period);

	/**
	 * 计划任务
	 * 
	 * @param run
	 *            运行任务
	 * @param delay
	 *            运行前延迟时间
	 */
	void execute(Runnable run, long delay);

	void execute(Runnable runnable);

	Notifier<Connection, Session> getNotifier();

	IFilterChain getFilterChain();

	WriteRequest createWriteRequest(Connection session);

	void setSessionFactory(SessionFactory sessionFactory);

	SessionFactory getSessionFactory();
}
