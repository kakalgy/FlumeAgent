package com.flume.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.flume.model.Command;

public interface AgentRMIService extends Remote {

	public void parseComand(Command cmd) throws RemoteException;

	public String getVersion() throws RemoteException;
}
