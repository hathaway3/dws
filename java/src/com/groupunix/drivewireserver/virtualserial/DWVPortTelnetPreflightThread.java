package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWVPortTelnetPreflightThread implements Runnable
{

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTelnetPreflightThread");
	
	
	private Socket skt;
	private int vport;

	private boolean banner = false;
	private boolean telnet = false;
	

	private DWVSerialPorts dwVSerialPorts;
	private DWProtocolHandler dwProto;
	
	public DWVPortTelnetPreflightThread(DWProtocolHandler dwProto, int vport, Socket skt, boolean doTelnet, boolean doBanner)
	{
		this.vport = vport;
		this.skt = skt;

		this.banner = doBanner;
		this.telnet = doTelnet;
		this.dwProto = dwProto;
		this.dwVSerialPorts = dwProto.getVPorts();
		
	}

	public void run()
	{
		Thread.currentThread().setName("tcppre-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		logger.info("preflight checks for new connection from " + skt.getInetAddress().getHostName());
		
		try
		{
			// hello
			if (this.telnet)
				skt.getOutputStream().write(("DriveWire Telnet Server " + DriveWireServer.DWServerVersion + "\r\n\n").getBytes());


			if (telnet == true)
			{
				// ask telnet to turn off echo, should probably be a setting or left to the client
				byte[] buf = new byte[9];
		
				buf[0] = (byte) 255;
				buf[1] = (byte) 251;
				buf[2] = (byte) 1;
				buf[3] = (byte) 255;
				buf[4] = (byte) 251;
				buf[5] = (byte) 3;
				buf[6] = (byte) 255;
				buf[7] = (byte) 253;
				buf[8] = (byte) 243;
		
		
				skt.getOutputStream().write(buf, 0, 9);
		
				// 	read back the echoed controls - TODO has issues
		
				for (int i = 0; i<9; i++)
				{
					skt.getInputStream().read();
				}
			}
				
			
			if (skt.isClosed())
			{
				// bail out
				logger.debug("thread exiting after auth");
				return;
			}
			
			
			if ((dwProto.getConfig().containsKey("TelnetBannerFile")) && (banner == true))
			{
				displayFile(skt.getOutputStream(), dwProto.getConfig().getString("TelnetBannerFile"));
			}
			
		} 
		catch (IOException e)
		{
			logger.warn("IOException: " + e.getMessage());
			
			if (skt.isConnected())
			{
				logger.debug("closing socket");
				try
				{
					skt.close();
				} catch (IOException e1)
				{
					logger.warn(e1.getMessage());
				}
			
			}
			
		}
			
		
		if (skt.isClosed() == false)
		{
			
			logger.debug("Preflight success for " + skt.getInetAddress().getHostName());
			
			//add connection to pool
			int conno = this.dwVSerialPorts.getListenerPool().addConn(this.vport, skt, 1);

			
			// announce new connection to listener
			try 
			{
				dwVSerialPorts.sendConnectionAnnouncement(this.vport, conno, skt.getLocalPort(), skt.getInetAddress().getHostAddress());
			} 
			catch (DWPortNotValidException e) 
			{
				logger.error("in announce: " + e.getMessage());
			}
					
		}
		
		logger.debug("exiting");
	}

	

	
	

	


	


	private void displayFile(OutputStream outputStream, String fname) 
	{
		FileInputStream fstream;
		
		try 
		{
			fstream = new FileInputStream(fname);
		
			DataInputStream in = new DataInputStream(fstream);
				
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
			String strLine;
			   
			logger.debug("sending file '" + fname + "' to telnet client");
			
			while ((strLine = br.readLine()) != null)
			{
				  outputStream.write(strLine.getBytes());
				  outputStream.write("\r\n".getBytes());
			}
			
			fstream.close();
			
		} 
		catch (FileNotFoundException e) 
		{
			logger.warn("File not found: " + fname);
		} 
		catch (IOException e1) 
		{
			logger.warn(e1.getMessage());
		}
		 
		 
	}
	
	
	
	
	
}
