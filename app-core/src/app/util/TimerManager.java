package app.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimerManager {
	private String name = "TimerManager";
	
	public TimerManager() {
	}
	
	public TimerManager(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void init() {
		if (executorService == null) {
			executorService = new ScheduledThreadPoolExecutor(corePoolSize, new AppThreadFactory(){
				@Override
				public String getNamePrefix() {
					return name+"-";
				}
			});
			executorService.setMaximumPoolSize(maxPoolSize);
			executorService.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);
		}
	}

	public void destory() {
		if (executorService != null) {
			executorService.shutdown();
			try {
				executorService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				executorService = null;
			}
		}
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public static TimerManager getInstance() {
		if (instance == null)
			instance = new TimerManager("TimerManager");
		return instance;
	}

	private static TimerManager instance;

	public void execute(Runnable run) {
		executorService.execute(new Delegate(run));
	}

	public ScheduledFuture<?> schedule(Runnable run, long delay,
			TimeUnit timeUnit) {
		return executorService.schedule(new Delegate(run), delay, timeUnit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable run, long delay,
			long period, TimeUnit timeUnit) {
		return executorService.scheduleAtFixedRate(new Delegate(run), delay,
				period, timeUnit);
	}

	private class Delegate implements Runnable {
		Runnable runnable;

		public Delegate(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				runnable.run();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	private int corePoolSize = 10;
	private int maxPoolSize = 50;
	private long keepAliveTime = 1200;
	private ScheduledThreadPoolExecutor executorService;
}
