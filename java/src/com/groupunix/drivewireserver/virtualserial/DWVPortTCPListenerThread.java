package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;

public class DWVPortTCPListenerThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTCPListenerThread");
	
	private int vport;
	private int tcpport;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	
	private int mode = 0;
	private boolean do_auth = false;
	private boolean do_protect = false;
	private boolean do_banner = false;
	private boolean do_telnet = false;
	private boolean wanttodie = false;
	
	private static int BACKLOG = 20;
	
	
	
	public DWVPortTCPListenerThread(int handlerno, int vport, int tcpport)
	{
		logger.debug("init tcp listener thread on port "+ tcpport);	
		this.vport = vport;
		this.tcpport = tcpport;
		this.handlerno = handlerno;
		this.dwVSerialPorts = DriveWireServer.getHandler(this.handlerno).getVPorts();
		
	}
	
	
	
	public void run() 
	{
		
		Thread.currentThread().setName("tcplisten-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		logger.debug("run");
		
		// startup server 
		ServerSocket srvr = null;
		
		try 
		{
			// check for listen address
			
			if (DriveWireServer.getHandler(this.handlerno).config.containsKey("ListenAddress"))
			{
				srvr = new ServerSocket(this.tcpport, BACKLOG, InetAddress.getByName(DriveWireServer.getHandler(this.handlerno).config.getString("ListenAddress")) );
			}
			else
			{
				srvr = new ServerSocket(this.tcpport, BACKLOG);
			}
			logger.info("tcp listening on port " + srvr.getLocalPort());
			
			dwVSerialPorts.writeToCoco(vport, "OK listening on port " + this.tcpport + (char) 0 + (char) 13);

			this.dwVSerialPorts.setUtilMode(vport, DWDefs.UTILMODE_TCPLISTEN);
			
			this.dwVSerialPorts.getListenerPool().addListener(this.vport, srvr);
		
		
			while ((wanttodie == false) && dwVSerialPorts.isOpen(this.vport) && (srvr.isClosed() == false))
			{
				logger.debug("waiting for connection");
				Socket skt = null;
				
				skt = srvr.accept();
				
			
				logger.info("new connection from " + skt.getInetAddress().getHostAddress());
			
				if (mode == 2)
				{
					// http mode
					logger.error("HTTP MODE NO LONGER SUPPORTED");
			
				}
				else
				{
					// run telnet preflight, let it add the connection to the pool if things work out
					Thread pfthread = new Thread(new DWVPortTelnetPreflightThread(this.handlerno, this.vport, skt, this.do_telnet, this.do_auth, this.do_protect, this.do_banner));
					pfthread.start();
				}
			
			
			}
		
		} 
		catch (IOException e2) 
		{
			logger.error(e2.getMessage());
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 12, e2.getMessage());
			wanttodie = true;
			return;
		} 
		catch (DWPortNotValidException e) 
		{
			logger.error(e.getMessage());
		}
	

		
		if (srvr != null)
		{
			try 
			{
				srvr.close();
			} 
			catch (IOException e) 
			{
				logger.error("error closing server socket: " + e.getMessage());
			}
		}
		
		logger.debug("tcp listener thread exiting");
	}

	

	




	
	public void setDo_auth(boolean do_auth)
	{
		this.do_auth = do_auth;
	}

	public boolean isDo_auth()
	{
		return do_auth;
	}

	public void setDo_protect(boolean do_protect)
	{
		this.do_protect = do_protect;
	}

	public boolean isDo_protect()
	{
		return do_protect;
	}

	public void setDo_banner(boolean do_banner)
	{
		this.do_banner = do_banner;
	}

	public boolean isDo_banner()
	{
		return do_banner;
	}

	
	public void setMode(int mode)
	{
		this.mode = mode;
	}
	
	public int getMode()
	{
		return(this.mode);
	}

	public void setDo_telnet(boolean b)
	{
		this.do_telnet = b;
		
	}
	
	public boolean isDo_telnet()
	{
		return do_telnet;
	}
	
}
