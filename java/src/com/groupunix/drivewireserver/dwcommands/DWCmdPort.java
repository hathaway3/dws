package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdPort extends DWCommand {

	static final String command = "port";
	private DWCommandList commands;
	private DWProtocolHandler dwProto;	
	
	public DWCmdPort(DWProtocolHandler dwProto,DWCommand parent)
	{
		setParentCmd(parent);
		this.dwProto = dwProto;
		commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
		commands.addcommand(new DWCmdPortShow(dwProto,this));
		commands.addcommand(new DWCmdPortClose(dwProto,this));
		commands.addcommand(new DWCmdPortOpen(dwProto,this));
	}

	
	public String getCommand() 
	{
		return command;
	}
	
	public DWCommandList getCommandList()
	{
		return(this.commands);
	}

	public DWCommandResponse parse(String cmdline)
	{
		if (cmdline.length() == 0)
		{
			return(new DWCommandResponse(this.commands.getShortHelp()));
		}
		return(commands.parse(cmdline));
	}


	public String getShortHelp() 
	{
		return "Manage virtual serial ports";
	}


	public String getUsage() 
	{
		return "dw port [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
}
