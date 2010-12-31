package com.groupunix.drivewireserver.dwcommands;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiSynthStatus implements DWCommand {

	private int handlerno;

	public DWCmdMidiSynthStatus(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "status";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show internal synth status";
	}


	public String getUsage() 
	{
		return "dw midi synth status";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doSynthStatus());
	}

	private DWCommandResponse doSynthStatus()
	{
		String text = new String();
			
		// dw midi synth show
		text = "\r\nInternal synthesizer status:\r\n\n";
		
		if (DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth() != null)
		{
			MidiDevice.Info midiinfo = DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth().getDeviceInfo();

			text += "Device:\r\n";
			text += midiinfo.getVendor() + ", " + midiinfo.getName() + ", " + midiinfo.getVersion() + "\r\n";
			text += midiinfo.getDescription() + "\r\n";

			text += "\r\n";
			
			text += "Soundbank: ";
	
			if (DriveWireServer.getHandler(handlerno).getVPorts().getMidiSoundbankFilename() == null)
			{
				Soundbank sbank = DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth().getDefaultSoundbank();
				
				if (sbank != null)
				{
					text += " (default)\r\n";
					text += sbank.getVendor() + ", " + sbank.getName() + ", " + sbank.getVersion() + "\r\n";
					text += sbank.getDescription() + "\r\n";
				}
				else
				{
					text += " none\r\n";
				}
			}
			else
			{
				File file = new File(DriveWireServer.getHandler(handlerno).getVPorts().getMidiSoundbankFilename());
				try 
				{
					Soundbank sbank = MidiSystem.getSoundbank(file);
			
					text += " (" + DriveWireServer.getHandler(handlerno).getVPorts().getMidiSoundbankFilename() + ")\r\n";
					text += sbank.getVendor() + ", " + sbank.getName() + ", " + sbank.getVersion() + "\r\n";
					text += sbank.getDescription() + "\r\n";
			
				}	 
				catch (InvalidMidiDataException e) 
				{
					return(new DWCommandResponse(false,DWDefs.RC_MIDI_INVALID_DATA,e.getMessage()));
				} 
				catch (IOException e) 
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
				}
			}
			
			text += "\r\n";
			
			text += "Latency:   " + DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth().getLatency() + "\r\n";
			text += "Polyphony: " + DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth().getMaxPolyphony() + "\r\n";
			text += "Position:  " + DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth().getMicrosecondPosition() + "\r\n\n";
			text += "Profile:   " + DriveWireServer.getHandler(handlerno).getVPorts().getMidiProfileName() + "\r\n";
			text += "Instrlock: " + DriveWireServer.getHandler(handlerno).getVPorts().getMidiVoicelock() + "\r\n";
			
		}
		else
		{
			text += "MIDI is disabled.\r\n";
		}
	
			
		return(new DWCommandResponse(text));
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
