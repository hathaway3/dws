package com.groupunix.drivewireserver.dwcommands;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdServerHelpGentopics extends DWCommand {

	DWProtocol dwProto;
	
	public DWCmdServerHelpGentopics(DWProtocol dwProtocol,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProtocol;
	}


	public String getCommand() 
	{
		return "gentopics";
	}

	
	public String getShortHelp() 
	{
		return "Generate help topics from internal commands";
	}


	public String getUsage() 
	{
		return "dw server help gentopics";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doGentopics(cmdline));
	}

	
	

	private DWCommandResponse doGentopics(String cmdline) 
	{
		try 
		{
			((DWProtocolHandler)dwProto).getHelp().genTopics(this.dwProto);
		} 
		catch (ConfigurationException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
		} 
		catch (IOException e) 
		{
			return(new DWCommandResponse(false,DWDefs.RC_SERVER_IO_EXCEPTION,e.getMessage()));
		}
		return(new DWCommandResponse("Help topics generated."));
	}





	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
