package com.groupunix.drivewireserver.dwcommands;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiSynthBank implements DWCommand {

	private int handlerno;

	public DWCmdMidiSynthBank(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "bank";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Load soundbank file";
	}


	public String getUsage() 
	{
		return "dw midi synth bank filepath";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw midi synth bank requires a file path as an argument"));
		}
		
		return(doMidiSynthBank(cmdline));
	}

	
	private DWCommandResponse doMidiSynthBank(String path)
	{
		Soundbank soundbank = null;
		
		File file = new File(path);
		try 
		{
			soundbank = MidiSystem.getSoundbank(file);
		} 
		catch (InvalidMidiDataException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_MIDI_INVALID_DATA,e.getMessage()));
		} 
		catch (IOException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
		}
		
		if (DriveWireServer.getHandler(handlerno).getVPorts().isSoundbankSupported(soundbank))
		{				
			if (DriveWireServer.getHandler(handlerno).getVPorts().setMidiSoundbank(soundbank, path))
			{
				return(new DWCommandResponse("Soundbank loaded without error"));
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_MIDI_SOUNDBANK_FAILED,"Failed to load soundbank"));
			}
			
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_MIDI_SOUNDBANK_NOT_SUPPORTED,"Soundbank not supported"));
		}
	
	}
	

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
