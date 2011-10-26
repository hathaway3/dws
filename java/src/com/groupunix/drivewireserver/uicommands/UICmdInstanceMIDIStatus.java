package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceMIDIStatus extends DWCommand {

	private DWUIClientThread dwuithread;

	public UICmdInstanceMIDIStatus(DWUIClientThread dwuiClientThread) 
	{
		this.dwuithread = dwuiClientThread;
	}


	@Override
	public String getCommand() 
	{
		return "midistatus";
	}


	@Override
	public String getShortHelp() {
		return "show MIDI status";
	}

	@Override
	public String getUsage() {
		return "ui instance midistatus";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String res = "none\r\nnone\r\n";
		
		if (this.dwuithread.getInstance() > -1)
		{
			DWProtocolHandler dwProto = (DWProtocolHandler)DriveWireServer.getHandler(this.dwuithread.getInstance());
		
			if (!(dwProto == null) && !(dwProto.getVPorts() == null) &&  !(dwProto.getVPorts().getMidiDeviceInfo() == null) )
			{
    			res = dwProto.getVPorts().getMidiDeviceInfo().getName() + "\r\n";
				res += dwProto.getVPorts().getMidiProfileName() + "\r\n";
			}
		}
		
		return(new DWCommandResponse(res));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
