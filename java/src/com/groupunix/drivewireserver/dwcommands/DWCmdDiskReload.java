package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;

public class DWCmdDiskReload implements DWCommand {

	private int handlerno;

	public DWCmdDiskReload(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "reload";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Reload disk in drive #";
	}


	public String getUsage() 
	{
		return "dw disk reload #";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk reload requires a drive # as an argument"));
		}
		return(doDiskReload(cmdline));
	}

	
	private DWCommandResponse doDiskReload(String drivestr)
	{

		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().ReLoadDisk(driveno);
	
			return(new DWCommandResponse("Disk in drive #"+ driveno + " reloaded."));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive #"));
		} 
		catch (IOException e1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e1.getMessage()));
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
		} 
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
		} 
		catch (DWDriveAlreadyLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_ALREADY_LOADED,e.getMessage()));
		}
		
	}
	
}
