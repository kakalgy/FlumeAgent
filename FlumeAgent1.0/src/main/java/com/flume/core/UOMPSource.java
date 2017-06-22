package com.flume.core;

import org.apache.flume.Context;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flume.plugin.PluginManager;

public class UOMPSource extends AbstractSource implements EventDrivenSource, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(UOMPSource.class);

	private static String pluginPath;
	public static PluginManager pluginManager;

	public void configure(Context context) {
		// TODO Auto-generated method stub
		pluginPath = context.getString("pluginPath");
	}

	public void start() {
		ChannelProcessor ch = getChannelProcessor();
		pluginManager = new PluginManager(pluginPath, ch);
		pluginManager.startAll();
	}

	public void stop(String pluginName) {
		pluginManager.stop(pluginName);
	}
}
