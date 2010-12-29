package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

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
		return "Toggle write protect on drive # ";
	}


	public String getUsage() 
	{
		return "dw disk wp # [on|off]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk wp requires a drive # as an argument"));
		} 
		else 
		{
			String[] args = cmdline.split(" ");
			
			if (args.length == 1)
			{
				return(doDiskWPToggle(args[0]));
			}
			else
			{
				return(doDiskWPSet(args[0],args[1]));
			}
			
		}
			
	}

	

	
	private DWCommandResponse doDiskWPSet(String drivestr, String tf) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DWUtils.isStringFalse(tf))
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().setWriteProtect(driveno, false);
				return(new DWCommandResponse("Disk in drive " + driveno + " is now writeable."));
			}
			else if (DWUtils.isStringTrue(tf))
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().setWriteProtect(driveno, true);
				return(new DWCommandResponse("Disk in drive " + driveno + " is now write protected."));
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: second parameter must be 'true' or 'false'"));
			}
			
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive #"));
			
		} catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED, e.getMessage()));
		} 
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
