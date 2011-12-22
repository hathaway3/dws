package com.groupunix.drivewireserver.uicommands;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWEvent;
import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;

public class UICmdSync extends DWCommand {

	static final String command = "sync";
		
	private static final Logger logger = Logger.getLogger("DWServer.DWUtilUIThread");
	
	private DWUIClientThread dwuiref;
	private DWEvent lastevt = new DWEvent((byte) 0);
	
	public UICmdSync(DWUIClientThread dwuiClientThread) 
	{
		this.dwuiref = dwuiClientThread;
	}

	public String getCommand() 
	{
		return command;
	}

	public DWCommandResponse parse(String cmdline)
	{
		boolean wanttodie = false;
		
		logger.debug("adding status sync client");
		
		try 
		{
			dwuiref.getOutputStream().write(13);
		} 
		catch (IOException e1) 
		{
			logger.debug("immediate I/O error: " + e1.getMessage());
			wanttodie = true;
		}
		
		
	
		while ((wanttodie == false) && (!dwuiref.getSocket().isClosed()))
		{
			try 
			{	

				
				DWEvent msg = this.dwuiref.getEventQueue().take();
				
				// send params
				Set<String> keys = msg.getParamKeys();
				Iterator<String> itr = keys.iterator();
				while (itr.hasNext())
				{
					String key = itr.next();
					
					// only send changed params 
					if ((lastevt.getParam(key) == null) || !lastevt.getParam(key).equals(msg.getParam(key)))
					{
						dwuiref.getOutputStream().write( (key + ':' + msg.getParam(key) ).getBytes());
						dwuiref.getOutputStream().write(13);
						lastevt.setParam(key, msg.getParam(key));
					}
				}
				
				dwuiref.getOutputStream().write(msg.getEventType());
				dwuiref.getOutputStream().write(13);
				dwuiref.getOutputStream().flush();
				
			
				
			} 
			catch (InterruptedException e) 
			{
				wanttodie = true;
			} 
			catch (IOException e) 
			{
				wanttodie = true;
			}
			
		}
		
		logger.debug("removing status sync client");
			
		return(new DWCommandResponse(false, DWDefs.RC_FAIL, "Sync closed"));
	}


	public String getShortHelp() 
	{
		return "Sync status (real time)";
	}


	public String getUsage() 
	{
		return "ui sync";
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}