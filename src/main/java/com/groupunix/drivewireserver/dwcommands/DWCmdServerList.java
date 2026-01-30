package com.groupunix.drivewireserver.dwcommands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdServerList extends DWCommand {

	public DWCmdServerList(final DWCommand parent)
	{
		setParentCmd(parent);
	}

	
	public String getCommand() 
	{
		return "list";
	}

	
	public String getShortHelp() 
	{
		return "List contents of file on server";
	}


	public String getUsage() 
	{
		return "dw server list URI/path";
	}

	public DWCommandResponse parse(final String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw server list requires a URI or local file path as an argument"));
		}
		return(doList(cmdline));
	}

	
	
	private DWCommandResponse doList(String path) 
	{
		FileSystemManager fsManager;
		InputStream ins = null;
		FileObject fileobj = null;
		FileContent fc = null;
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try
		{
			fsManager = VFS.getManager();
		
			path = DWUtils.convertStarToBang(path);
			
			fileobj = fsManager.resolveFile(path);
		
			fc = fileobj.getContent();
			
			ins = fc.getInputStream();

			final byte[] buffer = new byte[256];
			int sz = 0;
			
			while ((sz = ins.read(buffer)) >= 0)
			{
				baos.write(buffer,0,sz);
			}
			
			ins.close();
		}	
		catch (final FileSystemException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_FILESYSTEM_EXCEPTION,e.getMessage()));
		} 
		catch (final IOException e)
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
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return(new DWCommandResponse(baos.toByteArray()));
	}

	public boolean validate(final String cmdline) {
		return true;
	}

}
