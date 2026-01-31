package com.groupunix.drivewireserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.fazecast.jSerialComm.SerialPort;
import com.groupunix.drivewireserver.dwdisk.DWDiskLazyWriter;
import com.groupunix.drivewireserver.dwexceptions.DWPlatformUnknownException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.dwprotocolhandler.MCXProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.vmodem.VModemProtocolHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

public class DriveWireServer {

	// Version information
	private static Date dwVersionDate = new Date();
	private static Version dwVersion = new Version(0, 0, 0, "");

	// Logging
	private static final Logger logger = Logger.getLogger(DriveWireServer.class);
	private static DWLogAppender dwAppender;
	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %m%n");

	// Configuration
	private static XMLConfiguration serverConfig;
	private static String configFileName = "config.xml";
	private static int configSerial = 0;
	private static boolean configFreeze = false;
	private static boolean useBackup = false;

	// Protocol Handlers
	private static Vector<Thread> dwProtoHandlerThreads = new Vector<Thread>();
	private static Vector<DWProtocol> dwProtoHandlers = new Vector<DWProtocol>();

	// Lifecycle and State
	private static Thread lazyWriterT;
	private static DWUIThread uiObj;
	private static Thread uiT;
	private static boolean wantToDie = false;
	private static boolean ready = false;
	private static long magic = System.currentTimeMillis();

	// Events and Status
	private static DWEvent statusEvent = new DWEvent(DWDefs.EVENT_TYPE_STATUS, -1);
	private static DWEvent currentEvent = new DWEvent(DWDefs.EVENT_TYPE_STATUS, -1);
	private static long lastMemoryUpdate = 0;
	private static ArrayList<DWEvent> logCache = new ArrayList<DWEvent>();

	// Options
	private static boolean useDebug = false;
	private static boolean noMIDI = false;
	private static boolean noMount = false;

	public static int getConfigSerial() {
		return configSerial;
	}

	public static void setConfigSerial(final int configSerial) {
		DriveWireServer.configSerial = configSerial;
	}

	public static XMLConfiguration getConfig() {
		return serverConfig;
	}

