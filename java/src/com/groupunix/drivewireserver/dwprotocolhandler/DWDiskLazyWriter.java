package com.groupunix.drivewireserver.dwprotocolhandler;

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
				boolean diskchanged = false;
				boolean memchanged = false;
			
				if (!DWProtocolHandler.getDiskDrives().getChecksum(driveno).equals(DWProtocolHandler.getDiskDrives().getDiskChecksum(driveno)))
				{
					logger.debug("disk file for drive " + driveno + " has changed (mem chksum = " + DWProtocolHandler.getDiskDrives().getChecksum(driveno) + ", dsk chksum = " + DWProtocolHandler.getDiskDrives().getDiskChecksum(driveno) + ")" );
					diskchanged = true;
				}
				
				if (DWProtocolHandler.getDiskDrives().getDirtySectors(driveno) > 0)
				{
					logger.debug("cache for drive " + driveno + " has changed, " + DWProtocolHandler.getDiskDrives().getDirtySectors(driveno) + " dirty sectors");
					
					memchanged = true;
				}
				
				if (memchanged && !diskchanged)
				{
					// mem copy changed, disk did not, just write it out
					DWProtocolHandler.getDiskDrives().syncDisk(driveno);
					
				}
				else if (diskchanged)
				{
					// merge disk with mem
					DWProtocolHandler.getDiskDrives().mergeMemWithDisk(driveno);
					
				}
				
			}
		}
		
	}



	
	

	
}
