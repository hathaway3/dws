package com.groupunix.drivewireserver.dwcommands;


import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWCmdDisk implements DWCommand {

	static final String command = "disk";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdDisk(DWProtocolHandler dwProto)
	{
		commands.addcommand(new DWCmdDiskShow(dwProto));
		commands.addcommand(new DWCmdDiskEject(dwProto));
		commands.addcommand(new DWCmdDiskInsert(dwProto));
		commands.addcommand(new DWCmdDiskReload(dwProto));
		commands.addcommand(new DWCmdDiskWrite(dwProto));
		commands.addcommand(new DWCmdDiskCreate(dwProto));
		commands.addcommand(new DWCmdDiskWP(dwProto));
		commands.addcommand(new DWCmdDiskSync(dwProto));
		commands.addcommand(new DWCmdDiskExpand(dwProto));
		commands.addcommand(new DWCmdDiskOffset(dwProto));
		commands.addcommand(new DWCmdDiskLimit(dwProto));
		commands.addcommand(new DWCmdDiskSet(dwProto));
		commands.addcommand(new DWCmdDiskDump(dwProto));
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

		return null;
	}


	public String getShortHelp() 
	{
		return "Commands that manipulate disks";
	}


	public String getUsage() 
	{
		return "dw disk [command]";
	}
	
	public boolean validate(String cmdline) 
	{
		return(commands.validate(cmdline));
	}
	
}
