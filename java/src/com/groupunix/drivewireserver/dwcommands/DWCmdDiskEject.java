package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskEject extends DWCommand 
{
	private DWProtocolHandler dwProto;
	
	public DWCmdDiskEject(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}

	public String getCommand() 
	{
		return "eject";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: dw disk eject requires one or two arguments"));
		}
		
		String[] args = cmdline.split(" ");
		
		if (args[0].equals("all"))
		{
			// eject all disks
			return(doDiskEjectAll());
		}
		else if (this.dwProto.getDiskDrives().isDiskNo(args[0]))
		{
			// eject a disk
			return(doDiskEject(Integer.parseInt(args[0])));
		}
		else if ((args.length == 2) && this.dwProto.getDiskDrives().isDiskSetName(args[0]))
		{
			if (args[1].equals("all"))
			{
				// eject all disks
				return(doDiskEjectAll(args[0]));
			}
			else if (this.dwProto.getDiskDrives().isDiskNo(args[1]))
			{
				// eject a disk from a set
				return(doDiskEject(args[0], Integer.parseInt(args[1])));
			}
			
		}
		
		return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error"));
		
	}

	private DWCommandResponse doDiskEjectAll(String set) 
	{
		try 
		{
			this.dwProto.getDiskDrives().clearDisksetDisks(set);
			return(new DWCommandResponse("Ejected all disks from set '" + set + "'.\r\n"));
		} 
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		}
		
	}

	private DWCommandResponse doDiskEject(String set, int driveno)
	{
		try 
		{
			this.dwProto.getDiskDrives().clearDisksetDisk(set, driveno);
			return(new DWCommandResponse("Removed disk " + driveno + " from set '" + set + "'.\r\n"));
		} 
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		} 
		catch (DWDisksetDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED, e.getMessage()));
		}
	}

	private DWCommandResponse doDiskEjectAll()
	{
		dwProto.getDiskDrives().EjectAllDisks();
		return(new DWCommandResponse("Ejected all disks.\r\n"));
	}
	
	
	private DWCommandResponse doDiskEject(int driveno) 
	{
		try
		{
			dwProto.getDiskDrives().EjectDisk(driveno);
		
			return(new DWCommandResponse("Disk ejected from drive " + driveno + ".\r\n"));
		}
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
		} 
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		}

		
	}


	public String getShortHelp() 
	{
		return "Eject disk from drive #";
	}

	public String getUsage() 
	{
		return "dw disk eject [dset] {# | all}";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
