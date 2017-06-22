package com.flume.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.flume.plugin.AbstractPlugin;

public class SourceScheduler {

	protected static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
	protected static Map<String, ScheduledFuture> taskMap = new HashMap<String, ScheduledFuture>();

	public static void runTask(AbstractPlugin plugin) {
		ScheduledFuture<?> future = null;
		if (plugin.getPolicy().equals("cron")) {
			String cron = plugin.getCron();
		} else {
			long initTime = plugin.getInitTime();
			long delay = plugin.getDelay();

			if (plugin.getPolicy().equals("fixRate")) {
				future = scheduler.scheduleAtFixedRate(plugin, 5, 10, TimeUnit.SECONDS);
			} else {
				future = scheduler.scheduleWithFixedDelay(plugin, initTime, delay, TimeUnit.SECONDS);
			}
		}

		taskMap.put(plugin.getName(), future);
	}

	public static void removeTask(String pluginName) {
		ScheduledFuture<?> future = taskMap.get(pluginName);
		future.cancel(true);
		taskMap.remove(pluginName);
	}
}
