package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

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
	private static int HTTP_TIMEOUT = 200;
	
	
	
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
		} 
		catch (IOException e2) 
		{
			logger.error("Error opening socket on port " + this.tcpport +": " + e2.getMessage());
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 12, e2.getMessage());
			wanttodie = true;
			return;
		}
		
		dwVSerialPorts.writeToCoco(vport, "OK listening on port " + this.tcpport + (char) 0 + (char) 13);
		
		// DWVSerialPorts.setSocket(this.vport, srvr);
		DWVPortListenerPool.addListener(this.vport, srvr);
		
		
		while ((wanttodie == false) && dwVSerialPorts.isOpen(this.vport) && (srvr.isClosed() == false))
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
			
			if (mode == 2)
			{
				// http mode
				processHTTPReq(skt);
			
			}
			else
			{
				// run telnet preflight, let it add the connection to the pool if things work out
				Thread pfthread = new Thread(new DWVPortTelnetPreflightThread(this.handlerno, this.vport, skt, this.do_telnet, this.do_auth, this.do_protect, this.do_banner));
				pfthread.start();
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
		
		logger.debug("new http connection from " + skt.getInetAddress().getHostAddress());
		
		int inbyte = 0;
		int timeout = 0;
		
		// mark port connected
		dwVSerialPorts.markConnected(this.vport);
		
		while ((skt.isClosed() == false) && (dwVSerialPorts.isOpen(this.vport)))
		{
			
			
			try 
			{
				while ((skt.getInputStream().available() == 0) && (timeout < HTTP_TIMEOUT))
				{
					Thread.sleep(50);
					timeout++;
				}
			
				if (timeout >= HTTP_TIMEOUT)
				{
					logger.info("HTTPD: reading request timed out");
					skt.close();
				}
			
				timeout = 0;
				inbyte = skt.getInputStream().read();
				
				dwVSerialPorts.writeToCoco(this.vport, (byte)inbyte);
				
			}
			catch (IOException e) 
			{
				logger.debug("socket error: " + e.getMessage());
			
				try 
				{
					skt.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (InterruptedException e2)
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			
		}
		
		logger.debug("finished http connection for " + skt.getInetAddress().getHostAddress());
		
	}
	
	

	@SuppressWarnings("unused")
	private void processHTTPReqOld(Socket skt) 
	{
		
		// get request
		
		boolean inreq = true;
		DWVSerialCircularBuffer input = new DWVSerialCircularBuffer(-1, true);
		
		String thisline = new String();
	
		// mark port connected
		dwVSerialPorts.markConnected(this.vport);
		// set port's output to our buffer
		dwVSerialPorts.setPortOutput(this.vport, input.getOutputStream());
		
		while ((skt.isClosed() == false) && (inreq == true) && (dwVSerialPorts.isOpen(this.vport)))
		{
				
			int inbyte = 0;
			
			int timeout = 0;
			
			try 
			{
				while ((skt.getInputStream().available() == 0) && (timeout < HTTP_TIMEOUT))
				{
					Thread.sleep(50);
					timeout++;
				}
			
				if (timeout >= HTTP_TIMEOUT)
				{
					logger.info("HTTPD: reading request timed out");
					skt.close();
				}
			
			
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
			} catch (InterruptedException e2)
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
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
		
			
			
		
		int reqbytes = 0;
			
		if ((skt.isClosed() == false) && dwVSerialPorts.isOpen(this.vport)) 
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
		dwVSerialPorts.markDisconnected(this.vport);
		// set port output to null
		dwVSerialPorts.setPortOutput(this.vport,null);
		
		
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
		
			
		dwVSerialPorts.writeToCoco(this.vport, (byte) connid);
		dwVSerialPorts.writeToCoco(this.vport, (byte) ctl);
		dwVSerialPorts.writeToCoco(this.vport, req);
	
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
