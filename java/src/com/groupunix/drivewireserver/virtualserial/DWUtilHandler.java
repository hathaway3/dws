package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;




public class DWUtilHandler 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilHandler");
	
	private int port;
	
	private	String	command = new String();
	
	private int utilmode = 0;
	private DWVSerialCircularBuffer utilstream;
	
	private Thread utilthread;
	
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
			
			if (databyte == 13)
			{
				// we have our command
				logger.debug("port" + DWVSerialPorts.prettyPort(this.port) + " got command '" + this.command + "'");
				doUtility(command);
				command = new String();
			}
			else
			{
				// add this byte to command
				this.command += Character.toString((char) databyte);
			}
		}
	}
	
		
	private void doUtility(String cmd) 
	{
		// new API based implementation 1/2/10
		
		String[] cmdparts = cmd.split("\\s+");
		
		if (cmdparts.length > 0)
		{
			if (cmdparts[0].equalsIgnoreCase("tcp"))
			{
				if ((cmdparts.length == 4) && (cmdparts[1].equalsIgnoreCase("connect"))) 
				{
					doTCPConnect(cmdparts[2],cmdparts[3]);
				}
				else if ((cmdparts.length == 3) && (cmdparts[1].equalsIgnoreCase("listen"))) 
				{
					doTCPListen(cmdparts[2]);
				}
				else
				{
					respondFail(2,"Syntax error in TCP command");
				}
			}
			if (cmdparts[0].equalsIgnoreCase("url"))
			{
				if ((cmdparts.length == 3) && (cmdparts[1].equalsIgnoreCase("get"))) 
				{
					doURL("get",cmdparts[2]);
				}
				else if ((cmdparts.length == 3) && (cmdparts[1].equalsIgnoreCase("post"))) 
				{
					doURL("post",cmdparts[2]);
				}
				else
				{
					respondFail(2,"Syntax error in URL command");
				}
			}
			else if (cmdparts[0].equalsIgnoreCase("dw"))
			{
				// start DWcmd thread
				this.utilstream = new DWVSerialCircularBuffer(-1, true);
				this.utilthread = new Thread(new DWUtilDWThread(this.port, this.utilstream, cmd));
				this.utilthread.start();
			}
		}
		else
		{
			logger.debug("got empty command?");
			respondFail(2,"Syntax error: no command?");
		}
		
	}

	


	private void doURL(String action,String url) 
	{
		this.utilthread = new Thread(new DWUtilURLThread(this.port, action, url));
		this.utilthread.start();
	}

	private void doTCPConnect(String tcphost, String tcpportstr) 
	{
		int tcpport;
		
		// get port #
		try
		{
			tcpport = Integer.parseInt(tcpportstr);
		}
		catch (NumberFormatException e)
		{
			respondFail(2,"non-numeric port number in tcp connect command");
			return;
		}
		
		// start TCP thread
		this.utilstream = new DWVSerialCircularBuffer(-1, true);
		this.utilthread = new Thread(new DWUtilTCPConnectionThread(this.port, this.utilstream, tcphost, tcpport));
		this.utilthread.start();
		
	}
	
	private void doTCPListen(String tcpportstr) 
	{
		int tcpport;
		
		// get port #
		try
		{
			tcpport = Integer.parseInt(tcpportstr);
		}
		catch (NumberFormatException e)
		{
			respondFail(2,"non-numeric port number in tcp listen command");
			return;
		}
		
		// start TCP thread
		this.utilstream = new DWVSerialCircularBuffer(-1, true);
		this.utilthread = new Thread(new DWUtilTCPListenerThread(this.port, this.utilstream, tcpport));
		this.utilthread.start();
		
	}
	

	public void respondOk(String txt) 
	{
		logger.debug("command ok: " + txt);
		DWVSerialPorts.write(this.port, "OK " + txt + (char) 13);
	}
	
	
	public void respondFail(int errno, String txt) 
	{
		String perrno = String.format("%03d", errno);
		logger.debug("command failed: " + perrno + " " + txt);
		DWVSerialPorts.write(this.port, "FAIL " + perrno + " " + txt + (char) 13);
	}

	
		

}

