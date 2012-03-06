package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWVPortTCPConnectionThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTCPConnectionThread");
	
	private int vport = -1;
	private int tcpport = -1;
	private String tcphost = null;
	private Socket skt; 
	private boolean wanttodie = false;

	private DWVSerialPorts dwVSerialPorts;
	
	
	public DWVPortTCPConnectionThread(DWProtocolHandler dwProto, int vport, String tcphostin, int tcpportin)
	{
		logger.debug("init tcp connection thread");	
		this.vport = vport;
		this.tcpport = tcpportin;
		this.tcphost = tcphostin;

		this.dwVSerialPorts = dwProto.getVPorts();
		
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
			try
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, DWDefs.RC_NET_UNKNOWN_HOST,"Unknown host '" + this.tcphost + "'");
			} 
			catch (DWPortNotValidException e1)
			{
				logger.warn(e1.getMessage());
			}
			this.wanttodie = true;
		} 
		catch (IOException e1) 
		{
			logger.debug("IO error: " + e1.getMessage());
			try
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, DWDefs.RC_NET_IO_ERROR, e1.getMessage());
			} 
			catch (DWPortNotValidException e)
			{
				logger.warn(e1.getMessage());
			}
			this.wanttodie = true;
		}
		
		if (wanttodie == false)
		{
			
			try
			{
				dwVSerialPorts.sendUtilityOKResponse(this.vport, "Connected to " + this.tcphost + ":" + this.tcpport);
				dwVSerialPorts.markConnected(vport);
				dwVSerialPorts.setUtilMode(vport, DWDefs.UTILMODE_TCPOUT);
				logger.debug("Connected to " + this.tcphost + ":" + this.tcpport);
				dwVSerialPorts.setPortOutput(vport, skt.getOutputStream());
				
			} 
			catch (DWPortNotValidException e2)
			{
				logger.warn(e2.getMessage());
				this.wanttodie = true;
			}
			catch (IOException e1) 
			{
				logger.error("IO Error setting output: " + e1.getMessage());
				wanttodie = true;
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
				catch (DWPortNotValidException e) 
				{
					logger.error(e.getMessage());
					
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
						logger.debug("pause for the cause: " + dwVSerialPorts.bytesWaiting(this.vport) + " bytes left" );
						Thread.sleep(100);
					}
				} 
				catch (InterruptedException e) 
				{
					logger.error(e.getMessage());
				} 
				catch (DWPortNotValidException e) 
				{
					logger.error(e.getMessage());
				}
		
				logger.debug("exit stage 2, send peer signal");
		
				try 
				{
					dwVSerialPorts.closePort(this.vport);
				} 
				catch (DWPortNotValidException e) 
				{
					logger.error("in close port: " + e.getMessage());
				}
			}
			
			logger.debug("thread exiting");
		}
	}	
}

	