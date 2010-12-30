package com.groupunix.drivewireserver.dwcommands;

import javax.sound.midi.Instrument;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiSynthShowInstr implements DWCommand {


	private int handlerno;

	public DWCmdMidiSynthShowInstr(int handlerno)
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
		return "Show internal synth instruments";
	}


	public String getUsage() 
	{
		return "dw midi synth show instr";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String text = new String();
		
		text = "\r\nInternal synthesizer instrument list:\r\n\n";
		
		if (DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth() != null)
		{
			Instrument[] instruments = DriveWireServer.getHandler(handlerno).getVPorts().getMidiSynth().getLoadedInstruments();
		
			if (instruments.length == 0)
			{
				text += "No instruments found, you may need to load a soundbank.\r\n";
			}
		
			for (int i = 0;i<instruments.length;i++)
			{
				text += String.format("%3d:%-15s",i,instruments[i].getName());
			
				if ((i % 4) == 0)
				{
					text += "\r\n";
				}
			
			}
			//	instruments[i].getDataClass().getSimpleName()
			text += "\r\n";
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
