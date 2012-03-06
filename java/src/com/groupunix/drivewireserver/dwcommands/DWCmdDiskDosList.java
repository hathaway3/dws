package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwdisk.DWDECBFileSystem;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskDosList extends DWCommand 
{
	private DWProtocolHandler dwProto;
	
	public DWCmdDiskDosList(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}

	public String getCommand() 
	{
		return "list";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		
		String[] args = cmdline.split(" ");
		
		if (args.length == 2)
		{
				try
				{
					return(doDiskDosList(dwProto.getDiskDrives().getDriveNoFromString(args[0]), args[1]));
				} 
				catch (DWDriveNotValidException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
				} 
				catch (DWDriveNotLoadedException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
				} 
				catch (DWDECBFileSystemFileNotFoundException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILE_NOT_FOUND,e.getMessage()));
				} 
				catch (DWDECBFileSystemInvalidFATException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,e.getMessage()));
				}
				catch (IOException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
				}
			
		}
	
		return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error"));
	}
		
		
	private DWCommandResponse doDiskDosList(int driveno, String filename) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDECBFileSystemFileNotFoundException, DWDECBFileSystemInvalidFATException, IOException
	{
		String res = "";
		
		DWDECBFileSystem tmp = new DWDECBFileSystem(dwProto.getDiskDrives().getDisk(driveno));
		
		res = tmp.getFileContents(filename);
		
		return(new DWCommandResponse(res));
		
	}




	public String getShortHelp() 
	{
		return "List contents of DOS file";
	}

	public String getUsage() 
	{
		return "dw disk dos list # filename";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
