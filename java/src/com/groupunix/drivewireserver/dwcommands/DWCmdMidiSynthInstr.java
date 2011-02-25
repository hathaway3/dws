package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynthInstr implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdMidiSynthInstr(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "instr";
	}

	public String getLongHelp() 
	{
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Manually set channel X to instrument Y";
	}


	public String getUsage() 
	{
		return "dw midi synth instr #X #Y";
	}

	
	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		if (args.length != 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw midi synth instr requires a channel # and an instrument # as arguments"));
		}
		
		int channel;
		int instr;
		
		try
		{
			channel = Integer.parseInt(args[0]) - 1;
			instr = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw midi synth instr requires a channel # and an instrument # as arguments"));
		}
		
		if (dwProto.getVPorts().setMIDIInstr(channel,instr)) 
		{
			return(new DWCommandResponse("Set MIDI channel " + (channel + 1) + " to instrument " + instr));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_MIDI_ERROR,"Failed to set instrument"));
		}
		
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
