package com.groupunix.drivewireserver.dwcommands;

public class DWCmdPort implements DWCommand {

	static final String command = "port";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdPort(int handlerno)
	{
		commands.addcommand(new DWCmdPortShow(handlerno));
		commands.addcommand(new DWCmdPortClose(handlerno));
		
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
