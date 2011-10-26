package com.groupunix.drivewireserver.uicommands;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DWUILogAppender;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdLogview extends DWCommand {

	static final String command = "logview";
		
	private static final Logger logger = Logger.getLogger("DWServer.DWUtilUIThread");
	
	private static PatternLayout logLayout;
	private DWUIClientThread dwuiref;
	
	
	public UICmdLogview(DWUIClientThread dwuiClientThread) 
	{
		this.dwuiref = dwuiClientThread;
	}

	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
			logger.debug("adding log watch appender");
			logLayout = new PatternLayout(DriveWireServer.serverconfig.getString("LogFormat","%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %26.26C: %m%n"));
			DWUILogAppender logAppender = new DWUILogAppender(logLayout, dwuiref.getSocket(), dwuiref);
			
			Logger.getRootLogger().addAppender(logAppender);
		
			boolean wanttodie = false;
		
			while ((wanttodie == false) && (!dwuiref.getSocket().isClosed()))
			{
			// wait for window to be closed... ?
			
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) 
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				wanttodie = true;
				}
			}
			
			logger.debug("removing log watch appender");
			Logger.getRootLogger().removeAppender(logAppender);
	
		return(new DWCommandResponse("Logviewer closed"));
	}


	public String getShortHelp() 
	{
		return "View log (real time)";
	}


	public String getUsage() 
	{
		return "ui logview";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}