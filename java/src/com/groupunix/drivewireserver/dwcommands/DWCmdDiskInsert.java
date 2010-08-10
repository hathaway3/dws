package com.groupunix.drivewireserver.dwcommands;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskInsert implements DWCommand {

	private int handlerno;

	public DWCmdDiskInsert(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "insert";
	}


	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		if (args.length != 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk insert requires a drive number and a URI or local path as arguments"));
		}
		
		return(doDiskInsert(args[0],args[1]));
		
	}

	
	
	private DWCommandResponse doDiskInsert(String drivestr, String path) 
	{
		
		path = DWUtils.convertStarToBang(path);
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
		
			DriveWireServer.getHandler(handlerno).getDiskDrives().validateDriveNo(driveno);
			
			// eject any current disk
			if (DriveWireServer.getHandler(handlerno).getDiskDrives().diskLoaded(driveno))
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().EjectDisk(driveno);
			}
			
			// load new disk
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().LoadDiskFromFile(driveno, path);
			
			return(new DWCommandResponse("Disk loaded in drive " + driveno));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk insert requires a numeric disk number as the first argument"));
		}
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
				
		} 
		catch (FileSystemException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,e.getMessage()));
			
		} 
		catch (DWDriveAlreadyLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_ALREADY_LOADED,e.getMessage()));
		} 
		catch (FileNotFoundException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILE_NOT_FOUND,e.getMessage()));
		} 
		catch (IOException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
		}
		
		
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getShortHelp() 
	{
		return "Load disk into drive #";
	}


	public String getUsage() 
	{
		return "dw disk insert # URI/path";
	}
	
	
}
