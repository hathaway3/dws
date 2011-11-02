
package com.groupunix.drivewireserver;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
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

import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPlatformUnknownException;
import com.groupunix.drivewireserver.dwhelp.DWHelp;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskLazyWriter;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.dwprotocolhandler.MCXProtocolHandler;



public class DriveWireServer 
{
	public static final String DWServerVersion = "4.0.0";
	public static final String DWServerVersionDate = "10/29/2011";
	
	
	private static Logger logger = Logger.getLogger(com.groupunix.drivewireserver.DriveWireServer.class);
	private static ConsoleAppender consoleAppender;
	private static DWLogAppender dwAppender;
	private static FileAppender fileAppender;
	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %m%n");
	
	public static XMLConfiguration serverconfig;
	
	public static int configserial = 0;
	
	private static Thread[] dwProtoHandlerThreads;
	private static DWProtocol[] dwProtoHandlers;

	private static Thread lazyWriterT;
	private static DWUIThread uiObj;
	private static Thread uiT;	
	
	
	private static boolean wanttodie = false;
	private static String configfile = "config.xml";
	

	public static void main(String[] args) throws ConfigurationException
	{
		init(args);
		
		// install clean shutdown handler
        //Runtime.getRuntime().addShutdownHook(new DWShutdownHandler());
		
	 	// hang around 
		logger.debug("waiting...");	
		
		while (!wanttodie)
    	{
    	
    		try
    		{
    			Thread.sleep(1000);
    		} 
    		catch (InterruptedException e)
    		{
    			logger.debug("I've been interrupted, now I want to die");
    			wanttodie = true;
    		}

    	}
    	
		System.exit(0);
	}

	


	public static void init(String[] args) 
	{
		// set thread name
		Thread.currentThread().setName("dwserver-" + Thread.currentThread().getId());
		
		// command line arguments
        doCmdLineArgs(args);
               
		// 	set up initial logging config
        initLogging();
        
        
		// load server settings
        try 
        {
    		// try to load/parse config
    		serverconfig = new XMLConfiguration(configfile);
		} 
        catch (ConfigurationException e1) 
    	{
    		logger.fatal(e1.getMessage());
    		System.exit(-1);
		}

        
        // apply settings to logger
        applyLoggingSettings();
		
    	
    	// Try to add native rxtx to lib path
		if (serverconfig.getBoolean("LoadRXTX",true))
		{
		   loadRXTX();
		}
		
		// test for RXTX..
		if (serverconfig.getBoolean("UseRXTX", true) && !checkRXTXLoaded())
		{
			logger.fatal("UseRXTX is true, but RXTX native libraries could not be loaded");
			logger.fatal("Please see http://sourceforge.net/apps/mediawiki/drivewireserver/index.php?title=Installation");
			System.exit(-1);
		}

    	// add server config listener
    	serverconfig.addConfigurationListener(new DWServerConfigListener());    	
    	
    	// apply configuration
    	
    	// auto save
    	if (serverconfig.getBoolean("ConfigAutosave",true))
    	{
    		logger.debug("Auto save of configuration is enabled");
    		serverconfig.setAutoSave(true);
    	}
    	
    	
    	
    	
    	
		
    	// start protocol handler instance(s)
    	startProtoHandlers();
    	
    	// start lazy writer
		startLazyWriter();
    	
		// start UI server
		applyUISettings();

	}




	private static void startProtoHandlers() 
	{
	   	@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> handlerconfs = serverconfig.configurationsAt("instance");
    	
    	dwProtoHandlers = new DWProtocol[handlerconfs.size()];
    	dwProtoHandlerThreads = new Thread[handlerconfs.size()];
    	
    	int hno = 0;
    	
		for(HierarchicalConfiguration hconf : handlerconfs)
		{
		    if (hconf.containsKey("Protocol"))
		    {
		    	if (hconf.getString("Protocol").equals("DriveWire"))
		    	{
		    		dwProtoHandlers[hno] = new DWProtocolHandler(hno, hconf);
		    	}
		    	else if (hconf.getString("Protocol").equals("MCX"))
		    	{
		    		dwProtoHandlers[hno] = new MCXProtocolHandler(hno, hconf);
		    	}
		    	else
		    	{
		    		logger.error("Unknown protocol '" + hconf.getString("Protocol") + "' in handler #" + hno);
		    	}
		    }
		    else
		    {
		    	dwProtoHandlers[hno] = new DWProtocolHandler(hno, hconf);
		    }
		    
		    
		    
		    if (hconf.getBoolean("AutoStart", true))
		    {
		    	logger.info("Starting #" + hno + ": " + hconf.getString("Name","unnamed") + " (" + dwProtoHandlers[hno].getClass().getSimpleName() + ")");
		    	dwProtoHandlerThreads[hno] = new Thread(dwProtoHandlers[hno]);
		    	dwProtoHandlerThreads[hno].start();	
    	    }
		    
		    hno++;
		}
	}




