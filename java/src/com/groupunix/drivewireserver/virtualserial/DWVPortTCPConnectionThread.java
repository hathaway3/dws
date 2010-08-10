package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;

public class DWVPortTCPConnectionThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTCPConnectionThread");
	
	private int vport = -1;
	private int tcpport = -1;
	private String tcphost = null;
	private Socket skt; 
	private boolean wanttodie = false;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	
	
	public DWVPortTCPConnectionThread(int handlerno, int vport, String tcphostin, int tcpportin)
	{
		logger.debug("init tcp connection thread");	
		this.vport = vport;
		this.tcpport = tcpportin;
		this.tcphost = tcphostin;
		this.handlerno = handlerno;
		this.dwVSerialPorts = DriveWireServer.getHandler(this.handlerno).getVPorts();
		
	}
	

	public void run() 
	{
		Thread.currentThread().setName("tcpconn-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		logger.debug("run");
		
		
		// try to establish connection
		try 
		{
			skt = new Socket(this.tcphost, this.tcpport);
		} 
		catch (UnknownHostException e) 
		{
			logger.debug("unknown host " + tcphost );
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 4,"Unknown host '" + this.tcphost + "'");
			this.wanttodie = true;
		} 
		catch (IOException e1) 
		{
			logger.debug("IO error: " + e1.getMessage());
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 5, e1.getMessage());
			this.wanttodie = true;
		}
		
		if (wanttodie == false)
		{
			logger.debug("Connected to " + this.tcphost + ":" + this.tcpport);
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "Connected to " + this.tcphost + ":" + this.tcpport);
			
			//DWVSerialPorts.setSocket(this.vport, skt);

			dwVSerialPorts.markConnected(vport);	
			try 
			{
				dwVSerialPorts.setPortOutput(vport, skt.getOutputStream());
			} 
			catch (IOException e1) 
			{
				logger.error("IO Error setting output: " + e1.getMessage());
			}
			
			while ((wanttodie == false) && (skt.isClosed() == false) && (dwVSerialPorts.isOpen(this.vport)))
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
						dwVSerialPorts.writeToCoco(this.vport,(byte)databyte);
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
			
			if (!dwVSerialPorts.isOpen(this.vport))
				logger.debug("exit because port is not open");			
			
			dwVSerialPorts.markDisconnected(this.vport);
			dwVSerialPorts.setPortOutput(vport, null);
			
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
					while ((dwVSerialPorts.bytesWaiting(this.vport) > 0) && (dwVSerialPorts.isOpen(this.vport)))
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
		
				try 
				{
					dwVSerialPorts.closePort(this.vport);
				} 
				catch (DWPortNotValidException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			logger.debug("thread exiting");
		}
	}	
}

	