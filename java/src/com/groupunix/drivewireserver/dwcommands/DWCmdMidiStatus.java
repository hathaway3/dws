package com.groupunix.drivewireserver.dwcommands;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiStatus implements DWCommand {

	private int handlerno;

	public DWCmdMidiStatus(int handlerno)
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
		return "Show MIDI status";
	}


	public String getUsage() 
	{
		return "dw midi status";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doMidiStatus());
	}

	private DWCommandResponse doMidiStatus()
	{
		String text = new String();
		
		text += "\r\nDriveWire MIDI status:\r\n\n";

		if (DriveWireServer.getHandler(handlerno).config.getBoolean("UseMIDI",true))
		{
			text +="Devices:\r\n";
	
			MidiDevice device;
			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
	
			for (int i = 0; i < infos.length; i++) 
			{
				try 
				{
					device = MidiSystem.getMidiDevice(infos[i]);
					text += "[" + i + "] ";
					text += device.getDeviceInfo().getName() + " (" + device.getClass().getSimpleName()  + ")\r\n";
					text += "    " + device.getDeviceInfo().getDescription() + ", ";
					text += device.getDeviceInfo().getVendor() + " ";
					text += device.getDeviceInfo().getVersion() + "\r\n";
	        
				} 
				catch (MidiUnavailableException e) 
				{
					return(new DWCommandResponse(false,DWDefs.RC_MIDI_UNAVAILABLE,e.getMessage()));
				}
	    
			}

			text += "\r\nCurrent MIDI output device: ";
        
			if (DriveWireServer.getHandler(handlerno).getVPorts().getMidiDeviceInfo() == null)
			{
        	
				text += "none\r\n";
			}
			else
			{
				text += DriveWireServer.getHandler(handlerno).getVPorts().getMidiDeviceInfo().getName() + "\r\n";  
			}
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
