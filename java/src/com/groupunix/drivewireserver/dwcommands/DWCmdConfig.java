package com.groupunix.drivewireserver.dwcommands;

public class DWCmdConfig implements DWCommand
{

	static final String command = "config";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdConfig(int handlerno)
	{
		commands.addcommand(new DWCmdConfigShow(handlerno));
		commands.addcommand(new DWCmdConfigSet(handlerno));
		commands.addcommand(new DWCmdConfigSave(handlerno));
		// save/load not implemented here
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
		return "Commands that manipulate the configuration";
	}


	public String getUsage() 
	{
		return "dw config [command]";
	}


	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
	
}
