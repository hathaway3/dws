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
	
	private DWUIClientThread uiref = null;
	private DWProtocolHandler dwProto = null;

	public UICmdInstanceDiskStatus(DWUIClientThread dwuiClientThread) 
	{

		this.uiref = dwuiClientThread;
	}

	public UICmdInstanceDiskStatus(DWProtocolHandler dwProto) 
	{
		this.dwProto = dwProto;
	}

	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		String res = new String();
		
		// TODO ASSumes we are using DW protocol
		if (this.dwProto == null)
			dwProto = (DWProtocolHandler) DriveWireServer.getHandler(this.uiref.getInstance());
		
		if (cmdline.length() > 0)
		{
			if ((dwProto != null) && (dwProto.getDiskDrives() != null))
			{
				res = "serial: " + dwProto.getDiskDrives().getDiskDriveSerial() + "\n";
			
				// 	disk details
				try
				{
			
					int driveno = Integer.parseInt(cmdline);
				
					if ((!(dwProto.getDiskDrives() == null)) && (dwProto.getDiskDrives().isLoaded(driveno)))
					{
						res += "loaded: true\n";
						res += "sectors: " + dwProto.getDiskDrives().getDisk(driveno).getParams().getInt("_sectors", 0) + "\n";
						res += "dirty: " + dwProto.getDiskDrives().getDisk(driveno).getParams().getInt("_dirty", 0) + "\n";
						res += "lsn: " + dwProto.getDiskDrives().getDisk(driveno).getParams().getInt("_lsn", 0) + "\n";
						res += "reads: " + dwProto.getDiskDrives().getDisk(driveno).getParams().getInt("_reads", 0) + "\n";
						res += "writes: " + dwProto.getDiskDrives().getDisk(driveno).getParams().getInt("_writes", 0) + "\n";

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