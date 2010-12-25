package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowInstances implements DWCommand {

	@Override
	public String getCommand() 
	{
		// TODO Auto-generated method stub
		return "instances";
	}

	@Override
	public String getLongHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortHelp() {
		// TODO Auto-generated method stub
		return "show available instances";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "ui server show instances";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String txt = new String();
		
		for (int i =0;i < DriveWireServer.getNumHandlers();i++)
		{
			txt += DriveWireServer.getHandlerName(i) + "\n";
			
		}
		
		return(new DWCommandResponse(txt));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}