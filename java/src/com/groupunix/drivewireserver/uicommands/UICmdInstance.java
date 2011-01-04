package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdInstance implements DWCommand {

	static final String command = "instance";
	private DWCommandList commands = new DWCommandList();
		
	public UICmdInstance(DWUIClientThread dwuiClientThread)
	{
		commands.addcommand(new UICmdInstanceAttach(dwuiClientThread));
		commands.addcommand(new UICmdInstanceConfig(dwuiClientThread));
		commands.addcommand(new UICmdInstanceDisk(dwuiClientThread));
		commands.addcommand(new UICmdInstanceReset(dwuiClientThread));
		commands.addcommand(new UICmdInstanceStatus(dwuiClientThread));
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
		return "Instance commands";
	}


	public String getUsage() 
	{
		return "ui instance [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}