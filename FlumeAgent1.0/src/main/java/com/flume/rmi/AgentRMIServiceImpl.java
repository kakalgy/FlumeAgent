package com.flume.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flume.model.Command;

public class AgentRMIServiceImpl extends UnicastRemoteObject implements AgentRMIService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1499141431522389674L;

	private static final Logger logger = LoggerFactory.getLogger(AgentRMIServiceImpl.class);

	public AgentRMIServiceImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)
			throws RemoteException {
		// TODO Auto-generated constructor stub
		super(port, csf, ssf);
	}

	public AgentRMIServiceImpl() throws RemoteException {
		// TODO Auto-generated constructor stub
		super();
	}

	public String getVersion() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void parseComand(Command cmd) throws RemoteException {
		// TODO Auto-generated method stub

	}

}
