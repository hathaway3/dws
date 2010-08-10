package com.groupunix.drivewireserver.dwcommands;

public class DWCmdMidi implements DWCommand {

	static final String command = "midi";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdMidi(int handlerno)
	{
		commands.addcommand(new DWCmdMidiStatus(handlerno));
		commands.addcommand(new DWCmdMidiOutput(handlerno));
		commands.addcommand(new DWCmdMidiSynth(handlerno));	
		
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
		return "Commands that manage the MIDI system";
	}


	public String getUsage() 
	{
		return "dw midi [command]";
	}
	
}
