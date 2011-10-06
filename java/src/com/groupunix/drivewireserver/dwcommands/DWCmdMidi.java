package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidi implements DWCommand {

	static final String command = "midi";
	private DWCommandList commands;
		
	public DWCmdMidi(DWProtocolHandler dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdMidiStatus(dwProto));
		commands.addcommand(new DWCmdMidiOutput(dwProto));
		commands.addcommand(new DWCmdMidiSynth(dwProto));	
		
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
		return null;
	}


	public String getShortHelp() 
	{
		return "Commands that manage the MIDI system";
	}


	public String getUsage() 
	{
		return "dw midi [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
}
