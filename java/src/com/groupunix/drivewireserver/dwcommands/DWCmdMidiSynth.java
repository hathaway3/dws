package com.groupunix.drivewireserver.dwcommands;

public class DWCmdMidiSynth implements DWCommand {

	static final String command = "synth";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdMidiSynth(int handlerno)
	{
		commands.addcommand(new DWCmdMidiSynthStatus(handlerno));
		commands.addcommand(new DWCmdMidiSynthShow(handlerno));
		commands.addcommand(new DWCmdMidiSynthBank(handlerno));
		commands.addcommand(new DWCmdMidiSynthProfile(handlerno));
		commands.addcommand(new DWCmdMidiSynthLock(handlerno));
		commands.addcommand(new DWCmdMidiSynthInstr(handlerno));
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
		return "Commands that manage the MIDI synthesizer";
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
