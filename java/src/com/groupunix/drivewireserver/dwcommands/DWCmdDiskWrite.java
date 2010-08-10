package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskWrite implements DWCommand {

	private int handlerno;
	
	public DWCmdDiskWrite(int handlerno)
	{
		this.handlerno = handlerno;
	}
	
	
	public String getCommand() 
	{
		return "write";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Write disk image in drive #";
	}

	public String getUsage() 
	{
		return "dw disk write # [URI/path]";
	}


	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk write requires a drive # and optional URI/path"));
		}

		String[] args = cmdline.split(" ");
		
		if (args.length == 1)
		{
			return(doDiskWrite(args[0]));
		}
		else
		{
			return(doDiskWrite(args[0],args[1]));
		}
		
	}

	
	private DWCommandResponse doDiskWrite(String drivestr)
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).writeDisk();
					
			return(new DWCommandResponse("Disk #" + driveno + " written to source."));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk write requires a numeric drive # as an argument"));
		} 
		catch (IOException e1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e1.getMessage()));
		}
	}
	
	
	private DWCommandResponse doDiskWrite(String drivestr, String path)
	{
		path = DWUtils.convertStarToBang(path);
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).writeDisk(path);
					
			return(new DWCommandResponse("Disk #" + driveno + " written to '" + path + "'"));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk write requires a numeric drive # as an argument"));
		} 
		catch (IOException e1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e1.getMessage()));
		}
	}
	
}
