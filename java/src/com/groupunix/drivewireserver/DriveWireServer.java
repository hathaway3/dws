
package com.groupunix.drivewireserver;


import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.tcpserver.DWTCPServer;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;
import com.mynumnum.drivewire.server.Jetty;


public class DriveWireServer 
{
	public static final String DWServerVersion = "3.1.2";
	public static final String DWServerVersionDate = "12/21/2009";
	private static boolean loggingToFile = false;
	private static String logLevel = "WARN";
	
	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-10t] %20.20C: %m%n");

	public static Logger logger = Logger.getLogger("DWServer");
	private static FileAppender fileAppender = null;
	private static ConsoleAppender consoleAppender = null;
	
	
	public static PropertiesConfiguration config = new PropertiesConfiguration();
	
	private static Thread protoHandlerT = new Thread(new DWProtocolHandler());
	
	private static Thread TCPServerT = new Thread(new DWTCPServer());
	
	private static GregorianCalendar startTime = new GregorianCalendar();
	private static int totalServed = 0;

		
	public static void main(String[] args)
	{
		Thread.currentThread().setName("dwserver-" + Thread.currentThread().getId());
		
		BasicConfigurator.configure();
		
		// load settings
    	
    	try 
    	{
			config = new PropertiesConfiguration("DriveWireServer.properties");
		} 
    	catch (ConfigurationException e1) 
    	{
    		System.out.println("Fatal - Could not process config file 'DriveWireServer.properties'.  Please consult the documentation.");
    		System.exit(-1);
		}

    	// set up logging
    	
    	// must be a better way
    	// logger.getRoot().removeAllAppenders();
    	
    	loggingToFile = config.getBoolean("LogToConsole", loggingToFile);
    	    	
    	logToFile(loggingToFile);
    	
    	logger.setLevel(Level.toLevel(config.getString("LogLevel", logLevel)));
    	
    	logger.info("DriveWire Server " + DWServerVersion + " (" + DWServerVersionDate + ") starting up");
    	
    	
    	if (config.containsKey("DefaultDiskSet"))
    	{
    		DWDiskDrives.loadDiskSet(config.getString("DefaultDiskSet"));
    	}
    	if (config.containsKey("DefaultPortSet"))
    	{
    		DWVSerialPorts.LoadPortSet(config.getString("DefaultPortSet"));
    	}
    	
		
		// set up protocol handler and autostart if defined in config
		
		if (config.containsKey("SerialDevice"))
		{
			logger.info("setting protocol handler device to " + config.getString("SerialDevice"));
			if (DWProtocolHandler.setPort(config.getString("SerialDevice")) == 1)
			{
				if (config.getBoolean("AutoStart", true))
				{
					logger.info("autostarting protocol handler");
					protoHandlerT.start();
				}
			}
		}
		
		
		TCPServerT.start();
		
		// Start up the web interface.
		new Jetty();

		// headless mode, just wait for protohandler to die
		try {
			protoHandlerT.join();
		} catch (InterruptedException e) {
			logger.info("we've been interrupted? rude");
		}
		
	}
	
	public static void logToFile(boolean loggingToFile2) {
    	if (loggingToFile2)
    	{
    		consoleAppender = new ConsoleAppender(logLayout);
    		logger.addAppender(consoleAppender);
    	} else {
        	if (!config.getString("LogFile","").equals(""))
        	{
        		setLogFileName(config.getString("LogFile"));
        	}    		
    	}
		
	}

	public static void connectSerialPort()
	{
		// stop PH if we have one running
		if (protoHandlerT.isAlive())
		{
			
			logger.debug("interrupting/killing protocol handler");
			DWProtocolHandler.shutdown();
			protoHandlerT.interrupt();	
			try 
			{
				// wait for it to die
				protoHandlerT.join();
			} 
			catch (InterruptedException e) 
			{
				logger.debug("interrupted while waiting for handler to exit");
			}
			
		}
		
		logger.debug("setting protocol handler's device to '" + config.getString("SerialDevice") + "'");
		if (DWProtocolHandler.setPort(config.getString("SerialDevice")) == 1)
		{
			protoHandlerT = new Thread(new DWProtocolHandler());
			protoHandlerT.start();
		}
		else
		{
			logger.error("Failed to set port");
		}
		
	}
    	
	
	public static String getUptimeStr()
	{
		String upt = new String();
	
		GregorianCalendar nowTime = new GregorianCalendar();
	      
	    Date d1 = startTime.getTime();
	    Date d2 = nowTime.getTime();
	    long l1 = d1.getTime();
	    long l2 = d2.getTime();
	    long millis = l2 - l1;
	      
	    // upt = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	    	
	    upt = millis + " milliseconds";
	    
		return(upt);
	}

	
	public static void incServed()
	{
		totalServed++;
	}

	public static int getServed()
	{
		return(totalServed);
	}
	// TODO Have this determine if TCP is enabled.  Code not yet implemented here.
	public static boolean isTcpEnabled() {
		return true;
	}
	/**
	 * Returns the current logger level for use by the client UI
	 * @return
	 */
	public static String getLogLevel() {
		return logLevel;
	}
	/**
	 * Used by the client UI to display if the write to file option is selected
	 * @return
	 */
	public static boolean isWriteToFileEnabled() {
		return loggingToFile;
	}
	/**
	 * Used by the client UI to show the current log file name
	 * @return
	 */
	public static String getLogFileName() {
		return config.getString("LogFile");
	}

	public static void setLogLevel(String level) {
		logger.setLevel(Level.toLevel(level));
		logLevel = level;
		// TODO Write this new value back to .properties file
	}

	public static void setLogFileName(String fileName) {
    	logger.removeAllAppenders();
		try 
		{
			fileAppender = new FileAppender(logLayout,fileName,true,true,4096);
			logger.addAppender(fileAppender);
			loggingToFile = true;
		} 
		catch (IOException e) 
		{
			logger.error("Cannot log to file '" + fileName +"': " + e.getMessage());
		}
		// TODO Write the new log filename value back to the .properties file
		
	}


	
}
