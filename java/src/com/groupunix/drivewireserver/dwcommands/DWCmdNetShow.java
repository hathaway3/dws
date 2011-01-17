package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.virtualserial.DWVPortListenerPool;

public class DWCmdNetShow implements DWCommand {

	private int handlerno;

	public DWCmdNetShow(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "show";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show networking status";
	}


	public String getUsage() 
	{
		return "dw net show";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doNetShow());
	}

	
	private DWCommandResponse doNetShow()
	{
		String text = new String();
		
		text += "\r\nDriveWire Network Connections:\r\n\n";

			
		for (int i = 0; i<DWVPortListenerPool.MAX_CONN;i++)
		{
				
	
			try 
			{
				text += "Connection " + i + ": " + DriveWireServer.getHandler(this.handlerno).getVPorts().getListenerPool().getConn(i).getInetAddress().getHostName() + ":" + DriveWireServer.getHandler(this.handlerno).getVPorts().getListenerPool().getConn(i).getPort() + " (connected to port " + DriveWireServer.getHandler(handlerno).getVPorts().prettyPort(DriveWireServer.getHandler(this.handlerno).getVPorts().getListenerPool().getConnPort(i)) + ")\r\n";
			} 
			catch (DWConnectionNotValidException e) 
			{
				// text += e.getMessage();
			}
		}
			
		text += "\r\n";
			
		for (int i = 0; i<DWVPortListenerPool.MAX_LISTEN;i++)
		{
			if (DriveWireServer.getHandler(this.handlerno).getVPorts().getListenerPool().getListener(i) != null)
			{
				text += "Listener " + i + ": TCP port " + DriveWireServer.getHandler(this.handlerno).getVPorts().getListenerPool().getListener(i).getLocalPort() + " (control port " + DriveWireServer.getHandler(handlerno).getVPorts().prettyPort(DriveWireServer.getHandler(this.handlerno).getVPorts().getListenerPool().getListenerPort(i)) +")\r\n";
			}
		}
		
		return(new DWCommandResponse(text));
		
	}
	
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}

}
