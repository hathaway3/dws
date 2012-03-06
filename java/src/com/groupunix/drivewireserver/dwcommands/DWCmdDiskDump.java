package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskDump extends DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdDiskDump(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "dump";
	}


	
	public String getShortHelp() 
	{
		return "Dump sector from disk";
	}


	public String getUsage() 
	{
		return "dw disk dump # sector";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		if ((cmdline.length() == 0) || (args.length < 2))
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
			
			return(new DWCommandResponse(new String(dwProto.getDiskDrives().getDisk(driveno).getSector(sectorno).getData())));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric drive # or sector #"));
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
		} 
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
		}
		catch (IOException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION ,e.getMessage()));
		}
		
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
