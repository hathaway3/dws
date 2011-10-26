package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetInvalidDiskDefException;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
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
	
	private int diskDriveSerial = -1;
	
	public DWDiskDrives(DWProtocolHandler dwProto)
	{
		logger.debug("disk drives init for handler #" + dwProto.getHandlerNo());
		this.dwProto = dwProto;
		this.diskDrives = new DWDisk[getMaxDrives()];
	}
	
	
	@SuppressWarnings("unchecked")
	public void LoadDiskSet(String setname) throws DWDisksetInvalidDiskDefException, DWDisksetNotValidException, IOException, DWDriveNotValidException, DWDriveNotLoadedException, DWDriveAlreadyLoadedException
	{
	
		logger.info("loading diskset '" + setname + "'");
		HierarchicalConfiguration dset = DriveWireServer.getDiskset(setname);
		
		synchronized (DriveWireServer.serverconfig)
		{
			// causes blank lines in config..
			//dwProto.getConfig().clearProperty("CurrentDiskSet");
			dwProto.getConfig().setProperty("CurrentDiskSet", "");
		}		
	
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
				
				
		// disk defaults
		HierarchicalConfiguration defaults = new HierarchicalConfiguration();
		if (dset.containsKey("diskdefaults"))
			defaults = dset.configurationAt("diskdefaults");
				
		List<HierarchicalConfiguration> disks = dset.configurationsAt("disk");
		    	
		for(HierarchicalConfiguration disk : disks)
		{
				     
			// valid disk definition?
			if (disk.containsKey("path") && disk.containsKey("drive"))
			{
				int driveno =  disk.getInt("drive");
						
				String filepath = new String();
				    
				if (disk.getBoolean("relativepath", defaults.getBoolean("relativepath", false)))
				{
					File curdir = new File(".");

					filepath = curdir.getCanonicalPath() + "/" + disk.getString("path");
				}
				else
				{
					filepath = disk.getString("path"); 
				}
						
				// sectorsize and maxsectors from disk,defaults,instance,dwdef?
				DWDisk tmpdisk = new DWDisk(filepath, disk.getInt("sectorsize", defaults.getInt("sectorsize" , this.getConfig().getInt("DiskSectorSize" , DWDefs.DISK_SECTORSIZE))),  disk.getInt("maxsectors", defaults.getInt("maxsectors", this.getConfig().getInt("DiskMaxSectors", DWDefs.DISK_MAXSECTORS))));
				    
				    
				//	params..
						
				// first set defaults..
				for(Iterator<String> itk = defaults.getKeys(); itk.hasNext();)
				{
					String p = itk.next();
					tmpdisk.setParam(p, defaults.getProperty(p));
				}
						
				// then disk params..
				for(Iterator<String> itk = disk.getKeys(); itk.hasNext();)
				{
					String p = itk.next();
					tmpdisk.setParam(p, disk.getProperty(p));
				}
						
										    		
				// 	eject existing disk if necessary
				if (this.diskLoaded(driveno))
		    	{
		    		EjectDisk(driveno);
		    	}
		    		
				LoadDisk(driveno, tmpdisk, false);
				
			}
			else
			{
				logger.warn("Invalid disk definition in diskset '" + setname + "'");
				throw new DWDisksetInvalidDiskDefException("Invalid disk definition in diskset '" + setname + "'");
			}
		}
		
		synchronized (DriveWireServer.serverconfig)
		{
			dwProto.getConfig().setProperty("CurrentDiskSet", setname);
		}	
	}
	

	
	public void LoadDiskFromFile(int driveno, String path) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, IOException, DWDisksetNotValidException
	{
		DWDisk tmpdisk = new DWDisk(path, this.getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE), this.getConfig().getInt("DiskMaxSectors", DWDefs.DISK_MAXSECTORS));
	    
    	
    	LoadDisk(driveno, tmpdisk, true);
	}
	
	
	public void LoadDisk(int driveno, DWDisk disk, boolean savech) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, DWDisksetNotValidException
	{
		validateDriveNo(driveno);
		
		if (diskDrives[driveno] != null)
		{
			throw new DWDriveAlreadyLoadedException("There is already a disk in drive " + driveno);
		}
		else
		{
			diskDrives[driveno] = disk;

			if (savech)
			{
				updateCurrentDiskSet(driveno);
			}

			logger.info("loaded disk '" + disk.getFilePath() + "' in drive " + driveno);
			incDiskDriveSerial();
		}
	}
	

	private void updateCurrentDiskSet(int driveno) throws DWDisksetNotValidException 
	{
		if (dwProto.getConfig().containsKey("CurrentDiskSet") && !dwProto.getConfig().getString("CurrentDiskSet").equals(""))
		{
			HierarchicalConfiguration diskset = DriveWireServer.getDiskset(dwProto.getConfig().getString("CurrentDiskSet"));
		
			if (diskset.getBoolean("SaveChanges",false))
			{
		
				// 	sync disk set with disk object for drive
		
				logger.debug("updating disk set " + dwProto.getConfig().getString("CurrentDiskSet"));
		
				this.SaveDiskSet(dwProto.getConfig().getString("CurrentDiskSet"));
			}
		}		
	}


	public void ReLoadDisk(int driveno) throws DWDriveNotLoadedException, IOException, DWDriveNotValidException 
	{
		this.validateDriveNo(driveno);
		
		if (diskDrives[driveno] == null)
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
		
		diskDrives[driveno].reload();
		
	}
	

	public void ReLoadAllDisks() throws IOException 
	{
		for (int i=0;i<getMaxDrives();i++)
		{
			if (diskDrives[i] != null)
			{
				diskDrives[i].reload();
			}
		}
	}
	
	
	
	public void EjectDisk(int driveno) throws DWDriveNotValidException, DWDriveNotLoadedException, DWDisksetNotValidException
	{
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno] = null;
		updateCurrentDiskSet(driveno);
		logger.info("ejected disk from drive " + driveno);
		incDiskDriveSerial();
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
				catch (DWDisksetNotValidException e) 
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


	public void setWriteProtect(int driveno, boolean onoff) throws DWDriveNotLoadedException, DWDisksetNotValidException 
	{
		if (diskDrives[driveno] != null)
		{
			diskDrives[driveno].setParam("writeprotect",onoff);
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


	public DWDisk getDisk(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException 
	{
		this.checkLoadedDriveNo(driveno);
		
		return(diskDrives[driveno]);
	}

	
	public void writeDisk(int driveno) throws IOException, DWDriveNotLoadedException, DWDriveNotValidException
	{
		this.checkLoadedDriveNo(driveno);
		diskDrives[driveno].writeDisk();
	}
	

	public void writeDisk(int driveno, String path) throws IOException, DWDriveNotLoadedException, DWDriveNotValidException
	{
		this.checkLoadedDriveNo(driveno);
		diskDrives[driveno].writeDisk(path);
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


	public boolean isSyncTo(int driveno) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return this.diskDrives[driveno].isSyncTo();
			
	}
	
	
	public boolean isExpand(int driveno) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return this.diskDrives[driveno].isExpand();
		
	}


	public void setSyncTo(int driveno, boolean b) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setParam("syncto", b);
	}


	public void setExpand(int driveno, boolean b) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setParam("expand",b);	
	}


	public void setOffset(int driveno, int offset) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setParam("offset", offset);	
		
	}


	public void setLimit(int driveno, int limit) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	

		this.diskDrives[driveno].setParam("sizelimit", limit);
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
						
					if (isSyncTo(driveno))
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
										logger.error("Lazy write failed: " + e.getMessage());
									} 
									catch (DWDriveNotValidException e) 
									{
										logger.error("Lazy write failed: " + e.getMessage());
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
	

	public int getWriteErrors(int driveno) 
	{
		return this.diskDrives[driveno].getWriteErrors();
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
		int res = getMaxDrives() - 1;
		
		while (this.diskLoaded(res) && (res > 0))
		{
			res--;
		}
		
		return res;
	}
	
	
	public int nameObjMount(String objname) 
	{
		int result = 0;
		
		String filename = objname;
		
		if (dwProto.getConfig().containsKey("NamedObjectDir"))
		{
			filename = dwProto.getConfig().getString("NamedObjectDir") + '/' + filename;
		}
		
		// is object loaded..
		
		result = getDriveForNamedObject(filename);
		
		if (result == 0) 
		{
			// get a free drive #.. maybe expire old name objects at some point
			int drive = this.getFreeDriveNo();
		
			if (drive > 0)
			{
				try 
				{
					this.LoadDiskFromFile(drive, filename);
					this.diskDrives[drive].setParam("namedobject", true);
					result = drive;
				} 
				catch (DWDriveNotValidException e) 
				{
					logger.warn(e.getMessage());
				} 
				catch (DWDriveAlreadyLoadedException e) 
				{
					logger.warn(e.getMessage());
				} 
				catch (IOException e) 
				{
					logger.warn(e.getMessage());
				} 
				catch (DWDisksetNotValidException e) 
				{
					logger.warn(e.getMessage());
				}
			}
		}
		
		return result;
	}


	private int getDriveForNamedObject(String filename)
	{
		int res = 0;
		int drv = getMaxDrives() - 1;
		FileObject fileobj = null;
		
		FileSystemManager fsManager;
		try 
		{
			fsManager = VFS.getManager();
			fileobj = fsManager.resolveFile(filename);
		}
		catch (FileSystemException e) 
		{
			drv = 0;
		}
		
		
			
		while ((res == 0) && (drv > 0) && (fileobj != null))
		{
			if (this.diskLoaded(drv))
			{
				if (this.diskDrives[drv].isNamedObject())
				{
					if (this.diskDrives[drv].getFilePath().equals(fileobj.getName().toString()))
					{
						res = drv;
					}
				}	
			}
			drv--;
		}
		
		return res;
	}


	public void incDiskDriveSerial() 
	{
		this.diskDriveSerial++;
	}


	public int getDiskDriveSerial() {
		return diskDriveSerial;
	}


	public void setNamedObj(int driveno, boolean onoff) throws DWDriveNotLoadedException, DWDisksetNotValidException 
	{
		if (diskDrives[driveno] != null)
		{
			diskDrives[driveno].setParam("namedobject", onoff);
			updateCurrentDiskSet(driveno);
		}
		else
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}		
	}
	
	public void setSyncFromSource(int driveno, boolean onoff) throws DWDriveNotLoadedException, DWDisksetNotValidException 
	{
		if (diskDrives[driveno] != null)
		{
			diskDrives[driveno].setParam("syncfrom", onoff);
			updateCurrentDiskSet(driveno);
		}
		else
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}		
	}


	public boolean isNamedObj(int driveno) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return this.diskDrives[driveno].isNamedObject();
	}
	
	public boolean isSyncFromSource(int driveno) throws DWDriveNotLoadedException 
	{
		if (!diskLoaded(driveno))
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);	
		
		return this.diskDrives[driveno].isSyncFrom();
	}

	
	
	public void SaveDiskSet(String src, String dst) throws DWDisksetNotValidException
	{
		HierarchicalConfiguration srcset = DriveWireServer.getDiskset(src);
		
		HierarchicalConfiguration dstset;
		
		// does diskset already exist..
		if (DriveWireServer.hasDiskset(dst))
		{
			// clean out disk info
			dstset = (HierarchicalConfiguration) DriveWireServer.getDiskset(dst);
			dstset.clearTree("disk");
			
		}
		else
		{
			// make a new one
			DriveWireServer.serverconfig.addProperty("diskset(-1).Name", dst);
			dstset = (HierarchicalConfiguration) DriveWireServer.getDiskset(dst);
			
		}
		
		// copy current diskset params, if any

		for(@SuppressWarnings("unchecked")
		Iterator<String> itk = srcset.getKeys(); itk.hasNext();)
		{
			String option = itk.next();
				
			if (!option.startsWith("disk") && !option.equals("Name"))
			{
				if (dstset.containsKey(option))
				{
					dstset.setProperty(option, srcset.getProperty(option));
				}
				else
				{
					dstset.addProperty(option, srcset.getProperty(option));
				}
			}
				
		}
			

		
		// add disks
		
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> disks = srcset.configurationsAt("disk");
		
		for(HierarchicalConfiguration disk : disks)
		{
			
			// add config for this disk
			dstset.addProperty("disk(-1).drive", disk.getInt("drive"));
				
			for(@SuppressWarnings("unchecked")
			Iterator<String> itk = disk.getKeys(); itk.hasNext();)
			{
				String option = itk.next();
					
				if (!option.equals("drive"))
				{
					dstset.addProperty("disk." + option, disk.getProperty(option));
				
				}
			}
		
		}

	}
	
	
	

	@SuppressWarnings("unchecked")
	public void SaveDiskSet(String setname) throws DWDisksetNotValidException 
	{
		HierarchicalConfiguration diskset;
		
		// does diskset already exist..
		if (DriveWireServer.hasDiskset(setname))
		{
			// clean out old def
			diskset = DriveWireServer.getDiskset(setname);
			diskset.clearTree("disk");
		}
		else
		{
			// make a new one
			DriveWireServer.serverconfig.addProperty("diskset(-1).Name", setname);
			diskset = (HierarchicalConfiguration) DriveWireServer.getDiskset(setname);
			
		}
		
		
		// add disks
		for (int driveno = 0;driveno<getMaxDrives();driveno++)
		{
	
			if (this.diskLoaded(driveno))
			{
				// add config for this disk
				
				diskset.addProperty("disk(-1).drive", driveno);
				
				for(Iterator<String> itk = this.diskDrives[driveno].getParams().getKeys(); itk.hasNext();)
				{
					String option = itk.next();
					
					if (!option.equals("drive") && !option.startsWith("_"))
					{
						diskset.addProperty("disk." + option, this.diskDrives[driveno].getParams().getProperty(option));
					}
				}
			}
		}
		

	}


	public void CreateDiskSet(String setname) 
	{
		DriveWireServer.serverconfig.addProperty("diskset(-1).Name", setname);
	}


	public void setDisksetParam(String setname, String param, String value) throws DWDisksetNotValidException 
	{
		HierarchicalConfiguration diskset;
		
		// does diskset already exist..
		if (DriveWireServer.hasDiskset(setname))
		{

			diskset = (HierarchicalConfiguration) DriveWireServer.getDiskset(setname);
			
			if (value == null)
			{
				diskset.clearProperty(param);
			}
			else
			{
				diskset.setProperty(param, value);
			}
		}
		else
		{
			throw new DWDisksetNotValidException("No diskset '" + setname + "' is defined.");
		}
		
	}


	public void clearDisksetDisks(String setname) throws DWDisksetNotValidException 
	{
		HierarchicalConfiguration diskset;
		
		// does diskset already exist..
		if (DriveWireServer.hasDiskset(setname))
		{

			diskset = (HierarchicalConfiguration) DriveWireServer.getDiskset(setname);
			diskset.clearTree("disk");
			
		}
		else
		{
			throw new DWDisksetNotValidException("No diskset '" + setname + "' is defined.");
		}
		
	}

	@SuppressWarnings("unchecked")
	public void clearDisksetDisk(String setname, int driveno) throws DWDisksetNotValidException, DWDisksetDriveNotLoadedException 
	{
		HierarchicalConfiguration diskset;
		
		// does diskset already exist..
		if (DriveWireServer.hasDiskset(setname))
		{

			diskset = (HierarchicalConfiguration) DriveWireServer.getDiskset(setname);
			
			List<HierarchicalConfiguration> disks = diskset.configurationsAt("disk");
	    	
			for(HierarchicalConfiguration disk : disks)
			{
				if (disk.getInt("drive",-1) == driveno)
				{
					disk.clear();
					return;
				}
			}
			
			throw new DWDisksetDriveNotLoadedException("No disk is defined for drive " + driveno + " in set '" + setname + "'.");
		}
		else
		{
			throw new DWDisksetNotValidException("No diskset '" + setname + "' is defined.");
		}	
	}

	public void addDisksetDisk(String setname, int driveno, String path) throws DWDisksetNotValidException 
	{
		HierarchicalConfiguration diskset;
		
		// does diskset already exist..
		if (DriveWireServer.hasDiskset(setname))
		{

			diskset = (HierarchicalConfiguration) DriveWireServer.getDiskset(setname);
			
			diskset.addProperty("disk(-1).drive", driveno);
			diskset.addProperty("disk.path", path);
			
			
		}
		else
		{
			throw new DWDisksetNotValidException("No diskset '" + setname + "' is defined.");
		}	
	}


	public boolean isDiskNo(String arg) 
	{
		try
		{
			int diskno = Integer.parseInt(arg);
			this.validateDriveNo(diskno);
		}
		catch (NumberFormatException e)
		{
			return false;
		} 
		catch (DWDriveNotValidException e) 
		{
			return false;
		}
		
		return true;
	}

	public boolean isDiskSetName(String arg) 
	{
		return( DriveWireServer.hasDiskset(arg) );
	}


	
	
}
	
