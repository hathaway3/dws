package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class DWUtilTCPConnectionThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilTCPConnectionThread");
	
	private DWVSerialCircularBuffer input;
	private int vport = -1;
	private int tcpport = -1;
	private String tcphost = null;
	private Socket skt; 
	private boolean wanttodie = false;
	private String loutbytes = new String();
	private String linbytes = new String();
	
	public DWUtilTCPConnectionThread(int vport, DWVSerialCircularBuffer utilstream, String tcphostin, int tcpportin)
	{
		logger.debug("init tcp connection thread");	
		this.vport = vport;
		this.input = utilstream;
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
			
			// connection mode
			
			// set pass through mode
			DWVSerialPorts.setUtilMode(this.vport, 1);
						
			while ((wanttodie == false) && (skt.isClosed() == false) && (DWVSerialPorts.isCocoInit(this.vport)))
			{
				// poll inputs
				
				// serial input
				int vserAvail = input.getAvailable();
				if (vserAvail > 0)
				{
					byte[] buffer = new byte[vserAvail];
					try 
					{
						input.getInputStream().read(buffer, 0, vserAvail);
						skt.getOutputStream().write(buffer);
						for (int i = 0;i< buffer.length ; i++)
						{
							if (buffer[i] == 10)
							{
								logger.debug("TCPout: " + loutbytes);
								loutbytes = new String();
							}
							else
							{
								loutbytes += Character.toString((char) buffer[i]);
							}
						}
					} 
					catch (IOException e) 
					{
						logger.debug("IO error polling vserial: " + e.getMessage());
						wanttodie = true;
					}
				}
				
				// tcp input
				if (wanttodie == false)
				{
					try 
					{
						int tcpAvail = skt.getInputStream().available();
						if (tcpAvail > 0)
						{
							byte[] buffer = new byte[tcpAvail];
							skt.getInputStream().read(buffer, 0, tcpAvail);
							DWVSerialPorts.write(this.vport, new String(buffer));
							for (int i = 0;i< buffer.length ; i++)
							{
								if (buffer[i] == 10)
								{
									logger.debug("TCPin:  " + linbytes);
									linbytes = new String();
								}
								else
								{
									linbytes += Character.toString((char) buffer[i]);
								}
							}
						}
					} 
					catch (IOException e) 
					{
						logger.debug("IO error polling tcp: " + e.getMessage());
						wanttodie = true;
					}
				}
				
				if (wanttodie == false)
				{
					// wait a bit
					try 
					{
						Thread.sleep(50);
					} 
					catch (InterruptedException e) 
					{
						logger.debug("interrupted during sleep");
						wanttodie = true;
					}
				}
				
			}
		
			// close this utility port
			// TODO: can't really
			
					
			// back to command mode
			DWVSerialPorts.setUtilMode(this.vport, 0);
			
			
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
		
		logger.debug("exit stage 1, flush buffer");
		
		// flush buffer, term port
		try {
			while ((DWVSerialPorts.bytesWaiting(this.vport) > 0) && (DWVSerialPorts.isCocoInit(this.vport)))
			{
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.debug("exit stage 2, send peer signal");
		
		DWVSerialPorts.closePort(this.vport);
		
		logger.debug("thread exiting");
	}

}

	