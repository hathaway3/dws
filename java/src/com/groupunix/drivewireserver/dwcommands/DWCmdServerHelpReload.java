package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerHelpReload extends DWCommand {

	 
	public DWCmdServerHelpReload(DWProtocol dwProtocol,DWCommand parent)
	{
		setParentCmd(parent);
	}


	public String getCommand() 
	{
		return "reload";
	}

	
	public String getShortHelp() 
	{
		return "Reload help topics";
	}


	public String getUsage() 
	{
		return "dw help reload";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doHelpReload(cmdline));
	}

	
	

	private DWCommandResponse doHelpReload(String cmdline) 
	{
		DriveWireServer.getHelp().reload();
		
		return(new DWCommandResponse("Reloaded help topics."));
	}




	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
