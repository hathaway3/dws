package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdDiskWP implements DWCommand {

	private int handlerno;

	public DWCmdDiskWP(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "wp";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Toggle write protect on drive #";
	}


	public String getUsage() 
	{
		return "dw disk wp #";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk wp requires a drive # as an argument"));
		}
		return(doDiskWPToggle(cmdline));
	}

	

	
	private DWCommandResponse doDiskWPToggle(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DriveWireServer.getHandler(handlerno).getDiskDrives().getWriteProtect(driveno))
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().setWriteProtect(driveno, false);
				return(new DWCommandResponse("Disk in drive " + driveno + " is now writeable."));
			}
			else
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().setWriteProtect(driveno, true);
				return(new DWCommandResponse("Disk in drive " + driveno + " is now write protected."));
			}
			
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive #"));
			
		} 

	}

}
