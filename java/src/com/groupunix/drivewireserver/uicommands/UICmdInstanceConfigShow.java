package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdInstanceConfigShow implements DWCommand {

	static final String command = "show";
	
	private DWUIClientThread uiref;

	public UICmdInstanceConfigShow(DWUIClientThread dwuiClientThread) 
	{

		this.uiref = dwuiClientThread;
	}

	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		String res = new String();
		
		int instance = this.uiref.getInstance();
		
		if (cmdline.length() == 0)
		{
			for (Iterator i = DriveWireServer.getHandler(instance).config.getKeys(); i.hasNext();)
			{
				String key = (String) i.next();
				String value = StringUtils.join(DriveWireServer.getHandler(instance).config.getStringArray(key), ", ");
		
				res += key + " = " + value + "\r\n";
		            
			}
		}
		else
		{
			if (DriveWireServer.getHandler(instance).config.containsKey(cmdline))
			{
				String value = StringUtils.join(DriveWireServer.getHandler(instance).config.getStringArray(cmdline), ", ");
				return(new DWCommandResponse(value));
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_CONFIG_KEY_NOT_SET, "Key '" + cmdline + "' is not set."));
			}
		}
		
		return(new DWCommandResponse(res));
	}


	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}


	public String getShortHelp() 
	{
		return "Show instance configuration";
	}


	public String getUsage() 
	{
		return "ui instance config show [item]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}