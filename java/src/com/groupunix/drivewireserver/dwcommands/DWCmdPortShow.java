package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

public class DWCmdPortShow implements DWCommand {

	private int handlerno;

	public DWCmdPortShow(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "show";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show port status";
	}


	public String getUsage() 
	{
		return "dw port show";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doPortShow());
	}

	
	private DWCommandResponse doPortShow()
	{
		String text = new String();
		
		text += "\r\nCurrent port status:\r\n\n";
		
		for (int i = 0;i<DWVSerialPorts.MAX_PORTS;i++)
		{
			text += String.format("%6s", DriveWireServer.getHandler(handlerno).getVPorts().prettyPort(i));
			
			try 
			{
				
			
				if (DriveWireServer.getHandler(handlerno).getVPorts().isOpen(i))
				{
					text += String.format(" %-8s", "open(" + DriveWireServer.getHandler(handlerno).getVPorts().getOpen(i) + ")");
				
					text += String.format(" %-10s", "PD.INT=" + DriveWireServer.getHandler(handlerno).getVPorts().getPD_INT(i));
					text += String.format(" %-10s", "PD.QUT=" + DriveWireServer.getHandler(handlerno).getVPorts().getPD_QUT(i));
					text += String.format(" %-15s", "buffer: " + DriveWireServer.getHandler(handlerno).getVPorts().bytesWaiting(i));
				
				}
				else
				{
					text += String.format(" %-46s", "closed");
				}
			
			
				if (DriveWireServer.getHandler(handlerno).getVPorts().getUtilMode(i) != DWDefs.UTILMODE_UNSET)
					text += " " + DWUtils.prettyUtilMode(DriveWireServer.getHandler(handlerno).getVPorts().getUtilMode(i));
				
				//text += " " + DWProtocolHandler.byteArrayToHexString(DWVSerialPorts.getDD(i));	
			}
			catch (DWPortNotValidException e)
			{
				text += " Error: " + e.getMessage();
			} 

			
			text += "\r\n";
		}
		
		return(new DWCommandResponse(text));
		
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
