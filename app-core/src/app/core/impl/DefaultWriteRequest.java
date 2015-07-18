package app.core.impl;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import app.core.Connection;
import app.core.MessageInput;
import app.core.MessageWriter;
import app.core.Session;
import app.core.WriteRequest;

public class DefaultWriteRequest implements WriteRequest, Runnable {
	private MessageWriter<Connection, Session> writer;

	private Connection conn;
	private BlockingQueue<Session> flushQueue;

	private long packetCount;
	private long nanoTime;
	private long byteCount;

	public DefaultWriteRequest(Connection conn) {
		init(conn);
	}

	@Override
	public void destroy() {
		if (flushQueue != null)
			flushQueue.clear();
		flushQueue = null;
		conn = null;
		writer = null;
	}

	@Override
	public void init(Connection conn) {
		this.conn = conn;
		this.writer = conn.getSession().getServerHandler().getConnector().getMessageWriter();
		this.flushQueue = new ArrayBlockingQueue<Session>(10);
		this.byteCount = 0;
		this.nanoTime = 0;
	}

	@Override
	public void run() {
		WritableByteChannel sc = conn.getSocketChannel();
		Session session = null;
		MessageInput q = null;
		ByteBuffer p = null;
		boolean nomal = false;
		long times;
		try {
			// ---------- Send begin ---------
			for (; (session = flushQueue.poll()) != null;) {
				q = session.getMessageOutputQueue();
				times = System.nanoTime();
				for (; !q.isEmpty();) {
					p = (ByteBuffer) q.getFirst();

					sc.write(p);

					if (p.remaining() == 0) {
						packetCount++;
						byteCount += p.limit();
						q.removeFirst();// 移除已发送数据包
					} else {
						try {
							Thread.sleep(10);// 网速不佳，延迟发送
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				times = System.nanoTime() - times;
				nanoTime += times;
			}
			nomal = true;
			// ----------- Send end ----------
		} catch (Throwable e) {
			e.printStackTrace();
			// 发送失败，复位，等待重连发送
			if(p!=null){
				p.position(0);
			}
		} finally {
			synchronized (this) {
				if (nomal && this.flushQueue.size() > 0)
					writer.execute(this);// 队列没有发送完，继续发送
				else
					conn.setBusy(false);// 全部发送完成，发送结束
			}
		}
	}

	@Override
	public void flush(Session session) {
		if (conn.isClosed()) {
			log.warn("flush invalid, Connect [" + conn.getInetAddress()
					+ "] is closed!");
			return;
		}
		if (session.getConnection() != conn) {
			log.warn("flush invalid, Connect [" + conn.getInetAddress()
					+ "] and [" + session.getConnection().getInetAddress()
					+ "] is distinct!!!");
			return;
		}
		if (!flushQueue.contains(session)) {
			try {
				flushQueue.put(session);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			// log.debug("Already add session flush queue: "
			// + session.getSessionId() + " , outputQueue: "
			// + session.getMessageOutputQueue().size());
		}
		synchronized (this) {
			if (!conn.isBusy()) {
				conn.setBusy(true);
				writer.execute(this);
			} else {
				// log.warn("Already flush, Connect[" + conn.getInetAddress()
				// + "] is writing busy!");
			}
		}
	}

	@Override
	public long getByteCount() {
		return byteCount;
	}

	@Override
	public long getNanoTime() {
		return nanoTime;
	}

	@Override
	public long getPacketCount() {
		return packetCount;
	}

	private static final Logger log = Logger
			.getLogger(DefaultWriteRequest.class);
}
