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
import app.core.MessageWriter;
import app.core.Notifier;

public class DefaultMessageWriter<R, S> implements MessageWriter<R, S> {
	private static final int CACHE_TASK_MAX = 50;
	private Connector<R, S> connector;
	private Notifier<R, S> notifier;
	private Executor executor;

	public DefaultMessageWriter() {
	}

	public DefaultMessageWriter(int corePoolSize, int maximiumPoolSize,
			int keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		this.executor = new ThreadPoolExecutor(corePoolSize, maximiumPoolSize,
				keepAliveTime, unit, workQueue);
	}

	public DefaultMessageWriter(Executor writer) {
		this.executor = writer;
	}

	@Override
	public void destory() {
		if (this.executor == connector.getExecutor())
			this.executor = null;
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
		this.connector = null;
		this.notifier = null;
	}

	@Override
	public void init(Connector<R, S> connector) {
		this.connector = connector;
		this.notifier = connector.getNotifier();

		if (this.executor == null)
			this.executor = connector.getExecutor();
	}

	@Override
	public void processRequest(SelectionKey key) {
		executor.execute(createTask(key));
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

		if (notifier.fireOnWrite(request))
			connector.processRead(key);// 报文完整写出，请求读取
		else
			connector.processWrite(key);// 报文未写完，继续请求写
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public Executor getExecutor() {
		return executor;
	}
}
