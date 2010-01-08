package com.groupunix.drivewireserver.tcpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWTCPServer implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWTCPServer");
	
	private int wanttodie = 0;

	
	public void run() 
	{
		Thread.currentThread().setName("tcpsrv-" + Thread.currentThread().getId());
		
		ServerSocket srvr;

		
		try {
			srvr = new ServerSocket(DriveWireServer.config.getInt("TCPPort",6809));
			
			logger.info("server listening on port " + srvr.getLocalPort());
			
			while (wanttodie == 0)
			{
				Socket skt = srvr.accept();
				logger.info("new connection from " + skt.getInetAddress().toString());
				
				DriveWireServer.incServed();
				
				Thread tcpthread = new Thread(new DWTCPServerThread(skt));
				tcpthread.start();
				
			}
			
		} 
		catch (IOException e) 
		{
			logger.error("Error binding to socket, TCP server exiting");
		}
	
	}

	
}
