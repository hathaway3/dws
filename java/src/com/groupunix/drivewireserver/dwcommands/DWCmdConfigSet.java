package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdConfigSet implements DWCommand {

	private int handlerno;

	public DWCmdConfigSet(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "set";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Set config item, omit value to remove item";
	}


	public String getUsage() 
	{
		return "dw config set item [value]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR, "Syntax error: dw config set requires an item and value as arguments"));
		}
		
		String[] args = cmdline.split(" ");
		
		if (args.length == 1)
		{
			return(doSetConfig(args[0]));
		}
		else
		{
			return(doSetConfig(args[0],args[1]));
		}
		
	}

	private DWCommandResponse doSetConfig(String item)
	{
		
		if (DriveWireServer.getHandler(this.handlerno).config.containsKey(item))
		{
			DriveWireServer.getHandler(this.handlerno).config.clearProperty(item);
			return(new DWCommandResponse("Item '" + item + "' removed from config"));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_CONFIG_KEY_NOT_SET, "Key '" + item + "' is not set."));
		}
		
		
	}
	
	
	private DWCommandResponse doSetConfig(String item, String value)
	{
		DriveWireServer.getHandler(this.handlerno).config.setProperty(item, value);
		return(new DWCommandResponse("Item '" + item + "' set to '" + value + "'"));
	}

	
	public boolean validate(String cmdline) 
	{

		return true;
	}
	
	
}
