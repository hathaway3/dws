package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDisksetNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;


public class DWDiskDrives 
{

	
	private DWDisk[] diskDrives;
	private static final Logger logger = Logger.getLogger("DWServer.DWDiskDrives");

	private DWProtocolHandler dwProto;
	private int hdbdrive;
	
	private int diskDriveSerial = -1;
	private FileSystemManager fsManager;
	
	public DWDiskDrives(DWProtocolHandler dwProto)
	{
		logger.debug("disk drives init for handler #" + dwProto.getHandlerNo());
		this.dwProto = dwProto;
		this.diskDrives = new DWDisk[getMaxDrives()];
	}
	

	public DWDisk getDisk(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException 
	{
		// HDBDOSMode means ignore driveno, use LSN/630
		if (dwProto.getConfig().getBoolean("HDBDOSMode",false))
		{
			driveno = this.hdbdrive;
			logger.debug("HDBDOSMode write: mapped to drive " + driveno );
			
		}
		
		// validate drive number
		if (!isDriveNo(driveno))
		{
			throw new DWDriveNotValidException("There is no drive " + driveno + ". Valid drives numbers are 0 - "  + (dwProto.getConfig().getInt("DiskMaxDrives", DWDefs.DISK_MAXDRIVES) - 1));
		}
		
		// make sure we have a disk
		if (!isLoaded(driveno))
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
		
		return(diskDrives[driveno]);
	}
	
	
	
	public void writeSector(int driveno, byte[] data) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveWriteProtectedException, IOException
	{
		getDisk(driveno).writeSector(data);
	}
	
	public byte[] readSector(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, IOException, DWImageFormatException
	{
		return(getDisk(driveno).readSector());
	}
	
	
	public void seekSector(int driveno, int lsn) throws DWDriveNotLoadedException, DWDriveNotValidException, DWInvalidSectorException, DWSeekPastEndOfDeviceException
	{
		if (dwProto.getConfig().getBoolean("HDBDOSMode",false))
		{
			// every 630 sectors is drive, lsn to remainder
			
			driveno = lsn / 630;
			
			lsn = lsn - (driveno * 630);
			
			logger.debug("HDBDOSMode seek: mapped to drive " + driveno + " sector " + lsn);
			
			this.hdbdrive = driveno;
		}

		getDisk(driveno).seekSector(lsn);
	}
	
	
	
	public boolean isDriveNo(int driveno) 
	{
		if ((driveno < 0) || (driveno >= getMaxDrives()))
		{
			return false;
		}
		
		return true;
	}
	
	
	
	public boolean isLoaded(int driveno)
	{
		if (this.diskDrives[driveno] == null)
			return false;
		
		return true;
	}
	
	
	public void ReLoadDisk(int driveno) throws DWDriveNotLoadedException, IOException, DWDriveNotValidException, DWImageFormatException 
	{
		getDisk(driveno).reload();
	}
	
	
	public void ReLoadAllDisks() throws IOException, DWImageFormatException 
	{
		for (int i=0;i<getMaxDrives();i++)
		{
				try
				{
					if (isLoaded(i))
						getDisk(i).reload();
				} 
				catch (DWDriveNotLoadedException e)
				{
					logger.warn(e.getMessage());
				} 
				catch (DWDriveNotValidException e)
				{
					logger.warn(e.getMessage());
				}
		}
	}
	
	
	public void EjectDisk(int driveno) throws DWDriveNotValidException, DWDriveNotLoadedException, DWDisksetNotValidException
	{
		getDisk(driveno).eject();
		
		diskDrives[driveno] = null;
		
		logger.info("ejected disk from drive " + driveno);
		incDiskDriveSerial();
	}
	
	
	public void EjectAllDisks()
	{
		for (int i=0;i<getMaxDrives();i++)
		{
			if (isLoaded(i))
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
	
	
	public void writeDisk(int driveno) throws IOException, DWDriveNotLoadedException, DWDriveNotValidException
	{
		getDisk(driveno).write();
	}
	

	public void writeDisk(int driveno, String path) throws IOException, DWDriveNotLoadedException, DWDriveNotValidException
	{
		getDisk(driveno).writeTo(path);
	}
	
	
	


	public void LoadDiskFromFile(int driveno, String path) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, IOException, DWDisksetNotValidException, DWImageFormatException
	{
		// Determine what kind of disk we have
		
		this.fsManager = VFS.getManager();
		
		try
		{
			FileObject fileobj = fsManager.resolveFile(path);
			
			if (fileobj.exists() && fileobj.isReadable())
			{
				
				Hashtable<Integer,Integer> votes = new Hashtable<Integer, Integer>();
				
				votes.put(DWDefs.DISK_FORMAT_DMK, DWDMKDisk.considerImage(fileobj));
				votes.put(DWDefs.DISK_FORMAT_RAW, DWRawDisk.considerImage(fileobj));
				votes.put(DWDefs.DISK_FORMAT_VDK, DWVDKDisk.considerImage(fileobj));
				votes.put(DWDefs.DISK_FORMAT_JVC, DWJVCDisk.considerImage(fileobj));
				
				int format = getBestFormat(votes);
				
				switch(format)
				{
					case DWDefs.DISK_FORMAT_DMK:
						this.LoadDisk(driveno, new DWDMKDisk(this.dwProto, fileobj));
						break;
						
					case DWDefs.DISK_FORMAT_VDK:
						this.LoadDisk(driveno, new DWVDKDisk(this.dwProto, fileobj));
						break;
						
					case DWDefs.DISK_FORMAT_JVC:
						this.LoadDisk(driveno, new DWJVCDisk(this.dwProto, fileobj));
						break;
					
					case DWDefs.DISK_FORMAT_RAW:
						this.LoadDisk(driveno, new DWRawDisk(this.dwProto, fileobj, DWDefs.DISK_SECTORSIZE , DWDefs.DISK_MAXSECTORS));
						break;
						
						
					default:
						// this.LoadDisk(driveno, new DWRawDisk(this.dwProto, fileobj, DWDefs.DISK_SECTORSIZE , DWDefs.DISK_MAXSECTORS));
						
						throw new DWImageFormatException("Unsupported image format");
						
				}
								
			}
			else
			{
				logger.error("Unreadable path '" + path + "'");
				throw new IOException("Unreadable path");
			}
		}
		catch (org.apache.commons.vfs.FileSystemException e)
		{
			logger.error("FileSystemException: " + e.getMessage());
			throw new IOException(e.getMessage());
		}
		

	}
	
	
	private int getBestFormat(Hashtable<Integer, Integer> votes) throws DWImageFormatException
	{
		// who wants it.. lets get silly playing with java collections
		
		int res = DWDefs.DISK_FORMAT_NONE;
		
		// yes
		if (votes.containsValue(DWDefs.DISK_CONSIDER_YES))
		{
			if (Collections.frequency(votes.values(), DWDefs.DISK_CONSIDER_YES) > 1)
			{
				throw new DWImageFormatException("Multiple formats claim this image?");
			}
			else
			{
				// a single yes vote.. we are good to go
				for (Entry<Integer,Integer> entry : votes.entrySet() ) 
				{
			        if (entry.getValue().equals(DWDefs.DISK_CONSIDER_YES)) 
			        {
			            return entry.getKey();
			        }
			    }
			}
		}
		// maybe
		else if (votes.containsValue(DWDefs.DISK_CONSIDER_MAYBE))
		{
			if (Collections.frequency(votes.values(), DWDefs.DISK_CONSIDER_MAYBE) > 1)
			{
				// TODO maybe something better here..
				throw new DWImageFormatException("Multiple formats might read this image?");
			}
			else
			{
				// a single maybe vote.. we are good to go
				for (Entry<Integer,Integer> entry : votes.entrySet() ) 
				{
			        if (entry.getValue().equals(DWDefs.DISK_CONSIDER_MAYBE)) 
			        {
			            return entry.getKey();
			        }
			    }
			}
		}
		
		
		return res;
	}


	public void LoadDisk(int driveno, DWDisk disk) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, DWDisksetNotValidException
	{
//	 	eject existing disk if necessary
		if (this.isLoaded(driveno))
    	{
    		try {
				EjectDisk(driveno);
			} catch (DWDriveNotLoadedException e) 
			{
				logger.warn("bug - not loaded exception when loaded is true?");
			}
    	}
		
		// put into use
		disk.setDiskNo(driveno);
		diskDrives[driveno] = disk;

		logger.info("loaded disk '" + disk.getFilePath() + "' in drive " + driveno);
		incDiskDriveSerial();
		
	}
	





	




	public byte[] nullSector() 
	{
		byte[] tmp = new byte[dwProto.getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)];
		
		for (int i = 0;i<dwProto.getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE);i++)
			tmp[i] = (byte) 0;
		
		return(tmp);
	}


	
	








	

	
	
	


	public void shutdown()
	{
		logger.debug("shutting down");
		
		// sync all disks
		sync();
		
	}


	

	public void sync() 
	{
		// sync all loaded drives
		for (int driveno = 0;driveno<getMaxDrives();driveno++)
		{
			if (isLoaded(driveno))
			{
				try
				{
					getDisk(driveno).sync();
				} 
				catch (DWDriveNotLoadedException e)
				{
					logger.warn(e.getMessage());
				} 
				catch (DWDriveNotValidException e)
				{
					logger.warn(e.getMessage());
				} 
				catch (IOException e)
				{
					logger.warn(e.getMessage());
				}
			}
				
		}
	}


	public int getMaxDrives() 
	{
		return dwProto.getConfig().getInt("DiskMaxDrives", DWDefs.DISK_MAXDRIVES);
	}


	public int getFreeDriveNo()
	{
		int res = getMaxDrives() - 1;
		
		while (isLoaded(res) && (res > 0))
		{
			res--;
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


	public int getDriveNoFromString(String str) throws DWDriveNotValidException
	{
		int res = -1;
		
		try
		{
			res = Integer.parseInt(str);
		}
		catch (NumberFormatException e)
		{
			throw new DWDriveNotValidException("Drive numbers must be numeric");
		}
		
		this.isDriveNo(res);
		
		return(res);
	}


	
	



	
	
	
	

	
	
}
	
