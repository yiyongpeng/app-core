package app.core;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutorService;

/**
 * 连接器
 * 
 * @author yiyongpeng
 * 
 * @param <C>
 *            请求类型
 * @param <W>
 *            响应类型
 */
public interface Connector<C, S> {

	void start();

	void stop();

	Notifier<C, S> getNotifier();

	void processRead(SelectionKey key);

	void processWrite(SelectionKey key);

	boolean isRuning();

	ExecutorService getExecutor();

	void wakeup();

	boolean isSelecting(SelectableChannel channel);
	
	String getName();
}
