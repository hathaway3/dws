package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DECBDefs;
import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWImageHasNoSourceException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;


public class DWDiskDrives 
{

	
	private DWDiskDrive[] diskDrives;
	private static final Logger logger = Logger.getLogger("DWServer.DWDiskDrives");

	private DWProtocolHandler dwProto;
	private int hdbdrive;
	
	private int diskDriveSerial = -1;
	private FileSystemManager fsManager;
	
	public DWDiskDrives(DWProtocolHandler dwProto)
	{
		logger.debug("disk drives init for handler #" + dwProto.getHandlerNo());
		this.dwProto = dwProto;
		this.diskDrives = new DWDiskDrive[getMaxDrives()];
		
		for (int i=0;i<getMaxDrives();i++)
		{
			this.diskDrives[i] = new DWDiskDrive(this,i);
			
			if (dwProto.getConfig().getBoolean("RestoreDrivePaths", true) && (dwProto.getConfig().getString("Drive"+i+"Path",null) != null))
			{
				try
				{
					logger.debug("Restoring drive " + i + " from " + dwProto.getConfig().getString("Drive"+i+"Path"));
					this.LoadDiskFromFile(i, dwProto.getConfig().getString("Drive"+i+"Path"));
				} 
				catch (DWDriveNotValidException e)
				{
					logger.warn("Restoring drive " + i + ": " + e.getMessage());
				} 
				catch (DWDriveAlreadyLoadedException e)
				{
					logger.warn("Restoring drive " + i + ": " + e.getMessage());
				} 
				catch (IOException e)
				{
					logger.warn("Restoring drive " + i + ": " + e.getMessage());
				} 
				catch (DWImageFormatException e)
				{
					logger.warn("Restoring drive " + i + ": " + e.getMessage());
				}
			}
		}
		
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
		
		
		return(diskDrives[driveno].getDisk());
	}
	
	
	
	public void writeSector(int driveno, byte[] data) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveWriteProtectedException, IOException
	{
		this.diskDrives[driveno].writeSector(data);
	}
	
	public byte[] readSector(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, IOException, DWImageFormatException
	{
		return(this.diskDrives[driveno].readSector());
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

		this.diskDrives[driveno].seekSector(lsn);
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
		return(this.diskDrives[driveno].isLoaded());
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
	
	
	public void EjectDisk(int driveno) throws DWDriveNotValidException, DWDriveNotLoadedException
	{
		diskDrives[driveno].eject();
		
		if (dwProto.getConfig().getBoolean("SaveDrivePaths", true))
			dwProto.getConfig().setProperty("Drive" + driveno + "Path", null);
		
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
				
			}
		}
	}
	
	
	public void writeDisk(int driveno) throws IOException, DWDriveNotLoadedException, DWDriveNotValidException, DWImageHasNoSourceException
	{
		getDisk(driveno).write();
	}
	

	public void writeDisk(int driveno, String path) throws IOException, DWDriveNotLoadedException, DWDriveNotValidException
	{
		getDisk(driveno).writeTo(path);
	}
	
	
	


