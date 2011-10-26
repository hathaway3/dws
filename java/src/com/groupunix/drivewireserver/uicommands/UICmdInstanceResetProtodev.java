package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdInstanceResetProtodev extends DWCommand {

	static final String command = "protodev";
	
	private DWUIClientThread uiref;

	public UICmdInstanceResetProtodev(DWUIClientThread dwuiClientThread) 
	{

		this.uiref = dwuiClientThread;
	}

	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		String res = "Resetting protocol device in instance " + this.uiref.getInstance();
		
		DriveWireServer.getHandler(this.uiref.getInstance()).resetProtocolDevice();
		
		return(new DWCommandResponse(res));
	}



	public String getShortHelp() 
	{
		return "Reset protocol device";
	}


	public String getUsage() 
	{
		return "ui instance reset protodev";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}