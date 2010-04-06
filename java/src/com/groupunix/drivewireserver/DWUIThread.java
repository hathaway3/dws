package com.groupunix.drivewireserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;


public class DWUIThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWUIThread");

	private int tcpport;
	private boolean wanttodie = false;
	
	public DWUIThread(int port) 
	{
		this.tcpport = port;
	}

	
	public void run() 
	{
		Thread.currentThread().setName("dwUIserver-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				
		logger.debug("run");
		
		// open server socket
		
		ServerSocket srvr = null;
		
		try 
		{
			// check for listen address
			
			srvr = new ServerSocket(this.tcpport);
			logger.info("UI listening on port " + srvr.getLocalPort());
			
			
			
		} 
		catch (IOException e2) 
		{
			logger.error("Error opening UI socket on port " + this.tcpport +": " + e2.getMessage());
		}
		
		
		while ((wanttodie == false) && (srvr.isClosed() == false))
		{
			logger.debug("UI waiting for connection");
			Socket skt = null;
			try 
			{
				skt = srvr.accept();
				
				logger.info("new UI connection from " + skt.getInetAddress().getHostAddress());
				
				Thread uiclientthread = new Thread(new DWUIClientThread(skt));
				uiclientthread.start();
			
				
			} 
			catch (IOException e1) 
			{
				logger.info("IO error: " + e1.getMessage());
				wanttodie = true;
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
		
		
		logger.debug("exit");
	}

}
