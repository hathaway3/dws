package com.groupunix.drivewireserver.uicommands;

import java.util.ArrayList;
import java.util.Iterator;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowLog extends DWCommand {

	private DWUIClientThread dwuiref;

	public UICmdServerShowLog(DWUIClientThread dwuiClientThread) 
	{
		this.dwuiref = dwuiClientThread;
	}


	@Override
	public String getCommand() 
	{
		// TODO Auto-generated method stub
		return "log";
	}


	@Override
	public String getShortHelp() {
		// TODO Auto-generated method stub
		return "show log buffer";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "ui server show log";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String txt = new String();
		
		ArrayList<String> log = DriveWireServer.getLogEvents(DriveWireServer.getLogEventsSize());
		
		for (String l : log)
			txt += l;

		return(new DWCommandResponse(txt));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}