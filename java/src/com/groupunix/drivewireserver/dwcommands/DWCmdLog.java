package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdLog implements DWCommand {

	static final String command = "log";
	private DWCommandList commands;
		
	public DWCmdLog(DWProtocol dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdLogShow());
		
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
		return "Commands for viewing the system log";
	}


	public String getUsage() 
	{
		return "dw log [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
}
