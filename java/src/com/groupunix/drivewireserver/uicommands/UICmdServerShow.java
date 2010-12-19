package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShow implements DWCommand {

	static final String command = "show";
	
	private DWCommandList commands = new DWCommandList();
		
	public UICmdServerShow(DWUIClientThread dwuiClientThread)
	{
		commands.addcommand(new UICmdServerShowVersion());
		commands.addcommand(new UICmdServerShowInstances());
		commands.addcommand(new UICmdServerShowDisksets());
		commands.addcommand(new UICmdServerShowMIDIDevs());
		commands.addcommand(new UICmdServerShowSynthProfiles());
		commands.addcommand(new UICmdServerShowLocalDisks());
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
		return "Informational commands";
	}


	public String getUsage() 
	{
		return "ui server show [item]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}