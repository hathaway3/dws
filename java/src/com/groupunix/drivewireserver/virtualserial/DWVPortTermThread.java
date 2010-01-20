package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWVPortTermThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTermThread");
	
	private int tcpport;
	private boolean wanttodie = false;
	
	private static final int TERM_PORT = 0;
	private static final int MODE_TERM = 3;
	private static final int BACKLOG = 0;
	
	public DWVPortTermThread(int tcpport)
	{
		logger.debug("init term device thread on port "+ tcpport);	
		this.tcpport = tcpport;
		
		
	}
	
	public void run() 
	{
		
		Thread.currentThread().setName("termdev-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		// setup port
		DWVSerialPorts.resetPort(TERM_PORT);
		
		// startup server 
		ServerSocket srvr = null;
		
		try 
		{
			// check for listen address
			
			if (DriveWireServer.config.containsKey("ListenAddress"))
			{
				srvr = new ServerSocket(this.tcpport, BACKLOG, InetAddress.getByName(DriveWireServer.config.getString("ListenAddress")) );
			}
			else
			{
				srvr = new ServerSocket(this.tcpport, BACKLOG);
			}
			logger.info("listening on port " + srvr.getLocalPort());
		} 
		
		catch (IOException e2) 
		{
			logger.error("Error opening socket on port " + this.tcpport +": " + e2.getMessage());
			return;
		}
		
		while ((wanttodie == false) && (srvr.isClosed() == false))
		{
			logger.debug("waiting for connection");
			Socket skt = null;
			try 
			{
				skt = srvr.accept();
			} 
			catch (IOException e1) 
			{
				logger.info("IO error: " + e1.getMessage());
				wanttodie = true;
				return;
			}
			
			
			logger.info("new connection from " + skt.getInetAddress().getHostAddress());
			
			// pass through till connection is lost
			int conno = DWVPortListenerPool.addConn(skt,MODE_TERM);
			Thread connthread = new Thread(new DWVPortTCPServerThread(TERM_PORT, conno));
			connthread.start();
			try
			{
				connthread.join();
			} 
			catch (InterruptedException e)
			{
				logger.warn("interrupted while waiting on server thread: " + e.getMessage());
			}
			
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
		
		logger.debug("exiting");
	}
		
}
