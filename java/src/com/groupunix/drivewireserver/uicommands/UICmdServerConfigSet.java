package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfigSet implements DWCommand {

	static final String command = "set";
	

	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{

		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR , "Must specify item"));
		}	
		
		String[] args = cmdline.split(" ");
		
		if (args.length == 1)
		{
			return(doSetConfig(args[0]));
		}
		else
		{
			
			
			return(doSetConfig(args[0],cmdline.substring(args[0].length()+1)));
		}
		
	}


	public String getLongHelp() 
	{

		return null;
	}


	public String getShortHelp() 
	{
		return "Set server configuration item";
	}


	public String getUsage() 
	{
		return "ui server config set [item] [value]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
	
	private DWCommandResponse doSetConfig(String item)
	{
		
		if (DriveWireServer.serverconfig.containsKey(item))
		{
			synchronized(DriveWireServer.serverconfig)
			{
				DriveWireServer.serverconfig.clearProperty(item);
			}
			return(new DWCommandResponse("Item '" + item + "' removed from config."));
		}
		else
		{
			return(new DWCommandResponse("Item '" + item + "' is not set."));
		}
		
		
	}
	
	
	private DWCommandResponse doSetConfig(String item, String value)
	{
		synchronized(DriveWireServer.serverconfig)
		{
			DriveWireServer.serverconfig.setProperty(item, value);
		}
		return(new DWCommandResponse("Item '" + item + "' set to '" + value + "'."));
	}
	
	
}