	public void LoadDiskFromFile(int driveno, String path) throws DWDriveNotValidException, DWDriveAlreadyLoadedException, IOException, DWImageFormatException
	{
		// Determine what kind of disk we have
		
		this.fsManager = VFS.getManager();
		
		try
		{
			FileObject fileobj = fsManager.resolveFile(path);
			
			if (fileobj.exists() && fileobj.isReadable())
			{
				
				FileContent fc = fileobj.getContent();
				long fobjsize = fc.getSize();
				
				// size check
				if (fobjsize > Integer.MAX_VALUE)
					throw new DWImageFormatException("Image too big, maximum size is " + Integer.MAX_VALUE + " bytes.");
				
				// get header
				int hdrsize = (int) Math.min(DWDefs.DISK_IMAGE_HEADER_SIZE, fobjsize);
				
				byte[] header = new byte[hdrsize];
				
				if (hdrsize > 0)
				{
					int readres = 0;
					InputStream fis = fc.getInputStream();
					
					while (readres < hdrsize)
				    	readres += fis.read(header, readres, hdrsize - readres);
					
					fis.close();
				}
				
				
				// collect votes
				Hashtable<Integer,Integer> votes = new Hashtable<Integer, Integer>();
				
				votes.put(DWDefs.DISK_FORMAT_DMK, DWDMKDisk.considerImage(header, fobjsize));
				votes.put(DWDefs.DISK_FORMAT_RAW, DWRawDisk.considerImage(header, fobjsize));
				votes.put(DWDefs.DISK_FORMAT_VDK, DWVDKDisk.considerImage(header, fobjsize));
				votes.put(DWDefs.DISK_FORMAT_JVC, DWJVCDisk.considerImage(header, fobjsize));
				votes.put(DWDefs.DISK_FORMAT_CCB, DWCCBDisk.considerImage(header, fobjsize));
				
				int format = getBestFormat(votes);
				
				switch(format)
				{
					case DWDefs.DISK_FORMAT_DMK:
						logger.debug("trying dmk image load");
						this.LoadDisk(driveno, new DWDMKDisk(fileobj));
						break;
						
					case DWDefs.DISK_FORMAT_VDK:
						logger.debug("trying vdk image load");
						this.LoadDisk(driveno, new DWVDKDisk(fileobj));
						break;
						
					case DWDefs.DISK_FORMAT_JVC:
						logger.debug("trying jvc image load");
						this.LoadDisk(driveno, new DWJVCDisk(fileobj));
						break;
						
					case DWDefs.DISK_FORMAT_CCB:
						logger.debug("trying ccb image load");
						this.LoadDisk(driveno, new DWCCBDisk(fileobj));
						break;
					
					case DWDefs.DISK_FORMAT_RAW:
						this.LoadDisk(driveno, new DWRawDisk(fileobj, DWDefs.DISK_SECTORSIZE , DWDefs.DISK_MAXSECTORS));
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


	public void LoadDisk(int driveno, DWDisk disk) throws DWDriveNotValidException, DWDriveAlreadyLoadedException
	{
		//	 eject existing disk if necessary
		if (this.isLoaded(driveno))
    	{
			try
			{
				this.diskDrives[driveno].eject();
			} 
			catch (DWDriveNotLoadedException e)
			{
				logger.warn("Loaded but not loaded.. well what is this about then?");
			}
    	}
		
		// put into use
		diskDrives[driveno].insert(disk);
		
		if (dwProto.getConfig().getBoolean("SaveDrivePaths", true))
			dwProto.getConfig().setProperty("Drive" + driveno + "Path", disk.getFilePath());
		
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


	public void submitEvent(int driveno, String key, String val)
	{
		DriveWireServer.submitDiskEvent(this.dwProto.getHandlerNo(), driveno, key, val);
		
	}


	public HierarchicalConfiguration getConfig()
	{
		return this.dwProto.getConfig();
		
	}


	public void createDisk(int driveno) throws DWDriveAlreadyLoadedException
	{
		if (this.isLoaded(driveno))
			throw (new DWDriveAlreadyLoadedException("Already a disk in drive " + driveno));
		
		this.diskDrives[driveno].insert(new DWRawDisk(DWDefs.DISK_SECTORSIZE, DWDefs.DISK_MAXSECTORS));
	}


	public void formatDOSFS(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, DWInvalidSectorException, DWSeekPastEndOfDeviceException, DWDriveWriteProtectedException, IOException
	{
		if (!this.isLoaded(driveno))
			throw (new DWDriveNotLoadedException("No disk in drive " + driveno));
		
		DWDECBFileSystem.format(this.getDisk(driveno));
		
	}
	


	public static int getDiskFSType(Vector<DWDiskSector> sectors)
	{
		
		if (!sectors.isEmpty())
		{
			// OS9 ?
			if ((sectors.get(0).getData()[3] == 18) && (sectors.get(0).getData()[73] == 18) && (sectors.get(0).getData()[75] == 18))
			{
				return(DWDefs.DISK_FILESYSTEM_OS9);
			}
			
			// LWFS
			byte[] lwfs = new byte[4];
			System.arraycopy( sectors.get(0).getData(), 0, lwfs, 0, 4 );
			
			if (new String(lwfs).equals("LWFS") || new String(lwfs).equals("LW16"))
			{
				return(DWDefs.DISK_FILESYSTEM_LWFS);
			}
			
			// TODO - outdated? cocoboot isave
			if (sectors.get(0).getData()[0] == (byte) 'f' && sectors.get(0).getData()[1] == (byte) 'c')
			{
				return(DWDefs.DISK_FILESYSTEM_CCB);
			}
			
			
			// DECB? no 100% sure way that i know of
			if (sectors.size() == 630)
			{
				DWDECBFileSystem fs = new DWDECBFileSystem(sectors);
				
				List<DWDECBFileSystemDirEntry> dir = fs.getDirectory();
				
				// look for wacky directory entries?
				boolean wacky = false;
				for (DWDECBFileSystemDirEntry e : dir)
				{
					
					if ((e.getFirstGranule() > DECBDefs.FAT_SIZE) || (e.getFileType() > 3) || ((e.getFileFlag() != 0) && (e.getFileFlag() != (byte)255)) )
					{
						wacky = true;
					}
				}
				
				// look for wacky fat
				for (int i = 0; i < 256;i++)
				{
					int val = (0xFF & sectors.get(0).getData()[i]);
					
					if ((val > DECBDefs.FAT_SIZE) && (val < 0xC0))
						wacky = true;
					
					if ((val > 0xC9) && (val < 0xFF))
						wacky = true;
					
				}
				
				
				if (!wacky)
					return(DWDefs.DISK_FILESYSTEM_DECB);
			}
			
		}
		
		return(DWDefs.DISK_FILESYSTEM_UNKNOWN);
	}
	
	
	
}
	
