package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmd extends DWCommand
{

	static final String command = "dw";
	private DWCommandList commands;
	private DWProtocol dwProto;

	
	public DWCmd(DWProtocol dwProtocol)
	{
		
		this.dwProto = dwProtocol;
		
		commands = new DWCommandList(this.dwProto, this.dwProto.getCMDCols());
		commands.addcommand(new DWCmdDisk((DWProtocolHandler) dwProtocol, this));
		commands.addcommand(new DWCmdServer(dwProtocol, this));
		commands.addcommand(new DWCmdConfig(dwProtocol, this));
		commands.addcommand(new DWCmdPort((DWProtocolHandler) dwProtocol, this));
		commands.addcommand(new DWCmdLog(dwProtocol, this));
		commands.addcommand(new DWCmdNet((DWProtocolHandler) dwProtocol, this));
		commands.addcommand(new DWCmdMidi((DWProtocolHandler) dwProtocol, this));
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
		return "Manage all aspects of the server";
	}


	public String getUsage() 
	{
		return "dw [command]";
	}


	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
	
}
