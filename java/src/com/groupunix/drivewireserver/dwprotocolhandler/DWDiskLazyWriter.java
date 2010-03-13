package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWDiskLazyWriter implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWProtoReader");
	private boolean wanttodie = false;
	
	public void run() 
	{
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		Thread.currentThread().setName("dskwriter-" + Thread.currentThread().getId());
	
		logger.debug("started, write interval is " + DriveWireServer.config.getLong("DiskLazyWriteInterval",15000) );
		
		while (wanttodie == false)
		{

			try 
			{
				Thread.sleep(DriveWireServer.config.getLong("DiskLazyWriteInterval",15000));
				syncDisks();
			}	 
			catch (InterruptedException e) 
			{
				logger.debug("interrupted");
				wanttodie = true;
			}

		}
		
		logger.debug("exit");
	}

	
	private void syncDisks()
	{

		// scan all loaded drives
		for (int driveno = 0;driveno<DWDiskDrives.MAX_DRIVES;driveno++)
		{
			if (DWProtocolHandler.getDiskDrives().diskLoaded(driveno))
			{
				if (DWProtocolHandler.getDiskDrives().isRandomWriteable(driveno))
				{ 
				
					if (DWProtocolHandler.getDiskDrives().getDirtySectors(driveno) > 0)
					{
						logger.debug("cache for drive " + driveno + " has changed, " + DWProtocolHandler.getDiskDrives().getDirtySectors(driveno) + " dirty sectors");
						
						try
						{
							DWProtocolHandler.getDiskDrives().writeDisk(driveno);
						} 
						catch (IOException e)
						{
							logger.error("Lazy write failed: " + e.getMessage());
						}
					}
				
				}
			}
		}
		
	}


	
}
