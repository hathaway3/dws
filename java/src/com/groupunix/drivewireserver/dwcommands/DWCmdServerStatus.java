package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWCmdServerStatus implements DWCommand {

	private DWProtocol dwProto;
	
	public DWCmdServerStatus(DWProtocol dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "status";
	}

	public String getLongHelp() 
	{

		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show server status information";
	}


	public String getUsage() 
	{
		return "dw server status";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doServerStatus());
	}
	
	private DWCommandResponse doServerStatus()
	{
		String text = new String();
		
		text += "DriveWire version " + DriveWireServer.DWServerVersion + " (" + DriveWireServer.DWServerVersionDate + ") status:\r\n\n";
		
		text += "Total memory:  " + Runtime.getRuntime().totalMemory() / 1024 + " KB";
	    text += "\r\nFree memory:   " + Runtime.getRuntime().freeMemory() / 1024 + " KB";
	    
	    if (dwProto.getConfig().getString("DeviceType","serial").equalsIgnoreCase("serial"))
	    {
	    	if (dwProto.getProtoDev() != null)
	    	{
	    		text += "\r\n\nDevice:        " + dwProto.getConfig().getString("SerialDevice","unknown");
	    		text += " (" + dwProto.getProtoDev().getRate() + " bps)\r\n";
	    		text += "CoCo Type:     " + dwProto.getConfig().getInt("CocoModel", 0) + "\r\n";
	    	}
	    	else
	    	{
	    		text += "\r\n\nDevice:        Serial, not started\r\n";
	    	}
	    }
	    else
	    {
	    	text += "\r\n\nDevice:        TCP, listening on port " + dwProto.getConfig().getString("TCPDevicePort","unknown");
			text += "\r\n";
	    }
		text += "\r\n";
	
		text += dwProto.getStatusText();
		
		return(new DWCommandResponse(text));
		
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}

}