	public static Version getVersion() {
		return dwVersion;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void incrementConfigSerial() {
		configSerial++;
	}

	public static void main(final String[] args) {

		// catch everything
		Thread.setDefaultUncaughtExceptionHandler(new DWExceptionHandler());

		init(args);

		// hang around
		logger.debug("ready...");

		DriveWireServer.ready = true;

		logger.debug("Serial ports:");
		for (final String s : getAvailableSerialPorts())
			logger.debug(s);

		while (!wantToDie) {

			try {
				Thread.sleep(DriveWireServer.serverConfig.getInt("StatusInterval", 1000));

				checkHandlerHealth();

				submitServerStatus();

			} catch (final InterruptedException e) {
				logger.debug("I've been interrupted, now I want to die");
				wantToDie = true;
			}

		}

		serverShutdown();
	}

	private static void checkHandlerHealth() {
		for (int i = 0; i < DriveWireServer.dwProtoHandlers.size(); i++) {
			if ((dwProtoHandlers.get(i) != null) && (dwProtoHandlers.get(i).isReady())
					&& (!dwProtoHandlers.get(i).isDying())) {

				// check thread
				if (dwProtoHandlerThreads.get(i) == null) {
					logger.error("Null thread for handler #" + i);
				} else {
					if (!dwProtoHandlerThreads.get(i).isAlive()) {
						logger.error("Handler #" + i + " has died. RIP.");

						if (dwProtoHandlers.get(i).getConfig().getBoolean("ZombieResurrection", true)) {
							logger.info("Arise chicken! Reanimating handler #" + i + ": "
									+ dwProtoHandlers.get(i).getConfig().getString("[@name]", "unnamed"));

							final List<HierarchicalConfiguration> handlerconfs = serverConfig
									.configurationsAt("instance");

							dwProtoHandlers.set(i, new DWProtocolHandler(i, handlerconfs.get(i)));

							dwProtoHandlerThreads.set(i, new Thread(dwProtoHandlers.get(i)));
							dwProtoHandlerThreads.get(i).start();
						}

					}

				}
			}
		}
	}

	private static void submitServerStatus() {
		final long ticktime = System.currentTimeMillis();

		if (uiObj != null) {

			// add everything

			currentEvent.setParam(DWDefs.EVENT_ITEM_MAGIC, DriveWireServer.getMagicString());
			currentEvent.setParam(DWDefs.EVENT_ITEM_INTERVAL,
					DriveWireServer.serverConfig.getInt("StatusInterval", 1000) + "");
			currentEvent.setParam(DWDefs.EVENT_ITEM_INSTANCES, DriveWireServer.getNumHandlers() + "");
			currentEvent.setParam(DWDefs.EVENT_ITEM_INSTANCESALIVE, DriveWireServer.getNumHandlersAlive() + "");
			currentEvent.setParam(DWDefs.EVENT_ITEM_THREADS, DWUtils.getRootThreadGroup().activeCount() + "");
			currentEvent.setParam(DWDefs.EVENT_ITEM_UICLIENTS, DriveWireServer.uiObj.getNumUIClients() + "");

			// ops
			currentEvent.setParam(DWDefs.EVENT_ITEM_OPS, DriveWireServer.getTotalOps() + "");
			currentEvent.setParam(DWDefs.EVENT_ITEM_DISKOPS, DriveWireServer.getDiskOps() + "");
			currentEvent.setParam(DWDefs.EVENT_ITEM_VSERIALOPS, DriveWireServer.getVSerialOps() + "");

			// some things should not be updated every tick..
			if (ticktime - lastMemoryUpdate > DWDefs.SERVER_MEM_UPDATE_INTERVAL) {
				// System.gc();
				currentEvent.setParam(DWDefs.EVENT_ITEM_MEMTOTAL, (Runtime.getRuntime().totalMemory() / 1024) + "");
				currentEvent.setParam(DWDefs.EVENT_ITEM_MEMFREE, (Runtime.getRuntime().freeMemory() / 1024) + "");
				lastMemoryUpdate = ticktime;
			}

			// only send updated vals
			DWEvent fEvent = new DWEvent(DWDefs.EVENT_TYPE_STATUS, -1);

			for (final String key : currentEvent.getParamKeys()) {
				if (!statusEvent.hasParam(key) || (!statusEvent.getParam(key).equals(currentEvent.getParam(key)))) {
					fEvent.setParam(key, currentEvent.getParam(key));
					statusEvent.setParam(key, currentEvent.getParam(key));
				}
			}

			if (fEvent.getParamKeys().size() > 0)
				uiObj.submitEvent(fEvent);
		}

	}

	public static DWEvent getServerStatusEvent() {
		return DriveWireServer.statusEvent;
	}

	private static long getTotalOps() {
		long res = 0;

		for (final DWProtocol p : dwProtoHandlers) {
			if (p != null)
				res += p.getNumOps();
		}

		return res;
	}

	static long getDiskOps() {
		long res = 0;

		for (final DWProtocol p : dwProtoHandlers) {
			if (p != null)
				res += p.getNumDiskOps();
		}

		return res;
	}

	private static long getVSerialOps() {
		long res = 0;

		for (final DWProtocol p : dwProtoHandlers) {
			if (p != null)
				res += p.getNumVSerialOps();
		}

		return res;
	}

	public static void init(final String[] args) {
		// set thread name
		Thread.currentThread().setName("dwserver-" + Thread.currentThread().getId());

		// command line arguments
		doCmdLineArgs(args);

		// set up initial logging config
		initLogging();

		loadVersion();

		logger.info("DriveWire Server " + dwVersion + " starting");
		// logger.info("Heap max: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 +
		// "MB " + " cur: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");
		// load server settings
		try {
			// load config
			final File testconfig = new File(DriveWireServer.configFileName);
			if (testconfig.exists()) {
				DriveWireServer.serverConfig = new XMLConfiguration(DriveWireServer.configFileName);
			} else if (DriveWireServer.useBackup) {
				DriveWireServer.serverConfig = new XMLConfiguration("config.xml.backup");
			} else {
				throw new ConfigurationException("File " + DriveWireServer.configFileName + " not found!");
			}

			// only backup if it loads
			if (useBackup)
				backupConfig(configFileName);

		} catch (final ConfigurationException e1) {
			logger.fatal("Configuration error: " + e1.getMessage());
			if (useDebug) {
				logger.debug("Stack trace:", e1);
			}
			System.exit(1);
		}

		// start UI server first, so we can bail if UIorBust
		if (applyUISettings() == true) {

			// apply settings to logger
			applyLoggingSettings();

			// add server config listener
			serverConfig.addConfigurationListener(new DWServerConfigListener());

			// apply configuration

			// auto save
			if (serverConfig.getBoolean("ConfigAutosave", true)) {
				logger.debug("Auto save of configuration is enabled");
				serverConfig.setAutoSave(true);
			}

			// start protocol handler instance(s)
			startProtoHandlers();

			// start lazy writer
			startLazyWriter();
		}

	}

	private static void loadVersion() {
		Properties props = new Properties();
		try (InputStream is = DriveWireServer.class.getResourceAsStream("/version.properties")) {
			if (is != null) {
				props.load(is);
				String fullVersion = props.getProperty("version.full", "0.0.0");
				String build = props.getProperty("version.build", "0");
				String dateStr = props.getProperty("version.date", "");

				// project.version is usually 4.3.7 or 4.3.7-SNAPSHOT
				String[] parts = fullVersion.split("[\\.-]");
				int major = parts.length > 0 ? Integer.parseInt(parts[0].replaceAll("[^0-9]", "0")) : 0;
				int minor = parts.length > 1 ? Integer.parseInt(parts[1].replaceAll("[^0-9]", "0")) : 0;
				int buildNum = parts.length > 2 ? Integer.parseInt(parts[2].replaceAll("[^0-9]", "0")) : 0;
				String revision = parts.length > 3 ? parts[3] : "";

				if (fullVersion.contains("-")) {
					revision = fullVersion.substring(fullVersion.indexOf("-") + 1);
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
				try {
					if (!dateStr.isEmpty())
						dwVersionDate = sdf.parse(dateStr);
				} catch (Exception e) {
					logger.warn("Could not parse version date: " + e.getMessage());
					if (useDebug) {
						logger.debug("Stack trace:", e);
					}
				}

				dwVersion = new Version(major, minor, buildNum, revision, dwVersionDate);
				dwVersion.setBuildNumber(build);
			} else {
				logger.warn("version.properties not found.");
			}
		} catch (IOException e) {
			logger.error("Error loading version properties: " + e.getMessage());
		}
	}

	private static void startProtoHandlers() {
		@SuppressWarnings("unchecked")
		final List<HierarchicalConfiguration> handlerconfs = serverConfig.configurationsAt("instance");

		dwProtoHandlers.ensureCapacity(handlerconfs.size());
		dwProtoHandlerThreads.ensureCapacity(handlerconfs.size());

		int hno = 0;

		for (final HierarchicalConfiguration hconf : handlerconfs) {
			try {
				if (hconf.containsKey("Protocol")) {
					final String protocol = hconf.getString("Protocol");
					if (protocol.equals("DriveWire")) {
						dwProtoHandlers.add(new DWProtocolHandler(hno, hconf));
					} else if (protocol.equals("MCX")) {
						dwProtoHandlers.add(new MCXProtocolHandler(hno, hconf));
					} else if (protocol.equals("VModem")) {
						dwProtoHandlers.add(new VModemProtocolHandler(hno, hconf));
					} else {
						logger.error("Unknown protocol '" + protocol + "' in handler #" + hno);
					}
				} else {
					// default to drivewire
					dwProtoHandlers.add(new DWProtocolHandler(hno, hconf));
				}

				if (dwProtoHandlers.size() > hno) {
					dwProtoHandlerThreads.add(new Thread(dwProtoHandlers.get(hno)));

					if (hconf.getBoolean("AutoStart", true)) {
						startHandler(hno);
					}
				}
			} catch (Exception e) {
				logger.error("Failed to initialize handler #" + hno + ": " + e.getMessage(), e);
			}

			hno++;
		}
	}

	public static void startHandler(final int hno) {
		if (dwProtoHandlerThreads.get(hno).isAlive()) {
			logger.error("Requested start of already alive handler #" + hno);
		} else {
			logger.info("Starting handler #" + hno + ": " + dwProtoHandlers.get(hno).getClass().getSimpleName());

			dwProtoHandlerThreads.get(hno).start();

			while (!dwProtoHandlers.get(hno).isReady()) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					logger.warn("Interrupted while waiting for instance " + hno + "  to become ready.");
				}
			}
		}
	}

