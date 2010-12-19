package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerConfig implements DWCommand {

	static final String command = "config";
	
	private DWCommandList commands = new DWCommandList();
		
	public UICmdServerConfig(DWUIClientThread dwuiClientThread)
	{

		commands.addcommand(new UICmdServerConfigShow());
		commands.addcommand(new UICmdServerConfigSet());
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
		return "Configuration commands";
	}


	public String getUsage() 
	{
		return "ui server config [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}