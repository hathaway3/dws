package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdPort implements DWCommand {

	static final String command = "port";
	private DWCommandList commands;
		
	public DWCmdPort(DWProtocolHandler dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdPortShow(dwProto));
		commands.addcommand(new DWCmdPortClose(dwProto));
		commands.addcommand(new DWCmdPortOpen(dwProto));
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
		return "Commands that manage virtual ports";
	}


	public String getUsage() 
	{
		return "dw port [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
}
