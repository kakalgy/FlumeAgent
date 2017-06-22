package com.flume.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginLoader {
	private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
	private static final String PLUGIN_PROP = "/plugin.properties";

	public static Map<String, AbstractPlugin> loadAll(String pluginPath) {
		Map<String, AbstractPlugin> map = new HashMap<String, AbstractPlugin>();
		File file = new File(pluginPath);
		String[] fileList = file.list();
		for (int i = 0; i < fileList.length; i++) {
			String propFile = pluginPath + fileList[i] + PLUGIN_PROP;
			AbstractPlugin plugin = createPluginInstance(propFile);
			map.put(plugin.getName(), plugin);
		}
		return map;
	}

	public static Map<String, AbstractPlugin> load(String pluginPath, String pluginName) {
		Map<String, AbstractPlugin> map = new HashMap<String, AbstractPlugin>();
		String propFile = pluginPath + pluginName + PLUGIN_PROP;
		AbstractPlugin plugin = createPluginInstance(propFile);
		map.put(plugin.getName(), plugin);
		return map;
	}

	private static AbstractPlugin createPluginInstance(String path) {
		AbstractPlugin plugin = null;
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(path));
			Properties p = new Properties();

			p.load(in);

			Class<?> pluginClazz = Class.forName(p.getProperty("pluginClass"));
			plugin = (AbstractPlugin) pluginClazz.newInstance();
			plugin.setName(p.getProperty("name"));
			plugin.setVersion(p.getProperty("version"));
			plugin.setPolicy(p.getProperty("policy"));
			plugin.setInitTime(Long.parseLong(p.getProperty("initTime")));
			plugin.setDelay(Long.parseLong(p.getProperty("delay")));
			plugin.setCron(p.getProperty("cron"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plugin;
	}
}
