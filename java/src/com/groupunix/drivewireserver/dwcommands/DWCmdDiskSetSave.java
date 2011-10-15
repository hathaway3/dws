package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskSetSave implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskSetSave(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
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
		return "Save diskset disk details";
	}


	public String getUsage() 
	{
		return "dw disk set save [setname]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			if (this.dwProto.getConfig().containsKey("CurrentDiskSet"))
			{
				return(doDiskSetSave(this.dwProto.getConfig().getString("CurrentDiskSet")));
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk set save requires a set name as an argument if no current disk set."));
			}
		}
		return(doDiskSetSave(cmdline));
	}

	
	private DWCommandResponse doDiskSetSave(String setname)
	{
		dwProto.getDiskDrives().SaveDiskSet(setname);
		return(new DWCommandResponse("Saved disk set '" + setname + "'."));
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
