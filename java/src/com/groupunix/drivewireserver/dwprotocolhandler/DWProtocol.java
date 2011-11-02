package com.groupunix.drivewireserver.dwprotocolhandler;


import java.util.GregorianCalendar;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwhelp.DWHelp;




public interface DWProtocol extends Runnable
{
	void shutdown();
	boolean isDying();
	HierarchicalConfiguration getConfig();
	//DWCommandList getDWCmds();
	//DWDiskDrives getDiskDrives();
	//DWVSerialPorts getVPorts();
	DWProtocolDevice getProtoDev();
	GregorianCalendar getInitTime();
	String getStatusText();
	void resetProtocolDevice();
	void syncStorage();
	int getHandlerNo();
	Logger getLogger();
	int getCMDCols();
	DWHelp getHelp();
	
}
