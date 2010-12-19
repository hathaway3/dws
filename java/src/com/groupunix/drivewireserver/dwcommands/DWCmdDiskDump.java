package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class DWCmdDiskDump implements DWCommand {

	private int handlerno;

	public DWCmdDiskDump(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	public String getCommand() 
	{
		return "dump";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Dump sector from disk";
	}


	public String getUsage() 
	{
		return "dw disk dump disk# sector#";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		if ((args.length < 2) || (cmdline.length() == 0))
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk dump requires a drive # and sector # as arguments"));
		}
		return(doDiskDump(args[0],args[1]));
	}

	
	
	
	private DWCommandResponse doDiskDump(String drivestr, String sectorstr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			int sectorno = Integer.parseInt(sectorstr);
			
			return(new DWCommandResponse(new String(DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).getSector(sectorno).getData())));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive # or sector #"));
		} 
		
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
