package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;




public class DWUtilHandler 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilHandler");
	
	// utility commands
	private static final String CMD_WGET = "wget";
	private static final String CMD_DW = "dw";
	private static final String	CMD_FTP	= "ftp";
	private static final String CMD_CHAT = "chat";
	private static final String CMD_WEATHER = "weather";
	private static final String CMD_HTTPD = "httpd";
	
	private int port;
	
	private	String	command = new String();
	private String	argument = new String();
	private boolean	readingCommand = true;
	private Thread ftpThread = null;
	private DWUtilFTPThread ftpHandler = null;
	public static String ftpInput = null;
	public static String ftpSync = "whatever";
	
	private int utilmode = 0;
	private DWVSerialCircularBuffer utilstream;
	
	public DWUtilHandler(int port, OutputStream output) 
	{
		this.port = port;
				
		logger.debug("new util handler for port " + DWVSerialPorts.prettyPort(port));
	}

	public void setUtilmode(int mode)
	{
		this.utilmode = mode;
	}
	
	public DWVSerialCircularBuffer getUtilstream()
	{
		return(this.utilstream);
	}
	
	
	public void takeInput(int databyte) 
	{
		//  util mode input 
		
		if (this.utilmode == 1)
		{
			// character by character mode
			
			if (this.utilstream != null)
			{
				try 
				{
					this.utilstream.getOutputStream().write(databyte);
				} 
				catch (IOException e) 
				{
					logger.debug(e.getMessage());
				}
			}
			
		}
		else
		{
			if (readingCommand)
			{
			// command read mode
			
				if (databyte == 13)
				{
				// we have our command, switch to argument mode
					readingCommand = false;
					logger.debug("port" + DWVSerialPorts.prettyPort(this.port) + " got command '" + this.command + "'");
				}
				else
				{
					// add this byte to command
					this.command += Character.toString((char) databyte);
				}
			}
			else
			{
				// argument reading mode
				if (databyte == 13)
				{
					// we have our argument, reset mode and do the operation
					readingCommand = true;
					logger.debug("port" + DWVSerialPorts.prettyPort(this.port) + "got argument '" + this.argument + "'");
					doUtility(command,argument);
					command = new String();
					argument = new String();
				
				}
				else
				{
					// add this byte to command
					this.argument += Character.toString((char) databyte);
				}
			}
		}
	}
	
		
	private void doUtility(String cmd, String arg) 
	{
		// switch command to handler, could do some fancy reflection thing but
		// this is easy
		
		// spawn threads for anything that isn't quick and short
		// to keep the main handler flowing (and avoid filling the output
		// buffer which deadlocks us
		
		if (cmd.equalsIgnoreCase(CMD_WGET))
		{
			
			Thread wgetthread = new Thread(new DWUtilWgetThread(this.port, arg));
			wgetthread.start();
			
		}
		else if (cmd.equalsIgnoreCase(CMD_WEATHER))
		{
			
			Thread weatherthread = new Thread(new DWUtilWeatherThread(this.port, arg));
			weatherthread.start();
			
		}
		else if (cmd.equalsIgnoreCase(CMD_HTTPD))
		{
			this.utilmode = 1;
			
			this.utilstream = new DWVSerialCircularBuffer();
			Thread httpdthread = new Thread(new DWUtilHTTPDThread(this.port, arg, this.utilstream ));
			httpdthread.start();
			
		}
		else if (cmd.equalsIgnoreCase(CMD_DW))
		{
			
			Thread dwthread = new Thread(new DWUtilDWThread(this.port, arg));
			dwthread.start();
			
		}
		else if (cmd.equalsIgnoreCase(CMD_CHAT) || cmd.startsWith(CMD_CHAT) )
		{
			this.utilmode = 1;
			
			this.utilstream = new DWVSerialCircularBuffer();
						
			Thread ircthread = new Thread(new DWUtilIRCThread(this.port, this.utilstream));
			ircthread.start();
		}
		
		else if (cmd.equalsIgnoreCase(CMD_FTP))
		{
			if (ftpThread != null)
			{
				if (ftpThread.isAlive())
				{
					synchronized (ftpSync) 
					{
						this.ftpInput = arg;
						ftpSync.notifyAll();
					}
				}
				else
				{
					// start up a new ftp thread
					ftpHandler = new DWUtilFTPThread(this.port,arg);
		
					ftpThread = new Thread(ftpHandler);
					ftpThread.start();
				}
			}
			else
			{
				// start up a new ftp thread
				ftpHandler = new DWUtilFTPThread(this.port,arg);
	
				ftpThread = new Thread(ftpHandler);
				ftpThread.start();
			}
		}
		
	}

	
		

}

