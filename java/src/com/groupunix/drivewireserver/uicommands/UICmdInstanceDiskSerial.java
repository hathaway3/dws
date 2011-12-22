package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceDiskSerial extends DWCommand {

	static final String command = "serial";
	
	private DWUIClientThread uiref;

	public UICmdInstanceDiskSerial(DWUIClientThread dwuiClientThread) 
	{

		this.uiref = dwuiClientThread;
	}

	public String getCommand() 
	{
		return command;
	} 

	public DWCommandResponse parse(String cmdline)
	{
		String res = new String();
		
		// TODO ASSumes we are using DW protocol
		DWProtocolHandler dwProto = (DWProtocolHandler) DriveWireServer.getHandler(this.uiref.getInstance());
		
		if ((dwProto != null) && (dwProto.getDiskDrives() != null))
		{
			res = "" + dwProto.getDiskDrives().getDiskDriveSerial();
		}
		else
		{
			res = "-1";
		}
		
		return(new DWCommandResponse(res));
	}


	public String getShortHelp() 
	{
		return "Show current disks";
	}


	public String getUsage() 
	{
		return "ui instance disk show";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}