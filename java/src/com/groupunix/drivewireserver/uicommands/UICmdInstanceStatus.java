package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class UICmdInstanceStatus extends DWCommand {

	private DWUIClientThread clientref = null;
	private DWProtocolHandler dwProto = null;
	
	public UICmdInstanceStatus(DWUIClientThread dwuiClientThread) 
	{
		clientref = dwuiClientThread;
	}

	public UICmdInstanceStatus(DWProtocolHandler dwProto) 
	{
		this.dwProto = dwProto;
	}

	@Override
	public String getCommand() 
	{
		return "status";
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
		
		if (this.clientref != null)
			dwProto = (DWProtocolHandler) DriveWireServer.getHandler(clientref.getInstance());   
		
		
		txt = "name: " + dwProto.getConfig().getString("Name","not set") + "\n";
		
		txt += "connected: " + dwProto.connected() + "\n";
		
		txt += "devicetype: " + dwProto.getProtoDev().getDeviceType() + "\n";
		txt += "devicerate: " + dwProto.getProtoDev().getRate() + "\n";
		txt += "devicename: " + dwProto.getProtoDev().getDeviceName() + "\n";
		txt += "deviceconnected: " + dwProto.getProtoDev().connected() + "\n";
		
		txt += "lastopcode: " + DWUtils.prettyOP(dwProto.getLastOpcode()) + "\n";
		txt += "lastgetstat: " + DWUtils.prettySS(dwProto.getLastGetStat()) + "\n";
		txt += "lastsetstat: " + DWUtils.prettySS(dwProto.getLastSetStat()) + "\n";
		txt += "lastlsn: " + DWUtils.int3(dwProto.getLastLSN()) + "\n";
		txt += "lastdrive: " + dwProto.getLastDrive() +"\n";
		txt += "lasterror: " + dwProto.getLastError() + "\n";
		txt += "lastchecksum: " + dwProto.getLastChecksum() + "\n";
		
		
		
		return(new DWCommandResponse(txt));
	
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
