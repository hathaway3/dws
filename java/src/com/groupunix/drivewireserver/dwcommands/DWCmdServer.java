package com.groupunix.drivewireserver.dwcommands;

public class DWCmdServer implements DWCommand {

	static final String command = "server";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdServer(int handlerno)
	{
		commands.addcommand(new DWCmdServerStatus(handlerno));
		commands.addcommand(new DWCmdServerShow(handlerno));
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
		// TODO Auto-generated method stub
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
	
}
