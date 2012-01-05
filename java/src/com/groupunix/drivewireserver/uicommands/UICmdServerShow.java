package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShow extends DWCommand {

	static final String command = "show";
	
	private DWCommandList commands = new DWCommandList(null);
		
	public UICmdServerShow(DWUIClientThread dwuiClientThread)
	{
		commands.addcommand(new UICmdServerShowVersion());
		commands.addcommand(new UICmdServerShowInstances());
		commands.addcommand(new UICmdServerShowMIDIDevs());
		commands.addcommand(new UICmdServerShowSynthProfiles());
		commands.addcommand(new UICmdServerShowLocalDisks());
		commands.addcommand(new UICmdServerShowSerialDevs());
		commands.addcommand(new UICmdServerShowStatus());
		commands.addcommand(new UICmdServerShowNet());
		commands.addcommand(new UICmdServerShowLog(dwuiClientThread));
		commands.addcommand(new UICmdServerShowTopics(dwuiClientThread));
		commands.addcommand(new UICmdServerShowHelp(dwuiClientThread));
		commands.addcommand(new UICmdServerShowErrors(dwuiClientThread));
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