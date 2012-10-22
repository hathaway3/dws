package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerFile extends DWCommand {

	static final String command = "file";
	
		
	public UICmdServerFile(DWUIClientThread dwuiClientThread)
	{

		commands.addcommand(new UICmdServerFileRoots(dwuiClientThread));
		commands.addcommand(new UICmdServerFileDefaultDir(dwuiClientThread));
		commands.addcommand(new UICmdServerFileDir(dwuiClientThread));
		commands.addcommand(new UICmdServerFileInfo(dwuiClientThread));
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
		return "File commands";
	}


	public String getUsage() 
	{
		return "ui server file [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}