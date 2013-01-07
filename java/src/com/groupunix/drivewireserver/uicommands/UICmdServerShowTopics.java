package com.groupunix.drivewireserver.uicommands;

import java.util.ArrayList;
import java.util.Iterator;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdServerShowTopics extends DWCommand {


	private DWProtocolHandler dwProto;

	public UICmdServerShowTopics(DWUIClientThread dwuiClientThread) 
	{
		this.dwProto = (DWProtocolHandler) DriveWireServer.getHandler(dwuiClientThread.getInstance());
	}


	public UICmdServerShowTopics(DWProtocolHandler dwProto) 
	{
		this.dwProto = dwProto;
	}


	@Override
	public String getCommand() 
	{
		// TODO Auto-generated method stub
		return "topics";
	}


	@Override
	public String getShortHelp() {
		// TODO Auto-generated method stub
		return "show available help topics";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "ui server show topics";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String txt = new String();
		
		ArrayList<String> tops = dwProto.getHelp().getTopics(null);
		
		Iterator<String> t = tops.iterator();
		while (t.hasNext())
        {
			txt += t.next() + "\n";
        }
		
		return(new DWCommandResponse(txt));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
