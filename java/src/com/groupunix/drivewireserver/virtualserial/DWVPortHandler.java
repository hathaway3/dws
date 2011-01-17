package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;


// this replaces the separate mode handlers with a single API consisting of both hayes AT commands and the TCP API commands

public class DWVPortHandler 
{
	private static final Logger logger = Logger.getLogger("DWServer.DWVPortHandler");
	
	private String port_command = new String();
	private int vport;
	private DWVModem vModem;
	private Thread utilthread;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	private	DWVSerialCircularBuffer inputBuffer = new DWVSerialCircularBuffer(1024, true);
	
	
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
		
		// logger.debug("takeinput: " + databyte);
		
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
				
				// check for MIDI header
				if (this.port_command.equals("MThd"))
				{
					// seems we have a MIDI file headed our way
					
					// immediately capture to buffer
					try 
					{
						this.inputBuffer.getOutputStream().write("MThd".getBytes());
					} 
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					DriveWireServer.getHandler(handlerno).getVPorts().setPortOutput(vport, this.inputBuffer.getOutputStream());
					DriveWireServer.getHandler(handlerno).getVPorts().markConnected(vport);
					
					logger.info("MIDI file detected on handler # " + this.handlerno + " port " + this.vport);
					
					this.utilthread = new Thread(new DWVPortMIDIPlayerThread(this.handlerno, this.vport, this.inputBuffer));
					this.utilthread.start();
					
					this.port_command = new String();
				}
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
				// old
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
			/* else if (cmdparts[0].equalsIgnoreCase("serial"))
			{
				if ((cmdparts.length == 3) && (cmdparts[1].equalsIgnoreCase("con"))) 
				{
					doSerialCon(cmdparts[2]);
				}
				else if ((cmdparts.length == 2) && (cmdparts[1].equalsIgnoreCase("list"))) 
				{
					doSerialList();
				}
				else
				{
					respondFail(2,"Syntax error in serial command");
				}
			} */
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


/*	private void doSerialList()
	{
		// list available serial ports
		ArrayList<String> ports = DWUtils.getPortNames();
		
		String txt = new String();
		
		for (int i = 0;i<ports.size();i++)
		{
			txt += ports.get(i) + " ";
		}
		
		respondOk(txt);
		
	}

	private void doSerialCon(String args)
	{
		// attempt to bridge vport with serial port
		Thread serconT = new Thread(new DWVPortSerialBridgeThread(this.handlerno, this.vport, args));
		serconT.start();
	}
*/
	
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
		

		try 
		{
			this.dwVSerialPorts.getListenerPool().validateConn(conno);
			respondOk("attaching to connection " + conno);
			
			// start TCP thread
			this.utilthread = new Thread(new DWVPortTCPServerThread(this.handlerno, this.vport, conno));
			this.utilthread.start();
		} 
		catch (DWConnectionNotValidException e) 
		{
			respondFail(101,"invalid connection number");
		}
		
		
		
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
		
		
		logger.warn("Killing connection " + conno);
		
		// close socket
		try 
		{
			this.dwVSerialPorts.getListenerPool().killConn(conno);
			respondOk("killed connection " + conno);
		} 
		catch (DWConnectionNotValidException e) 
		{
			respondFail(101,"invalid connection number");
		}
		
		
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
		try 
		{
			dwVSerialPorts.writeToCoco(this.vport, "OK " + txt + (char) 13);
		} 
		catch (DWPortNotValidException e) 
		{
			logger.error(e.getMessage());
		}
	}
	
	
	public void respondFail(int errno, String txt) 
	{
		String perrno = String.format("%03d", errno);
		logger.debug("command failed: " + perrno + " " + txt);
		try 
		{
			dwVSerialPorts.writeToCoco(this.vport, "FAIL " + perrno + " " + txt + (char) 13);
		} 
		catch (DWPortNotValidException e) 
		{
			logger.error(e.getMessage());
		}
	}
	
	public synchronized void announceConnection(int conno, int localport, String hostaddr)
	{
		try 
		{
			dwVSerialPorts.writeToCoco(this.vport, conno + " " + localport + " " +  hostaddr + (char) 13);
		} 
		catch (DWPortNotValidException e) 
		{
		
			logger.error(e.getMessage());
		}		
	}
	
}
