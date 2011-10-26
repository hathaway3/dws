package com.groupunix.drivewireserver.dwcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskShow extends DWCommand 
{

	private DWProtocolHandler dwProto;

	public DWCmdDiskShow(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "show";
	}


	
	public String getShortHelp() 
	{
		return "Show current disk/set details";
	}


	public String getUsage() 
	{
		return "dw disk show [{# | all | dset [#]}]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		
		
		// show loaded disks
		if (cmdline.length() == 0)
		{
			return(doDiskShow());
		}
		
		String[] args = cmdline.split(" ");
		
		if (this.dwProto.getDiskDrives().isDiskNo(args[0]))
		{
			// show loaded disk detail
			return(doDiskShow(Integer.parseInt(args[0])));
		}
		else 
		{
			if (args.length == 2)
			{
				if (this.dwProto.getDiskDrives().isDiskNo(args[1]))
				{
					// show disk in set detail
					return(doDisksetShow(args[0], Integer.parseInt(args[1])));
				}
				
			}
			else if (args[0].equalsIgnoreCase("all"))
			{
				return(doDisksetShow());
			}
			else
			{
				// show disk set details
				return(doDisksetShow(args[0]));
			}
		}

		return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error"));
		
	}

	
	private DWCommandResponse doDisksetShow()
	{
		String text = "Available disksets:\r\n\r\n";
		
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
    	
		ArrayList<String> ps = new ArrayList<String>();
		int maxlen = 1;
		
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
			String name = it.next().getString("Name","?noname?");
			ps.add(name);
			if (name.length() > maxlen)
				maxlen = name.length();
		}
		
		Collections.sort(ps);
		maxlen++;
		int cols = 40 / maxlen;
		
		
		Iterator<String> it = ps.iterator();
		int i = 0;
		while (it.hasNext())
        {
			text += String.format("%-" + maxlen + "s", it.next());
        	if (((i+1) % cols) == 0)
        	{
        		text += String.format("%-" + (40 - maxlen * cols) + "s", " ");
        		
        	}
        	
			i++;
        }
		

   		text += "\r\n";
		
		return(new DWCommandResponse(text));
	}
	
	
	
	@SuppressWarnings("unchecked")
	private DWCommandResponse doDisksetShow(String set, int diskno) 
	{
		String text;
		HierarchicalConfiguration theset;
		
		try 
		{
			theset = DriveWireServer.getDiskset(set);
			List<HierarchicalConfiguration> disks = theset.configurationsAt("disk");
	    	
			for(Iterator<HierarchicalConfiguration> it = disks.iterator(); it.hasNext();)
			{
				HierarchicalConfiguration disk = it.next();
				if (disk.getInt("drive")==diskno)
				{
					text = "Details for disk #" + diskno + " in set '" + set +"':\r\n\r\n";
					text += "Path: " + disk.getString("path") + "\r\n\r\n";
					ArrayList<String> ps = new ArrayList<String>();
					
					for(Iterator<String> itk = disk.getKeys(); itk.hasNext();)
					{
						String option = itk.next();
						
						if (!option.equals("drive") && !option.equals("path"))
						{
							ps.add(option + ": " + disk.getString(option));
						}
						
					}
					
					Collections.sort(ps);
					text +=  DWCommandList.colLayout(ps, this.dwProto.getCMDCols());
					
					
					return(new DWCommandResponse(text));
					
				}
			}
			
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE, "Disk " + diskno + " is not defined in diskset '" + set + "'."));
		}
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		}
	}

	
	
	@SuppressWarnings("unchecked")
	private DWCommandResponse doDisksetShow(String set) 
	{
		// disk set..
		String text;
		HierarchicalConfiguration theset;
		
		try 
		{
			theset = DriveWireServer.getDiskset(set);
			text = "Details for disk set '" + set + "':\r\n\r\n";
			text += "Description: " + theset.getString("Description","") + "\r\n\r\n";
			ArrayList<String> ps = new ArrayList<String>();
			
			for(Iterator<String> itk = theset.getKeys(); itk.hasNext();)
			{
				String option = itk.next();
				
				
				if (!option.equals("Name") && !option.startsWith("disk.") && !option.equals("Description"))
				{
					ps.add(option + ": " + theset.getString(option));
				}
				
			}
			
			Collections.sort(ps);
			text += DWCommandList.colLayout(ps, this.dwProto.getCMDCols());
			
				
			text += "\r\n";
			
			// disks
			List<HierarchicalConfiguration> disks = theset.configurationsAt("disk");
	    	
			for(Iterator<HierarchicalConfiguration> it = disks.iterator(); it.hasNext();)
			{
			    HierarchicalConfiguration disk = it.next();
			    text += String.format("X%-3d ", disk.getInt("drive")) + disk.getString("path");
			    
			    text +="\r\n";
			}
			
			
		} 
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		}
		
		return(new DWCommandResponse(text));
	}

	
	
	
	private DWCommandResponse doDiskShow(int driveno) 
	{
		String text;
		
		try
		{
			
			dwProto.getDiskDrives().checkLoadedDriveNo(driveno);
			
			text = "Details for disk in drive #" + driveno + ":\r\n\r\n";
			
			text += "Path: " + DWUtils.shortenLocalURI(dwProto.getDiskDrives().getDisk(driveno).getFilePath()) + "\r\n";
			
			String typ = dwProto.getDiskDrives().getDisk(driveno).getDiskFSType();
			
			text += "Type: " + typ;
			
			if (typ.equals("OS9"))
			{
				text += "  Name: " + dwProto.getDiskDrives().getDisk(driveno).getDiskName();
			}
			text += "\r\n\r\n";
			
			text += "Sectors  LSN      Reads    Writes\r\n";
			
			text += String.format("%-9d", dwProto.getDiskDrives().getDiskSectors(driveno));
			text += String.format("%-9d", dwProto.getDiskDrives().getLSN(driveno));
			text += String.format("%-9d", dwProto.getDiskDrives().getReads(driveno));
			text += String.format("%-9d", dwProto.getDiskDrives().getWrites(driveno));
			text += "\r\n\r\n";
			
			// optional/warning type info
			
			if (dwProto.getDiskDrives().getReadErrors(driveno) > 0)
			{
				text += "This drive reports " + dwProto.getDiskDrives().getReadErrors(driveno)+ " read errors.\r\n";
			}
			
			if (dwProto.getDiskDrives().getWriteErrors(driveno) > 0)
			{
				text += "This drive reports " + dwProto.getDiskDrives().getWriteErrors(driveno)+ " write errors.\r\n";
			}
			
			if (dwProto.getDiskDrives().getDirtySectors(driveno) > 0)
			{
				text += "This drive reports " + dwProto.getDiskDrives().getDirtySectors(driveno) + " dirty sectors.\r\n";
			}
			
			// file writable or no..
			if (!dwProto.getDiskDrives().getDisk(driveno).isFSWriteable())
			{	
				text += "Source filesystem is read only.\r\n";
			}
			else 
			{
				if (!dwProto.getDiskDrives().getDisk(driveno).isWriteable())
				{
					text += "Source file cannot be written to.\r\n";
				}
				else if (!dwProto.getDiskDrives().getDisk(driveno).isRandomWriteable())
				{
					text += "Source filesystem doesn't support random access, but manual writes should work.\r\n";
				}
			}
			
			HierarchicalConfiguration params = dwProto.getDiskDrives().getDisk(driveno).getParams();
			
			ArrayList<String> ps = new ArrayList<String>();
			
			for(@SuppressWarnings("unchecked") Iterator<String> itk = params.getKeys(); itk.hasNext();)
			{
				String p = itk.next();
				
				if (!p.equals("path") && !p.equals("drive"))
				{
					ps.add(p + ": " + params.getProperty(p));
					
				}
			}
			
			Collections.sort(ps);
			text += DWCommandList.colLayout(ps, this.dwProto.getCMDCols());
			
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
		
		text = "\r\nCurrent DriveWire disks:\r\n\r\n";
		
		
		for (int i = 0;i<dwProto.getDiskDrives().getMaxDrives();i++)
		{

			
			if (dwProto.getDiskDrives().diskLoaded(i))
			{
				text += String.format("X%-3d ",i) + DWUtils.shortenLocalURI(dwProto.getDiskDrives().getDiskFile(i)) + "\r\n";
			}
	
		}
		
		if (dwProto.getConfig().containsKey("CurrentDiskSet"))
		{
			text += "\r\nCurrent diskset: " + dwProto.getConfig().getString("CurrentDiskSet");
			try 
			{
				if (DriveWireServer.getDiskset(dwProto.getConfig().getString("CurrentDiskSet")).getBoolean("SaveChanges", false))
					text += " (save changes)";
				text += "\r\n";
			} 
			catch (DWDisksetNotValidException e) 
			{
				return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,"CurrentDisksSet error: " + e.getMessage()));
			}
		}
		
		return(new DWCommandResponse(text));
	}
	
		
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
