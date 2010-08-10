package com.groupunix.drivewireserver.dwcommands;


public class DWCmdServerShow implements DWCommand {

	private int handlerno;
	private DWCommandList commands = new DWCommandList();
	
	
	public DWCmdServerShow(int handlerno)
	{
		this.handlerno = handlerno;
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

	

}
