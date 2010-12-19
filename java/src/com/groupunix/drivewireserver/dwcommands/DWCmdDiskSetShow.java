package com.groupunix.drivewireserver.dwcommands;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdDiskSetShow implements DWCommand {

	
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
		return "Show disksets or details of [set]";
	}


	public String getUsage() 
	{
		return "dw disk set show [set]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(doDiskSetShow());
		}
		else
		{
			return(doDiskSetShow(cmdline));
		}
	}

	@SuppressWarnings("unchecked")
	private DWCommandResponse doDiskSetShow()
	{
		String text = new String();
		
		text = "Available disk sets:\r\n\n";
		
		List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
    	
		String[] setnames = new String[disksets.size()];
		int tmp = 0;
		
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration dset = (HierarchicalConfiguration) it.next();
		    
		    setnames[tmp]=dset.getString("Name","unnamed-" + tmp); 
		    tmp++;
		}
		
		int longest = 0;
    	
    	for (int i=0; i<setnames.length; i++) 
    	{
    		if (setnames[i].length() > longest)
    			longest = setnames[i].length();
    	}
    	
    	longest++;
    	longest++;
    	
    	int cols = (80 / longest);
    	
    	for (int i=0; i<setnames.length; i++) 
        {
        	text += String.format("%-" + longest + "s",setnames[i]);
        	if (((i+1) % cols) == 0)
        		text += "\r\n";
        }
		
		text += "\r\n";
    	
		return(new DWCommandResponse(text));
	}
	
	
	
	@SuppressWarnings("unchecked")
	private DWCommandResponse doDiskSetShow(String setname)
	{
		String text = new String();
		
		if (DriveWireServer.hasDiskset(setname))
		{
			HierarchicalConfiguration theset = DriveWireServer.getDiskset(setname);
			
			text = "Details for disk set '" + setname + "':\r\n\n";
			
			text += "Description: " + theset.getString("Description","none") + "\r\n";
			text += "Save disk changes: " + theset.getBoolean("SaveChanges",false) + "\r\n\n";
			
			// disks
			List<HierarchicalConfiguration> disks = theset.configurationsAt("disk");
	    	
			for(Iterator<HierarchicalConfiguration> it = disks.iterator(); it.hasNext();)
			{
			    HierarchicalConfiguration disk = (HierarchicalConfiguration) it.next();
			    text += "X" + disk.getInt("drive") + ": " + disk.getString("path");
			    if (disk.getBoolean("writeprotect",false))
			    {
			    	text += " (WP)";
			    }
			    
			    if (disk.getBoolean("bootable",false))
			    {
			    	text += " (boot)";
			    }
			    
			    
			    text +="\r\n";
			}
		
		
			return(new DWCommandResponse(text));
		}
		else
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET, "No disk set named '" + setname + "' found"));
		}
	}	

	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
