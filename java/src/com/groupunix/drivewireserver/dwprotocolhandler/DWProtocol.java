package com.groupunix.drivewireserver.dwprotocolhandler;


import java.io.IOException;
import java.io.OutputStream;
import java.util.GregorianCalendar;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;




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
	void doCmd(String cmd, OutputStream outputStream) throws IOException;
	void syncStorage();
	int getHandlerNo();
	Logger getLogger();
	
}
