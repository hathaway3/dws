package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;

public class SyncThread implements Runnable 
{
	private int diskserial = -1;
	private boolean noconn = true;
	
	


	public void run() 
	{
		DiskStatus dstat;
		
		while(MainWin.config.getBoolean("ServerSync",true) && !((MainWin.shell != null) && MainWin.shell.isDisposed()))
		{
			// are we ready to look at disks
			if (MainWin.getInstance() > -1)
			{
			
				try 
				{
					DiskDef dd = MainWin.getCurrentDisk();
				
					// do we have a disk selected for viewing
					if ((dd != null) && (dd.getDrive() > -1))
					{	
						// yep get status
						dstat = UIUtils.getDiskStatus(MainWin.getInstance(), dd.getDrive());
						
						// update disk view
						MainWin.getCurrentDisk().setLoaded(dstat.getLoaded());
						MainWin.getCurrentDisk().setDirty(dstat.getDirty());
						MainWin.getCurrentDisk().setSectors(dstat.getSectors());
						MainWin.getCurrentDisk().setLsn(dstat.getLsn());
						MainWin.getCurrentDisk().setReads(dstat.getReads());
						MainWin.getCurrentDisk().setWrites(dstat.getWrites());
						
						MainWin.displayCurrentDiskAsync();
					}
					else
					{
						// nope just get serial
						dstat = new DiskStatus();
						
						ArrayList<String> res = UIUtils.loadArrayList(MainWin.getInstance(), "ui instance disk serial");
						if (res.size() != 1)
						{
							throw new DWUIOperationFailedException("invalid response size: " + res.size());
						}
						
						try
						{
							dstat.setSerial(Integer.parseInt(res.get(0)));
						}
						catch (NumberFormatException e)
						{
							throw new DWUIOperationFailedException("invalid response: " + res.get(0));
						}
					}
					
					// if we get this far, we're talking to server just fine
					if (this.noconn == true)
					{
						log("connected to DW server");
					}
					this.noconn = false;
					
					// has a disk changed?
					if (dstat.getSerial() != this.diskserial)
					{
							//log("disk serial change: " + dstat.getSerial());
							this.diskserial = dstat.getSerial();
							MainWin.refreshDiskTableAsync();
					}
					
				}
				catch (java.net.ConnectException e)
				{
					if (!this.noconn)
					{
						log("lost connection to DW server");
					}
					this.noconn = true;
				}
				catch (IOException e1) 
				{
					log(e1.getMessage());
				} 
				catch (DWUIOperationFailedException e1) 
				{
					log(e1.getMessage());
				} 

			}
			
			// sleep
			try 
			{
				Thread.sleep(MainWin.config.getLong("ServerSyncInterval", 2000));
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		log("die");
	}




	private void log(String txt) 
	{
		try
		{
			MainWin.addToDisplay("sync: " + txt);
		}
		catch (java.lang.NullPointerException e)
		{
			System.out.println(txt);
		}
	}

}
