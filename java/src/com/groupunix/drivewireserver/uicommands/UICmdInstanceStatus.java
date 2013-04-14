package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
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
		int hno = 0;
		
		if (cmdline.length() > 0)
		{
			try
			{
				hno = Integer.parseInt(cmdline);
				if (DriveWireServer.isValidHandlerNo(hno))
					dwProto = (DWProtocolHandler) DriveWireServer.getHandler(hno);
				else
					return(new DWCommandResponse(false,DWDefs.RC_INVALID_HANDLER,"Invalid handler number"));
			}
			catch (NumberFormatException ne)
			{
				return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric instance #"));
			}
		}
		else
		{
			if (this.clientref != null)
			{
				dwProto = (DWProtocolHandler) DriveWireServer.getHandler(clientref.getInstance());
				hno = this.clientref.getInstance();
			}
		}
		
		txt = "num|" + hno + "\n";
		txt += "name|" + dwProto.getConfig().getString("[@name]","not set") + "\n";
		txt += "desc|" + dwProto.getConfig().getString("[@desc]","not set") + "\n";
		
		txt += "autostart|" + dwProto.getConfig().getBoolean("AutoStart", true) + "\n";
		txt += "dying|" + dwProto.isDying() + "\n";
		txt += "started|" + dwProto.isStarted() + "\n";
		txt += "ready|" + dwProto.isReady() + "\n";
		txt += "connected|" + dwProto.connected() + "\n";
		
		if (dwProto.getProtoDev() != null)
		{
			txt += "devicetype|" + dwProto.getProtoDev().getDeviceType() + "\n";
			
			txt += "devicename|" + dwProto.getProtoDev().getDeviceName() + "\n";
			txt += "deviceconnected|" + dwProto.getProtoDev().connected() + "\n";
			
			if (dwProto.getProtoDev().getRate() > -1)
				txt += "devicerate|" + dwProto.getProtoDev().getRate() + "\n";
			
			if (dwProto.getProtoDev().getClient() != null)
				txt += "deviceclient|" + dwProto.getProtoDev().getClient() + "\n";
			
		}
		
		txt += "lastopcode|" + DWUtils.prettyOP(dwProto.getLastOpcode()) + "\n";
		txt += "lastgetstat|" + DWUtils.prettySS(dwProto.getLastGetStat()) + "\n";
		txt += "lastsetstat|" + DWUtils.prettySS(dwProto.getLastSetStat()) + "\n";
		txt += "lastlsn|" + DWUtils.int3(dwProto.getLastLSN()) + "\n";
		txt += "lastdrive|" + dwProto.getLastDrive() +"\n";
		txt += "lasterror|" + dwProto.getLastError() + "\n";
		txt += "lastchecksum|" + dwProto.getLastChecksum() + "\n";
		
		
		
		return(new DWCommandResponse(txt));
	
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
