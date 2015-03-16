package app.core.impl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import app.core.Connection;
import app.core.Connector;
import app.core.MessageReader;
import app.core.MessageWriter;
import app.core.Notifier;
import app.util.AppThreadFactory;

public class DefaultConnector<R, S> implements Connector<R, S>, Runnable {
	private static final AtomicInteger nextId = new AtomicInteger(1);
	private static final int QUEUE_REQUEST_MAX = 2048;
	private Thread thread;
	protected ExecutorService executorService;

	protected Selector selector;

	protected MessageReader<R, S> reader;
	protected MessageWriter<R, S> writer;

	private BlockingQueue<SelectionKey> queue4read;// 读
	private BlockingQueue<SelectionKey> queue4write;// 写

	private BlockingQueue<ServerSocketChannel> queue4server;// 服务端
	private BlockingQueue<SocketChannel> queue4client;// 客户端
	private BlockingQueue<Object[]> queue4medley;// 混合请求

	protected Notifier<R, S> notifier;
	private String name = "NIOConnector-" + nextId.getAndIncrement();

	public DefaultConnector() {
		this(null);
		executorService = newCachedThreadPool(this, "executor");
	}

	public DefaultConnector(ExecutorService executer) {
		this(executer, new DefaultNotifier<R, S>(),
				new DefaultMessageReader<R, S>(),
				new DefaultMessageWriter<R, S>());
	}

	public DefaultConnector(ExecutorService executer, Notifier<R, S> notifer,
			MessageReader<R, S> reader, MessageWriter<R, S> writer) {

		this.executorService = executer;
		this.reader = reader;
		this.writer = writer;
		this.notifier = notifer;

		this.queue4read = new ArrayBlockingQueue<SelectionKey>(
				QUEUE_REQUEST_MAX);
		this.queue4write = new ArrayBlockingQueue<SelectionKey>(
				QUEUE_REQUEST_MAX);

		this.queue4server = new ArrayBlockingQueue<ServerSocketChannel>(
				QUEUE_REQUEST_MAX);
		this.queue4client = new ArrayBlockingQueue<SocketChannel>(
				QUEUE_REQUEST_MAX);
		this.queue4medley = new ArrayBlockingQueue<Object[]>(QUEUE_REQUEST_MAX);

	}
	
