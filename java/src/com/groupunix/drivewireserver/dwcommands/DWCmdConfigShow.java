package com.groupunix.drivewireserver.dwcommands;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdConfigShow implements DWCommand {

	private int handlerno;

	public DWCmdConfigShow(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "show";
	}

	public String getLongHelp() 
	{

		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show current instance config (or item)";
	}


	public String getUsage() 
	{
		return "dw config show [item]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() > 0)
		{
			return(doShowConfig(cmdline));
		}
		return(doShowConfig());
	}

	private DWCommandResponse doShowConfig(String item)
	{
		String text = new String();
		
		if (DriveWireServer.getHandler(this.handlerno).config.containsKey(item))
		{
			String key = item;
			String value = StringUtils.join(DriveWireServer.getHandler(this.handlerno).config.getStringArray(key), ", ");
		
			text += key + " = " + value;
			return(new DWCommandResponse(text));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_CONFIG_KEY_NOT_SET, "Key '" + item + "' is not set."));
		}
		
		
	}
	
	
	public boolean validate(String cmdline)
	{

		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	private DWCommandResponse doShowConfig()
	{
		String text = new String();
		
		text += "Current protocol handler configuration:\r\n\n";
		
		for (Iterator<String> i = DriveWireServer.getHandler(this.handlerno).config.getKeys(); i.hasNext();)
		{
			String key = i.next();
			String value = StringUtils.join(DriveWireServer.getHandler(this.handlerno).config.getStringArray(key), ", ");
		
			text += key + " = " + value + "\r\n";
		            
		}
		
		return(new DWCommandResponse(text));
	}
	

}
