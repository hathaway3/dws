package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdNet implements DWCommand {

	static final String command = "net";
	private DWCommandList commands;
		
	public DWCmdNet(DWProtocolHandler dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdNetShow(dwProto));
		
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
		return "Networking commands";
	}


	public String getUsage() 
	{
		return "dw net [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
}
