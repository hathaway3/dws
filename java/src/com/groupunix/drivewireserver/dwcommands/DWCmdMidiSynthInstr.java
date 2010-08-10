package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiSynthInstr implements DWCommand {

	private int handlerno;

	public DWCmdMidiSynthInstr(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "instr";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
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
		
		if (DriveWireServer.getHandler(handlerno).getVPorts().setMIDIInstr(channel,instr)) 
		{
			return(new DWCommandResponse("Set MIDI channel " + (channel + 1) + " to instrument " + instr));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_MIDI_ERROR,"Failed to set instrument"));
		}
		
	}

	
}
