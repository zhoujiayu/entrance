package com.ytsp.entrance.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

/**
 * 用以执行较低优先级的方法，以免阻塞主线程。注意其中各不同静态方法将根据不同业务的健壮性要求采用不同的
 * 方式处理。
 * @author YY
 *
 */
public class LowPriorityExecutor {
	private static ExecutorService logService =  new ThreadPoolExecutor(3, 15,
            30000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
	private static ExecutorService monitorService =  new ThreadPoolExecutor(3, 15,
            30000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
	private static Logger logger = Logger.getLogger(LowPriorityExecutor.class);
	/**
	 * 执行日志记录，低优先级低健壮性要求。
	 * @param task
	 * @return
	 */
	public static void execLog(final Runnable task)
	{
		final TaskMonitor monitor = new TaskMonitor();
		monitor.future = logService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					monitor.thread = Thread.currentThread();
					task.run();
				} catch (Exception e) {
					logger.error("Statistics log error : ", e);
				}
			}
		});
		monitorService.submit(monitor);
	}
	
	static class TaskMonitor implements Runnable{
		Future future;
		Thread thread;
		@Override
		public void run() {
			try {
				future.get(500, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			} catch (TimeoutException e) {
				logger.error("被InterruptedException", e);
				thread.interrupt();
			}
		}
	}
}
