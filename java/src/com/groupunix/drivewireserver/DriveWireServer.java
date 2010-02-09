
package com.groupunix.drivewireserver;



import gnu.io.UnsupportedCommOperationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.virtualserial.DWVPortTermThread;
import com.mynumnum.drivewire.server.Jetty;


public class DriveWireServer 
{
	public static final String DWServerVersion = "3.9.1";
	public static final String DWServerVersionDate = "1/27/2010";
	
	
	public static Logger logger = Logger.getLogger("DWServer");
	private static FileAppender fileAppender = null;
	private static ConsoleAppender consoleAppender = null;
	private static DWLogAppender dwAppender = null;
	
	public static PropertiesConfiguration config = new PropertiesConfiguration();
	
	private static Thread protoHandlerT = new Thread(new DWProtocolHandler());
	
	private static Thread termT;
	
	private static GregorianCalendar startTime = new GregorianCalendar();
	private static int totalServed = 0;

	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %26.26C: %m%n");
	
		
	//@SuppressWarnings({ "deprecation", "static-access" })   // for funky logger root call
	public static void main(String[] args)
	{
		Thread.currentThread().setName("dwserver-" + Thread.currentThread().getId());
		
		BasicConfigurator.configure();
		
		// load settings
    	
    	loadConfig();

    	// set up logging
    	
    	Logger.getRootLogger().removeAllAppenders();
    	
    	if (config.containsKey("LogFormat"))
    	{
    		logLayout = new PatternLayout(config.getString("LogFormat"));
    	}
    	
    	dwAppender = new DWLogAppender(logLayout);
    	Logger.getRootLogger().addAppender(dwAppender);
    	
    	if (config.getBoolean("LogToConsole", true))
    	{
    		consoleAppender = new ConsoleAppender(logLayout);
    		Logger.getRootLogger().addAppender(consoleAppender);
    	}
    	
    	
    	if ((config.getBoolean("LogToFile", false)) && !(config.getString("LogFile","").equals("")))
    	{
    		attachFileAppender(config.getString("LogFile"));
    	 		
    	}
    	
    	Logger.getRootLogger().setLevel(Level.toLevel(config.getString("LogLevel", "WARN")));
    	
    	logger.info("DriveWire Server " + DWServerVersion + " (" + DWServerVersionDate + ") starting up");
    	
    	
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

		if (config.getBoolean("UseGUI", false))
		{
			// Start up the web interface.
			Integer guiPort = config.getInt("GUIPort",8080);
			logger.debug("Starting Jetty (Web UI) on port " + guiPort);
			new Jetty(guiPort);
		}
		else
		{
			logger.debug("Running in headless mode (no web GUI)");
		}
		
		
		if (config.containsKey("TermPort"))
		{
			logger.info("Starting Term device listener thread");
			termT = new Thread(new DWVPortTermThread(config.getInt("TermPort")));
			termT.start();
		}
		else
		{
			logger.debug("not starting term listener");
		}
	
		
		//  wait for protohandler to die
		try {
			protoHandlerT.join();
		} catch (InterruptedException e) {
			logger.info("we've been interrupted? rude");
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
    	

	public static String getLogLevel()
	{
		return(config.getString("LogLevel","INFO"));
	}



	public static boolean isWriteToFileEnabled()
	{
		return(config.getBoolean("LogToFile",false));
	}
	
	public static int getCocoModel()
	{
		return(config.getInt("CocoModel",3));
	}



	public static String getLogFileName()
	{
		return(config.getString("LogFile",null));
	}



	public static void setLogLevel(String level)
	{
		logger.debug("setting log level to " + level);
		
		config.setProperty("LogLevel", level);
		logger.setLevel(Level.toLevel(level));
		
	}



	public static void setLogFileName(String fileName)
	{
		logger.debug("setting log file name to '" + fileName + "'");
		
		config.setProperty("LogFile", fileName);
		
		if (config.getBoolean("LogToFile",false))
		{
			
			// are we already logging to file?
			if (Logger.getRootLogger().isAttached(fileAppender))
			{
				Logger.getRootLogger().removeAppender(fileAppender);
			}
			
			attachFileAppender(fileName);
		}
		
	}



	private static void attachFileAppender(String fileName)
	{
		try 
		{
			fileAppender = new FileAppender(logLayout,fileName,true,true,4096);
			Logger.getRootLogger().addAppender(fileAppender);
		} 
		catch (IOException e) 
		{
			logger.error("Cannot log to file '" + fileName +"': " + e.getMessage());
		}
		
	}



	public static void logToFile(boolean logToFile)
	{
		logger.debug("set log to file: " + logToFile);
		
		config.setProperty("LogToFile", logToFile);
		
		if ((logToFile) && (!logger.isAttached(fileAppender)))
		{
			attachFileAppender(config.getString("LogFile"));
				
		}
		else if ((!logToFile) && (logger.isAttached(fileAppender)))
		{
			Logger.getRootLogger().removeAppender(fileAppender);

		}
		
	}



	public static void setCocoModel(int model)
	{
		
		if (config.getInt("CocoModel") != model)
		{
			logger.debug("set coco model to " + model);
			
			config.setProperty("CocoModel", model);
			
			try
			{
				DWProtocolHandler.resetCocoModel();
			} 
			catch (UnsupportedCommOperationException e)
			{
				e.printStackTrace();
			}
		}
		
		
	}



	public static String getPortName()
	{
		return(config.getString("SerialDevice","unknown"));
	}



	public static void resetLogFile()
	{
		logger.debug("WebUI sent log reset request");
		
		if (Logger.getRootLogger().isAttached(fileAppender))
		{
			// stop logging to the file
			Logger.getRootLogger().removeAppender(fileAppender);
			
			// delete file
			File f = new File(config.getString("LogFile"));

			if (f.exists())
			{
			    if (f.canWrite())
			    { 
			    	if (!f.isDirectory()) 
			    	{
			    		if (f.delete())
			    		{
			    			logger.info("deleted log file '" + config.getString("LogFile") + "'");
			    		}
			    		else
			    		{
			    			logger.warn("log file '" + config.getString("LogFile") + "' could not be deleted for reset");
			    			return;
			    		}
			    	}
			    	else
			    	{
			    		logger.warn("log file '" + config.getString("LogFile") + "' is a directory!");
			    		return;
			    	}
			    }
			    else
			    {
			    	logger.warn("log file '" + config.getString("LogFile") + "' is not writeable while trying to reset it");
			    	return;
			    }
			}
			else
			{
				logger.warn("log file '" + config.getString("LogFile") + "' does not exist while trying to reset it");
			}
			
					
		}
		
		// replace appender
		attachFileAppender(config.getString("LogFile") );
		
	}
	
	public static ArrayList<String> getLogEvents(int num)
	{
		return(dwAppender.getLastEvents(num));
	}



	public static int getLogEventsSize()
	{
		return(dwAppender.getEventsSize());
	}
	
	public static void saveConfig()
	{
		try
		{
			config.save();
		} catch (ConfigurationException e)
		{
			logger.error("ConfigurationException while saving config: " + e.getMessage());
		}
	}
	
	public static void loadConfig()
	{
		try 
    	{
			config = new PropertiesConfiguration("DriveWireServer.properties");
		} 
    	catch (ConfigurationException e1) 
    	{
    		System.out.println("Fatal - Could not process config file 'DriveWireServer.properties'.  Please consult the documentation.");
    		System.exit(-1);
		}
	}



	public static void reloadConfig()
	{
		config.reload();
	}
	
}
