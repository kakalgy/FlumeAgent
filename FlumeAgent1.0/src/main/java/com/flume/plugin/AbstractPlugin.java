package com.flume.plugin;

import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.EventBuilder;

public abstract class AbstractPlugin implements Runnable {

	private String name;
	private String version;

	private String policy;
	private long initTime;
	private long delay;
	private String cron;

	private ChannelProcessor ch;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public long getInitTime() {
		return initTime;
	}

	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public ChannelProcessor getCh() {
		return ch;
	}

	public void setCh(ChannelProcessor ch) {
		this.ch = ch;
	}

	public void run() {
		// TODO Auto-generated method stub
		String data = pollOnce();
		Event e = EventBuilder.withBody(data.getBytes());
		ch.processEvent(e);
	}

	public abstract void close();

	public abstract String pollOnce();

}
