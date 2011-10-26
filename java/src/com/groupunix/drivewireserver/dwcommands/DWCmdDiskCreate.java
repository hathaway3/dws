package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskCreate extends DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskCreate(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "create";
	}


	
	public String getShortHelp() 
	{
		return "Create new disk image or set";
	}


	public String getUsage() 
	{
		return "dw disk create {# path | dset}";
	}

	public DWCommandResponse parse(String cmdline)  
	{
		if ((cmdline.length() == 0))
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk create requires at least 1 argument."));
		}

		String[] args = cmdline.split(" ");

		if (dwProto.getDiskDrives().isDiskNo(args[0]))
		{
			if (args.length < 2)
			{
				return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk create # requires a path."));
			}
			else
			{
				return(doDiskCreate(Integer.parseInt(args[0]), DWUtils.dropFirstToken(cmdline)));
			}
		}
		
		
		return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error"));
	}

	
	private DWCommandResponse doDiskCreate(int driveno, String filepath)
	{
		FileSystemManager fsManager;
		FileObject fileobj;
		
		try
		{
			
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
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET, e.getMessage()));
		}
		
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
	
}
