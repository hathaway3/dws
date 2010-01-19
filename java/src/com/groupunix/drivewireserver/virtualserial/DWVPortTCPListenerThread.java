package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWVPortTCPListenerThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilWgetThread");
	
	private int vport;
	private int tcpport;
	private int mode;
	private boolean wanttodie = false;
	
	private static int BACKLOG = 20;
	
	public DWVPortTCPListenerThread(int vport, int tcpport, int mode)
	{
		logger.debug("init tcp listener thread on port "+ tcpport + " mode " + mode);	
		this.vport = vport;
		this.tcpport = tcpport;
		this.mode = mode;
		
	}
	
	public void run() 
	{
		
		Thread.currentThread().setName("tcplisten-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
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
			logger.info("tcp listening on port " + srvr.getLocalPort());
		} 
		catch (IOException e2) 
		{
			logger.error("Error opening socket on port " + this.tcpport +": " + e2.getMessage());
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 12, e2.getMessage());
			wanttodie = true;
			return;
		}
		
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "listening on port " + this.tcpport + (char) 0);
		
		DWVSerialPorts.setSocket(this.vport, srvr);
		
		while ((wanttodie == false) && DWVSerialPorts.isOpen(this.vport) && (srvr.isClosed() == false))
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
			
			if (mode == 0)
			{
				//add connection to pool
				int conno = DWVPortListenerPool.addConn(skt, mode);

				// announce new connection to listener
				DWVSerialPorts.writeToCoco(this.vport, conno + " " + this.tcpport + " " +  skt.getInetAddress().getHostAddress() + (char) 13);		
			}
			else if (mode == 1)
			{
				// run telnet preflight, let it add the connection to the pool if things work out
				Thread pfthread = new Thread(new DWVPortTelnetPreflightThread(this.vport, skt));
				pfthread.start();
			}
			else if (mode ==2)
			{
				// http mode
				processHTTPReq(skt);
				
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
		
		logger.debug("tcp listener thread exiting");
	}

	
	

	private void processHTTPReq(Socket skt) 
	{
		// get request
		
		boolean inreq = true;
		DWVSerialCircularBuffer input = new DWVSerialCircularBuffer(-1, true);
		
		String thisline = new String();
	
		// mark port connected
		DWVSerialPorts.markConnected(this.vport);
		// set port's output to our buffer
		DWVSerialPorts.setPortOutput(this.vport, input.getOutputStream());
		
		while ((skt.isClosed() == false) && (inreq == true) && (DWVSerialPorts.isOpen(this.vport)))
		{
				
			int inbyte = 0;
			
			
			
			try 
			{
				inbyte = skt.getInputStream().read();
			} 
			catch (IOException e) 
			{
				logger.debug("socket error: " + e.getMessage());
				inreq = false;
				try 
				{
					skt.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
				
			if (inbyte == 10)
			{
				if (thisline.length() == 0)
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
			else if (inbyte != 13)
			{
				thisline += (char) inbyte;
			}
		}
		
			
			
		// byte[] buffer = new byte[409600];
		int reqbytes = 0;
			
		if ((skt.isClosed() == false) && DWVSerialPorts.isOpen(this.vport)) 
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
					cocobyte = input.getInputStream().read();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				
				// logger.debug("coco: " + cocobyte + " / " + reqbytes);

				if (cocobyte == -1)
				{
					inresp = false;
				}
				else
				{
					
				
					if ((cocobyte == 13) && (numzeros > 0))
					{
						inresp = false;
						logger.debug("end of resp");
					}
				
					else 
					{
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
					
						try 
						{
							if (skt.isClosed() == false)
							{
								skt.getOutputStream().write(cocobyte);
							}
						} 
						catch (IOException e) 
						{
							logger.debug("closing socket, exception: " + e.getMessage());
						
							try 
							{
								skt.close();
							} 
							catch (IOException e1) 
							{
							// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			}
		}
	
			
			
			
//			skt.getOutputStream().write(buffer, 0, reqbytes);
			
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
		
		// set port disconnected
		DWVSerialPorts.markDisconnected(this.vport);
		// set port output to null
		DWVSerialPorts.setPortOutput(this.vport,null);
		
		
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
		
		// logger.debug("reqline: " + req.length() + " '" + req + "'");
		
			
		DWVSerialPorts.writeToCoco(this.vport, (byte) connid);
		DWVSerialPorts.writeToCoco(this.vport, (byte) ctl);
		DWVSerialPorts.writeToCoco(this.vport, req);
		
		
		
	}
	
}