	public static void stopHandler(final int hno) {
		logger.info("Stopping handler #" + hno + ": " + dwProtoHandlers.get(hno).getClass().getSimpleName());

		final HierarchicalConfiguration hc = dwProtoHandlers.get(hno).getConfig();

		dwProtoHandlers.get(hno).shutdown();

		try {
			dwProtoHandlerThreads.get(hno).join(15000);
		} catch (final InterruptedException e) {
			logger.warn("Interrupted while waiting for handler " + hno + " to exit");
		}

		dwProtoHandlers.remove(hno);
		dwProtoHandlers.add(hno, new DWProtocolHandler(hno, hc));
		dwProtoHandlerThreads.remove(hno);
		dwProtoHandlerThreads.add(hno, new Thread(dwProtoHandlers.get(hno)));

	}

	/**
	 * @throws DWPlatformUnknownException
	 * 
	 */

	private static void initLogging() {
		// Log4j 2.x Bridge handles basic initialization via log4j2.xml
		if (useDebug)
			Logger.getRootLogger().setLevel(Level.ALL);
		else
			Logger.getRootLogger().setLevel(Level.INFO);
	}

	private static void doCmdLineArgs(final String[] args) {
		// set options from cmdline args
		final Options cmdoptions = new Options();

		cmdoptions.addOption("config", true, "configuration file (defaults to config.xml)");
		cmdoptions.addOption("backup", false, "make a backup of config at server start");
		cmdoptions.addOption("help", false, "display command line argument help");
		cmdoptions.addOption("logviewer", false, "open GUI log viewer at server start");
		cmdoptions.addOption("debug", false, "log extra info to console");
		cmdoptions.addOption("nomidi", false, "disable MIDI");
		cmdoptions.addOption("nomount", false, "do not remount disks from last run");
		cmdoptions.addOption("noui", false, "do not start user interface");
		cmdoptions.addOption("noserver", false, "do not start server");
		cmdoptions.addOption("liteui", false, "use lite user interface");

		final CommandLineParser parser = new DefaultParser();
		try {
			final CommandLine line = parser.parse(cmdoptions, args);

			// help
			if (line.hasOption("help")) {
				final HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar DriveWire.jar [OPTIONS]", cmdoptions);
				System.exit(0);
			}

			if (line.hasOption("config")) {
				configFileName = line.getOptionValue("config");
			}

			if (line.hasOption("backup")) {
				useBackup = true;
			}

			if (line.hasOption("debug")) {
				useDebug = true;
			}

			if (line.hasOption("logviewer")) {
				logger.warn("logviewer (LF5) is no longer supported.");
			}

			if (line.hasOption("nomidi")) {
				noMIDI = true;
			}

			if (line.hasOption("nomount")) {
				noMount = true;
			}

		} catch (final ParseException exp) {
			System.err.println("Could not parse command line: " + exp.getMessage());
			System.exit(-1);
		}

	}