	public ExecutorService newCachedThreadPool(final Connector<R, S> connector, final String name) {
		return Executors.newCachedThreadPool(new AppThreadFactory(){
			public String getNamePrefix() {
				return connector.getName()+"-"+name+"-";
			}
		});
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			Thread current = Thread.currentThread();
			ServerSocketChannel ss;
			Iterator<SelectionKey> keys;
			SelectionKey key = null;
			int size;
			synchronized (this) {
				try {
					init();
				} finally {
					notify();
				}
			}
			while (current == thread) {
				size = selector.select();
				if (size == 0) {
					addRegistor();
					continue;
				}
				keys = selector.selectedKeys().iterator();
				while (keys.hasNext())
					try {
						key = keys.next();
						keys.remove();
						if (key.isValid()) {
							if (key.isReadable()) {
								key.cancel();
								reader.processRequest(key);
							} else if (key.isWritable()) {
								key.cancel();
								writer.processRequest(key);
							} else if (key.isAcceptable()) {
								notifier.fireOnAccept();
								ss = ((ServerSocketChannel) key.channel());
								if (ss.isOpen()) {
									accept4server(ss.accept());
								} else {
									key.cancel();
								}
							}
						} else {
							key.cancel();
							if (key.attachment() instanceof Connection) {
								onClosed((R) key.attachment());
							}
						}
					} catch (Throwable e) {
						notifier.fireOnError(null, e);
					}
			}
		} catch (Throwable e) {
			notifier.fireOnError(null, e);
		} finally {
			thread = null;
			destory();
		}
	}

	protected void destory() {
		if(thread!=null)
			throw new IllegalStateException("The server is runing.");

		beforeDestroy();
		
		shutdownExecutor();
		
		notifier.destory();
		reader.destory();
		writer.destory();

		afterDestroy();
	}

	protected void afterDestroy() {
		closeChannels();
		closeSelector();
	}

	protected void beforeDestroy() {
	}

	protected void shutdownExecutor() {
	   executorService.shutdown(); // Disable new tasks from being submitted
	   try {
	     // Wait a while for existing tasks to terminate
	     if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
	       executorService.shutdownNow(); // Cancel currently executing tasks
	       // Wait a while for tasks to respond to being cancelled
	       if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
	           System.err.println("ExecutorService did not terminate");
	     }
	   } catch (InterruptedException ie) {
	     // (Re-)Cancel if current thread also interrupted
	     executorService.shutdownNow();
	   }
	}

	protected void init() {
		if (executorService == null || executorService.isShutdown()) {
			executorService = newCachedThreadPool(this, "executor");
		}
		reader.init(this);
		writer.init(this);
		notifier.init(this);
	}

	@SuppressWarnings("unchecked")
	protected void accept(SelectableChannel sc, R request) throws Exception {
		try {
			request = notifier.fireOnAccepted(sc, request);// 必须先通知再请求读数据
			addRegistor(sc, SelectionKey.OP_READ, request);
		} catch (Throwable e) {
			notifier.fireOnError(
					request != null ? (S) ((Connection) request).getSession()
							: null, e);
			sc.close();
		}
	}

	protected void accept4server(SelectableChannel sc) throws Exception {
		accept(sc, null);
	}

	@SuppressWarnings("unchecked")
	protected void addRegistor() {
		// 添加读写请求
		addRegistors(queue4read, SelectionKey.OP_READ);
		addRegistors(queue4write, SelectionKey.OP_WRITE);

		// 处理自定义接收请求
		while (queue4medley.isEmpty() == false)
			try {
				Object[] aq = queue4medley.poll();
				SelectableChannel sc = (SelectableChannel) aq[0];
				R request = (R) aq[1];
				accept(sc, request);
			} catch (Exception e) {
				notifier.fireOnError(null, e);
			}

		// 处理接收套接字请求
		while (queue4client.isEmpty() == false)
			try {
				accept4server(queue4client.poll());
			} catch (Exception e) {
				e.printStackTrace();
			}

		// 处理接听套接字请求
		while (queue4server.isEmpty() == false)
			try {
				addRegistor(queue4server.poll(), SelectionKey.OP_ACCEPT, this);
			} catch (Exception e) {
				notifier.fireOnError(null, e);
			}
	}

	@SuppressWarnings("unchecked")
	private void addRegistors(Queue<SelectionKey> queue, int ops) {
		SelectionKey key;
		while ((key = queue.poll()) != null) {
			R request = null;
			try {
				request = (R) key.attachment();
				addRegistor(key.channel(), ops, request);
			} catch (ClosedChannelException e) {
				onClosed(request);
			} catch (Throwable e) {
				notifier.fireOnError((S) ((Connection) request).getSession(), e);
			}
		}
	}

	private SelectionKey addRegistor(SelectableChannel channel, int ops,
			Object attatch) throws IOException {
		if (channel != null) {
			channel.configureBlocking(false);
			return channel.register(selector, ops, attatch);
		}
		return null;
	}
	
	@Override
	public void start() {
		if (thread != null)
			return;

		synchronized (this) {
			try {
				selector = Selector.open();
				thread = new Thread(this, name);
				thread.setDaemon(true);
				thread.start();
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		if (thread == null)
			return;
		Thread tmp = thread;
		thread = null;
		selector.wakeup();
		try {
			tmp.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		onStop();
	}

	protected void onStop() {
	}

	@SuppressWarnings("unchecked")
	public void closeChannels() {
		// 关闭所有注册的键
		Set<SelectionKey> keysSet = new HashSet<SelectionKey>();
		keysSet.addAll(selector.keys());
		keysSet.addAll(queue4read);
		keysSet.addAll(queue4write);
		Iterator<SelectionKey> keys = keysSet.iterator();
		while (keys.hasNext())
			try {
				SelectionKey key = keys.next();
				key.cancel();
				try {
					key.channel().close();
				} catch (Throwable e) {
				} finally {
					Object attach = key.attachment();
					if (attach != null && attach instanceof Connection) {
						onClosed((R) attach);
					}
				}
			} catch (Throwable e) {
				notifier.fireOnError(null, e);
			}
			
	}

	protected void onClosed(R request) {
		notifier.fireOnClosed(request);
	}

	protected void closeSelector() {
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			selector = null;
		}
	}

	@Override
	public Notifier<R, S> getNotifier() {
		return notifier;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void processRead(SelectionKey key) {
		try {
			queue4read.put(key);
			selector.wakeup();
		} catch (Exception e) {
			notifier.fireOnError(
					(S) ((Connection) key.attachment()).getSession(), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void processWrite(SelectionKey key) {
		try {
			queue4write.put(key);
			selector.wakeup();
		} catch (Exception e) {
			notifier.fireOnError(
					(S) ((Connection) key.attachment()).getSession(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void registor(SelectableChannel sc, R request) {
		try {
			queue4medley.put(new Object[] { sc, request });
			selector.wakeup();
		} catch (InterruptedException e) {
			notifier.fireOnError((S) ((Connection) request).getSession(), e);
		}
	}

	public void registor(ServerSocketChannel... sscs) {
		try {
			for (ServerSocketChannel ssc : sscs)
				queue4server.put(ssc);
			selector.wakeup();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void registor(SocketChannel... scs) {
		try {
			for (SocketChannel sc : scs)
				queue4client.put(sc);
			selector.wakeup();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isRuning() {
		return thread != null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public ExecutorService getExecutor() {
		return executorService;
	}

	@Override
	public void wakeup() {
		if (selector != null) {
			selector.wakeup();
		}
	}

	@Override
	public boolean isSelecting(SelectableChannel channel) {
		SelectionKey key = channel.keyFor(selector);
		return key != null && key.isValid();
	}
}
