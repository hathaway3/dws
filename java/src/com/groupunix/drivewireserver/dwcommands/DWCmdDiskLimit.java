package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;

public class DWCmdDiskLimit implements DWCommand {

	private int handlerno;

	public DWCmdDiskLimit(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "limit";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Set size limit for drive #";
	}


	public String getUsage() 
	{
		return "dw disk limit # [# of sectors]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
			
		if (args.length < 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: dw disk limit requires a drive # and a number of sectors"));
		}
		
		return(doDiskLimitSet(args[0],args[1]));
			
	}

	

	
	private DWCommandResponse doDiskLimitSet(String drivestr, String ofs) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			int limit = Integer.parseInt(ofs);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().setLimit(driveno, limit);
			
			if (limit == -1)
				return(new DWCommandResponse("Size limit for drive " + driveno + " disabled."));
			
			return(new DWCommandResponse("Size limit for drive " + driveno + " set to " + limit + " sectors."));
			
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive # or size"));
			
		} catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED, e.getMessage()));
		} 
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
