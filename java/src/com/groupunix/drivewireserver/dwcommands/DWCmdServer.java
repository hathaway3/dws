package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServer implements DWCommand {

	static final String command = "server";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdServer(DWProtocol dwProto)
	{
		commands.addcommand(new DWCmdServerStatus(dwProto));
		commands.addcommand(new DWCmdServerShow());
		commands.addcommand(new DWCmdServerList());
		commands.addcommand(new DWCmdServerDir());
	//	commands.addcommand(new DWCmdServerRestart(handlerno));
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
		return null;
	}


	public String getShortHelp() 
	{
		return "Commands that interface with the server";
	}


	public String getUsage() 
	{
		return "dw server [command]";
	}

	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
}
