package com.groupunix.drivewireserver.dwcommands;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiOutput implements DWCommand {

	private int handlerno;

	public DWCmdMidiOutput(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "output";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Set midi output to device #";
	}


	public String getUsage() 
	{
		return "dw midi output #";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: dw midi output requires a device # as an argument"));
		}
		
		return(doMidiOutput(cmdline));
	}

	
	private DWCommandResponse doMidiOutput(String devstr) 
	{
		
		try
		{
			int devno = Integer.parseInt(devstr);
	
			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			
			if ((devno < 0) || (devno > infos.length))
			{
				return(new DWCommandResponse(false,DWDefs.RC_MIDI_INVALID_DEVICE, "Invalid device number for dw midi output."));
			}
			
			DriveWireServer.getHandler(handlerno).getVPorts().setMIDIDevice(MidiSystem.getMidiDevice(infos[devno]));
			
			return(new DWCommandResponse("Set MIDI output device: " + MidiSystem.getMidiDevice(infos[devno]).getDeviceInfo().getName()));
			
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR, "dw midi device requires a numeric device # as an argument"));
					
		} 
		catch (MidiUnavailableException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_MIDI_UNAVAILABLE,e.getMessage()));
		} 
	}
}
