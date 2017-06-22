package com.flume.plugin;

public class PluginTest1 extends AbstractPlugin {

	private static int count;

	@Override
	public void close() {
		// TODO Auto-generated method stub
		System.out.println("plugin closed");
	}

	@Override
	public String pollOnce() {
		// TODO Auto-generated method stub
		return "dataXXX:" + count++;
	}
}
