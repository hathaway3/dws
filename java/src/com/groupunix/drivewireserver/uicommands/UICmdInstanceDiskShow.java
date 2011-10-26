package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class UICmdInstanceDiskShow extends DWCommand {

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

	@SuppressWarnings("unchecked")
	public DWCommandResponse parse(String cmdline)
	{
		String res = new String();
		
		// TODO ASSumes we are using DW protocol
		DWProtocolHandler dwProto = (DWProtocolHandler) DriveWireServer.getHandler(this.uiref.getInstance());
		
		if (cmdline.length() == 0)
		{
			if (dwProto.getDiskDrives() == null)
			{
				return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET, "Disk drives are null, is server restarting?"));
			}
			
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
				
				if ((!(dwProto.getDiskDrives() == null)) && (dwProto.getDiskDrives().diskLoaded(driveno)))
				{
					res += "loaded: true\n"; 
					
					HierarchicalConfiguration disk = dwProto.getDiskDrives().getDisk(driveno).getParams();
					
					for(Iterator<String> itk = disk.getKeys(); itk.hasNext();)
					{
						String option = itk.next();
						
						res += option + ": " + disk.getProperty(option) + "\n";
					}

					
					res += "fswriteable: " + dwProto.getDiskDrives().getDisk(driveno).isFSWriteable() + "\n";
					res += "writeable: " + dwProto.getDiskDrives().getDisk(driveno).isWriteable() + "\n";
					res += "randomwriteable: " + dwProto.getDiskDrives().getDisk(driveno).isRandomWriteable() + "\n";
					
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