package com.groupunix.drivewireserver.dwcommands;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetInvalidDiskDefException;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskInsert extends DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskInsert(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "insert";
	}


	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		if (args.length < 1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk insert requires at least 1 arguments"));
		}
		
		if (this.dwProto.getDiskDrives().isDiskNo(args[0]))
		{
			// regular disk -> drive
			return(doDiskInsert(Integer.parseInt(args[0]), DWUtils.dropFirstToken(cmdline)));
		}
		else if (this.dwProto.getDiskDrives().isDiskSetName(args[0]))
		{
			if (args.length == 1)
			{
				// load disk set
				return(doDiskInsert(args[0]));
			}
			else if ((args.length > 2) && this.dwProto.getDiskDrives().isDiskNo(args[1]))
			{
				// add disk def to set
				return(doDiskInsert(args[0], Integer.parseInt(args[1]), DWUtils.dropFirstToken(DWUtils.dropFirstToken(cmdline))));
			}
		}
		
		
		return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"syntax error"));
		
	}

	
	private DWCommandResponse doDiskInsert(String set)
	{
		try 
		{
			dwProto.getDiskDrives().LoadDiskSet(set);
		} 
		catch (DWDisksetInvalidDiskDefException e) 
		{
			return(new DWCommandResponse(false, DWDefs.RC_INVALID_DISK_DEF, e.getMessage()));
		} 
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false, DWDefs.RC_NO_SUCH_DISKSET, e.getMessage()));
		} 
		catch (IOException e) 
		{
			return(new DWCommandResponse(false, DWDefs.RC_SERVER_IO_EXCEPTION, e.getMessage()));
		} 
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false, DWDefs.RC_INVALID_DRIVE, e.getMessage()));
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false, DWDefs.RC_DRIVE_NOT_LOADED, e.getMessage()));
		} 
		catch (DWDriveAlreadyLoadedException e) 
		{
			return(new DWCommandResponse(false, DWDefs.RC_DRIVE_ALREADY_LOADED, e.getMessage()));
		}
		
		return(new DWCommandResponse("Loaded disk set '" + set +"'."));
	}

	private DWCommandResponse doDiskInsert(String set, int driveno, String path) 
	{
		
		path = DWUtils.convertStarToBang(path);
		
		try
		{
			// load new disk
			
			try 
			{
				dwProto.getDiskDrives().clearDisksetDisk(set, driveno);
			} 
			catch (DWDisksetDriveNotLoadedException e) 
			{
				// dont care
			}
			
			dwProto.getDiskDrives().addDisksetDisk(set, driveno, path);
			
			return(new DWCommandResponse("Disk inserted into set '" + set + "' drive " + driveno + "."));

		}
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		}
		
		
	}
	
	
	
	
	private DWCommandResponse doDiskInsert(int driveno, String path) 
	{
		
		path = DWUtils.convertStarToBang(path);
		
		try
		{
		
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
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		}
		
		
	}



	public String getShortHelp() 
	{
		return "Load disk into drive #";
	}


	public String getUsage() 
	{
		return "dw disk insert [dset] # path";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
