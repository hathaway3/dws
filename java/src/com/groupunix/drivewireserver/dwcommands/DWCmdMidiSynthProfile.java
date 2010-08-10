package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiSynthProfile implements DWCommand {

	private int handlerno;

	public DWCmdMidiSynthProfile(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "profile";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Load synth translation profile";
	}


	public String getUsage() 
	{
		return "dw midi synth profile name";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw midi synth profile requires a profile name as an argument"));
		}
		
		return(doMidiSynthProfile(cmdline));
	}

	
	private DWCommandResponse doMidiSynthProfile(String path)
	{
		
		if (DriveWireServer.getHandler(handlerno).getVPorts().setMidiProfile(path))
		{
			return(new DWCommandResponse("Set translation profile to '" + path + "'"));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_MIDI_INVALID_PROFILE,"Invalid translation profile '" + path + "'"));
		}
	}
}
