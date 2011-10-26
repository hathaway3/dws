package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.virtualserial.DWVPortListenerPool;

public class DWCmdNetShow extends DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdNetShow(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "show";
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
				text += "Connection " + i + ": " + dwProto.getVPorts().getListenerPool().getConn(i).getInetAddress().getHostName() + ":" + dwProto.getVPorts().getListenerPool().getConn(i).getPort() + " (connected to port " + dwProto.getVPorts().prettyPort(dwProto.getVPorts().getListenerPool().getConnPort(i)) + ")\r\n";
			} 
			catch (DWConnectionNotValidException e) 
			{
				// text += e.getMessage();
			}
		}
			
		text += "\r\n";
			
		for (int i = 0; i<DWVPortListenerPool.MAX_LISTEN;i++)
		{
			if (dwProto.getVPorts().getListenerPool().getListener(i) != null)
			{
				text += "Listener " + i + ": TCP port " + dwProto.getVPorts().getListenerPool().getListener(i).getLocalPort() + " (control port " + dwProto.getVPorts().prettyPort(dwProto.getVPorts().getListenerPool().getListenerPort(i)) +")\r\n";
			}
		}
		
		return(new DWCommandResponse(text));
		
	}
	
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}

}