	private static void backupConfig(final String cfile) {
		try {
			DWUtils.copyFile(cfile, cfile + ".bak");
			logger.debug("Backed up config to " + cfile + ".bak");
		} catch (final IOException e) {
			logger.error("Could not create config backup: " + e.getMessage());
		}
	}

	public static void serverShutdown() {
		logger.info("server shutting down...");

		if (dwProtoHandlerThreads != null) {
			logger.debug("stopping protocol handler(s)...");

			for (final DWProtocol p : dwProtoHandlers) {
				if (p != null) {
					p.shutdown();
				}
			}

			for (final Thread t : dwProtoHandlerThreads) {
				if (t != null && t.isAlive()) {
					try {
						t.interrupt();
						t.join(5000);
					} catch (final InterruptedException e) {
						logger.warn("Shutdown interrupted while waiting for handler thread to exit: " + e.getMessage());
					}
				}
			}

		}

		if (lazyWriterT != null && lazyWriterT.isAlive()) {
			logger.debug("stopping lazy writer...");

			lazyWriterT.interrupt();
			try {
				lazyWriterT.join(5000);
			} catch (final InterruptedException e) {
				logger.warn("Shutdown interrupted while waiting for lazy writer to exit: " + e.getMessage());
			}
		}

		if (uiObj != null && uiT != null && uiT.isAlive()) {
			logger.debug("stopping UI thread...");
			uiObj.die();
			try {
				uiT.join(5000);
			} catch (final InterruptedException e) {
				logger.warn("Shutdown interrupted while waiting for UI thread to exit: " + e.getMessage());
			}
		}

		logger.info("server shutdown complete");
		logger.removeAllAppenders();
	}

