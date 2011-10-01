package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;





public class DWDiskDrives 
{

	
	private DWDisk[] diskDrives;
	private static final Logger logger = Logger.getLogger("DWServer.DWDiskDrives");

	private DWProtocolHandler dwProto;
	private int hdbdrive;
	
	public DWDiskDrives(DWProtocolHandler dwProto)
	{
		logger.debug("disk drives init for handler #" + dwProto.getHandlerNo());
		this.dwProto = dwProto;
		this.diskDrives = new DWDisk[getMaxDrives()];
	}
	
	
	@SuppressWarnings("unchecked")
	public void LoadDiskSet(String setname)
	{
		if (DriveWireServer.hasDiskset(setname))
		{
			
			
			synchronized (DriveWireServer.serverconfig)
			{
				dwProto.getConfig().setProperty("CurrentDiskSet", setname);
			}
			
			logger.info("loading diskset '" + setname + "'");
		
			try 
			{
				HierarchicalConfiguration dset = DriveWireServer.getDiskset(setname);
				
				// diskset options
				
				if (dset.containsKey("HDBDOSMode"))
				{
					synchronized (DriveWireServer.serverconfig)
					{
						dwProto.getConfig().setProperty("HDBDOSMode",dset.getString("HDBDOSMode","false"));
					}
				}
				
				if (dset.getBoolean("EjectAllOnLoad",false))
					EjectAllDisks();
				
				
				
				List<HierarchicalConfiguration> disks = dset.configurationsAt("disk");
		    	
				for(HierarchicalConfiguration disk : disks)
				{
				     
					// valid disk?
					if (disk.containsKey("path") && disk.containsKey("drive"))
					{
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
				    
				    
						DWDisk tmpdisk = new DWDisk(this,filepath);
				    
				    
						//	options
			    	
						tmpdisk.setSync(disk.getBoolean("sync",true));
						tmpdisk.setExpand(disk.getBoolean("expand",true));
						tmpdisk.setWriteProtect(disk.getBoolean("writeprotect",false));
						
						tmpdisk.setSizelimit(disk.getInt("sizelimit",-1));
						tmpdisk.setNamedObj(disk.getBoolean("namedobj", false));
						tmpdisk.setSyncFromSource(disk.getBoolean("syncfromsource", false));
						
		    		
						if (disk.containsKey("hdbdiskoffset"))
						{
							tmpdisk.setOffset(disk.getInt("hdbdiskoffset", 0) * 630);
						}
						else
						{
							tmpdisk.setOffset(disk.getInt("offset", 0));
						}
				    
						// 	eject if necessary
						if (this.diskLoaded(disk.getInt("drive")))
		    			{
		    				EjectDisk(disk.getInt("drive"));
		    			}
		    		
						LoadDisk(disk.getInt("drive"), tmpdisk);
				    
			    	
					}
					else
					{
						logger.warn("Invalid disk definition in diskset '" + setname + "'");
					}
			    	
			    	
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
			catch (DWDriveNotLoadedException e) 
			{
				logger.warn("Attempt to eject empty disk?");
			}
		}
		else
		{
			logger.warn("asked to load nonexistent diskset '" + setname + "'");
		}
		
	}
	

	
	public void LoadDiskFromFile(int driveno, String path) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, IOException
	{
		DWDisk tmpdisk = new DWDisk(this,path);
    	
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
		if (dwProto.getConfig().containsKey("CurrentDiskSet"))
		{
			HierarchicalConfiguration diskset = DriveWireServer.getDiskset(dwProto.getConfig().getString("CurrentDiskSet"));
		
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
					diskset.addProperty("disk.syncfromsource", this.diskDrives[driveno].isSyncFromSource());
					diskset.addProperty("disk.namedobj", this.diskDrives[driveno].isNamedObj());
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
		for (int i=0;i<getMaxDrives();i++)
		{
			if (diskDrives[i] != null)
			{
				try 
				{
					EjectDisk(i);
				} 
				catch (DWDriveNotValidException e) 
				{
					logger.warn(e.getMessage());
				} 
				catch (DWDriveNotLoadedException e) 
				{
					logger.warn(e.getMessage());
				}
			}
		}
	}
	
	
	public void seekSector(int driveno, int lsn) throws DWDriveNotLoadedException, DWDriveNotValidException, DWInvalidSectorException, DWSeekPastEndOfDeviceException
	{
		if (dwProto.getConfig().getBoolean("HDBDOSMode",false))
		{
			// every 630 sectors is drive + 1, lsn to remainder
			
			driveno = lsn / 630;
			
			lsn = lsn - (driveno * 630);
			
			logger.debug("HDBDOSMode seek: mapped to drive " + driveno + " sector " + lsn);
			
			this.hdbdrive = driveno;
		}
		
		checkLoadedDriveNo(driveno);

		diskDrives[driveno].seekSector(lsn);
		
		// DriveWireServer.getHandler(this.handlerno).getEventHandler().notifyEvent("seek",driveno+","+lsn);
		
	}
	
	public void writeSector(int driveno, byte[] data) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveWriteProtectedException, IOException
	{
		if (dwProto.getConfig().getBoolean("HDBDOSMode",false))
		{
			driveno = this.hdbdrive;
			logger.debug("HDBDOSMode write: mapped to drive " + driveno );
			
		}
		
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno].writeSector(data);
		
		// DriveWireServer.getHandler(this.handlerno).getEventHandler().notifyEvent("write", driveno +"");
		
	}
	
	public byte[] readSector(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, IOException
	{
		if (dwProto.getConfig().getBoolean("HDBDOSMode",false))
		{
			driveno = this.hdbdrive;
			logger.debug("HDB read: mapped to drive " + driveno );
			
		}
		
		checkLoadedDriveNo(driveno);
		
		// DriveWireServer.getHandler(this.handlerno).getEventHandler().notifyEvent("read",driveno +"");
		
		return(diskDrives[driveno].readSector());
	
	}
	
	
	public void validateDriveNo(int driveno) throws DWDriveNotValidException
	{
		if ((driveno < 0) || (driveno >= getMaxDrives()))
		{
			throw new DWDriveNotValidException("There is no drive " + driveno + ". Valid drives numbers are 0 - "  + (dwProto.getConfig().getInt("DiskMaxDrives", DWDefs.DISK_MAXDRIVES) - 1));
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
		byte[] tmp = new byte[getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)];
		
		for (int i = 0;i<getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE);i++)
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


	public void sync() 
	{
		// scan all loaded drives
		for (int driveno = 0;driveno<getMaxDrives();driveno++)
		{
	
			if (diskLoaded(driveno))
			{
				try 
				{
						
					if (isSync(driveno))
					{
						
						if (isRandomWriteable(driveno))
						{	 

							if (getDirtySectors(driveno) > 0)
							{
								logger.debug("cache for drive " + driveno + " in handler " + this.dwProto.getHandlerNo() + " has changed, " + getDirtySectors(driveno) + " dirty sectors");

									try
									{
										writeDisk(driveno);
									} 
									catch (IOException e)
									{
										logger.error("Lazy write failed: " + e.getMessage());
									} 
									catch (DWDriveNotLoadedException e) 
									{
										logger.error(e.getMessage());
									}
								}
							}

						}
					} 
					catch (DWDriveNotLoadedException e) 
					{
						e.printStackTrace();
					}
			
				}
			}
	
		}


	public int getReadErrors(int driveno) 
	{
		return this.diskDrives[driveno].getReadErrors();
		
	}


	public HierarchicalConfiguration getConfig() 
	{
		return this.dwProto.getConfig();
	}


	public int getMaxDrives() 
	{
		return dwProto.getConfig().getInt("DiskMaxDrives", DWDefs.DISK_MAXDRIVES);
	}


	public int getFreeDriveNo()
	{
		int res = 255;
		
		while (this.diskLoaded(res) && (res > 0))
		{
			res--;
		}
		
		return res;
	}
	
	
	public int nameObjMount(String objname) 
	{
		int result = 0;
		// get a free drive #.. maybe expire old name objects at some point
		int drive = this.getFreeDriveNo();
		
		if (drive > 0)
		{
			String filename = objname;
			// try to find object
			if (dwProto.getConfig().containsKey("NamedObjectDir"))
			{
				filename = dwProto.getConfig().getString("NamedObjectDir") + '/' + filename;
			}
			
			
			try 
			{
				this.LoadDiskFromFile(drive, filename);
				this.diskDrives[drive].setNamedObj(true);
				result = drive;
			} 
			catch (DWDriveNotValidException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (DWDriveAlreadyLoadedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return result;
	}
	
	
	
}
	
