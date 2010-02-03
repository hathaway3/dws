package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class DWVPortTCPConnectionThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTCPConnectionThread");
	
	private int vport = -1;
	private int tcpport = -1;
	private String tcphost = null;
	private Socket skt; 
	private boolean wanttodie = false;
	
	public DWVPortTCPConnectionThread(int vport, String tcphostin, int tcpportin)
	{
		logger.debug("init tcp connection thread");	
		this.vport = vport;
		this.tcpport = tcpportin;
		this.tcphost = tcphostin;
		
	}
	

	public void run() 
	{
		Thread.currentThread().setName("tcpconn-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		
		// try to establish connection
		try 
		{
			skt = new Socket(this.tcphost, this.tcpport);
		} 
		catch (UnknownHostException e) 
		{
			logger.debug("unknown host " + tcphost );
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 4,"Unknown host '" + this.tcphost + "'");
			this.wanttodie = true;
		} 
		catch (IOException e1) 
		{
			logger.debug("IO error: " + e1.getMessage());
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 5, e1.getMessage());
			this.wanttodie = true;
		}
		
		if (wanttodie == false)
		{
			logger.debug("Connected to " + this.tcphost + ":" + this.tcpport);
			DWVSerialPorts.sendUtilityOKResponse(this.vport, "Connected to " + this.tcphost + ":" + this.tcpport);
			
			//DWVSerialPorts.setSocket(this.vport, skt);

			DWVSerialPorts.markConnected(vport);	
			try 
			{
				DWVSerialPorts.setPortOutput(vport, skt.getOutputStream());
			} 
			catch (IOException e1) 
			{
				logger.error("IO Error setting output: " + e1.getMessage());
			}
			
			while ((wanttodie == false) && (skt.isClosed() == false) && (DWVSerialPorts.isOpen(this.vport)))
			{
							
				try 
				{
					int databyte = skt.getInputStream().read();
					if (databyte == -1)
					{
						logger.debug("got -1 in input stream");
						wanttodie = true;
					}
					else
					{
						DWVSerialPorts.writeToCoco(this.vport,(byte)databyte);
					}
					
				} 
				catch (IOException e) 
				{
						logger.debug("IO error reading tcp: " + e.getMessage());
						wanttodie = true;
				}
				
			}
		
			
			if (wanttodie)
				logger.debug("exit because wanttodie");
			
			if (skt.isClosed())
				logger.debug("exit because skt isClosed");
			
			if (!DWVSerialPorts.isOpen(this.vport))
				logger.debug("exit because port is not open");			
			
			DWVSerialPorts.markDisconnected(this.vport);
			DWVSerialPorts.setPortOutput(vport, null);
			
			if (skt.isClosed() == false)
			{
				// close socket
				try 
				{
					skt.close();
				} 
				catch (IOException e) 
				{
					logger.debug("error closing socket: " + e.getMessage());
				}
			}
			
			
			
		}
		
		
		// only if we got connected..
		if (skt != null)
		{
			if (skt.isConnected())
			{
		
				logger.debug("exit stage 1, flush buffer");
		
				// 	flush buffer, term port
				try {
					while ((DWVSerialPorts.bytesWaiting(this.vport) > 0) && (DWVSerialPorts.isOpen(this.vport)))
					{
						Thread.sleep(100);
					}
				} 
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				logger.debug("exit stage 2, send peer signal");
		
				DWVSerialPorts.closePort(this.vport);
			}
			
			logger.debug("thread exiting");
		}
	}	
}

	