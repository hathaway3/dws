package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdServerShowHandlers extends DWCommand {

	DWCmdServerShowHandlers(DWCommand parent)
	{
		setParentCmd(parent);
		}
	
	public String getCommand() 
	{
		return "handlers";
	}


	
	public String getShortHelp() 
	{
		return "Show handler instances";
	}


	public String getUsage() 
	{
		return "dw server show handlers";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String text = new String();
		
		text += "DriveWire protocol handler instances:\r\n";
		
		for (int i = 0;i<DriveWireServer.getNumHandlers();i++)
		{
			if (DriveWireServer.getHandler(i) != null)
			{
				text += "\r\nHandler #" + i + ": Device " + DriveWireServer.getHandler(i).getConfig().getString("SerialDevice") + " CocoModel " + DriveWireServer.getHandler(i).getConfig().getString("CocoModel") + "\r\n"; 
			}
		}
		
		return(new DWCommandResponse(text));
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
