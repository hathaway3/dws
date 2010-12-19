package com.groupunix.drivewireserver.dwcommands;


public class DWCmdMidiSynthShow implements DWCommand 
{

	private DWCommandList commands = new DWCommandList();
	
	public DWCmdMidiSynthShow(int handlerno)
	{
		commands.addcommand(new DWCmdMidiSynthShowChannels(handlerno));
		commands.addcommand(new DWCmdMidiSynthShowInstr(handlerno));
		commands.addcommand(new DWCmdMidiSynthShowProfiles());
	}
	
	public String getCommand() 
	{
		return "show";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show various internal synth info";
	}


	public String getUsage() 
	{
		return "dw midi synth show [item]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(commands.parse(cmdline));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}

}
