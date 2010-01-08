package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class DWUtilTCPListenerThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilWgetThread");
	
	private DWVSerialCircularBuffer input;
	private int vport = -1;
	private int tcpport = 80;
	
	public DWUtilTCPListenerThread(int vport, DWVSerialCircularBuffer utilstream, int tcpport)
	{
		logger.debug("init tcp listener thread on port "+ tcpport );	
		this.vport = vport;
		this.tcpport = tcpport;
		this.input = utilstream;
		
	}
	
	public void run() 
	{
		
		Thread.currentThread().setName("tcplisten-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		int wanttodie = 0;
		
		// startup server 
		ServerSocket srvr = null;
		
		try 
		{
			srvr = new ServerSocket(this.tcpport);
			logger.info("tcp listening on port " + srvr.getLocalPort());
		} 
		catch (IOException e2) 
		{
			logger.error("Error opening socket on port " + this.tcpport +": " + e2.getMessage());
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 12, e2.getMessage());
			wanttodie = 1;
			return;
		}
		
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "listening on port " + this.tcpport + (char) 0);
		
		// set pass through mode
		DWVSerialPorts.setUtilMode(this.vport, 1);
		
		
		while ((wanttodie == 0) && DWVSerialPorts.isCocoInit(this.vport) && (srvr.isClosed() == false))
		{
			logger.debug("waiting for connection");
			Socket skt = null;
			try {
				skt = srvr.accept();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			logger.info("new connection from " + skt.getInetAddress().toString());
				
			// get request
				
			boolean inreq = true;
				
			String thisline = new String();
				
			while ((skt.isClosed() == false) && (inreq == true) && (DWVSerialPorts.isCocoInit(this.vport)))
			{
					
				int inbyte = 0;
				
				
				
				try {
					inbyte = skt.getInputStream().read();
				} 
				catch (IOException e) 
				{
					logger.debug("socket error: " + e.getMessage());
					inreq = false;
					try {
						skt.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				thisline += (char) inbyte;
					
				if (inbyte == 10)
				{
					if (thisline.equals("\r\n"))
					{
						// blank line, end of req
						inreq = false;
						logger.debug("end of req");
						reqWrite(1,1,thisline);
							
					}
					else
					{
						reqWrite(1,2,thisline);
						thisline = "";
					}
				}
			}
			
				
				
			// byte[] buffer = new byte[409600];
			int reqbytes = 0;
				
			if ((skt.isClosed() == false) && DWVSerialPorts.isCocoInit(this.vport)) 
			{
				// read coco response
				int cocobyte = -1;
				int numzeros = 0;
					
				// int bytessent = 0;
					
				boolean inresp = true;
					
				while (inresp == true)
				{
						
					try 
					{
						cocobyte = this.input.getInputStream().read();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
						
						// logger.debug("coco: " + cocobyte + " / " + reqbytes);
						
					if ((cocobyte == 13) && (numzeros > 0))
					{
						inresp = false;
						logger.debug("end of resp");
					}
					/*
					else if (cocobyte == 0)
					{
							
						if (numzeros == 0)
						{
							logger.debug("first 0 - " + numzeros);
							numzeros++;
						}
						else
						{
							buffer[reqbytes] = 0;
							try 
							{
								skt.getOutputStream().write(cocobyte);
							} 
							catch (IOException e) 
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							logger.debug("second 0, dedup");
							
							numzeros = 0;
							
						}
					
					}
				  */
					else 
					{
//						buffer[reqbytes] = (byte) cocobyte;
						reqbytes++;
						
						if (cocobyte == 0)
						{
							
							numzeros++;
							// logger.debug("got 0 #" +numzeros);
						}
						else if (numzeros > 0)
						{
							// logger.debug("reset 0");
							numzeros = 0;
						}
						
						try {
							if (skt.isClosed() == false)
							{
								skt.getOutputStream().write(cocobyte);
							}
						} catch (IOException e) {
							logger.debug("closing socket, exception: " + e.getMessage());
							
								try {
								skt.close();
								} catch (IOException e1) {
								// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}
						
				}
					
		
				
				
				
//				skt.getOutputStream().write(buffer, 0, reqbytes);
				
				/*
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				
				// done with response
			try {
				skt.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
				
		}
			
		reqWrite(0,0,"exiting");
		
		DWVSerialPorts.setUtilMode(this.vport, 0);
		DWVSerialPorts.clearUtilityInputBuffer(this.vport);
		
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

	private void reqWrite(int connid, int ctl, String inp)
	{
		String req = inp;
	  
		if (req.length() > 128)
		{
			req = req.substring(0,128);
		}
		
		while (req.length() < 128)
		{
			req += (char) 32;
		}
		
		logger.debug(req);
		
			
		DWVSerialPorts.write1(this.vport, (byte) connid);
		DWVSerialPorts.write1(this.vport, (byte) ctl);
		DWVSerialPorts.write(this.vport, req);
		
		
		
	}
	
}
