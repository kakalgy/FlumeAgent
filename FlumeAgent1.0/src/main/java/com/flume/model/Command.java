package com.flume.model;

import java.util.Arrays;

public class Command {

	private int scope;
	private String action;
	private String name;
	private String[] param;

	/**
	 * 构造函数
	 * 
	 * @param str
	 */
	public Command(String str) {
		// TODO Auto-generated constructor stub
		String[] strArr = str.split(":");
		this.scope = Integer.parseInt(strArr[0]);
		this.action = strArr[1];
		this.name = strArr[2];
		this.setParam(Arrays.copyOfRange(strArr, 3, strArr.length));
	}

	public int getScope() {
		return scope;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getParam() {
		return param;
	}

	public void setParam(String[] param) {
		this.param = param;
	}

}
