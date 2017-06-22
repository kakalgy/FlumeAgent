package com.flume.core;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.SinkRunner;
import org.apache.flume.SourceRunner;
import org.apache.flume.instrumentation.MonitorService;
import org.apache.flume.instrumentation.MonitoringType;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.flume.lifecycle.LifecycleSupervisor;
import org.apache.flume.lifecycle.LifecycleSupervisor.SupervisorPolicy;
import org.apache.flume.node.MaterializedConfiguration;
import org.apache.flume.node.PropertiesFileConfigurationProvider;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flume.rmi.AgentRMIService;
import com.flume.rmi.AgentRMIServiceImpl;
import com.google.common.base.Throwables;

public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static final String version = "1.0.0";
	public static final String CONF_MONITOR_CLASS = "flume.monitoring.type";
	public static final String CONF_MONITOR_PREFIX = "flume.monitoring.";

	private final LifecycleSupervisor supervisor;
	private MaterializedConfiguration materializedConfiguration;
	private MonitorService monitorServer;

	private static Application instance;

	private static String agentName;
	private static URL confFileUrl;
	private static URL logFileUrl;

	/**
	 * 单例模式
	 * 
	 * @return
	 */
	public static synchronized Application getInstance() {
		if (instance == null) {
			instance = new Application();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	private Application() {
		supervisor = new LifecycleSupervisor();
	}

	public void restartAllComponents() {
		File configurationFile = new File(confFileUrl.getFile());
		PropertiesFileConfigurationProvider configurationProvider = new PropertiesFileConfigurationProvider(agentName, configurationFile);
		handleConfigurationEvent(configurationProvider.getConfiguration());
	}

	private synchronized void handleConfigurationEvent(MaterializedConfiguration conf) {
		stopAllComponents();
		startAllComponents(conf);
	}

	public synchronized void stop() {
		supervisor.stop();
		if (monitorServer != null) {
			monitorServer.stop();
		}
	}

	private void stopAllComponents() {
		if (this.materializedConfiguration != null) {
			logger.info("Shutting down configuration: {}", this.materializedConfiguration);
			for (Entry<String, SourceRunner> entry : this.materializedConfiguration.getSourceRunners().entrySet()) {
				try {
					logger.info("Stopping Source " + entry.getKey());
					supervisor.unsupervise(entry.getValue());
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("Error while stopping {}", entry.getValue(), e);
				}
			}

			for (Entry<String, SinkRunner> entry : this.materializedConfiguration.getSinkRunners().entrySet()) {
				try {
					logger.info("Stopping Source " + entry.getKey());
					supervisor.unsupervise(entry.getValue());
				} catch (Exception e) {
					// TODO: handle exception
					logger.error("Error while stopping {}", entry.getValue(), e);
				}
			}

			for (Entry<String, Channel> entry : this.materializedConfiguration.getChannels().entrySet()) {
				try {
					logger.info("Stopping Channel " + entry.getKey());
					supervisor.unsupervise(entry.getValue());
				} catch (Exception e) {
					logger.error("Error while stopping {}", entry.getValue(), e);
				}
			}
		}

		if (monitorServer != null) {
			monitorServer.stop();
		}
	}

	private void startAllComponents(MaterializedConfiguration materializedConfiguration) {
		logger.info("Starting new configuration:{}", materializedConfiguration);

		this.materializedConfiguration = materializedConfiguration;

		for (Entry<String, Channel> entry : materializedConfiguration.getChannels().entrySet()) {
			try {
				logger.info("Starting Channel " + entry.getKey());
				supervisor.supervise(entry.getValue(), new SupervisorPolicy.AlwaysRestartPolicy(), LifecycleState.START);
			} catch (Exception e) {
				logger.error("Error while starting {}", entry.getValue(), e);
			}
		}

		/*
		 * Wait for all channels to start.
		 */
		for (Channel ch : materializedConfiguration.getChannels().values()) {
			while (ch.getLifecycleState() != LifecycleState.START && !supervisor.isComponentInErrorState(ch)) {
				try {
					logger.info("Waiting for channel: " + ch.getName() + " to start. Sleeping for 500 ms");
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.error("Interrupted while waiting for channel to start.", e);
					Throwables.propagate(e);
				}
			}
		}

		for (Entry<String, SinkRunner> entry : materializedConfiguration.getSinkRunners().entrySet()) {
			try {
				logger.info("Starting Sink " + entry.getKey());
				supervisor.supervise(entry.getValue(), new SupervisorPolicy.AlwaysRestartPolicy(), LifecycleState.START);
			} catch (Exception e) {
				logger.error("Error while starting {}", entry.getValue(), e);
			}
		}

		for (Entry<String, SourceRunner> entry : materializedConfiguration.getSourceRunners().entrySet()) {
			try {
				logger.info("Starting Source " + entry.getKey());
				supervisor.supervise(entry.getValue(), new SupervisorPolicy.AlwaysRestartPolicy(), LifecycleState.START);
			} catch (Exception e) {
				logger.error("Error while starting {}", entry.getValue(), e);
			}
		}

		this.loadMonitoring();
	}

	@SuppressWarnings("unchecked")
	private void loadMonitoring() {
		Properties systemProps = System.getProperties();
		Set<String> keys = systemProps.stringPropertyNames();
		try {
			if (keys.contains(CONF_MONITOR_CLASS)) {
				String monitorType = systemProps.getProperty(CONF_MONITOR_CLASS);
				Class<? extends MonitorService> klass;
				try {
					// Is it a known type?
					klass = MonitoringType.valueOf(monitorType.toUpperCase(Locale.ENGLISH)).getMonitorClass();
				} catch (Exception e) {
					// Not a known type, use FQCN
					klass = (Class<? extends MonitorService>) Class.forName(monitorType);
				}
				this.monitorServer = klass.newInstance();
				Context context = new Context();
				for (String key : keys) {
					if (key.startsWith(CONF_MONITOR_PREFIX)) {
						context.put(key.substring(CONF_MONITOR_PREFIX.length()), systemProps.getProperty(key));
					}
				}
				monitorServer.configure(context);
				monitorServer.start();
			}
		} catch (Exception e) {
			logger.warn("Error starting monitoring. " + "Monitoring might not be available.", e);
		}

	}

	public static void main(String[] args) {
		agentName = "agent";
		confFileUrl = Application.class.getClassLoader().getResource("flume.conf");
		logFileUrl = Application.class.getClassLoader().getResource("log4j.properties");

		try {

			/**
			 * run flume
			 */
			PropertyConfigurator.configure(logFileUrl);
			Application application = Application.getInstance();
			application.restartAllComponents();

			/**
			 * regist local rmi service
			 */
			AgentRMIService ss = new AgentRMIServiceImpl();
			Registry reg = LocateRegistry.createRegistry(8888, null, new RMIServerSocketFactory() {

				public ServerSocket createServerSocket(int arg0) throws IOException {
					ServerSocket ss = new ServerSocket();
					ss.bind(new InetSocketAddress("127.0.0.1", 8888));
					return ss;
				}
			});
			reg.rebind("UOMP-agent", ss);
			logger.info("register rmi service successfully");

			/**
			 * exit process
			 */
			final Application appReference = application;
			Runtime.getRuntime().addShutdownHook(new Thread("agent-shutdown-hook") {
				@Override
				public void run() {
					appReference.stop();
				}
			});

		} catch (Exception e) {
			logger.error("A fatal error occurred while running. Exception follows.", e);
		}
	}

}
