package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdPortClose implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdPortClose(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "close";
	}

	public String getLongHelp() 
	{
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Close port #";
	}


	public String getUsage() 
	{
		return "dw port close #";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"dw port close requires a port # as an argument"));
		}
		return(doPortClose(cmdline));
	}

	
	private DWCommandResponse doPortClose(String port)
	{

		
		try
		{
			int portno = Integer.parseInt(port);
			
			dwProto.getVPorts().closePort(portno);
			
			return(new DWCommandResponse("Port #"+ portno + " closed."));

		}
		catch (NumberFormatException e)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Syntax error: non numeric port #"));
		} 
		catch (DWPortNotValidException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_INVALID_PORT,e.getMessage()));
		} 
		
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}

}
