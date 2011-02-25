package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdInstanceStatus implements DWCommand {

	private DWUIClientThread clientref;
	
	public UICmdInstanceStatus(DWUIClientThread dwuiClientThread) 
	{
		clientref = dwuiClientThread;
	}

	@Override
	public String getCommand() 
	{
		return "status";
	}

	@Override
	public String getLongHelp() {
		return null;
	}

	@Override
	public String getShortHelp() {
		return "show instance status";
	}

	@Override
	public String getUsage() {
		return "ui instance status";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String txt = "";
		
		DWProtocolHandler ph = (DWProtocolHandler) DriveWireServer.getHandler(clientref.getInstance());   
		
		txt = "name: " + ph.getConfig().getString("Name","not set") + "\n";
		
		txt += "connected: " + ph.connected() + "\n";
		
		txt += "devicetype: " + ph.getProtoDev().getDeviceType() + "\n";
		txt += "devicerate: " + ph.getProtoDev().getRate() + "\n";
		txt += "devicename: " + ph.getProtoDev().getDeviceName() + "\n";
		txt += "deviceconnected: " + ph.getProtoDev().connected() + "\n";
		
		txt += "lastopcode: " + DWUtils.prettyOP(ph.getLastOpcode()) + "\n";
		txt += "lastgetstat: " + DWUtils.prettySS(ph.getLastGetStat()) + "\n";
		txt += "lastsetstat: " + DWUtils.prettySS(ph.getLastSetStat()) + "\n";
		txt += "lastlsn: " + DWUtils.int3(ph.getLastLSN()) + "\n";
		txt += "lastdrive: " + ph.getLastDrive() +"\n";
		txt += "lasterror: " + ph.getLastError() + "\n";
		txt += "lastchecksum: " + ph.getLastChecksum() + "\n";
		
		
		
		return(new DWCommandResponse(txt));
	
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