	private static boolean checkRXTXLoaded() 
	{
		// try to load RXTX, redirect it's version messages into our logs
		
		PrintStream ops = System.out;
		PrintStream eps = System.err;
		
		ByteArrayOutputStream rxtxbaos = new ByteArrayOutputStream();
		ByteArrayOutputStream rxtxbaes = new ByteArrayOutputStream();
		
		PrintStream rxtxout = new PrintStream(rxtxbaos);
		PrintStream rxtxerr = new PrintStream(rxtxbaes);
		
		System.setOut(rxtxout);
		System.setErr(rxtxerr);
		
		boolean res = DWUtils.testClassPath("gnu.io.RXTXCommDriver");
		
		for (String l : rxtxbaes.toString().trim().split("\n"))
		{
			if (!l.equals(""))
				logger.warn(l);
		}
		
		for (String l : rxtxbaos.toString().trim().split("\n"))
		{
			// ignore pesky version warning that doesn't ever seem to matter
			if (!l.equals("WARNING:  RXTX Version mismatch") && !l.equals(""))
				logger.debug(l);
		}
	
		System.setOut(ops);
		System.setErr(eps);
		
		return(res);
	}




	private static void loadRXTX() 
	{
		
		try 
		{
			String rxtxpath;
			
			if (serverconfig.containsKey("LoadRXTXPath"))
			{
				rxtxpath = serverconfig.getString("LoadRXTXPath");
			}
			else
			{
				// look for native/x/x in current dir
				File curdir = new File(".");
				rxtxpath = curdir.getCanonicalPath();
			
				// 	+ native platform dir
				String[] osparts = System.getProperty("os.name").split(" ");
			
				if (osparts.length < 1)
				{
					throw new DWPlatformUnknownException("No native dir for os '" + System.getProperty("os.name") + "' arch '" + System.getProperty("os.arch") + "'");
				}
			
				rxtxpath += File.separator + "native" + File.separator + osparts[0] + File.separator + System.getProperty("os.arch");
			}
			
			File testrxtxpath = new File(rxtxpath);
			logger.debug("Using rxtx lib path: " + rxtxpath);
			
			if (!testrxtxpath.exists())
			{
				throw new DWPlatformUnknownException("No native dir for os '" + System.getProperty("os.name") + "' arch '" + System.getProperty("os.arch") + "'");
			}
			
			// add this dir to path..
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + rxtxpath);
		    
			//set sys_paths to null so they will be reread by jvm
			Field sysPathsField;
			sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
		    sysPathsField.set(null, null);
			
		} 
		catch (SecurityException e) 
		{
			logger.fatal(e.getMessage());
		} 
		catch (NoSuchFieldException e) 
		{
			logger.fatal(e.getMessage());
		} 
		catch (IllegalArgumentException e) 
		{
			logger.fatal(e.getMessage());
		} 
		catch (IllegalAccessException e) 
		{
			logger.fatal(e.getMessage());
		} 
		catch (IOException e) 
		{
			logger.fatal(e.getMessage());
		} 
		catch (DWPlatformUnknownException e) 
		{
			logger.fatal(e.getMessage());
		}
		
	}






	private static void initLogging() 
	{
		consoleAppender = new ConsoleAppender(logLayout);
		Logger.getRootLogger().addAppender(consoleAppender);
			
		Logger.getRootLogger().setLevel(Level.INFO);
		logger.info("DriveWire Server v" + DWServerVersion + " starting");	
	}




	private static void doCmdLineArgs(String[] args) 
	{
		// set options from cmdline args
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
		
	}




	public static void serverShutdown() 
	{
		logger.info("server shutting down...");
		
		if (dwProtoHandlerThreads != null)
		{
			logger.debug("stopping protocol handler(s)...");
		
			for (int i = 0;i<dwProtoHandlerThreads.length;i++)
			{
				if (dwProtoHandlers[i] != null)
				{
					dwProtoHandlers[i].shutdown();
				
				
					if (dwProtoHandlerThreads[i].isAlive())
					{
						try 
						{
							dwProtoHandlerThreads[i].join();
						} 
						catch (InterruptedException e) 
						{
							logger.warn(e.getMessage());
						}
					}
				
				}
			}
		
		}
		
		
		if (lazyWriterT != null)
		{
			logger.debug("stopping lazy writer...");
		
			lazyWriterT.interrupt();
			try 
			{
				lazyWriterT.join();
			} 
			catch (InterruptedException e) 
			{
				logger.warn(e.getMessage());
			}
		}
		
		
		if (uiObj != null)
		{
			logger.debug("stopping UI thread...");
			uiObj.die();
			try 
			{
				uiT.join();
			} 
			catch (InterruptedException e) 
			{
				logger.warn(e.getMessage());
			}
		}
		
		
		logger.info("server shutdown complete");
		logger.removeAllAppenders();
	}




	private static void startLazyWriter() 
	{
    	lazyWriterT = new Thread(new DWDiskLazyWriter());
		lazyWriterT.start();
	}




	public static void applyUISettings() 
	{
		if ((uiT != null) && (uiT.isAlive()))
		{
			uiObj.die();
			uiT.interrupt();
			try 
			{
				uiT.join();
			} 
			catch (InterruptedException e) 
			{
				logger.warn(e.getMessage());
			}
		}
		
		if (serverconfig.getBoolean("UIEnabled",false))
		{
		
			uiObj = new DWUIThread( serverconfig.getInt("UIPort",6800));
			uiT = new Thread(uiObj);
			uiT.start();
		}

	}




	public static void applyLoggingSettings() 
	{
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
    	
    	Logger.getRootLogger().setLevel(Level.toLevel(serverconfig.getString("LogLevel", "INFO")));

	}




	public static DWProtocol getHandler(int handlerno)
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
		return dwProtoHandlers.length;
	}




	public static boolean isValidHandlerNo(int handler)
	{
		if ((handler < dwProtoHandlers.length) && (handler >= 0))
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
			if ((!dwProtoHandlers[h].isDying()) && (dwProtoHandlerThreads[h] != null) && (dwProtoHandlerThreads[h].isAlive() ))
			{
				return(true);
			}
		}
		
		return false;
	}
	
	
	@SuppressWarnings("unchecked")
	public static boolean hasDiskset(String setname)
	{
		if (setname.equalsIgnoreCase("all"))
		{
			return(true);
		}
		
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
	
	@SuppressWarnings("unchecked")
	public static HierarchicalConfiguration getDiskset(String setname) throws DWDisksetNotValidException
	{
		if (setname.equalsIgnoreCase("all"))
		{
			throw new DWDisksetNotValidException("Diskset '" + setname + "' is not allowed.");
		}
		
		
		List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
	

		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
			HierarchicalConfiguration dset = it.next();
	    
			if ( dset.getString("Name","").equals(setname) )
			{
				return(dset);
			}
	    
		}
	
		throw new DWDisksetNotValidException("No diskset '" + setname + "' is defined.");
		
	}




	public static String getHandlerName(int handlerno) 
	{
		if (isValidHandlerNo(handlerno))
		{
			return(dwProtoHandlers[handlerno].getConfig().getString("Name","unnamed instance " + handlerno));
		}
		
		return("null handler " + handlerno);
	}
	
	
	public static void saveServerConfig() throws ConfigurationException
	{
		serverconfig.save();
	}
	
	 @SuppressWarnings("unchecked")
	public static ArrayList<String> getAvailableSerialPorts() 
	 {
	        ArrayList<String> h = new ArrayList<String>();
	        
	        java.util.Enumeration<gnu.io.CommPortIdentifier> thePorts =  gnu.io.CommPortIdentifier.getPortIdentifiers();
	        while (thePorts.hasMoreElements()) 
	        {
	            gnu.io.CommPortIdentifier com = thePorts.nextElement();
	            if (com.getPortType() == gnu.io.CommPortIdentifier.PORT_SERIAL)
	                 h.add(com.getName());
	                
	            
	        }
	        return h;
	    }




	public static void shutdown() 
	{
		logger.info("server shutdown requested");
		wanttodie = true;
	}
	
}
