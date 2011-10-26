package com.groupunix.drivewireserver.dwcommands;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskSet extends DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskSet(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "set";
	}


	
	public String getShortHelp() 
	{
		return "Set disk/diskset parameters";
	}


	public String getUsage() 
	{
		return "dw disk set [dset] # param [val]";
	}

	public DWCommandResponse parse(String cmdline)  
	{
		String[] args = cmdline.split(" ");
		
		if (args.length < 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk set requires at least 2 arguments."));
		}
		
		if (this.dwProto.getDiskDrives().isDiskNo(args[0]))
		{
			return(doDiskSet(Integer.parseInt(args[0]), DWUtils.dropFirstToken(cmdline)));
			
		}
		else if (this.dwProto.getDiskDrives().isDiskSetName(args[0]))
		{
			return(doDiskSet(args[0], DWUtils.dropFirstToken(cmdline)));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET ,"No diskset '" + args[0] + "' is defined."));
		}

		
	}

	
	
	private DWCommandResponse doDiskSet(String dsname, String cmdline) 
	{
		// diskset + disk/param/val
		
		String[] parts = cmdline.split(" ");
		
		if (this.dwProto.getDiskDrives().isDiskNo(parts[0]))
		{
			// ds / d#
			int driveno = Integer.parseInt(parts[0]);
			
			if (parts.length < 2)
			{
				return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"syntax error."));
			}
			
			try 
			{
				dwProto.getDiskDrives().validateDriveNo(driveno);
				
				HierarchicalConfiguration disk = DWUtils.getDiskDef(DriveWireServer.getDiskset(dsname), driveno);
				
				if (parts.length == 2)
				{
					// unset disk param
					disk.clearProperty(parts[1]);
					return(new DWCommandResponse("Item '" + parts[1] + "' unset for disk " + driveno + " in diskset '" + dsname +"'."));
				}
				else
				{
					disk.setProperty(parts[1], DWUtils.dropFirstToken(DWUtils.dropFirstToken(cmdline)));
					return(new DWCommandResponse("Item '" + parts[1] + "' set for disk " + driveno + " in diskset '" + dsname +"'."));
				}
				
			} 
			catch (DWDriveNotValidException e) 
			{
				return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
			} 
			catch (DWDisksetNotValidException e) 
			{
				return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET , e.getMessage()));
			} 
			catch (DWDisksetDriveNotLoadedException e) 
			{
				return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED , e.getMessage()));
			}
			
			
		}
		else
		{
			// diskset param
			if (parts.length == 1)
			{
				// unset diskset param
				try 
				{
					DriveWireServer.getDiskset(dsname).clearProperty(parts[0]);
					return(new DWCommandResponse("Item '" + parts[0] + "' unset for diskset '" + dsname +"'."));
				} 
				catch (DWDisksetNotValidException e) 
				{
					return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET , e.getMessage()));
				}
				
			}
			else
			{
				// set diskset param
				try 
				{
					DriveWireServer.getDiskset(dsname).setProperty(parts[0], DWUtils.dropFirstToken(cmdline));
					return(new DWCommandResponse("Item '" + parts[0] + "' set for diskset '" + dsname +"'."));
				} 
				catch (DWDisksetNotValidException e) 
				{
					return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET , e.getMessage()));
				}
			}
		}
		
		
	}

	private DWCommandResponse doDiskSet(int driveno, String cmdline) 
	{
		// driveno + param/val
		
		String[] parts = cmdline.split(" ");
		
		if (parts.length == 1)
		{
			try 
			{
				dwProto.getDiskDrives().validateDriveNo(driveno);
				
				// unset item
				if (dwProto.getDiskDrives().getDisk(driveno).getParams().containsKey(parts[0]))
				{
					dwProto.getDiskDrives().getDisk(driveno).getParams().clearProperty(parts[0]);
					return(new DWCommandResponse("Item '" + parts[0] + "' unset for disk " + driveno + "."));
				}
				else
				{
					return(new DWCommandResponse(false,DWDefs.RC_CONFIG_KEY_NOT_SET,"Item '" + parts[0] + "' is not set for drive " + driveno + "."));
				}
				
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
		else
		{
			// set item
				
			try 
			{
					
				dwProto.getDiskDrives().validateDriveNo(driveno);
				
				dwProto.getDiskDrives().getDisk(driveno).getParams().setProperty(parts[0], DWUtils.dropFirstToken(cmdline));
				
				return(new DWCommandResponse("Item '" + parts[0] + "' set for disk " + driveno + "."));
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
		
	}

	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
