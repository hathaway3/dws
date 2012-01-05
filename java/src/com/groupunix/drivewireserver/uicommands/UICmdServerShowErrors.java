package com.groupunix.drivewireserver.uicommands;

import java.util.List;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;

public class UICmdServerShowErrors extends DWCommand {

	private DWUIClientThread dwuiref;

	public UICmdServerShowErrors(DWUIClientThread dwuiClientThread) 
	{
		this.dwuiref = dwuiClientThread;
	}


	@Override
	public String getCommand() 
	{
		// TODO Auto-generated method stub
		return "errors";
	}


	@Override
	public String getShortHelp() {
		// TODO Auto-generated method stub
		return "show error descriptions";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "ui server show errors";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String res = "";
		
		List<String> rconfs = DriveWireServer.getHandler(this.dwuiref.getInstance()).getHelp().getSectionTopics("resultcode"); 
		
		if (rconfs != null)
		{
			for (String rc : rconfs)
			{
				try
				{
					res += rc.substring(1) + "|" + DriveWireServer.getHandler(this.dwuiref.getInstance()).getHelp().getTopicText("resultcode." + rc).trim() + "\r\n";
				} 
				catch (DWHelpTopicNotFoundException e)
				{
					// whatever 
				}
						
			}
			
			return(new DWCommandResponse(res));
		}
		
		return(new DWCommandResponse(false, DWDefs.RC_HELP_TOPIC_NOT_FOUND , "No error descriptions available from server"));
		
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
