package com.groupunix.drivewireserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwcommands.DWCmd;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.uicommands.UICmd;

public class DWUIClientThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWUIClientThread");

	private Socket skt;
	private boolean wanttodie = false;
	private int instance = -1;
	
	private DWCommandList commands;
	
	
	
	public DWUIClientThread(Socket skt) 
	{
		this.skt = skt;
		
		commands = new DWCommandList();
		commands.addcommand(new UICmd(this));
	}

	
	
	public void run() 
	{

		Thread.currentThread().setName("dwUIcliIn-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		
		logger.debug("run for client at " + skt.getInetAddress().getHostAddress());
		
		
		try 
		{
			skt.getOutputStream().write(("Connected to DriveWire " + DriveWireServer.DWServerVersion + "\r\n").getBytes());

			
			// cmd loop
			
			String cmd = new String();
			
			while ((!skt.isClosed()) && (!wanttodie))
			{

				int databyte = skt.getInputStream().read();
			
				if (databyte == -1)
				{	
					//logger.debug("got -1 in input stream");
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
			
			skt.close();
			
			
		} 
		catch (IOException e) 
		{
			logger.warn("IO Exception: " + e.getMessage());
		}

		
		//DriveWireServer.getHandler(ourHandler).getEventHandler().unregisterAllEvents(this.uiport);
		
		logger.debug("exit");
	}

	
	
	private void doCmd(String cmd) throws IOException 
	{
		
		skt.getOutputStream().write(("\n>>\n").getBytes());
		
		DWCommandResponse resp = this.commands.parse(cmd);
		
		if (resp.getSuccess())
		{

			sendUIresponse(skt.getOutputStream(),resp.getResponseText());
		}
		else
		{

			sendUIresponse(skt.getOutputStream(),"FAIL " + (resp.getResponseCode() & 0xFF) + " " + resp.getResponseText());

		}
		

		
		skt.getOutputStream().write(("<<\n").getBytes());
	}


	private void sendUIresponse(OutputStream outputStream, String txt) throws IOException 
	{

		outputStream.write(txt.getBytes());

	}



	public void setInstance(int handler) 
	{
		this.instance = handler;
		if (!this.commands.validate("dw"))
			this.commands.addcommand(new DWCmd(DriveWireServer.getHandler(handler)));
	}

	public int getInstance() 
	{
		return this.instance;
	}

	public OutputStream getOutputStream() throws IOException 
	{
		return skt.getOutputStream();
	}

	public Socket getSocket() {
		
		return skt;
	}
	
	

}
