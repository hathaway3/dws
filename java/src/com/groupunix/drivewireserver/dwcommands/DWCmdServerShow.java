package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;


public class DWCmdServerShow implements DWCommand {


	private DWCommandList commands;
	
	
	public DWCmdServerShow(DWProtocol dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdServerShowThreads());
		commands.addcommand(new DWCmdServerShowHandlers());
	}
	
	public String getCommand() 
	{
		return "show";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show various server information";
	}


	public String getUsage() 
	{
		return "dw server show [option]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(commands.parse(cmdline));
	}

	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}

}
