package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdConfig implements DWCommand
{
	
	static final String command = "config";
	private DWCommandList commands;
		
	
	public DWCmdConfig(DWProtocol dwProtocol)
	{
		commands = new DWCommandList(dwProtocol);
		commands.addcommand(new DWCmdConfigShow(dwProtocol));
		commands.addcommand(new DWCmdConfigSet(dwProtocol));
		commands.addcommand(new DWCmdConfigSave(dwProtocol));
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
		return "Commands that manipulate the config";
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
