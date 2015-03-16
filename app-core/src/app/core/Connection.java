package app.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface Connection {

	/** 创建新的Session */
	Session createSession(String sessionId);

	/** 设置默认Session */
	void setSession(Session session);

	/** 获取默认Session */
	Session getSession();

	/** 获取远程网络地址（含协议头），例如：TCP:127.0.0.1:9000 */
	String getInetAddress();

	/** 获取连接协议（TCP/UDP） */
	String getProtocol();

	/** 获取远程地址 */
	String getRemoteAddress();

	/** 获取远程端口 */
	int getRemotePort();

	/** 获取本地地址 */
	String getLocalAddress();

	/** 获取本地端口 */
	int getLocalPort();

	/** 关闭 */
	void close();

	/** 是否已经关闭 */
	boolean isClosed();

	/** 获取Socket字节管道 */
	ByteChannel getSocketChannel();

	/** 读取已到达的字节集 */
	ByteBuffer read() throws IOException;

	/** 消息写请求 */
	WriteRequest getWriteRequest();

	/** 清空接收缓冲区 */
	void clearRecvBuffer();

	/** 是否繁忙 */
	boolean isBusy();

	/** 是否繁忙，即是否正在发送一个数据包 */
	void setBusy(boolean busy);

}
