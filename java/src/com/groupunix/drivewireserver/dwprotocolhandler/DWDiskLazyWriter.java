package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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
				// check for dirtys
				if (DWProtocolHandler.getDiskDrives().getDirtySectors(driveno) > 0)
				{
					// write dirty sectors to dsk file
					logger.debug("drive " + driveno + " has " + DWProtocolHandler.getDiskDrives().getDirtySectors(driveno) + " dirty sectors");
					
					syncDisk(driveno);
					
				}
			}
		}
		
	}


	private void syncDisk(int driveno) 
	{
		
		try 
		{
			RandomAccessFile raf = new RandomAccessFile(DWProtocolHandler.getDiskDrives().getDiskFile(driveno), "rw");
			
			for (int i = 0;i<DWDisk.MAX_SECTORS;i++)
			{
				if (DWProtocolHandler.getDiskDrives().getDisk(driveno).getSector(i) != null)
				{
					if (DWProtocolHandler.getDiskDrives().getDisk(driveno).getSector(i).isDirty())
					{
						long pos = i * 256;
						raf.seek(pos);
						raf.write(DWProtocolHandler.getDiskDrives().getDisk(driveno).getSector(i).getData());
						logger.debug("wrote sector " + i + " in " + DWProtocolHandler.getDiskDrives().getDisk(driveno).getFilePath() );
						DWProtocolHandler.getDiskDrives().getDisk(driveno).getSector(i).setDirty(false);
					}
				}
			}
			
			raf.close();
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
