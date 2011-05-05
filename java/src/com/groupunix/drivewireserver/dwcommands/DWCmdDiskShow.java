package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskShow implements DWCommand 
{

	private DWProtocolHandler dwProto;

	public DWCmdDiskShow(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
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
		return "Show current disk details";
	}


	public String getUsage() 
	{
		return "dw disk show [#]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(doDiskShow());
		}
		return(doDiskDetail(cmdline));
	}

	
	private DWCommandResponse doDiskDetail(String drivestr)
	{
		String text;
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			dwProto.getDiskDrives().checkLoadedDriveNo(driveno);
			
			text = "Details for disk in drive #" + driveno + ":\r\n\n";
			
			text += "Path: " + dwProto.getDiskDrives().getDisk(driveno).getFilePath() + "\r\n";
			text += "Name: " + dwProto.getDiskDrives().getDisk(driveno).getDiskName() + "\r\n\n";
			
			text += "Sectors  LSN      WP       Reads    Writes   Dirty \r\n";
			
			text += String.format("%-9d", dwProto.getDiskDrives().getDiskSectors(driveno));
			text += String.format("%-9d", dwProto.getDiskDrives().getLSN(driveno));
			text += String.format("%-9s", dwProto.getDiskDrives().getWriteProtect(driveno));
			text += String.format("%-9d", dwProto.getDiskDrives().getReads(driveno));
			text += String.format("%-9d", dwProto.getDiskDrives().getWrites(driveno));
			text += String.format("%-9d", dwProto.getDiskDrives().getDirtySectors(driveno));
			text += "\r\n";
			text += "Read Errors: " + dwProto.getDiskDrives().getReadErrors(driveno);
			text += "\r\n\n";
			
			text += "Filesystem supports write: " + dwProto.getDiskDrives().getDisk(driveno).isFSWriteable() + "\r\n";
		    text += " FS supports random write: " + dwProto.getDiskDrives().getDisk(driveno).isRandomWriteable() + "\r\n";
			text += "      File can be written: " + dwProto.getDiskDrives().getDisk(driveno).isWriteable() + "\r\n";     
			text += "\r\n";
			text += "  Disk is write protected: " + dwProto.getDiskDrives().getDisk(driveno).getWriteProtect() + "\r\n";
			text += "     Sync to file allowed: " + dwProto.getDiskDrives().getDisk(driveno).isSync() + "\r\n";
			text += "   Disk expansion allowed: " + dwProto.getDiskDrives().getDisk(driveno).isExpand() + "\r\n";
			
			text += "          Disk size limit: ";
			if (dwProto.getDiskDrives().getDisk(driveno).getSizelimit() < 0)
			{
				text += "none\r\n";
			}
			else
			{
				text += dwProto.getDiskDrives().getDisk(driveno).getSizelimit() + " sectors\r\n";
			}
				
			text += "  File/disk sector offset: " + dwProto.getDiskDrives().getDisk(driveno).getOffset() + "\r\n";
			
			
			return(new DWCommandResponse(text));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric disk #"));
		}
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
		} 
	}

	
	private DWCommandResponse doDiskShow()
	{
		String text = new String();
		
		text = "\r\nCurrent DriveWire Disks:\r\n\n";
		
		text += "D#   DSK URI or file path (* = write protected)\r\n";
		text += "---- --------------------------------------------------------------------------\r\n";
	
		for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
		{

			
			if (dwProto.getDiskDrives().diskLoaded(i))
			{
				String df = dwProto.getDiskDrives().getDiskFile(i);
			
				if (dwProto.getDiskDrives().getWriteProtect(i))
				{
					df = df + "*";
				}
			 
				text += "X"+ String.format("%-3d ",i);
				text += String.format("%-74s",df);
				text += "\r\n";
			}
	
		}
		
		return(new DWCommandResponse(text));
	}
	
		
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
