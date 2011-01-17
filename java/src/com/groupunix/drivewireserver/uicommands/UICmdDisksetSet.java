package com.groupunix.drivewireserver.uicommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdDisksetSet implements DWCommand {

	
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
		return "Set diskset details";
	}


	public String getUsage() 
	{
		return "ui diskset set [set] [item] [value]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		
		if (args.length < 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"ui diskset set requires additional parameters"));
		}
		else if (args.length == 2)
		{
			return(doDiskSetClear(args[0],args[1]));
		}
		else
		{
			return(doDiskSetSet(cmdline));	
		}
	}


	private DWCommandResponse doDiskSetSet(String cmdline) 
	{
		Pattern p_item = Pattern.compile("^(.+?)\\s+(.+?)\\s+(.+)$");
		Matcher m = p_item.matcher(cmdline);
	  
		if (m.find())
		{
			if (DriveWireServer.hasDiskset(m.group(1)))
			{
				synchronized(DriveWireServer.serverconfig)
				{
					DriveWireServer.getDiskset(m.group(1)).setProperty(m.group(2), m.group(3));
				}
				
				return(new DWCommandResponse("Set item '" + m.group(2) + "' in diskset '" + m.group(1) + "'."));
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,"There is no diskset called '" + m.group(1) + "'"));
			}			
			
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error in ui diskset set command"));
		}
	}

	private DWCommandResponse doDiskSetClear(String setname, String item) 
	{
		if (DriveWireServer.hasDiskset(setname))
		{
			HierarchicalConfiguration diskset = DriveWireServer.getDiskset(setname);
			
			if (diskset.containsKey(item))
			{
				synchronized(DriveWireServer.serverconfig)
				{
					diskset.clearProperty(item);
				}
			}
			else
			{
				return(new DWCommandResponse("Item '"+item+"' is not set in diskset '"+setname+"'"));
			}
			
			return(new DWCommandResponse("Cleared item '" + item + "' from diskset '" + setname + "'."));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,"There is no diskset called '" + setname + "'"));
		}
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
