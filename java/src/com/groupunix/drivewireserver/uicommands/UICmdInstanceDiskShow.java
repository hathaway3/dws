package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

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
		
		// TODO ASSumes we are using DW protocol
		DWProtocolHandler dwProto = (DWProtocolHandler) DriveWireServer.getHandler(this.uiref.getInstance());
		
		if (cmdline.length() == 0)
		{
			for (int i =0;i<dwProto.getDiskDrives().getMaxDrives();i++)
			{
				if (dwProto.getDiskDrives().diskLoaded(i))
				{
					res += i + " " + dwProto.getDiskDrives().getDiskFile(i) + "\n";
				}
			}
		}
		else
		{
			// disk details
			try
			{
			
				int driveno = Integer.parseInt(cmdline);
				
				if (dwProto.getDiskDrives().diskLoaded(driveno))
				{
					res += "loaded: true\n"; 
					res += "path: " + dwProto.getDiskDrives().getDisk(driveno).getFilePath() + "\n";
					res += "sizelimit: " + dwProto.getDiskDrives().getDisk(driveno).getSizelimit() + "\n";
					res += "offset: " + dwProto.getDiskDrives().getDisk(driveno).getOffset() + "\n";
					res += "sync: " + dwProto.getDiskDrives().getDisk(driveno).isSync() + "\n";
					res += "expand: " + dwProto.getDiskDrives().getDisk(driveno).isExpand() + "\n";
					res += "writeprotect: " + dwProto.getDiskDrives().getDisk(driveno).getWriteProtect() + "\n";
					
					res += "fswriteable: " + dwProto.getDiskDrives().getDisk(driveno).isFSWriteable() + "\n";
					res += "writeable: " + dwProto.getDiskDrives().getDisk(driveno).isWriteable() + "\n";
					res += "randomwriteable: " + dwProto.getDiskDrives().getDisk(driveno).isRandomWriteable() + "\n";
					
					res += "sectors: " + dwProto.getDiskDrives().getDisk(driveno).getDiskSectors() + "\n";
					res += "dirty: " + dwProto.getDiskDrives().getDisk(driveno).getDirtySectors() + "\n";
					res += "lsn: " + dwProto.getDiskDrives().getDisk(driveno).getLSN() + "\n";
					res += "reads: " + dwProto.getDiskDrives().getDisk(driveno).getReads() + "\n";
					res += "writes: " + dwProto.getDiskDrives().getDisk(driveno).getWrites() + "\n";
					
					
					
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