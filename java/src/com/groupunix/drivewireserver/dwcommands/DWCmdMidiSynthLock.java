package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdMidiSynthLock implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdMidiSynthLock(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "lock";
	}

	public String getLongHelp() 
	{
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
		if (dwProto.getVPorts().getMidiVoicelock())
		{
			dwProto.getVPorts().setMidiVoicelock(false);
			return(new DWCommandResponse("Unlocked MIDI instruments, program changes will be processed"));
		}
		else
		{
			dwProto.getVPorts().setMidiVoicelock(true);
			return(new DWCommandResponse("Locked MIDI instruments, progam changes will be ignored"));
		}
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
