package com.groupunix.drivewireserver.dwcommands;

public interface DWCommand {

	String getCommand();
	DWCommandResponse parse(String cmdline);
	String getShortHelp();
	String getLongHelp();
	String getUsage();
}
