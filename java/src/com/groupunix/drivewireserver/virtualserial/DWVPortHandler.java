package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;


// this replaces the seperate mode handlers with a single API consisting of both hayes AT commands and the TCP API commands

public class DWVPortHandler 
{
	private static final Logger logger = Logger.getLogger("DWServer.DWVPortHandler");
	
	private String port_command = new String();
	private int vport;
	private DWVModem vModem;
	private Thread utilthread;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	
	
	public DWVPortHandler(int handlerno, int port) 
	{
		this.vport = port;	
		this.vModem = new DWVModem(handlerno, port);
		this.handlerno = handlerno;
		this.dwVSerialPorts = DriveWireServer.getHandler(this.handlerno).getVPorts();
		
		//logger.debug("init handler for port " + port);
	}

	public void takeInput(int databyte) 
	{
		
		// echo character if modem echo is on
		if (this.vModem.isEcho())
		{
			dwVSerialPorts.write1(this.vport, (byte) databyte);
					
			// send extra lf on cr, not sure if this is right
			if (databyte == this.vModem.getCR())
			{
				dwVSerialPorts.write1(this.vport,(byte) this.vModem.getLF());
			} 

		}
			
		// process command if enter
		
		//logger.debug("takeinput: " + databyte);
		
		if (databyte == this.vModem.getCR())
		{
			logger.debug("port command '" + port_command + "'");
				
			processCommand(port_command);
				
			this.port_command = new String();
		}
		else
		{
			// add character to command
			// handle backspace
			if ((databyte == this.vModem.getBS()) && (this.port_command.length() > 0))
			{
				this.port_command = this.port_command.substring(0, this.port_command.length() - 1);
			}
			else if (databyte > 0)
			{
				// is this really the easiest way to append a character to a string??  
				this.port_command += Character.toString((char) databyte);
			}
				
		}
			
	}

	private void processCommand(String cmd) 
	{
		// hitting enter on a blank line is ok
		if (cmd.length() == 0)
		{
			return;
		}
		
		// anything beginning with AT or A/ is a modem command
		if ((cmd.toUpperCase().startsWith("AT")) || (cmd.toUpperCase().startsWith("A/")))
		{
			this.vModem.processCommand(cmd);
		}
		else
		{
			processAPICommand(cmd);
		}
		
	}

