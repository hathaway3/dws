package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskEject implements DWCommand 
{
	private DWProtocolHandler dwProto;
	
	public DWCmdDiskEject(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}

	public String getCommand() 
	{
		return "eject";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: dw disk eject requires a drive # as an argument"));
		}
		
		return(doDiskEject(cmdline));
		
	}

	
	private DWCommandResponse doDiskEject(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			dwProto.getDiskDrives().EjectDisk(driveno);
		
			return(new DWCommandResponse("Disk ejected from drive " + driveno + "."));
			
		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR, "dw disk eject requires a numeric disk # as an argument"));
					
		} 
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
			
		}
		
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getShortHelp() 
	{
		return "Eject disk from drive #";
	}

	public String getUsage() 
	{
		return "dw disk eject #";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
