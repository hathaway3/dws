package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;
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
		
		for (int i = 0;i<DWVSerialPorts.MAX_COCO_PORTS;i++)
		{
			text += String.format("%6s", DriveWireServer.getHandler(handlerno).getVPorts().prettyPort(i));
			
			if (DriveWireServer.getHandler(handlerno).getVPorts().isOpen(i))
			{
				text += String.format(" %-10s", "open(" + DriveWireServer.getHandler(handlerno).getVPorts().getOpen(i) + ")");
				
				text += String.format(" %-10s", "PD.INT=" + DriveWireServer.getHandler(handlerno).getVPorts().getPD_INT(i));
				text += String.format(" %-10s", "PD.QUT=" + DriveWireServer.getHandler(handlerno).getVPorts().getPD_QUT(i));
				text += String.format(" %-17s", "buffer: " + DriveWireServer.getHandler(handlerno).getVPorts().bytesWaiting(i));
				
			}
			else
			{
				text += String.format(" %-50s", "closed");
			}
			
			
			
			if (DriveWireServer.getHandler(handlerno).getVPorts().isConnected(i))
			{
				text += " " + DriveWireServer.getHandler(handlerno).getVPorts().getHostIP(i) + ":" + DriveWireServer.getHandler(handlerno).getVPorts().getHostPort(i);
			}
			else
			{
				text += " not connected";
			}
			
			//text += " " + DWProtocolHandler.byteArrayToHexString(DWVSerialPorts.getDD(i));	
			
			
			text += "\r\n";
		}
		
		return(new DWCommandResponse(text));
		
	}
	
}
