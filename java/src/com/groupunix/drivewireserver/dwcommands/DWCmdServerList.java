package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdServerList implements DWCommand {


	
	public String getCommand() 
	{
		return "list";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "List contents of file on server";
	}


	public String getUsage() 
	{
		return "dw server list URI/path";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw server list requires a URI or local file path as an argument"));
		}
		return(doList(cmdline));
	}

	
	
	private DWCommandResponse doList(String path) 
	{
		String text = new String();
		
		FileSystemManager fsManager;
		InputStream ins = null;
		FileObject fileobj = null;
		FileContent fc = null;
		
		try
		{
			fsManager = VFS.getManager();
		
			path = DWUtils.convertStarToBang(path);
			
			fileobj = fsManager.resolveFile(path);
		
			fc = fileobj.getContent();
			
			ins = fc.getInputStream();

			byte[] buffer = new byte[256];
			
			int data = ins.read(buffer);
			
			while (data > 0)
			{
				text += new String(buffer).substring(0,data);
				data = ins.read(buffer);
						
			}
			
		} 
		catch (FileSystemException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,e.getMessage()));
		} 
		catch (IOException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
	    }	
		finally
		{
			try
			{
				if (ins != null)
					ins.close();
				
				if (fc != null)
					fc.close();
				
				if (fileobj != null)
					fileobj.close();
				
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		return(new DWCommandResponse(text));
	}

	public boolean validate(String cmdline) {
		return true;
	}
	
	
	

}
