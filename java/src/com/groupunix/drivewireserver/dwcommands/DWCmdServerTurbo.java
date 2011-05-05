package com.groupunix.drivewireserver.dwcommands;

import gnu.io.UnsupportedCommOperationException;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWSerialDevice;

public class DWCmdServerTurbo implements DWCommand {

	private DWProtocol dwProto;
	
	public DWCmdServerTurbo(DWProtocol dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "turbo";
	}

	public String getLongHelp() 
	{

		return null;
	}

	
	public String getShortHelp() 
	{
		return "Turn on DATurbo mode (testing only)";
	}


	public String getUsage() 
	{
		return "dw server turbo";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doServerTurbo());
	}
	
	private DWCommandResponse doServerTurbo()
	{
		String text = new String();
		
		
		DWSerialDevice serdev = (DWSerialDevice) this.dwProto.getProtoDev();
		
		try 
		{
			serdev.enableDATurbo();
			text = "Device is now in DATurbo mode";
		} 
		catch (UnsupportedCommOperationException e) 
		{
			text = "Failed to enable DATurbo mode: " + e.getMessage();
		}
		

		
		return(new DWCommandResponse(text));
		
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}

}
