package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynth implements DWCommand {

	static final String command = "synth";
	private DWCommandList commands;
		
	public DWCmdMidiSynth(DWProtocolHandler dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdMidiSynthStatus(dwProto));
		commands.addcommand(new DWCmdMidiSynthShow(dwProto));
		commands.addcommand(new DWCmdMidiSynthBank(dwProto));
		commands.addcommand(new DWCmdMidiSynthProfile(dwProto));
		commands.addcommand(new DWCmdMidiSynthLock(dwProto));
		commands.addcommand(new DWCmdMidiSynthInstr(dwProto));
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
		return "Commands that manage the MIDI synth";
	}


	public String getUsage() 
	{
		return "dw midi synth [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
}
