package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;



public class DWDiskDrives 
{
	public static final int MAX_DRIVES = 256;
	
	private DWDisk[] diskDrives = new DWDisk[MAX_DRIVES];
	private static final Logger logger = Logger.getLogger("DWServer.DWDiskDrives");

	private int handlerno;
	private int hdbdrive;
	
	public DWDiskDrives(int hno)
	{
		logger.debug("disk drives init for handler #" + hno);
		this.handlerno = hno;
	}
	
	
	@SuppressWarnings("unchecked")
	public void LoadDiskSet(String setname)
	{
		if (DriveWireServer.hasDiskset(setname))
		{
			
			EjectAllDisks();
		
			DriveWireServer.getHandler(handlerno).config.setProperty("CurrentDiskSet", setname);
			
			logger.info("loading diskset '" + setname + "'");
		
			try 
			{
				HierarchicalConfiguration dset = DriveWireServer.getDiskset(setname);
				
				List<HierarchicalConfiguration> disks = dset.configurationsAt("disk");
		    	
				for(Iterator<HierarchicalConfiguration> it = disks.iterator(); it.hasNext();)
				{
				    HierarchicalConfiguration disk = it.next();
				    
				    String filepath = new String();
				    
				    if (disk.getBoolean("relativepath",false))
				    {
			    	   File curdir = new File(".");

			    	   filepath = curdir.getCanonicalPath() + "/" + disk.getString("path");
				    }
				    else
				    {
				    	filepath = disk.getString("path"); 
				    }
				    
				    
				    DWDisk tmpdisk = new DWDisk(filepath);
				    
				    
				    //	options
			    	
		    		tmpdisk.setSync(disk.getBoolean("sync",true));
			    	tmpdisk.setExpand(disk.getBoolean("expand",true));
		    		tmpdisk.setWriteProtect(disk.getBoolean("writeprotect",false));
				    
		    		tmpdisk.setSizelimit(disk.getInt("sizelimit",-1));
		    		
		    		if (disk.containsKey("hdbdiskoffset"))
		    		{
		    			tmpdisk.setOffset(disk.getInt("hdbdiskoffset", 0) * 630);
		    		}
		    		else
		    		{
		    			tmpdisk.setOffset(disk.getInt("offset", 0));
		    		}
				    
				    
			    	LoadDisk(disk.getInt("drive"), tmpdisk);
				    
			    	
			    	
			    	
			    	
				}
				
			}
			catch (DWDriveNotValidException e3) 
			{
				logger.error("DriveNotValid: " + e3.getMessage());
			} 
			catch (DWDriveAlreadyLoadedException e4) 
			{
				logger.error("DriveAlreadyLoaded: " + e4.getMessage());
			} 
			catch (IOException e) 
			{
				logger.error("IO error: " + e.getMessage());
			}
			catch (IllegalArgumentException e)
			{
				logger.error("IllegalArgumentException: " + e.getMessage());
			}
		}
		else
		{
			logger.warn("asked to load nonexistent diskset '" + setname + "'");
		}
		
	}
	

	
	public void LoadDiskFromFile(int driveno, String path) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, IOException
	{
		DWDisk tmpdisk = new DWDisk(path);
    	
    	LoadDisk(driveno, tmpdisk);
	}
	
	
	public void LoadDisk(int driveno, DWDisk disk) throws DWDriveNotValidException, DWDriveAlreadyLoadedException
	{
		validateDriveNo(driveno);
		
		if (diskDrives[driveno] != null)
		{
			throw new DWDriveAlreadyLoadedException("There is already a disk in drive " + driveno);
		}
		else
		{
			diskDrives[driveno] = disk;

			updateCurrentDiskSet(driveno);

			logger.info("loaded disk '" + disk.getFilePath() + "' in drive " + driveno);
			
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private void updateCurrentDiskSet(int driveno) 
	{
		if (DriveWireServer.getHandler(this.handlerno).config.containsKey("CurrentDiskSet"))
		{
			HierarchicalConfiguration diskset = DriveWireServer.getDiskset(DriveWireServer.getHandler(handlerno).config.getString("CurrentDiskSet"));
		
			if (diskset.getBoolean("SaveChanges",false))
			{
		
				// 	sync disk set with disk object for drive
		
				logger.debug("updating drive " + driveno + " in disk set " + diskset.getString("Name","Noname"));
		
				// 	clear old config
		
				List<HierarchicalConfiguration> disks = diskset.configurationsAt("disk");
		
				for (int i = disks.size()-1;i>-1;i--)
				{
					if (diskset.configurationAt("disk(" + i + ")").getInt("drive") == driveno)
						diskset.configurationAt("disk(" + i + ")").clear();
				}
		
				if (this.diskLoaded(driveno))
				{
					// add config for this disk
				
					diskset.addProperty("disk(-1).drive", driveno);
					diskset.addProperty("disk.path", this.diskDrives[driveno].getFilePath());
					diskset.addProperty("disk.writeprotect", this.diskDrives[driveno].getWriteProtect());
					diskset.addProperty("disk.sync", this.diskDrives[driveno].isSync());
					diskset.addProperty("disk.expand", this.diskDrives[driveno].isExpand());
					diskset.addProperty("disk.sizelimit", this.diskDrives[driveno].getSizelimit());
					diskset.addProperty("disk.offset", this.diskDrives[driveno].getOffset());
				}
			}
		}		
	}


	public void ReLoadDisk(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveAlreadyLoadedException, IOException
	{
		if (diskDrives[driveno] == null)
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
		
		String filename = diskDrives[driveno].getFilePath();
		
		EjectDisk(driveno);
		LoadDiskFromFile(driveno,filename);
		
	}
	
	
	public void EjectDisk(int driveno) throws DWDriveNotValidException, DWDriveNotLoadedException
	{
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno] = null;
		updateCurrentDiskSet(driveno);
		logger.info("ejected disk from drive " + driveno);
	}
	
	public void EjectAllDisks()
	{
		for (int i=0;i<MAX_DRIVES;i++)
		{
			if (diskDrives[i] != null)
			{
				try 
				{
					EjectDisk(i);
				} 
				catch (DWDriveNotValidException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (DWDriveNotLoadedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void seekSector(int driveno, int lsn) throws DWDriveNotLoadedException, DWDriveNotValidException, DWInvalidSectorException, DWSeekPastEndOfDeviceException
	{
		if (DriveWireServer.getHandler(this.handlerno).config.getBoolean("HDBDOSMode",false))
		{
			// every 630 sectors is drive + 1, lsn to remainder
			
			driveno = lsn / 630;
			
			lsn = lsn - (driveno * 630);
			
			logger.debug("HDBDOSMode seek: mapped to drive " + driveno + " sector " + lsn);
			
			this.hdbdrive = driveno;
		}
		
		checkLoadedDriveNo(driveno);

		diskDrives[driveno].seekSector(lsn);
		
		DriveWireServer.getHandler(this.handlerno).getEventHandler().notifyEvent("seek",driveno+","+lsn);
		
	}
	
	public void writeSector(int driveno, byte[] data) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveWriteProtectedException, IOException
	{
		if (DriveWireServer.getHandler(this.handlerno).config.getBoolean("HDBDOSMode",false))
		{
			driveno = this.hdbdrive;
			logger.debug("HDBDOSMode write: mapped to drive " + driveno );
			
		}
		
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno].writeSector(data);
		
		DriveWireServer.getHandler(this.handlerno).getEventHandler().notifyEvent("write", driveno +"");
		
	}
	
	public byte[] readSector(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, IOException
	{
		if (DriveWireServer.getHandler(this.handlerno).config.getBoolean("HDBDOSMode",false))
		{
			driveno = this.hdbdrive;
			logger.debug("HDB read: mapped to drive " + driveno );
			
		}
		
		checkLoadedDriveNo(driveno);
		
		DriveWireServer.getHandler(this.handlerno).getEventHandler().notifyEvent("read",driveno +"");
		
		return(diskDrives[driveno].readSector());
	
	}
	
	
	public void validateDriveNo(int driveno) throws DWDriveNotValidException
	{
		if ((driveno < 0) || (driveno >= MAX_DRIVES))
		{
			throw new DWDriveNotValidException("There is no drive " + driveno + ". Valid drives numbers are 0 - "  + (MAX_DRIVES - 1));
		}
	}
	
	public void checkLoadedDriveNo(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException
	{
		validateDriveNo(driveno);
		
		if (diskDrives[driveno] == null)
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
	}


	public boolean diskLoaded(int i) 
	{
		if (diskDrives[i] == null)
		{
			return false;
		}
		return true;
	}


	public String getDiskFile(int i) 
	{
		if (diskDrives[i] != null)
		{
			return(diskDrives[i].getFilePath());
		}
		return null;
	}


	public boolean getWriteProtect(int i) {
		if (diskDrives[i] != null)
		{
			return(diskDrives[i].getWriteProtect());
		}
		return false;
	}


	public byte[] nullSector() 
	{
		byte[] tmp = new byte[256];
		
		for (int i = 0;i<256;i++)
			tmp[i] = (byte) 0;
		
		return(tmp);
	}


	public void setWriteProtect(int driveno, boolean onoff) throws DWDriveNotLoadedException 
	{
		if (diskDrives[driveno] != null)
		{
			diskDrives[driveno].setWriteProtect(onoff);
			updateCurrentDiskSet(driveno);
		}
		else
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
	}
	
	public String getDiskName(int driveno)
	{
		
		return(diskDrives[driveno].getDiskName());
	}
	
	public int getDiskSectors(int driveno)
	{
		return(diskDrives[driveno].getDiskSectors());
	}


	public int getLSN(int i) 
	{
		return(diskDrives[i].getLSN());
	}


	public int getReads(int i) 
	{
		return(diskDrives[i].getReads());
	}


	public int getWrites(int i) 
	{
		return(diskDrives[i].getWrites());
	}


	public int getDirtySectors(int i) 
	{
		return(diskDrives[i].getDirtySectors());
	}


	public DWDisk getDisk(int driveno) 
	{
		return(diskDrives[driveno]);
	}

	
	public void writeDisk(int driveno) throws IOException, DWDriveNotLoadedException
	{
		if (diskLoaded(driveno))
		{
			diskDrives[driveno].writeDisk();
		}
		else
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
	}
	

	public void writeDisk(int driveno, String path) throws IOException, DWDriveNotLoadedException
	{
		if (diskLoaded(driveno))
		{
			diskDrives[driveno].writeDisk(path);
		}
		else
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
		
	}
	
	
	public boolean isWriteable(int driveno) throws DWDriveNotLoadedException
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return(diskDrives[driveno].isWriteable());
	}
	
	public boolean isRandomWriteable(int driveno) throws DWDriveNotLoadedException
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return(diskDrives[driveno].isRandomWriteable());
	}


	public void shutdown()
	{
		logger.debug("shutting down");
		
		// sync all disks
		for (int i = 0;i<this.diskDrives.length;i++)
		{
			if (this.diskDrives[i] != null)
			{
				this.diskDrives[i].shutdown();
			}
		}
		
	}


	public boolean isSync(int driveno) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return this.diskDrives[driveno].isSync();
			
	}
	
	
	public boolean isExpand(int driveno) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return this.diskDrives[driveno].isExpand();
		
	}


	public void setSync(int driveno, boolean b) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setSync(b);
	}


	public void setExpand(int driveno, boolean b) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setExpand(b);	
	}


	public void setOffset(int driveno, int offset) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setOffset(offset);	
		
	}


	public void setLimit(int driveno, int limit) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setSizelimit(limit);
	}
	
}
