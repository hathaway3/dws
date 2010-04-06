
package com.groupunix.drivewireserver;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskLazyWriter;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;


public class DriveWireServer 
{
	public static final String DWServerVersion = "3.9.51";
	public static final String DWServerVersionDate = "4/5/2010";
	
	
	private static Logger logger = Logger.getLogger("DWServer");
	private static ConsoleAppender consoleAppender;
	private static DWLogAppender dwAppender;
	private static FileAppender fileAppender;
	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %26.26C: %m%n");
	
	public static XMLConfiguration serverconfig;
	
	private static Thread[] dwProtoHandlerThreads;
	private static DWProtocolHandler[] dwProtoHandlers;
	private static int numHandlers;

	private static Thread lazyWriterT;
	private static Thread uiT;	
	
	//@SuppressWarnings({ "deprecation", "static-access" })   // for funky logger root call
	public static void main(String[] args) throws ConfigurationException
	{
		Thread.currentThread().setName("dwserver-" + Thread.currentThread().getId());
	
		// 	set up initial logging - server stuff goes to console
		consoleAppender = new ConsoleAppender(logLayout);
		Logger.getRootLogger().addAppender(consoleAppender);
		logger.info("DriveWire Server " + DWServerVersion + " (" + DWServerVersionDate + ") starting up");
    			
		// load server settings
		try 
    	{
			serverconfig = new XMLConfiguration("config.xml");
		} 
    	catch (ConfigurationException e1) 
    	{
    		System.out.println("Fatal - Could not process config file 'config.xml'.  Please consult the documentation.");
    		System.exit(-1);
		}
    	
    	// apply configuration
    	List handlerconfs = serverconfig.configurationsAt("instance");
    	
    	numHandlers = handlerconfs.size();
    	
    	dwProtoHandlers = new DWProtocolHandler[numHandlers];
    	dwProtoHandlerThreads = new Thread[numHandlers];
    	
    	// logging
    	if (serverconfig.containsKey("LogFormat"))
    	{
    		logLayout = new PatternLayout(serverconfig.getString("LogFormat"));
    	}
    	
    	Logger.getRootLogger().removeAllAppenders();
		
    	dwAppender = new DWLogAppender(logLayout);
    	Logger.getRootLogger().addAppender(dwAppender);
    	
    	if (serverconfig.getBoolean("LogToConsole", true))
    	{
    		consoleAppender = new ConsoleAppender(logLayout);
    		Logger.getRootLogger().addAppender(consoleAppender);
    	}
    	
    	
    	if ((serverconfig.getBoolean("LogToFile", false)) && (serverconfig.containsKey("LogFile")))
    	{
    		try 
    		{
    			fileAppender = new FileAppender(logLayout,serverconfig.getString("LogFile"),true,true,4096);
    			Logger.getRootLogger().addAppender(fileAppender);
    		} 
    		catch (IOException e) 
    		{
    			logger.error("Cannot log to file '" + serverconfig.getString("LogFile") +"': " + e.getMessage());
    		}
    	 		
    	}
    	
    	Logger.getRootLogger().setLevel(Level.toLevel(serverconfig.getString("LogLevel", "WARN")));
    	
    	
    	// start protocol handler instances
    	int hno = 0;
    	
		for(Iterator it = handlerconfs.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration hconf = (HierarchicalConfiguration) it.next();
		    
		    // sub contains now all data about a single instance
		    
		    logger.info("Starting protocol handler #" + hno + ": " + hconf.getString("Name","unnamed"));
			dwProtoHandlers[hno] = new DWProtocolHandler(hno, hconf);
    		dwProtoHandlerThreads[hno] = new Thread(dwProtoHandlers[hno]);
    		dwProtoHandlerThreads[hno].start();	
    	    
		    
    		hno++;
		    
		}
    	
    	
    	
    	// start lazy writer
    	lazyWriterT = new Thread(new DWDiskLazyWriter());
		lazyWriterT.start();
    	
		// start UI server
		if (serverconfig.getBoolean("UIEnabled",false))
		{
		
			uiT = new Thread(new DWUIThread(serverconfig.getInt("UIPort",6800)));
			uiT.start();
		}
		
    	// wait for all my children to die
    	
    	int activehandlers = 0;
    	
    	do
    	{
    	
    		try
    		{
    			Thread.sleep(2000);
    		} 
    		catch (InterruptedException e)
    		{
    			logger.warn("Server thread interrupted");
    		}

    		activehandlers = 0;
    		
    		for (int i = 0;i<numHandlers;i++)
    		{
    			if (dwProtoHandlerThreads[i].isAlive())
    			{
    				activehandlers++;
    			}
    		}
    	}
    	while (activehandlers >= 0);
    		
    	logger.info("Server exiting");	
    	
	}




	public static DWProtocolHandler getHandler(int handlerno)
	{
		return(dwProtoHandlers[handlerno]);
	}
	
	
	public static ArrayList<String> getLogEvents(int num)
	{
		return(dwAppender.getLastEvents(num));
	}



	public static int getLogEventsSize()
	{
		return(dwAppender.getEventsSize());
	}




	public static int getNumHandlers()
	{
		return numHandlers;
	}




	public static boolean isValidHandlerNo(int handler)
	{
		if ((handler < numHandlers) && (handler >= 0))
		{
			if (dwProtoHandlers[handler] != null)
			{
				return true;
			}
		}
		
		return false;
	}




	public static void restartHandler(int handler)
	{
	/*	String configfile = dwProtoHandlers[handler].config.getPath();
		
		logger.info("Restarting handler #" + handler);
		
		// signal shutdown
		dwProtoHandlers[handler].shutdown();
		dwProtoHandlerThreads[handler].interrupt();
		
		// join thread to wait for death to finish
		try
		{
			dwProtoHandlerThreads[handler].join();
		} 
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// start handler
		dwProtoHandlers[handler] = new DWProtocolHandler(handler, configfile);
		dwProtoHandlerThreads[handler] = new Thread(dwProtoHandlers[handler]);
		dwProtoHandlerThreads[handler].start();	
		
		*/
		
		logger.error("instance restart not implemented!");
		
	}




	public static boolean handlerIsAlive(int h)
	{
		if (dwProtoHandlers[h] != null)
		{
			if ((!dwProtoHandlers[h].isDying()) && (dwProtoHandlerThreads[h].isAlive() ))
			{
				return(true);
			}
		}
		
		return false;
	}
	
	
	public static boolean hasDiskset(String setname)
	{
		List disksets = serverconfig.configurationsAt("diskset");
    	
		boolean setexists = false;
		
		for(Iterator it = disksets.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration dset = (HierarchicalConfiguration) it.next();
		    
		    if ( dset.getString("Name","").equalsIgnoreCase(setname) )
		    {
		    	setexists = true;
		    }
		 
		}
		
		return(setexists);
	}
	
	public static HierarchicalConfiguration getDiskset(String setname)
	{
		List disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
	

		for(Iterator it = disksets.iterator(); it.hasNext();)
		{
			HierarchicalConfiguration dset = (HierarchicalConfiguration) it.next();
	    
			if ( dset.getString("Name","").equalsIgnoreCase(setname) )
			{
				return(dset);
			}
	    
		}
	
		return(null);
	}
	
}
