package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdInstanceDiskShow implements DWCommand {

	static final String command = "show";
	
	private DWUIClientThread uiref;

	public UICmdInstanceDiskShow(DWUIClientThread dwuiClientThread) 
	{

		this.uiref = dwuiClientThread;
	}

	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		String res = new String();
		
		int instance = this.uiref.getInstance();
		
		if (cmdline.length() == 0)
		{
			for (int i =0;i<256;i++)
			{
				if (DriveWireServer.getHandler(instance).getDiskDrives().diskLoaded(i))
				{
					res += i + " " + DriveWireServer.getHandler(instance).getDiskDrives().getDiskFile(i) + "\n";
				}
			}
		}
		else
		{
			// disk details
			try
			{
			
				int driveno = Integer.parseInt(cmdline);
				
				if (DriveWireServer.getHandler(instance).getDiskDrives().diskLoaded(driveno))
				{
					res += "loaded: true\n"; 
					res += "path: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getFilePath() + "\n";
					res += "sizelimit: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getSizelimit() + "\n";
					res += "offset: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getOffset() + "\n";
					res += "sync: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).isSync() + "\n";
					res += "expand: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).isExpand() + "\n";
					res += "writeprotect: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getWriteProtect() + "\n";
					
					res += "fswriteable: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).isFSWriteable() + "\n";
					res += "writeable: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).isWriteable() + "\n";
					res += "randomwriteable: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).isRandomWriteable() + "\n";
					
					res += "sectors: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getDiskSectors() + "\n";
					res += "dirty: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getDirtySectors() + "\n";
					res += "lsn: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getLSN() + "\n";
					res += "reads: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getReads() + "\n";
					res += "writes: " + DriveWireServer.getHandler(instance).getDiskDrives().getDisk(driveno).getWrites() + "\n";
					
					
					
				}
				else
				{
					res += "loaded: false\n";
				}
			}
			catch (NumberFormatException e)
			{
				return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Non numeric drive number"));
			}
			
		}
		
		return(new DWCommandResponse(res));
	}


	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}


	public String getShortHelp() 
	{
		return "Show current disks";
	}


	public String getUsage() 
	{
		return "ui instance disk show";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}