
package com.groupunix.drivewireserver;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
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
	public static final String DWServerVersion = "3.9.59";
	public static final String DWServerVersionDate = "07/12/2010";
	
	
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
		String configfile = "config.xml";
		
		Thread.currentThread().setName("dwserver-" + Thread.currentThread().getId());
	
		// command line arguments
		Options cmdoptions = new Options();
		
		cmdoptions.addOption("config", true, "configuration file (defaults to config.xml)");
		cmdoptions.addOption("help", false, "display command line argument help");
		
		CommandLineParser parser = new GnuParser();
		try 
		{
			CommandLine line = parser.parse( cmdoptions, args );
		    
			// help
			if (line.hasOption("help"))
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "java -jar DriveWire.jar [OPTIONS]", cmdoptions );
				System.exit(0);
			}
			
			if( line.hasOption( "config" ) ) 
			{
			    configfile = line.getOptionValue( "config" );
			}
		}
		catch( ParseException exp ) 
		{
		    System.err.println( "Could not parse command line: " + exp.getMessage() );
		    System.exit(-1);
		}
		
		
		// 	set up initial logging - server stuff goes to console
		consoleAppender = new ConsoleAppender(logLayout);
		Logger.getRootLogger().addAppender(consoleAppender);
		logger.info("DriveWire Server " + DWServerVersion + " (" + DWServerVersionDate + ") starting up");
    			
		
		// load server settings
		logger.info("reading config from '" + configfile + "'");
		try 
    	{
			serverconfig = new XMLConfiguration(configfile);
		} 
    	catch (ConfigurationException e1) 
    	{
    		System.out.println("Fatal - Could not process config file '" + configfile + "'.  Please consult the documentation.");
    		System.exit(-1);
		}
    	
    	// apply configuration
    	List<HierarchicalConfiguration> handlerconfs = serverconfig.configurationsAt("instance");
    	
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
    			fileAppender = new FileAppender(logLayout,serverconfig.getString("LogFile"),true,false,128);
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
    	
		for(Iterator<HierarchicalConfiguration> it = handlerconfs.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration hconf = it.next();
		      
		    dwProtoHandlers[hno] = new DWProtocolHandler(hno, hconf);
		    
		    if (hconf.getBoolean("AutoStart", true))
		    {
		    	logger.info("Starting protocol handler #" + hno + ": " + hconf.getString("Name","unnamed"));
		    	dwProtoHandlerThreads[hno] = new Thread(dwProtoHandlers[hno]);
		    	dwProtoHandlerThreads[hno].start();	
    	    }
		    
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
		List<HierarchicalConfiguration> disksets = serverconfig.configurationsAt("diskset");
    	
		boolean setexists = false;
		
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration dset = it.next();
		    
		    if ( dset.getString("Name","").equalsIgnoreCase(setname) )
		    {
		    	setexists = true;
		    }
		 
		}
		
		return(setexists);
	}
	
	public static HierarchicalConfiguration getDiskset(String setname)
	{
		List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
	

		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
			HierarchicalConfiguration dset = it.next();
	    
			if ( dset.getString("Name","").equalsIgnoreCase(setname) )
			{
				return(dset);
			}
	    
		}
	
		return(null);
	}
	
}
