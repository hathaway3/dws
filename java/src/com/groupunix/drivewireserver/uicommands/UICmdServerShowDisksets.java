package com.groupunix.drivewireserver.uicommands;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdServerShowDisksets implements DWCommand {

	@Override
	public String getCommand() 
	{
		// TODO Auto-generated method stub
		return "disksets";
	}

	@Override
	public String getLongHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortHelp() {
		// TODO Auto-generated method stub
		return "show server disk sets";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "ui server show disksets";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
    	
		int tmp = 0;
		String res = new String();
			
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
			HierarchicalConfiguration dset = (HierarchicalConfiguration) it.next();
		    
			res += dset.getString("Name","unnamed-" + tmp) + "\n";
		   	tmp++;
		} 
			
		return(new DWCommandResponse(res));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
