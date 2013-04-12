package com.groupunix.drivewireserver.dwcommands;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdServerTerminate extends DWCommand {

	
	public DWCmdServerTerminate(DWCommand parent)
	{
		setParentCmd(parent);
	}
	
	
	public String getCommand() 
	{
		return "terminate";
	}


	public String getShortHelp() 
	{
		return "Shut down server";
	}


	public String getUsage() 
	{
		return "dw server terminate [force]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.equals("force"))
			System.exit(1);
		
		DriveWireServer.shutdown();
		
		return(new DWCommandResponse("Server shutdown requested."));
	}



	public boolean validate(String cmdline) {
		return true;
	}
	
	
}
