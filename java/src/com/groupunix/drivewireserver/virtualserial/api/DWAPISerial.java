package com.groupunix.drivewireserver.virtualserial.api;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class DWAPISerial {

	
	private String[] command;

	public DWAPISerial(String[] cmd)
	{
		this.setCommand(cmd);
	}

	public DWCommandResponse process() 
	{
		return new DWCommandResponse(false, DWDefs.RC_SERVER_NOT_IMPLEMENTED, "Not implemented");
	}

	public String[] getCommand() {
		return command;
	}

	public void setCommand(String[] command) {
		this.command = command;
	}
	
}
