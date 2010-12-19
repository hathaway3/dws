package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdDiskSetLoad implements DWCommand {

	private int handlerno;

	public DWCmdDiskSetLoad(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "load";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Load all disks in diskset";
	}


	public String getUsage() 
	{
		return "dw disk set load setname";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk set load requires a set name as an argument"));
		}
		return(doDiskSetLoad(cmdline));
	}

	
	private DWCommandResponse doDiskSetLoad(String setname)
	{
		if (DriveWireServer.hasDiskset(setname))
		{
			DriveWireServer.getHandler(handlerno).getDiskDrives().LoadDiskSet(setname);
			return(new DWCommandResponse("Loaded disk set '" + setname +"'.  Check log for errors."));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,"No disk set named '" + setname + "' found"));
		}

	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
