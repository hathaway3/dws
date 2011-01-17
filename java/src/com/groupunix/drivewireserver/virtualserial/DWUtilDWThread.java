package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWUtilDWThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilDWThread");
	
	private int vport = -1;
	private String strargs = null;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	private boolean protect = false;
	
	private DWCommandList commands;
	
	public DWUtilDWThread(int handlerno, int vport, String args)
	{
		this.vport = vport;
		this.strargs = args;
		this.handlerno = handlerno;
		this.dwVSerialPorts = DriveWireServer.getHandler(handlerno).getVPorts();
		
		if (vport <= DWVSerialPorts.MAX_COCO_PORTS)
		{
			this.protect = DriveWireServer.getHandler(handlerno).config.getBoolean("ProtectedMode", false); 
		}
		
		// setup command list
		commands = DriveWireServer.getHandler(handlerno).getDWCmds();
		
		logger.debug("init dw util thread (protected mode: " + this.protect + ")");	
	}
	
	
	
	public void run() 
	{
		
		Thread.currentThread().setName("dwutil-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		logger.debug("run for handler #" + handlerno);
		
		this.dwVSerialPorts.markConnected(vport);
		this.dwVSerialPorts.setUtilMode(this.vport, DWDefs.UTILMODE_DWCMD);
		
		DWCommandResponse resp = commands.parse(DWUtils.dropFirstToken(this.strargs));
		
		if (resp.getSuccess())
		{
			if (resp.isUsebytes())
			{
				dwVSerialPorts.sendUtilityOKResponse(this.vport, resp.getResponseBytes());
			}
			else
			{
				dwVSerialPorts.sendUtilityOKResponse(this.vport, resp.getResponseText());
			}
		}
		else
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, resp.getResponseCode(), resp.getResponseText());
		}
		
		// wait for output to flush
		try 
		{
			while ((dwVSerialPorts.bytesWaiting(this.vport) > 0) && (dwVSerialPorts.isOpen(this.vport)))
			{
				logger.debug("pause for the cause: " + dwVSerialPorts.bytesWaiting(this.vport) + " bytes left" );
				Thread.sleep(100);
			}
			
			if (this.vport < DWVSerialPorts.MAX_COCO_PORTS)
			{
				dwVSerialPorts.closePort(this.vport);
			}	
			
		} 
		catch (InterruptedException e) 
		{
			logger.error(e.getMessage());
		} 
		catch (DWPortNotValidException e) 
		{
			logger.error(e.getMessage());
		}
		
		
		logger.debug("exiting");
		
	}

	
	


	// total crap hack.  does user management even belong in dw?
/*
	private void doMakePass(String pw)
	{
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "encrypted pw follows");
		
		BasicPasswordEncryptor bpe = new BasicPasswordEncryptor();
				
		dwVSerialPorts.writeToCoco(this.vport, "Encypted form of '" + pw + "' is: " + bpe.encryptPassword(pw));

	}
*/


	
}
