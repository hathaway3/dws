package com.groupunix.drivewireserver.uicommands;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowMIDIDevs implements DWCommand {

	@Override
	public String getCommand() 
	{
		// TODO Auto-generated method stub
		return "mididevs";
	}

	@Override
	public String getLongHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortHelp() {
		// TODO Auto-generated method stub
		return "show available MIDI devices";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "ui server show mididevs";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String res = new String();
	
		MidiDevice device;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
	
		for (int i = 0; i < infos.length; i++) 
		{
			try 
			{
				device = MidiSystem.getMidiDevice(infos[i]);
				res += i + " " + device.getDeviceInfo().getName()+ " (" + device.getClass().getSimpleName() + ")\n";
				
			} 
			catch (MidiUnavailableException e) 
			{
				return(new DWCommandResponse(false,DWDefs.RC_MIDI_UNAVAILABLE,"MIDI unavailable during UI device listing"));
			}
	    
		}
		
		
		return(new DWCommandResponse(res));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
