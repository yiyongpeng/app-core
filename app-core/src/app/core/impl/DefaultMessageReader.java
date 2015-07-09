package app.core.impl;

import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import app.core.Connector;
import app.core.MessageReader;
import app.core.Notifier;

public class DefaultMessageReader<R, S> implements MessageReader<R, S> {
	private static final int CACHE_TASK_MAX = 50;
	private Connector<R, S> connector;
	private Notifier<R, S> notifier;
	private Executor executor;

	public DefaultMessageReader() {
	}

	public DefaultMessageReader(int corePoolSize, int maximiumPoolSize,
			int keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		executor = new ThreadPoolExecutor(corePoolSize, maximiumPoolSize,
				keepAliveTime, unit, workQueue);
	}

	public DefaultMessageReader(Executor reader) {
		this.executor = reader;
	}

	@Override
	public void destory() {
		if (executor == connector.getExecutor())
			executor = null;
		else {
			if (executor instanceof ExecutorService) {
				ExecutorService service = (ExecutorService) executor;
				executor = null;
				service.shutdown();
				try {
					service.awaitTermination(10L, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void init(Connector<R, S> connector) {
		this.connector = connector;
		notifier = connector.getNotifier();
		if (executor == null)
			executor = connector.getExecutor();
	}

	@Override
	public void processRequest(SelectionKey key) {
		execute(createTask(key));
	}

	protected Runnable createTask(SelectionKey key) {
		Task task = recycle.poll();
		if (task == null)
			task = new Task();
		task.key = key;
		return task;
	}

	private class Task implements Runnable {
		private SelectionKey key;

		@Override
		public void run() {
			execute(key);
			destory();
		}

		void destory() {
			key = null;
			recycle.offer(this);
		}
	}

	private Queue<Task> recycle = new ArrayBlockingQueue<Task>(CACHE_TASK_MAX);

	@SuppressWarnings("unchecked")
	protected void execute(SelectionKey key) {
		R request = (R) key.attachment();

		if (notifier.fireOnRead(request))
			connector.processWrite(key);// 读到完整报文，请求写
		else
			connector.processRead(key);// 不完整报文，继续读取

	}

	@Override
	public void execute(Runnable runnable) {
		executor.execute(runnable);
	}
	
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public Executor getExecutor() {
		return executor;
	}
}
