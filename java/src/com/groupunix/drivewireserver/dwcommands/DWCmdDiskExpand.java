package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskExpand implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskExpand(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "expand";
	}

	public String getLongHelp() 
	{
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Toggle auto expansion on drive #";
	}


	public String getUsage() 
	{
		return "dw disk expand # [on|off]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
			
		if (args.length < 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: dw disk expand requires a drive # and a setting"));
		}
		
		return(doDiskExpandSet(args[0],args[1]));
			
	}

	

	
	private DWCommandResponse doDiskExpandSet(String drivestr, String tf) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DWUtils.isStringFalse(tf))
			{
				dwProto.getDiskDrives().setExpand(driveno, false);
				return(new DWCommandResponse("Image in drive " + driveno + " will NOT be automatically expanded."));
			}
			else if (DWUtils.isStringTrue(tf))
			{
				dwProto.getDiskDrives().setExpand(driveno, true);
				return(new DWCommandResponse("Image in drive " + driveno + " will be automatically expanded."));
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
