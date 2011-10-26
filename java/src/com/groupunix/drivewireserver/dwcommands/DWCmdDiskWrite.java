package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWCmdDiskWrite extends DWCommand {

	private DWProtocolHandler dwProto;
	
	public DWCmdDiskWrite(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
	}
	
	
	public String getCommand() 
	{
		return "write";
	}


	
	public String getShortHelp() 
	{
		return "Write disk image in drive #";
	}

	public String getUsage() 
	{
		return "dw disk write {# [path] | dset [dset]}";
	}


	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk write requires at least one argument."));
		}

		String[] args = cmdline.split(" ");
		

		
		if (dwProto.getDiskDrives().isDiskNo(args[0]))
		{
			// write disk in drive..
			
			if (args.length > 1)
			{
				// write to new .dsk path
				return(doDiskWrite(Integer.parseInt(args[0]), DWUtils.dropFirstToken(cmdline)));
				
			}
			else
			{
				// write to current .dsk
				return(doDiskWrite(Integer.parseInt(args[0])));
			}
		}
		else if (args.length > 2)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw disk write takes up to 2 arguments."));
		}
		else if (args.length == 1)
		{
			// write current drives to set..
			return(doDiskWrite(args[0]));
		}
		else
		{
			if (this.dwProto.getDiskDrives().isDiskSetName(args[0]))
			{
				// write one set to another..
				return(doDiskWrite(args[0], args[1]));
			}
			else
			{
				return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,"Unknown diskset '" + args[0] + "'."));
			}
		}
	}

	
	private DWCommandResponse doDiskWrite(String srcset, String dstset) 
	{

			try 
			{
				this.dwProto.getDiskDrives().SaveDiskSet(srcset, dstset);
				return(new DWCommandResponse("Wrote set '" + srcset + "' to set '" + dstset + "'."));
			} 
			catch (DWDisksetNotValidException e) 
			{
				return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
			}
	}
	
	
	private DWCommandResponse doDiskWrite(String setname) 
	{
		try 
		{
			this.dwProto.getDiskDrives().SaveDiskSet(setname);
			return(new DWCommandResponse("Wrote current disk definitions to set '" + setname + "'."));
		} 
		catch (DWDisksetNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_NO_SUCH_DISKSET,e.getMessage()));
		}
	}
		
	private DWCommandResponse doDiskWrite(int driveno)
	{
		
		try
		{
			
			dwProto.getDiskDrives().writeDisk(driveno);
					
			return(new DWCommandResponse("Wrote disk #" + driveno + " to source image."));

		}
		catch (IOException e1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e1.getMessage()));
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
		} 
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
		}
	}
	
	
	private DWCommandResponse doDiskWrite(int driveno, String path)
	{
		path = DWUtils.convertStarToBang(path);
		
		try
		{
			
			dwProto.getDiskDrives().writeDisk(driveno,path);
					
			return(new DWCommandResponse("Wrote disk #" + driveno + " to '" + path + "'"));

		}
		catch (IOException e1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e1.getMessage()));
		} 
		catch (DWDriveNotLoadedException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_DRIVE_NOT_LOADED,e.getMessage()));
		} 
		catch (DWDriveNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_DRIVE,e.getMessage()));
		}
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
