package app.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AppThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public AppThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
    }
	public String getNamePrefix() {
		return "app-thread-";
	}
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              getNamePrefix() + threadNumber.getAndIncrement(),
                              0);
        t.setContextClassLoader(AppThreadFactory.class.getClassLoader());
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}