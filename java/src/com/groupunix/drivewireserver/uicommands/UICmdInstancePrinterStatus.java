package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstancePrinterStatus extends DWCommand {

	private DWUIClientThread dwuithread;

	public UICmdInstancePrinterStatus(DWUIClientThread dwuiClientThread) 
	{
		this.dwuithread = dwuiClientThread;
	}


	@Override
	public String getCommand() 
	{
		return "printerstatus";
	}


	@Override
	public String getShortHelp() {
		return "show printer status";
	}

	@Override
	public String getUsage() {
		return "ui instance printerstatus";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String res = "";
		
		if (this.dwuithread.getInstance() > -1)
		{
			DWProtocolHandler dwProto = (DWProtocolHandler)DriveWireServer.getHandler(this.dwuithread.getInstance());
			
			
			
			if (!(dwProto == null))
			{
				res = "currentprinter|" + DriveWireServer.getHandler(this.dwuithread.getInstance()).getConfig().getString("CurrentPrinter","none") + "\r\n";
					
				@SuppressWarnings("unchecked")
				List<HierarchicalConfiguration> profiles =  DriveWireServer.getHandler(this.dwuithread.getInstance()).getConfig().configurationsAt("Printer");
		    	
				for(Iterator<HierarchicalConfiguration> it = profiles.iterator(); it.hasNext();)
				{
				    HierarchicalConfiguration mprof = it.next();
				    
				    res += "printer|" + mprof.getString("[@name]") +"|" + mprof.getString("[@desc]") + "\r\n";
				}
			
			}
		}
		
		return(new DWCommandResponse(res));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
