package com.groupunix.drivewireserver.dwprotocolhandler;


import java.util.GregorianCalendar;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwhelp.DWHelp;

// should really be an abstract class with much of dwprotocolhandler moved in
// but until i work on mcx or another protocol some more doesnt matter


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
	boolean isReady();
	void submitConfigEvent(String propertyName, String string);
	long getNumOps();
	long getNumDiskOps();
	long getNumVSerialOps();
	
}