	private static void startLazyWriter() {
		lazyWriterT = new Thread(new DWDiskLazyWriter());
		lazyWriterT.start();
	}

	public static boolean applyUISettings() {
		if ((uiT != null) && (uiT.isAlive())) {
			uiObj.die();
			uiT.interrupt();
			try {
				uiT.join();
			} catch (final InterruptedException e) {
				logger.warn(e.getMessage());
			}
		}

		if (serverConfig.getBoolean("UIEnabled", false)) {
			java.net.ServerSocket srvr = null;

			// check for port in use (another server providing UI on our port)
			try {
				// check for listen address

				srvr = new java.net.ServerSocket(serverConfig.getInt("UIPort", 6800));
				logger.info("UI listening on port " + srvr.getLocalPort());

			} catch (final IOException e2) {

				// BAIL if UIorBust
				if (serverConfig.getBoolean("UIorBust", true))
					return false;

				logger.warn("Error opening UI socket: " + e2.getClass().getSimpleName() + " " + e2.getMessage());
				if (useDebug) {
					logger.debug("Stack trace:", e2);
				}

			}

			if (srvr != null) {
				uiObj = new DWUIThread(srvr);
				uiT = new Thread(uiObj);
				uiT.start();
			}

		}

		return true;
	}

	public static void applyLoggingSettings() {
		// logging
		if (!serverConfig.getString("LogFormat", "").equals("")) {
			logLayout = new PatternLayout(serverConfig.getString("LogFormat"));
		}

		// Keep DWLogAppender for web UI
		if (dwAppender == null) {
			dwAppender = new DWLogAppender(logLayout);
			Logger.getRootLogger().addAppender(dwAppender);
		} else {
			dwAppender.setLayout(logLayout);
		}

		if (useDebug)
			Logger.getRootLogger().setLevel(Level.ALL);
		else
			Logger.getRootLogger().setLevel(Level.toLevel(serverConfig.getString("LogLevel", "INFO")));
	}

	public static DWProtocol getHandler(final int handlerno) {
		if ((handlerno < dwProtoHandlers.size()) && (handlerno > -1))
			return (dwProtoHandlers.get(handlerno));

		return null;
	}

	public static ArrayList<String> getLogEvents(final int num) {
		return (dwAppender.getLastEvents(num));
	}

	public static int getLogEventsSize() {
		return (dwAppender.getEventsSize());
	}

	public static int getNumHandlers() {
		return dwProtoHandlers.size();
	}

	public static int getNumHandlersAlive() {
		int res = 0;

		for (final DWProtocol p : dwProtoHandlers) {
			if (p != null) {
				if (!p.isDying() && p.isReady())
					res++;
			}
		}

		return res;
	}

	public static boolean isValidHandlerNo(final int handler) {
		if ((handler < dwProtoHandlers.size()) && (handler >= 0)) {
			if (dwProtoHandlers.get(handler) != null) {
				return true;
			}
		}

		return false;
	}

	public static void restartHandler(final int handler) {

		logger.info("Restarting handler #" + handler);

		stopHandler(handler);
		startHandler(handler);

	}

	public static boolean handlerIsAlive(final int h) {
		if ((dwProtoHandlers.get(h) != null) && (dwProtoHandlerThreads.get(h) != null)) {
			if ((!dwProtoHandlers.get(h).isDying()) && (dwProtoHandlerThreads.get(h).isAlive())) {
				return (true);
			}
		}

		return false;
	}

	public static String getHandlerName(final int handlerno) {
		if (isValidHandlerNo(handlerno)) {
			return (dwProtoHandlers.get(handlerno).getConfig().getString("[@name]", "unnamed instance " + handlerno));
		}

		return ("null handler " + handlerno);
	}

	public static void saveServerConfig() throws ConfigurationException {
		serverConfig.save();
	}

	public static ArrayList<String> getAvailableSerialPorts() {
		logger.debug("Searching for serial ports...");
		final ArrayList<String> h = new ArrayList<String>();

		for (SerialPort port : SerialPort.getCommPorts()) {
			logger.debug("Adding serial port " + port.getSystemPortName() + " to list of available ports");
			h.add(port.getSystemPortName());
		}

		return h;
	}

	public static String getSerialPortStatus(final String portName) {
		String res = "Unknown";

		SerialPort port = SerialPort.getCommPort(portName);
		if (port.openPort()) {
			res = "Available";
			port.closePort();
		} else {
			res = "Not available or in use";
		}

		return res;
	}

