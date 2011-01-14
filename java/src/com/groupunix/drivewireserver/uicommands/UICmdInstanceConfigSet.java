package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdInstanceConfigSet implements DWCommand {

	static final String command = "set";
	private DWUIClientThread uiref;

	public UICmdInstanceConfigSet(DWUIClientThread dwuiClientThread) 
	{
		this.uiref = dwuiClientThread;
	}

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
		return "Set instance configuration item";
	}


	public String getUsage() 
	{
		return "ui instance config set [item] [value]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
	
	private DWCommandResponse doSetConfig(String item)
	{
		
		if (DriveWireServer.getHandler(this.uiref.getInstance()).config.containsKey(item))
		{
			DriveWireServer.getHandler(this.uiref.getInstance()).config.clearProperty(item);
			return(new DWCommandResponse("Item '" + item + "' removed from config."));
		}
		else
		{
			return(new DWCommandResponse("Item '" + item + "' is not set."));
		}
		
		
	}
	
	
	private DWCommandResponse doSetConfig(String item, String value)
	{
		DriveWireServer.getHandler(this.uiref.getInstance()).config.setProperty(item, value);
		return(new DWCommandResponse("Item '" + item + "' set to '" + value + "'."));
	}
	
	
}