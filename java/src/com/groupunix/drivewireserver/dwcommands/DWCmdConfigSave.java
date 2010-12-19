package com.groupunix.drivewireserver.dwcommands;

import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdConfigSave implements DWCommand {

	private int handlerno;

	public DWCmdConfigSave(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "save";
	}

	public String getLongHelp() 
	{

		return null;
	}

	
	public String getShortHelp() 
	{
		return "Save configuration)";
	}


	public String getUsage() 
	{
		return "dw config save";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		
		try 
		{
			DriveWireServer.saveServerConfig();
		} 
		catch (ConfigurationException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
		}
		
		return(new DWCommandResponse("Configuration saved."));
	}

	
	
	public boolean validate(String cmdline)
	{

		return true;
	}
	
	
	

}
