package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServer implements DWCommand {

	static final String command = "server";
	private DWCommandList commands;
		
	public DWCmdServer(DWProtocol dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdServerStatus(dwProto));
		commands.addcommand(new DWCmdServerShow(dwProto));
		commands.addcommand(new DWCmdServerList());
		commands.addcommand(new DWCmdServerDir());
		commands.addcommand(new DWCmdServerTurbo(dwProto));
		commands.addcommand(new DWCmdServerPrint(dwProto));
		
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
		return "Commands for the server";
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
