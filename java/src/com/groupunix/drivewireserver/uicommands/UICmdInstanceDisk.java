package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdInstanceDisk implements DWCommand {

	static final String command = "disk";
	
	private DWCommandList commands = new DWCommandList();
		
	public UICmdInstanceDisk(DWUIClientThread dwuiClientThread)
	{

		commands.addcommand(new UICmdInstanceDiskShow(dwuiClientThread));
		
	}

	
	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		return(commands.parse(cmdline));
	}


	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}


	public String getShortHelp() 
	{
		return "Instance disk commands";
	}


	public String getUsage() 
	{
		return "ui instance disk [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}