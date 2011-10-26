package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceDiskStatus extends DWCommand {

	static final String command = "status";
	
	private DWUIClientThread uiref;

	public UICmdInstanceDiskStatus(DWUIClientThread dwuiClientThread) 
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
		
		if (cmdline.length() > 0)
		{
			if ((dwProto != null) && (dwProto.getDiskDrives() != null))
			{
				res = "serial: " + dwProto.getDiskDrives().getDiskDriveSerial() + "\n";
			
				// 	disk details
				try
				{
			
					int driveno = Integer.parseInt(cmdline);
				
					if ((!(dwProto.getDiskDrives() == null)) && (dwProto.getDiskDrives().diskLoaded(driveno)))
					{
						res += "loaded: true\n";
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
				catch (DWDriveNotLoadedException e) 
				{
					res += "loaded: false\n";
				} 
				catch (DWDriveNotValidException e) 
				{
					return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE, e.getMessage()));
				}
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_INVALID_HANDLER,"Null handler or diskset (is server restarting?)"));
			}
			
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Must specify drive number"));
		}
		
		return(new DWCommandResponse(res));
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