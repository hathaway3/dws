package com.groupunix.drivewireserver;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class DWUIClientOutputThread implements Runnable {

	private int uiport;
	private OutputStream ostream;
	private static final Logger logger = Logger.getLogger("DWUIClientOutputThread");
	private boolean wanttodie = false;
	
	public DWUIClientOutputThread(OutputStream outputStream, int uiport) 
	{
		this.ostream = outputStream;
		this.uiport = uiport;
	}

	public void run() 
	{
		Thread.currentThread().setName("dwUIcliOut-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		logger.debug("run");
		
		while(!wanttodie)
		{
			try 
			{
				int databyte = DriveWireServer.getHandler(0).getVPorts().getPortOutput(this.uiport).read();
				ostream.write(databyte);
			} 
			catch (IOException e) 
			{
				logger.warn("IO Exception: " + e.getMessage());
				wanttodie = true;
			}
			
			
		}
		
		logger.debug("exit");
	}

}
