package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;

public class DWCmdDiskOffset implements DWCommand {

	private int handlerno;

	public DWCmdDiskOffset(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "offset";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Set sector offset for drive #";
	}


	public String getUsage() 
	{
		return "dw disk offset # [# of sectors]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
			
		if (args.length < 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: dw disk offset requires a drive # and a number of sectors"));
		}
		
		return(doDiskExpandSet(args[0],args[1]));
			
	}

	

	
	private DWCommandResponse doDiskExpandSet(String drivestr, String ofs) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			int offset = Integer.parseInt(ofs);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().setOffset(driveno, offset);
			return(new DWCommandResponse("Offset for drive " + driveno + " set to " + offset + " sectors."));
			
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive # or offset"));
			
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
