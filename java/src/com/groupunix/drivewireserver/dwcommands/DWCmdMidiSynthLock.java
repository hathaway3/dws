package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdMidiSynthLock implements DWCommand {

	private int handlerno;

	public DWCmdMidiSynthLock(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "lock";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Toggle instrument lock";
	}


	public String getUsage() 
	{
		return "dw midi synth lock";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doMidiSynthLock());
	}

	
	private DWCommandResponse doMidiSynthLock()
	{
		if (DriveWireServer.getHandler(handlerno).getVPorts().getMidiVoicelock())
		{
			DriveWireServer.getHandler(handlerno).getVPorts().setMidiVoicelock(false);
			return(new DWCommandResponse("Unlocked MIDI instruments, program changes will be processed"));
		}
		else
		{
			DriveWireServer.getHandler(handlerno).getVPorts().setMidiVoicelock(true);
			return(new DWCommandResponse("Locked MIDI instruments, progam changes will be ignored"));
		}
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
