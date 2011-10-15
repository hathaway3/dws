package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskNamedObject implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskNamedObject(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "namedobject";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Toggle named object on drive # ";
	}


	public String getUsage() 
	{
		return "dw disk namedobject # [on|off]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk namedobject requires a drive # as an argument"));
		} 
		else 
		{
			String[] args = cmdline.split(" ");
			
			if (args.length == 1)
			{
				return(doDiskNOToggle(args[0]));
			}
			else
			{
				return(doDiskNOSet(args[0],args[1]));
			}
			
		}
			
	}

	

	
	private DWCommandResponse doDiskNOSet(String drivestr, String tf) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DWUtils.isStringFalse(tf))
			{
				dwProto.getDiskDrives().setNamedObj(driveno, false);
				return(new DWCommandResponse("Disk in drive " + driveno + " is not a named object."));
			}
			else if (DWUtils.isStringTrue(tf))
			{
				dwProto.getDiskDrives().setNamedObj(driveno, true);
				return(new DWCommandResponse("Disk in drive " + driveno + " is a named object."));
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

	private DWCommandResponse doDiskNOToggle(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (dwProto.getDiskDrives().isNamedObj(driveno))
			{
				dwProto.getDiskDrives().setNamedObj(driveno, false);
				return(new DWCommandResponse("Disk in drive " + driveno + " is not a named object."));
			}
			else
			{
				dwProto.getDiskDrives().setNamedObj(driveno, true);
				return(new DWCommandResponse("Disk in drive " + driveno + " is a named object."));
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
