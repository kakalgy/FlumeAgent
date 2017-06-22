package com.flume.rmi;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flume.core.Application;
import com.flume.core.UOMPSource;
import com.flume.model.Command;
import com.flume.plugin.PluginManager;

public class AgentRMIServiceImpl extends UnicastRemoteObject implements AgentRMIService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1499141431522389674L;

	private static final Logger logger = LoggerFactory.getLogger(AgentRMIServiceImpl.class);

	public AgentRMIServiceImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
		// TODO Auto-generated constructor stub
		super(port, csf, ssf);
	}

	public AgentRMIServiceImpl() throws RemoteException {
		// TODO Auto-generated constructor stub
		super();
	}

	public String getVersion() throws RemoteException {
		// TODO Auto-generated method stub
		Application flumApp = Application.getInstance();
		return flumApp.version;
	}

	public void parseComand(Command cmd) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			if (cmd.getScope() == 0) {
				Application flueApp = Application.getInstance();
				if (cmd.getAction() == 3) {
					flueApp.restartAllComponents();
				}
			}

			if (cmd.getScope() == 1) {
				PluginManager pluginManager = UOMPSource.pluginManager;
				if (pluginManager == null) {
					Thread.sleep(5000);
				}
				String pluginName = cmd.getName();

				switch (cmd.getAction()) {
				case 0:
					pluginManager.start(pluginName);
					break;
				case 1:
					pluginManager.stop(pluginName);
					break;
				case 2:
					pluginManager.restart(pluginName);
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("fail to excute command", e.getMessage());
		}
	}

}
