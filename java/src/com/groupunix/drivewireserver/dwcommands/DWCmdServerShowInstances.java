package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdServerShowInstances extends DWCommand {

	DWCmdServerShowInstances(DWCommand parent)
	{
		setParentCmd(parent);
		}
	
	public String getCommand() 
	{
		return "instances";
	}


	
	public String getShortHelp() 
	{
		return "Show handler instances";
	}


	public String getUsage() 
	{
		return "dw server show instances";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String text = new String();
		
		text += "DriveWire protocol handler instances:\r\n";
		
		for (int i = 0;i<DriveWireServer.getNumHandlers();i++)
		{
			if (DriveWireServer.getHandler(i) != null)
			{
				String dtype = DriveWireServer.getHandler(i).getConfig().getString("DeviceType");
				
				text += "\r\nInstance #" + i + ":  Type " + dtype;
				
				if (DriveWireServer.getHandler(i).getConfig().getString("DeviceType").equals("serial") )
				{
					text += " Device " + DriveWireServer.getHandler(i).getConfig().getString("SerialDevice");
				}
				else if (DriveWireServer.getHandler(i).getConfig().getString("DeviceType").equals("tcp-server") )
				{
					text += " Port " + DriveWireServer.getHandler(i).getConfig().getString("TCPServerPort");
				}
				else if (DriveWireServer.getHandler(i).getConfig().getString("DeviceType").equals("tcp-client") )
				{
					text += " Client " + DriveWireServer.getHandler(i).getConfig().getString("TCPClientHost");
					text += " Port " + DriveWireServer.getHandler(i).getConfig().getString("TCPClientPort");
				}
				
				text += " Status: ";
				
				if (DriveWireServer.getHandler(i).isDying())
					text += "Dying..";
				else if (DriveWireServer.getHandler(i).isReady())
					text += "Ready";
				else
					text += "Starting..";
				
				text += "\r\n";
			}
			
		}
		
		return(new DWCommandResponse(text));
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
