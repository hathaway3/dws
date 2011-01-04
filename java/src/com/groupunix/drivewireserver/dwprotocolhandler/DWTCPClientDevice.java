package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;


public class DWTCPClientDevice implements DWProtocolDevice {

	private static final Logger logger = Logger.getLogger("DWServer.DWTCPClientDev");
	private int tcpport;
	private String tcphost;
	private int handlerno;
	private Socket sock;

	private boolean bytelog = false;
	
	public DWTCPClientDevice(int handlerno, String tcphost, int tcpport) throws IOException 
	{
		this.handlerno = handlerno;
		this.tcpport = tcpport;
		this.tcphost = tcphost;
		
		bytelog = DriveWireServer.getHandler(this.handlerno).config.getBoolean("LogDeviceBytes",false);
		
		logger.debug("init tcp device client to " + tcphost + " port " + tcpport + " for handler #" + handlerno + " (logging bytes: " + bytelog + ")");
		
		// check for listen address
			
		sock = new Socket(this.tcphost, this.tcpport);
	
	

		
	}


	public void close() 
	{
		logger.info("closing tcp client device in handler #" + this.handlerno);
		
	
		
		try 
		{
			sock.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	

	public byte[] comRead(int len) throws DWCommTimeOutException 
	{

		byte[] buf = new byte[len];
		
		for (int i = 0;i<len;i++)
		{
			buf[i] = (byte) comRead1(true);
		}
		
		return(buf);

	}

	
	public int comRead1(boolean timeout) throws DWCommTimeOutException 
	{
		int data = -1;
		
			
		try 
		{
			data = sock.getInputStream().read();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			
		}
		
		if (data < 0)
		{
			// read problem
			
			logger.info("socket error reading device");
			
			return(-1);

		}
			
		if (bytelog)
			logger.debug("TCPREAD: " + data);
		
		return data;
	}

	
	
	public void comWrite(byte[] data, int len) 
	{
		try 
		{
				
			sock.getOutputStream().write(data, 0, len);
			
			if (bytelog)
			{
				String tmps = new String();
			
				for (int i = 0;i< data.length;i++)
				{
					tmps += " " + (int)(data[i] & 0xFF);
				}
			
				logger.debug("WRITE " + data.length + ":" + tmps);
			}
		} 
		catch (IOException e) 
		 {
			
			logger.error(e.getMessage());
			
		}
	}

	
	public void comWrite1(int data) 
	{
		try 
		{

				
			sock.getOutputStream().write((byte) data);
			
			if (bytelog)
				logger.debug("TCP-C-WRITE1: " + data);
			
		} 
		catch (IOException e) 
		{
			// problem with comm port, bail out
			logger.error(e.getMessage());
			
		}
		
	}


	public boolean connected() 
	{
		if (sock == null)
			return false;
		
		if (sock.isClosed())
			return false;
		
		return true;
	}


	public void shutdown() 
	{
		close();
	}

	
	
	public int getRate()
	{
		// doesn't make sense here?
		return(-1);
	}


	@Override
	public String getDeviceName() 
	{
		return(this.tcphost + ":" + this.tcpport);
	}


	@Override
	public String getDeviceType() 
	{
		return("tcp-client");
	}
	
}