	public static void shutdown() {
		logger.info("server shutdown requested");
		wantToDie = true;
	}

	public static void submitServerConfigEvent(final String propertyName, final String propertyValue) {
		if (uiObj != null) {
			final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_SERVERCONFIG, -1);

			evt.setParam(DWDefs.EVENT_ITEM_KEY, propertyName);
			evt.setParam(DWDefs.EVENT_ITEM_VALUE, propertyValue);

			uiObj.submitEvent(evt);
		}
	}

	public static void submitInstanceConfigEvent(final int instance, final String propertyName,
			final String propertyValue) {
		if (uiObj != null) {
			final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_INSTANCECONFIG, instance);

			evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
			evt.setParam(DWDefs.EVENT_ITEM_KEY, propertyName);
			evt.setParam(DWDefs.EVENT_ITEM_VALUE, propertyValue);

			uiObj.submitEvent(evt);
		}
	}

	public static void submitDiskEvent(final int instance, final int diskno, final String key, final String val) {
		if (uiObj != null) {
			final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_DISK, instance);

			evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
			evt.setParam(DWDefs.EVENT_ITEM_DRIVE, String.valueOf(diskno));
			evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
			evt.setParam(DWDefs.EVENT_ITEM_VALUE, val);

			uiObj.submitEvent(evt);
		}
	}

	public static void submitMIDIEvent(final int instance, final String key, final String val) {
		if (uiObj != null) {
			final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_MIDI, instance);

			evt.setParam(DWDefs.EVENT_ITEM_INSTANCE, String.valueOf(instance));
			evt.setParam(DWDefs.EVENT_ITEM_KEY, key);
			evt.setParam(DWDefs.EVENT_ITEM_VALUE, val);

			uiObj.submitEvent(evt);
		}
	}

	public static void submitLogEvent(final LoggingEvent event) {
		final DWEvent evt = new DWEvent(DWDefs.EVENT_TYPE_LOG, -1);

		evt.setParam(DWDefs.EVENT_ITEM_LOGLEVEL, event.getLevel().toString());
		evt.setParam(DWDefs.EVENT_ITEM_TIMESTAMP, event.getTimeStamp() + "");
		evt.setParam(DWDefs.EVENT_ITEM_LOGMSG, event.getMessage().toString());
		evt.setParam(DWDefs.EVENT_ITEM_THREAD, event.getThreadName());
		evt.setParam(DWDefs.EVENT_ITEM_LOGSRC, event.getLoggerName());

		synchronized (logCache) {
			logCache.add(evt);
			if (logCache.size() > DWDefs.LOGGING_MAX_BUFFER_EVENTS)
				logCache.remove(0);
		}

		if (uiObj != null) {
			uiObj.submitEvent(evt);
		}
	}

	public static void submitEvent(final DWEvent evt) {
		if (uiObj != null) {
			uiObj.submitEvent(evt);
		}
	}

	public static boolean isReady() {
		return DriveWireServer.ready;
	}

	public static ArrayList<DWEvent> getLogCache() {
		return DriveWireServer.logCache;
	}

	public static boolean isConsoleLogging() {
		return (DriveWireServer.serverConfig.getBoolean("LogToConsole", false));
	}

	public static boolean isDebug() {
		return (useDebug);
	}

	public static long getMagic() {
		return (magic);
	}

	public static String getMagicString() {
		return String.valueOf(magic);
	}

	public static boolean getNoMIDI() {
		return noMIDI;
	}

	public static boolean getNoMount() {
		return noMount;
	}

	public static void setConfigFreeze(final boolean b) {
		DriveWireServer.configFreeze = b;

	}

	public static boolean isConfigFreeze() {
		return DriveWireServer.configFreeze;
	}

	public static DWUIThread getDWUIThread() {
		return DriveWireServer.uiObj;
	}

	public static boolean getLogUIConnections() {
		return serverConfig.getBoolean("LogUIConnections", false);
	}

	public static Integer getDiskLazyWriteInterval(final Integer defaultValue) {
		return serverConfig.getInteger("DiskLazyWriteInterval", defaultValue);

	}

	public synchronized static List<HierarchicalConfiguration> getConfigurationsAt(final String configurations) {
		return DriveWireServer.serverConfig.configurationsAt(configurations);
	}

}
