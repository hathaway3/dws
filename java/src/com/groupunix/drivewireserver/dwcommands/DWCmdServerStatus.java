package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdServerStatus implements DWCommand {

	private int handlerno;
	
	public DWCmdServerStatus(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "status";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
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
	    
	    if (DriveWireServer.getHandler(this.handlerno).config.getString("DeviceType","serial").equalsIgnoreCase("serial"))
	    {
			text += "\r\n\nDevice:        " + DriveWireServer.getHandler(this.handlerno).config.getString("SerialDevice","unknown");
			text += " (" + DriveWireServer.getHandler(this.handlerno).getProtoDev().getRate() + " bps)\r\n";
			text += "CoCo Type:     " + DriveWireServer.getHandler(this.handlerno).config.getInt("CocoModel", 0) + "\r\n";
	    }
	    else
	    {
	    	text += "\r\n\nDevice:        TCP, listening on port " + DriveWireServer.getHandler(this.handlerno).config.getString("TCPDevicePort","unknown");
			text += "\r\n";
	    }
		text += "\r\n";
	
		text += "Last OpCode:   " + DWUtils.prettyOP(DriveWireServer.getHandler(handlerno).getLastOpcode()) + "\r\n";
		text += "Last GetStat:  " + DWUtils.prettySS(DriveWireServer.getHandler(handlerno).getLastGetStat()) + "\r\n";
		text += "Last SetStat:  " + DWUtils.prettySS(DriveWireServer.getHandler(handlerno).getLastSetStat()) + "\r\n";
		text += "Last Drive:    " + DriveWireServer.getHandler(handlerno).getLastDrive() + "\r\n";
		text += "Last LSN:      " + DWUtils.int3(DriveWireServer.getHandler(handlerno).getLastLSN()) + "\r\n";
		text += "Last Error:    " + (int) (DriveWireServer.getHandler(handlerno).getLastError() & 0xFF) + "\r\n";
	
		text += "\r\n";
	
		// TODO:  include read and write retries per disk
		// text += "Total Read Sectors:  " + String.format("%6d",DriveWireServer.getHandler(handlerno).getSectorsRead()) + "  (" + DriveWireServer.getHandler(handlerno).getReadRetries() + " retries)\r\n";
		//text += "Total Write Sectors: " + String.format("%6d",DriveWireServer.getHandler(handlerno).getSectorsWritten()) + "  (" + DriveWireServer.getHandler(handlerno).getWriteRetries() + " retries)\r\n";
	
		text += "\r\n";
	
		text += "D#     Sectors        LSN   WP       Reads  Writes  Dirty \r\n";
	
		for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
		{
			if (DriveWireServer.getHandler(handlerno).getDiskDrives().diskLoaded(i))
			{
			 
				text += "X"+ String.format("%-3d ",i);
				text += String.format("%9d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getDiskSectors(i));
				text += String.format(" %9d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getLSN(i));
				text += String.format("  %5s  ", DriveWireServer.getHandler(handlerno).getDiskDrives().getWriteProtect(i));
				text += String.format(" %6d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getReads(i));
				text += String.format(" %6d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getWrites(i));
				text += String.format(" %5d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getDirtySectors(i));
				text += "\r\n";
			}
		}
		
		return(new DWCommandResponse(text));
		
	}
	

}
