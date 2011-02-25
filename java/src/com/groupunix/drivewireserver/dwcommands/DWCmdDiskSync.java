package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskSync implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskSync(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "sync";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Toggle sync to source on drive #";
	}


	public String getUsage() 
	{
		return "dw disk sync # [on|off]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
			
		if (args.length < 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: dw disk sync requires a drive # and a setting"));
		}
		
		return(doDiskSyncSet(args[0],args[1]));
			
	}

	

	
	private DWCommandResponse doDiskSyncSet(String drivestr, String tf) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DWUtils.isStringFalse(tf))
			{
				dwProto.getDiskDrives().setSync(driveno, false);
				return(new DWCommandResponse("Changes to the disk in drive " + driveno + " will NOT be synced to the source image."));
			}
			else if (DWUtils.isStringTrue(tf))
			{
				dwProto.getDiskDrives().setSync(driveno, true);
				return(new DWCommandResponse("Changes to the disk in drive " + driveno + " will be synced to the source image."));
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

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
