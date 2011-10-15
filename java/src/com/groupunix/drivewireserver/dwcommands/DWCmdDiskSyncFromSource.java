package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskSyncFromSource implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskSyncFromSource(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "ssync";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Toggle sync from source on drive # ";
	}


	public String getUsage() 
	{
		return "dw disk ssync # [on|off]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk ssync requires a drive # as an argument"));
		} 
		else 
		{
			String[] args = cmdline.split(" ");
			
			if (args.length == 1)
			{
				return(doDiskSSToggle(args[0]));
			}
			else
			{
				return(doDiskSSSet(args[0],args[1]));
			}
			
		}
			
	}

	

	
	private DWCommandResponse doDiskSSSet(String drivestr, String tf) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DWUtils.isStringFalse(tf))
			{
				dwProto.getDiskDrives().setSyncFromSource(driveno, false);
				return(new DWCommandResponse("Disk in drive " + driveno + " will not sync from source."));
			}
			else if (DWUtils.isStringTrue(tf))
			{
				dwProto.getDiskDrives().setSyncFromSource(driveno, true);
				return(new DWCommandResponse("Disk in drive " + driveno + " will sync from source."));
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: second parameter must be 'true' or 'false'"));
			}
			
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive #"));
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED, e.getMessage()));
		} 
	}

	private DWCommandResponse doDiskSSToggle(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (dwProto.getDiskDrives().isSyncFromSource(driveno))
			{
				dwProto.getDiskDrives().setSyncFromSource(driveno, false);
				return(new DWCommandResponse("Disk in drive " + driveno + " will not sync from source."));
			}
			else
			{
				dwProto.getDiskDrives().setSyncFromSource(driveno, true);
				return(new DWCommandResponse("Disk in drive " + driveno + " will sync from source."));
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
