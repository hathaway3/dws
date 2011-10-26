package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdDiskset extends DWCommand {

	static final String command = "diskset";
	private DWCommandList commands = new DWCommandList();
		
	public UICmdDiskset(DWUIClientThread dwuiClientThread)
	{
		commands.addcommand(new UICmdDisksetShow());
		commands.addcommand(new UICmdDisksetSet());
	}

	
	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		return(commands.parse(cmdline));
	}



	public String getShortHelp() 
	{
		return "Diskset commands";
	}


	public String getUsage() 
	{
		return "ui diskset [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}