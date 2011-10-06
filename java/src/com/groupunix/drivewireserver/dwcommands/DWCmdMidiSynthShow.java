package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;


public class DWCmdMidiSynthShow implements DWCommand 
{

	private DWCommandList commands;
	
	public DWCmdMidiSynthShow(DWProtocolHandler dwProto)
	{
		commands = new DWCommandList(dwProto);
		commands.addcommand(new DWCmdMidiSynthShowChannels(dwProto));
		commands.addcommand(new DWCmdMidiSynthShowInstr(dwProto));
		commands.addcommand(new DWCmdMidiSynthShowProfiles());
	}
	
	public String getCommand() 
	{
		return "show";
	}

	public String getLongHelp() 
	{
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
