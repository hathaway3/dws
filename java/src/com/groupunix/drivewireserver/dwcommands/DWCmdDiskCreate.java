package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskCreate implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskCreate(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "create";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Create new disk image";
	}


	public String getUsage() 
	{
		return "dw disk create # URI/path";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		
		String[] args = cmdline.split(" ");
		
		if ((cmdline.length() == 0) || (args.length < 2))
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk create requires a drive # and URI/path as arguments"));
		}
				
		return(doDiskCreate(args[0],args[1]));
	}

	
	private DWCommandResponse doDiskCreate(String drivestr, String filepath)
	{
		FileSystemManager fsManager;
		FileObject fileobj;
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			// create file
			fsManager = VFS.getManager();
			fileobj = fsManager.resolveFile(filepath);
			
			if (fileobj.exists())
			{
				fileobj.close();
				throw new IOException("File already exists");
			}
		
			fileobj.createFile();
			
			if (dwProto.getDiskDrives().diskLoaded(driveno))
				dwProto.getDiskDrives().EjectDisk(driveno);
				
			dwProto.getDiskDrives().LoadDiskFromFile(driveno, filepath);
					
			return(new DWCommandResponse("Disk #" + driveno + " created."));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive #"));
				
		} 
		catch (IOException e1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e1.getMessage()));
				
		} 
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
		} 
		catch (DWDriveAlreadyLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_ALREADY_LOADED,e.getMessage()));
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
		}
		
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
	
}
