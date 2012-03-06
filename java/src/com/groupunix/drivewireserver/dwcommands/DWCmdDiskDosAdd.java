package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwdisk.DWDECBFileSystem;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemBadFilenameException;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemFullException;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskDosAdd extends DWCommand 
{
	private DWProtocolHandler dwProto;
	
	public DWCmdDiskDosAdd(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}

	public String getCommand() 
	{
		return "add";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		
		String[] args = cmdline.split(" ");
		
		if (args.length == 2)
		{
				try
				{
					return(doDiskDosAdd(dwProto.getDiskDrives().getDriveNoFromString(args[0]), args[1] ));
				} 
				// so much can go wrong with this...
				catch (DWDriveNotValidException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
				} 
				catch (DWDriveNotLoadedException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
				} 
				catch (DWDECBFileSystemFullException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,e.getMessage()));
				} 
				catch (DWDECBFileSystemBadFilenameException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,e.getMessage()));
				} 
				catch (IOException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
				} 
				catch (DWDECBFileSystemFileNotFoundException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILE_NOT_FOUND,e.getMessage()));
				} 
				catch (DWDECBFileSystemInvalidFATException e)
				{
					return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,e.getMessage()));
				} 
				
				
		}
	
		return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error"));
	}
		
		
	private DWCommandResponse doDiskDosAdd(int driveno, String path) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDECBFileSystemFullException, DWDECBFileSystemBadFilenameException, IOException, DWDECBFileSystemFileNotFoundException, DWDECBFileSystemInvalidFATException
	{
		DWDECBFileSystem decbfs = new DWDECBFileSystem(dwProto.getDiskDrives().getDisk(driveno));
		
		FileObject fileobj = VFS.getManager().resolveFile(path);
		
		if (fileobj.exists() && fileobj.isReadable())
		{
			
			FileContent fc = fileobj.getContent();
			long fobjsize = fc.getSize();
			
			// size check
			if (fobjsize > Integer.MAX_VALUE)
				throw new DWDECBFileSystemFullException("File too big, maximum size is " + Integer.MAX_VALUE + " bytes.");
			
			// get header
			
			byte[] content = new byte[(int) fobjsize];
			
			if (content.length > 0)
			{
				int readres = 0;
				InputStream fis = fc.getInputStream();
				
				while (readres < content.length)
			    	readres += fis.read(content, readres, content.length - readres);
				
				fis.close();
			}
			
			decbfs.addFile(fileobj.getName().getBaseName().toUpperCase(), content);
		}
		else
		{
			throw (new IOException("Unreadable source path"));
		}
		
		return(new DWCommandResponse("File added to DOS disk."));
	}




	public String getShortHelp() 
	{
		return "Add file to disk image with DOS filesystem";
	}

	public String getUsage() 
	{
		return "dw disk dos add # path";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
