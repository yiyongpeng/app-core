package app.core.impl;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import app.core.Connection;
import app.core.Connector;
import app.core.Session;
import app.core.WriteRequest;
import app.util.ByteBufferUtils;
import app.util.POJO;

public class DefaultConnection extends POJO implements Connection {
	private Session session;

	protected ByteChannel channel;

	private String localAddress;
	private int localPort;

	private String protocol;
	private String address;
	private int port;

	private String inetAddress;

	private ByteBuffer recvBuffer;

	private volatile boolean closed;
	private volatile boolean writeBusy;
	private volatile WriteRequest writer;

	public void init(ByteChannel sc) {
		this.channel = sc;
		this.localAddress = getAddress(sc, false);
		this.localPort = getPort(sc, false);
		this.protocol = getProtocol(sc);
		this.address = getAddress(sc, true);
		this.port = getPort(sc, true);
		this.inetAddress = null;
		this.closed = false;
		recvBuffer = null;
		writeBusy = false;
	}

	public void destory() {
		this.recvBuffer = null;
		this.writeBusy = false;
		this.writer = null;
	}

	public void onClosed() {
		closed = true;
		clearRecvBuffer();
	}

	@Override
	public WriteRequest getWriteRequest() {
		if (writer == null)
			synchronized (this) {
				if(writer==null){
					writer = session.getServerHandler().createWriteRequest(this);
				}
			}
		return writer;
	}

	@Override
	public ByteBuffer read() throws IOException {
		ByteBuffer buff = getInputBuffer();
		ReadableByteChannel sc = getSocketChannel();
		int size = 0;
		for (;(size = sc.read(buff)) > 0;) {
			if (buff.remaining() == 0) {
				ByteBuffer tmp = buff;
				buff = ByteBufferUtils.create((int) (tmp.capacity() * 1.75));// 1.75倍自增
				buff.put((ByteBuffer) tmp.flip());
				setInputBuffer(buff);
			}
			if(buff.position()>=Session.DEFAULT_IO_BUFFER_CAPACITY){
				break;
			}
		}
		if (size == -1) {
			sc.close();// 到达文件尾
		}
		buff.flip();
		return buff;
	}

	@Override
	public boolean isBusy() {
		return writeBusy;
	}

	@Override
	public void setBusy(boolean value) {
		writeBusy = value;
	}

	private void setInputBuffer(ByteBuffer buff) {
		recvBuffer = buff;
	}

	public ByteBuffer getInputBuffer() {
		if (recvBuffer == null) {
			recvBuffer = ByteBufferUtils.create(getBufferCapacity());
		}
		return recvBuffer;
	}

	private int getBufferCapacity() {
		return (Integer) session.getCoverAttributeOfUser(Session.IO_BUFFER_CAPACITY, Session.DEFAULT_IO_BUFFER_CAPACITY);
	}

	private String getProtocol(ByteChannel sc) {
		if (sc instanceof DatagramChannel) {
			return "UDP";
		}
		if (sc instanceof SocketChannel) {
			return "TCP";
		}
		return "Unkown";
	}

	private int getPort(ByteChannel sc, boolean remote) {
		if (sc instanceof DatagramChannel) {
			DatagramSocket sock = ((DatagramChannel) sc).socket();
			return remote ? sock.getPort() : sock.getLocalPort();
		}
		if (sc instanceof SocketChannel) {
			Socket sock = ((SocketChannel) sc).socket();
			return remote ? sock.getPort() : sock.getLocalPort();
		}
		return 0;
	}

	private String getAddress(ByteChannel sc, boolean remote) {
		if (sc instanceof DatagramChannel) {
			DatagramSocket sock = ((DatagramChannel) sc).socket();
			return remote ? sock.getInetAddress().getHostAddress() : sock.getLocalAddress().getHostAddress();
		}
		if (sc instanceof SocketChannel) {
			Socket sock = ((SocketChannel) sc).socket();
			return remote ? sock.getInetAddress().getHostAddress() : sock.getLocalAddress().getHostAddress();
		}
		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void close() {
		try {
			if (closed)
				return;
			closed = true;

			if (session != null && session.getServerHandler() != null) {
				Connector<Connection, Session> connector = this.session.getServerHandler().getConnector();
				boolean selecting = connector.isSelecting((SelectableChannel) channel);
				// close channel
				channel.close();
				//
				if (selecting) {
					((DefaultConnector) connector).onClosed(this);
				}
			}
		} catch (IOException e) {
		} finally {
			Thread.dumpStack();
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public void setSession(Session session) {
		if (this.session != session) {
			this.session = session;
		}
	}

	@Override
	public String getInetAddress() {
		if (inetAddress == null)
			inetAddress = getProtocol() + ":" + getRemoteAddress() + ":" + getRemotePort();
		return inetAddress;
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getRemoteAddress() {
		return address;
	}

	@Override
	public int getRemotePort() {
		return port;
	}

	@Override
	public String getLocalAddress() {
		return localAddress;
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}

	@Override
	public ByteChannel getSocketChannel() {
		return channel;
	}

	@Override
	public void clearRecvBuffer() {
		recvBuffer = null;
	}

	@Override
	public Session createSession(String sessionId) {
		return getSession().getServerHandler().createSession(this, sessionId);
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(getLocalAddress()).append(":").append(getLocalPort()).append("  <=>  ").append(getInetAddress()).append("]").toString();
	}
}
