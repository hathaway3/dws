package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDiskSet implements DWCommand 
{

	static final String command = "set";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdDiskSet(DWProtocolHandler dwProto)
	{
		commands.addcommand(new DWCmdDiskSetShow());
		commands.addcommand(new DWCmdDiskSetLoad(dwProto));
		
	}

	
	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		return(commands.parse(cmdline));
	}


	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}


	public String getShortHelp() 
	{
		return "Commands that manipulate disk sets";
	}


	public String getUsage() 
	{
		return "dw disk set [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}
