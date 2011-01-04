package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdDisksetSet implements DWCommand {

	
	public String getCommand() 
	{
		return "set";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Set diskset details";
	}


	public String getUsage() 
	{
		return "ui diskset set [set] [item] [value]";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() < 3)
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
		
		List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
    	
		String[] setnames = new String[disksets.size()];
		int tmp = 0;
		
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration dset = (HierarchicalConfiguration) it.next();
		    
		    setnames[tmp]=dset.getString("Name","unnamed-" + tmp); 
		    tmp++;
		}
    	for (int i=0; i<setnames.length; i++) 
        {
        	text += setnames[i]+"\n";
        	
        }
		
		return(new DWCommandResponse(text));
	}
	
	
	
	@SuppressWarnings("unchecked")
	private DWCommandResponse doDiskSetShow(String setname)
	{
		String text = new String();
		
		if (DriveWireServer.hasDiskset(setname))
		{
			HierarchicalConfiguration theset = DriveWireServer.getDiskset(setname);
			
			text = "Description: " + theset.getString("Description","") + "\n";
			text += "Notes: " + theset.getString("Notes","") + "\n";
			text += "SaveChanges: " + theset.getBoolean("SaveChanges",false) + "\n";
			text += "HDBDOSMode: " + theset.getBoolean("HDBDOSMode",false) + "\n";
			text += "ImageURL: " + theset.getString("ImageURL","") + "\n";
			text += "EjectAllOnLoad: " + theset.getBoolean("EjectAllOnLoad",false) + "\n";
			
			// disks
			List<HierarchicalConfiguration> disks = theset.configurationsAt("disk");
	    	
			for(Iterator<HierarchicalConfiguration> it = disks.iterator(); it.hasNext();)
			{
			    HierarchicalConfiguration disk = (HierarchicalConfiguration) it.next();
			    text += "path(" + disk.getInt("drive") + "): " + disk.getString("path","") + "\n";
			    text += "writeprotect(" + disk.getInt("drive") + "): " + disk.getBoolean("writeprotect",false) + "\n";
			    text += "sync(" + disk.getInt("drive") + "): " + disk.getBoolean("sync",false) + "\n";
			    text += "expand(" + disk.getInt("drive") + "): " + disk.getBoolean("expand",false) + "\n";
			    text += "sizelimit(" + disk.getInt("drive") + "): " + disk.getInt("sizelimit",-1) + "\n";
			    text += "offset(" + disk.getInt("drive") + "): " + disk.getInt("offset",0) + "\n";

			    
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
