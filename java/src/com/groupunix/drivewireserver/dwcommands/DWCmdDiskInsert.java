package com.groupunix.drivewireserver.dwcommands;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskInsert implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskInsert(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "insert";
	}


	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		if (args.length < 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk insert requires a drive number and a URI or local path as arguments"));
		}
		
		return(doDiskInsert(args[0], cmdline.substring(args[0].length()+1)));
		
	}

	
	
	private DWCommandResponse doDiskInsert(String drivestr, String path) 
	{
		
		path = DWUtils.convertStarToBang(path);
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
		
			dwProto.getDiskDrives().validateDriveNo(driveno);
			
			// eject any current disk
			if (dwProto.getDiskDrives().diskLoaded(driveno))
			{
				dwProto.getDiskDrives().EjectDisk(driveno);
			}
			
			// load new disk
			
			dwProto.getDiskDrives().LoadDiskFromFile(driveno, path);
			
			return(new DWCommandResponse("Disk inserted in drive " + driveno + "."));

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
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
