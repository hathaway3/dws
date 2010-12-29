package com.groupunix.drivewireserver.dwcommands;

public class DWCmdDisk implements DWCommand {

	static final String command = "disk";
	private DWCommandList commands = new DWCommandList();
		
	public DWCmdDisk(int handlerno)
	{
		commands.addcommand(new DWCmdDiskShow(handlerno));
		commands.addcommand(new DWCmdDiskEject(handlerno));
		commands.addcommand(new DWCmdDiskInsert(handlerno));
		commands.addcommand(new DWCmdDiskReload(handlerno));
		commands.addcommand(new DWCmdDiskWrite(handlerno));
		commands.addcommand(new DWCmdDiskCreate(handlerno));
		commands.addcommand(new DWCmdDiskWP(handlerno));
		commands.addcommand(new DWCmdDiskSync(handlerno));
		commands.addcommand(new DWCmdDiskExpand(handlerno));
		commands.addcommand(new DWCmdDiskOffset(handlerno));
		commands.addcommand(new DWCmdDiskLimit(handlerno));
		commands.addcommand(new DWCmdDiskSet(handlerno));
		commands.addcommand(new DWCmdDiskDump(handlerno));
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
