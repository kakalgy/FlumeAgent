package com.flume.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.flume.channel.ChannelProcessor;

import com.flume.core.SourceScheduler;

public class PluginManager {
	public Map<String, AbstractPlugin> plugins = new HashMap<String, AbstractPlugin>();
	private String pluginPath;
	private ChannelProcessor ch;

	public PluginManager(String pluginPath, ChannelProcessor ch) {
		// TODO Auto-generated constructor stub
		this.pluginPath = pluginPath;
		this.ch = ch;
	}

	public void startAll() {
		plugins = PluginLoader.loadAll(pluginPath);
		for (Entry<String, AbstractPlugin> entry : plugins.entrySet()) {
			entry.getValue().setCh(ch);
			SourceScheduler.runTask(entry.getValue());
		}
	}

	public void start(String pluginName) {
		plugins.putAll(PluginLoader.load(pluginPath, pluginName));
		SourceScheduler.runTask(plugins.get(pluginName));
	}

	public void stop(String pluginName) {
		SourceScheduler.removeTask(pluginName);
		plugins.get(pluginName).close();
	}

	public void restart(String pluginName) {
	}
}