	private void processAPICommand(String cmd) 
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
				else if ((cmdparts.length >= 3) && (cmdparts[1].equalsIgnoreCase("listen"))) 
				{
					doTCPListen(cmdparts);
				}
				else if ((cmdparts.length == 3) && (cmdparts[1].equalsIgnoreCase("listentelnet"))) 
				{
					doTCPListen(cmdparts[2],1);
				}
				else if ((cmdparts.length == 3) && (cmdparts[1].equalsIgnoreCase("join"))) 
				{
					doTCPJoin(cmdparts[2]);
				}
				else if ((cmdparts.length == 3) && (cmdparts[1].equalsIgnoreCase("kill"))) 
				{
					doTCPKill(cmdparts[2]);
				}
				else
				{
					respondFail(2,"Syntax error in TCP command");
				}
			}
			else if (cmdparts[0].equalsIgnoreCase("url"))
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
				
				this.utilthread = new Thread(new DWUtilDWThread(this.handlerno, this.vport, cmd));
				this.utilthread.start();
			}
			else if (cmdparts[0].equalsIgnoreCase("log"))
			{
				// log entry
				logger.info("coco " + cmd);
			}
		}
		else
		{
			logger.debug("got empty command?");
			respondFail(2,"Syntax error: no command?");
		}
			
	}


	private void doTCPJoin(String constr) 
	{
		int conno;
		
		try
		{
			conno = Integer.parseInt(constr);
		}
		catch (NumberFormatException e)
		{
			respondFail(2,"non-numeric port in tcp join command");
			return;
		}
		

		if (DWVPortListenerPool.getConn(conno) == null)
		{
			respondFail(101,"invalid connection number");
		}
		
		respondOk("attaching to connection " + conno);
		
		// start TCP thread
		this.utilthread = new Thread(new DWVPortTCPServerThread(this.handlerno, this.vport, conno));
		this.utilthread.start();
		
	}
	
	private void doTCPKill(String constr) 
	{
		int conno;
		
		try
		{
			conno = Integer.parseInt(constr);
		}
		catch (NumberFormatException e)
		{
			respondFail(2,"non-numeric port in tcp kill command");
			return;
		}
		

		if (DWVPortListenerPool.getConn(conno) == null)
		{
			respondFail(101,"invalid connection number");
			return;
		}
		
		logger.warn("Killing connection " + conno);
		
		// close socket
		DWVPortListenerPool.killConn(conno);
		
		respondOk("killed connection " + conno);
		
	}
	

	private void doURL(String action,String url) 
	{
		this.utilthread = new Thread(new DWUtilURLThread(this.handlerno, this.vport, url, action));
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
			respondFail(2,"non-numeric port in tcp connect command");
			return;
		}
		
		// respondOk("connecting");
		
		// start TCP thread
		this.utilthread = new Thread(new DWVPortTCPConnectionThread(this.handlerno, this.vport, tcphost, tcpport));
		this.utilthread.start();
		
	}
	
	private void doTCPListen(String strport, int mode)
	{
		int tcpport;
		
		// get port #
		try
		{
			tcpport = Integer.parseInt(strport);
		}
		catch (NumberFormatException e)
		{
			respondFail(2,"non-numeric port in tcp listen command");
			return;
		}
		DWVPortTCPListenerThread listener = new DWVPortTCPListenerThread(this.handlerno, this.vport, tcpport);
		
		
		// simulate old behavior
		listener.setMode(mode);
		listener.setDo_auth(true);
		listener.setDo_protect(true);
		listener.setDo_banner(true);
		listener.setDo_telnet(true);
		
		// start TCP listener thread
		Thread listenThread = new Thread(listener);
		listenThread.start();
		
	}
	
	private void doTCPListen(String[] cmdparts) 
	{
		int tcpport;
		
		// get port #
		try
		{
			tcpport = Integer.parseInt(cmdparts[2]);
		}
		catch (NumberFormatException e)
		{
			respondFail(2,"non-numeric port in tcp listen command");
			return;
		}
		DWVPortTCPListenerThread listener = new DWVPortTCPListenerThread(this.handlerno, this.vport, tcpport);
				
		// parse options
		if (cmdparts.length > 3)
		{
			for (int i = 3;i<cmdparts.length;i++)
			{
				if (cmdparts[i].equalsIgnoreCase("telnet"))
				{
					listener.setDo_telnet(true);
				}
				else if (cmdparts[i].equalsIgnoreCase("httpd"))
				{
					listener.setMode(2);
				}
				else if (cmdparts[i].equalsIgnoreCase("auth"))
				{
					listener.setDo_auth(true);
				}
				else if (cmdparts[i].equalsIgnoreCase("protect"))
				{
					listener.setDo_protect(true);
				}
				else if (cmdparts[i].equalsIgnoreCase("banner"))
				{
					listener.setDo_banner(true);
				}
				
			}
				
		}
		
		// start TCP listener thread
		Thread listenThread = new Thread(listener);
		listenThread.start();
		
	}
	

	public void respondOk(String txt) 
	{
		logger.debug("command ok: " + txt);
		dwVSerialPorts.writeToCoco(this.vport, "OK " + txt + (char) 13);
	}
	
	
	public void respondFail(int errno, String txt) 
	{
		String perrno = String.format("%03d", errno);
		logger.debug("command failed: " + perrno + " " + txt);
		dwVSerialPorts.writeToCoco(this.vport, "FAIL " + perrno + " " + txt + (char) 13);
	}
	
	public synchronized void announceConnection(int conno, int localport, String hostaddr)
	{
		dwVSerialPorts.writeToCoco(this.vport, conno + " " + localport + " " +  hostaddr + (char) 13);		
	}
	
}
