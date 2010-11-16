package com.groupunix.drivewireserver;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;

public class DWUIClientThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWUIClientThread");

	private Socket skt;
	private boolean wanttodie = false;
	private int uiport;
	private Thread outputT;
	
	public DWUIClientThread(Socket skt) 
	{
		this.skt = skt;
		
	}

	public void run() 
	{
		Thread.currentThread().setName("dwUIcliIn-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		
		logger.debug("run for client at " + skt.getInetAddress().getCanonicalHostName());
		
		
		try 
		{
			skt.getOutputStream().write(("Connected to DriveWire " + DriveWireServer.DWServerVersion + "\r\n").getBytes());

			// open UI port, default to instance 0 for now... this does not really make sense?
			this.uiport = DriveWireServer.getHandler(0).getVPorts().openUIPort();
			
			if (this.uiport == -1)
			{
				logger.warn("failed to open UI port");
			 	this.skt.getOutputStream().write("FAIL could not open UI port\r\n".getBytes());
			}
			else
			{
			   this.skt.getOutputStream().write(("OK using UI port " + this.uiport + "\r\n").getBytes());
			   
			   
				// start output thread
				this.outputT = new Thread(new DWUIClientOutputThread(this.skt.getOutputStream(),this.uiport));
				outputT.start();
				
				String cmd = new String();
			
				while ((!skt.isClosed()) && (!wanttodie))
				{

					int databyte = skt.getInputStream().read();
					
					if (databyte == -1)
					{	
						logger.debug("got -1 in input stream");
						wanttodie = true;
					}
					else
					{
						if (databyte == 10)
						{
							doCmd(cmd);
							cmd = "";
						}
						else
						{
							if ((databyte == 8) && (cmd.length() > 0))
							{
								cmd = cmd.substring(0, cmd.length() - 1);
							}
							else if (databyte > 0)
							{
								cmd += Character.toString((char) databyte);
							}
						}
					}
				}
			
				DriveWireServer.getHandler(0).getVPorts().closePort(uiport);
			}
			
			this.outputT.interrupt();
			
			skt.close();
			
			
		} 
		catch (IOException e) 
		{
			logger.warn("IO Exception: " + e.getMessage());
		}
		catch (DWPortNotValidException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DriveWireServer.getHandler(0).getEventHandler().unregisterAllEvents(this.uiport);
		
		logger.debug("exit");
	}

	private void doCmd(String cmd) throws IOException 
	{
		DriveWireServer.getHandler(0).getVPorts().write(uiport, cmd + "\r");
		//skt.getOutputStream().write(0);
	}

}
