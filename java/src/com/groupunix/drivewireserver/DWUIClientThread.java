package com.groupunix.drivewireserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.uicommands.UICmdDiskset;
import com.groupunix.drivewireserver.uicommands.UICmdInstance;
import com.groupunix.drivewireserver.uicommands.UICmdLogview;
import com.groupunix.drivewireserver.uicommands.UICmdServer;

public class DWUIClientThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWUIClientThread");

	private Socket skt;
	private boolean wanttodie = false;
	private int uiport;
	private int instance = -1;
	
	private DWCommandList uiCmds = new DWCommandList();
	
	
	private int ourHandler = 0;
	
	public DWUIClientThread(Socket skt) 
	{
		this.skt = skt;
		
		uiCmds.addcommand(new UICmdInstance(this));
		uiCmds.addcommand(new UICmdServer(this));
		uiCmds.addcommand(new UICmdDiskset(this));
		uiCmds.addcommand(new UICmdLogview(this));
	}

	public void run() 
	{
		
		
		Thread.currentThread().setName("dwUIcliIn-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		
		logger.debug("run for client at " + skt.getInetAddress().getCanonicalHostName());
		
		
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

		
		DriveWireServer.getHandler(ourHandler).getEventHandler().unregisterAllEvents(this.uiport);
		
		logger.debug("exit");
	}

	private void doCmd(String cmd) throws IOException 
	{
		
		skt.getOutputStream().write(("\n>>\n").getBytes());
		
		if (serverCommand(cmd))
		{
			logger.debug("server command: " + cmd);
		}
		else
		{
			if (this.instance < 0)
			{
				logger.debug("no instance requested by client");
			}
			else
			if (instanceCommand(cmd))
			{
				logger.debug("instance command: " + cmd);
			}
			else
			{
				skt.getOutputStream().write(("FAIL " + DWDefs.RC_SYNTAX_ERROR + " unknown command\r\n").getBytes());
			}	
				
		}
		
		skt.getOutputStream().write(("<<\n").getBytes());
	}

	private boolean instanceCommand(String cmd) throws IOException 
	{
		if (cmd.startsWith("dw"))
		{
			if (DriveWireServer.isValidHandlerNo(instance))
			{
				if (DriveWireServer.handlerIsAlive(instance))
				{
			
					DWCommandResponse resp = DriveWireServer.getHandler(instance).getDWCmds().parse(DWUtils.dropFirstToken(cmd));
			
					if (resp.getSuccess())
					{

						sendUIresponse(skt.getOutputStream(),resp.getResponseText());
					}
					else
					{

						sendUIresponse(skt.getOutputStream(),"FAIL " + resp.getResponseCode() + " " + resp.getResponseText());
					}
			
				}
				else
				{
					sendUIresponse(skt.getOutputStream(),"FAIL " + DWDefs.RC_INVALID_HANDLER + " Instance is not running");
				
				}
			}
			else
			{
				sendUIresponse(skt.getOutputStream(),"FAIL " + DWDefs.RC_INVALID_HANDLER + " Instance is not valid");
			
			}
		
			return true;
		}
		
		return false;
	}

	
	private boolean serverCommand(String cmd) throws IOException 
	{
		
		if (cmd.startsWith("ui"))
		{
			DWCommandResponse resp = uiCmds.parse(DWUtils.dropFirstToken(cmd));
			
			if (resp.getSuccess())
			{

				sendUIresponse(skt.getOutputStream(),resp.getResponseText());
			}
			else
			{

				sendUIresponse(skt.getOutputStream(),"FAIL " + resp.getResponseCode() + " " + resp.getResponseText());

			}
			
			return true;
		}
		else
		{
			return false;
		}
	}

	private void sendUIresponse(OutputStream outputStream, String txt) throws IOException 
	{

		outputStream.write(txt.getBytes());

	}

	public void setInstance(int handler) 
	{
		this.instance = handler;
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
