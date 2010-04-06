package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
	
	
	public void LoadDiskSet(String setname)
	{
		if (DriveWireServer.hasDiskset(setname))
		{
		
			EjectAllDisks();
		
			logger.info("loading diskset '" + setname + "'");
		
			try 
			{
				HierarchicalConfiguration dset = DriveWireServer.getDiskset(setname);
				
				List disks = dset.configurationsAt("disk");
		    	
				for(Iterator it = disks.iterator(); it.hasNext();)
				{
				    HierarchicalConfiguration disk = (HierarchicalConfiguration) it.next();
				    
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
				    
				    LoadDiskFromFile(disk.getInt("drive"),filepath);
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
		}
		else
		{
			logger.warn("asked to load nonexistent diskset '" + setname + "'");
		}
		
	}
	
	
	public void saveDiskSet(String filename)
	{
		// save current disk set to file
		
		if (filename != null)
		{
			try 
			{
		     
				FileWriter fstream = new FileWriter(filename);
				BufferedWriter out = new BufferedWriter(fstream);

			
				for (int i = 0;i < MAX_DRIVES;i++)
				{
					if (diskLoaded(i))
					{
						String tstr = i + "," + getDiskFile(i) + ",";
					
						if (getWriteProtect(i))
						{
							tstr = tstr + "1";
						}
						else
						{
							tstr = tstr + "0";
						}
					
						tstr = tstr + "\n";
						
						out.write(tstr);
					}
				}
			
				out.close();
			
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage());
			}
		}
	}
	
	
	public  void LoadDiskFromFile(int driveno, String path) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, IOException
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
			logger.info("loaded disk '" + disk.getFilePath() + "' in drive " + driveno);
			
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
	
	
	public void seekSector(int driveno, int lsn) throws DWDriveNotLoadedException, DWDriveNotValidException
	{
		if (DriveWireServer.getHandler(this.handlerno).config.getBoolean("HDBDOSMode",false))
		{
			// every 630 sectors is drive + 1, lsn to remainder
			
			driveno = lsn / 630;
			
			lsn = lsn - (driveno * 630);
			
			logger.debug("HDB seek: mapped to drive " + driveno + " sector " + lsn);
			
			this.hdbdrive = driveno;
		}
		
		checkLoadedDriveNo(driveno);

		diskDrives[driveno].seekSector(lsn);

	}
	
	public void writeSector(int driveno, byte[] data) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveWriteProtectedException, IOException
	{
		if (DriveWireServer.getHandler(this.handlerno).config.getBoolean("HDBDOSMode",false))
		{
			driveno = this.hdbdrive;
			logger.debug("HDB write: mapped to drive " + driveno );
			
		}
		
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno].writeSector(data);
	}
	
	public byte[] readSector(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, IOException
	{
		if (DriveWireServer.getHandler(this.handlerno).config.getBoolean("HDBDOSMode",false))
		{
			driveno = this.hdbdrive;
			logger.debug("HDB read: mapped to drive " + driveno );
			
		}
		
		checkLoadedDriveNo(driveno);
		
		return(diskDrives[driveno].readSector());
	
	}
	
	
	public void validateDriveNo(int driveno) throws DWDriveNotValidException
	{
		if ((driveno < 0) || (driveno >= MAX_DRIVES))
		{
			throw new DWDriveNotValidException("There is no drive " + driveno + ". Valid drives numbers are 0 - "  + (MAX_DRIVES - 1));
		}
	}
	
	private void checkLoadedDriveNo(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException
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


	public void setWriteProtect(int driveno, boolean onoff) 
	{
		diskDrives[driveno].setWriteProtect(onoff);
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

	
	public void writeDisk(int driveno) throws IOException
	{
		diskDrives[driveno].writeDisk();
	}

	
	public boolean isWriteable(int driveno)
	{
		return(diskDrives[driveno].isWriteable());
	}
	
	public boolean isRandomWriteable(int driveno)
	{
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
	
	
}
