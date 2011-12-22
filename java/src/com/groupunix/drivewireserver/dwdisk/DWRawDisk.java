package com.groupunix.drivewireserver.dwdisk;
import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;


public class DWRawDisk extends DWDisk {


	
	private static final Logger logger = Logger.getLogger("DWServer.DWRawDisk");
	
	
	public DWRawDisk(FileObject fileobj, int sectorsize, int maxsectors) throws IOException, DWImageFormatException
	{
		super(fileobj);
		
		// set internal info
		this.setParam("_sectorsize", sectorsize);
		this.setParam("_maxsectors", maxsectors);
		this.setParam("_format", "raw");
		
		// expose user options
		this.setParam("syncfrom",DWDefs.DISK_DEFAULT_SYNCFROM);
		this.setParam("syncto",DWDefs.DISK_DEFAULT_SYNCTO);
		
		this.setParam("offset", DWDefs.DISK_DEFAULT_OFFSET);
		this.setParam("sizelimit",DWDefs.DISK_DEFAULT_SIZELIMIT);
		this.setParam("expand",DWDefs.DISK_DEFAULT_EXPAND);
		load();
		
		logger.debug("New DWRawDisk for '" + this.getFilePath() + "'");
		
	}
	
	public int getDiskFormat()
	{
		return(DWDefs.DISK_FORMAT_RAW);
	}
	
	
	public synchronized void seekSector(int newLSN) throws DWInvalidSectorException, DWSeekPastEndOfDeviceException
	{
	
		
		if ((newLSN < 0) || (newLSN > this.getMaxSectors()))
		{
			throw new DWInvalidSectorException("Sector " + newLSN + " is not valid");
			
		}
		else if ((newLSN >= this.getDiskSectors()) && (!this.params.getBoolean("expand",DWDefs.DISK_DEFAULT_EXPAND)))
		{
			throw new DWSeekPastEndOfDeviceException("Sector " + newLSN + " is beyond end of file, and expansion is not allowed");
		}
		else if ((this.getSizelimit() > -1) && (newLSN >= this.getSizelimit()))
		{
			throw new DWSeekPastEndOfDeviceException("Sector " + newLSN + " is beyond specified sector size limit");
		}
		else
		{
			this.setParam("_lsn", newLSN);
			
		}
	}

	
	

	public void load() throws IOException, DWImageFormatException 
	{
		// load file into sector array
	
	     
	    long filesize = this.fileobj.getContent().getSize();
	    
	    if ((filesize > Integer.MAX_VALUE) || ((filesize / this.getSectorSize()) > DWDefs.DISK_MAXSECTORS))
	    	throw new DWImageFormatException("Image file is too large");
	    
	     
	    if (filesize > Runtime.getRuntime().freeMemory())
	    {
	    	throw new DWImageFormatException("Image file will not fit in memory (" + (Runtime.getRuntime().freeMemory() / 1024) + " Kbytes free)");
	    }
	    
	    BufferedInputStream fis = new BufferedInputStream(this.fileobj.getContent().getInputStream());
	    
	    this.setLastModifiedTime(this.fileobj.getContent().getLastModifiedTime()); 
	    
	    int sector = 0;
	    int sectorsize = this.getSectorSize();
	    int readres = 0;
	    int bytesRead = 0;
	    byte[] buffer = new byte[sectorsize];
	   	
	    this.sectors.setSize( (int) (filesize / sectorsize));
	     
	    readres = fis.read(buffer, 0, sectorsize);
	    
		while (readres > -1)
		{

			
			bytesRead += readres; 
			
		   	if (bytesRead == sectorsize)
		   	{
		   		
		   		this.sectors.set(sector, new DWDiskSector(this, sector, sectorsize));
		   		this.sectors.get(sector).setData(buffer, false);
		   		
		   		sector++;
		   		bytesRead = 0;
		   		
		   	}	
		   	
		   	readres = fis.read(buffer, bytesRead, (sectorsize - bytesRead));
		}
		
		if (bytesRead > 0)
		{
			
			throw new DWImageFormatException("Incomplete sector data on sector " + sector);
		}
		
		
		logger.debug("read " + sector +" sectors from '" + this.fileobj.getName() + "'");
			
		//logger.error("Encoding: " + this.fileobj.getContent().getContentInfo().getContentEncoding() + "  Type: " + this.fileobj.getContent().getContentInfo().getContentType());
		
		this.setParam("_sectors", sector);
		fis.close();
			
	}

	


	
	public synchronized byte[] readSector() throws IOException, DWImageFormatException
	{
		// logger.debug("Read sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));
		this.incParam("_reads");
		
		
		// check source for changes...
		if (this.isSyncFrom())
		{
			if (this.fileobj.getContent().getLastModifiedTime() != this.getLastModifiedTime())
			{
				// source has changed.. have we?
				if (this.getDirtySectors() > 0)
				{
					// doh
					logger.warn("Sync conflict on " + getFilePath() + ", both the source and our cached image have changed.  Source will be overwritten!");
					this.write();
				}
				else
				{
					logger.info("Disk source " + getFilePath() + " has changed, reloading");
					this.reload();
				}
			}
		}
		
		
		int effLSN = this.getLSN() + this.getOffset();
		
		// we can read beyond the current size of the image
		if (effLSN >= this.sectors.size())
		{
			logger.debug("request for undefined sector " + effLSN + " (" + this.getLSN() + ")");
			
			// expand disk
			expandDisk(effLSN);
			this.sectors.add(effLSN, new DWDiskSector(this, effLSN, this.getSectorSize()));
		}
		
		return(this.sectors.get(effLSN).getData());	
	}
	
	
	


	

	private void expandDisk(int target) 
	{
		this.sectors.setSize(target);
		
		for (int i = this.sectors.size();i < target;i++)
		{
			this.sectors.add(i, new DWDiskSector(this, 0, this.getSectorSize()));
		}
		this.setParam("_sectors", target);
	}



	public synchronized void writeSector(byte[] data) throws DWDriveWriteProtectedException, IOException
	{
		
		if (this.getWriteProtect())
		{
			throw new DWDriveWriteProtectedException("Disk is write protected");
		}
		else
		{
			int effLSN = this.getLSN() + this.getOffset();
			
			// we can write beyond our current size
			if (effLSN>= this.sectors.size())
			{
				// expand disk / add sector
				expandDisk(effLSN);
				this.sectors.add(effLSN, new DWDiskSector(this, effLSN, this.getSectorSize()));
				//logger.debug("new sector " + effLSN);
			}
			
			this.sectors.get(effLSN).setData(data);
			
			this.incParam("_writes");
			
			// logger.debug("write sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));

			
		}
	}

	
	


	

	
	
	
	public synchronized void write() throws IOException
	{
		// write in memory image to source 
		
		if (this.fileobj.isWriteable())
		{
			if (this.fileobj.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_WRITE))
			{
				// we can sync individual sectors
				syncSectors();
			}
			else if (this.fileobj.getFileSystem().hasCapability(Capability.WRITE_CONTENT))
			{
				// we must rewrite the entire object
				writeSectors(this.fileobj);
			}
			else
			{
				// no way to write to this filesystem
				throw new FileSystemException("Filesystem is unwriteable");
			}
		}
		else
		{
			throw new FileSystemException("File is unwriteable");
		}
		
		
	}
	
	
	
	private synchronized void syncSectors() 
	{
		
		try 
		{
			RandomAccessContent raf = fileobj.getContent().getRandomAccessContent(RandomAccessMode.READWRITE);
		
			for (int i = 0;i<this.sectors.size();i++)
			{
				if (getSector(i) != null)
				{
					if (getSector(i).isDirty())
					{
						long pos = i * this.getSectorSize();
						raf.seek(pos);
						raf.write(getSector(i).getData());
						logger.debug("wrote sector " + i + " in " + getFilePath() );
						getSector(i).makeClean();
					}
				}
			}
			
		
			raf.close();
			fileobj.close();
			this.setLastModifiedTime(this.fileobj.getContent().getLastModifiedTime()); 
		} 
		catch (IOException e) 
		{
			logger.error("Error writing sectors in " + this.getFilePath() + ": " + e.getMessage() );
		}
		
		
	}
	
	


	private int getMaxSectors()
	{
		return this.params.getInt("_maxsectors", DWDefs.DISK_MAXSECTORS);
	}
	
	private int getSectorSize()
	{
		return this.params.getInt("_sectorsize", DWDefs.DISK_SECTORSIZE);
	}


	private boolean isSyncFrom() 
	{
		return this.params.getBoolean("syncfrom",DWDefs.DISK_DEFAULT_SYNCFROM);
	}

	private boolean isSyncTo() 
	{
		return this.params.getBoolean("syncto",DWDefs.DISK_DEFAULT_SYNCTO);
	}
	


	private int getOffset() 
	{
		return this.params.getInt("offset", DWDefs.DISK_DEFAULT_OFFSET);
	}



	private int getSizelimit() 
	{
		return this.params.getInt("sizelimit",DWDefs.DISK_DEFAULT_SIZELIMIT);
	}




	public void sync() throws IOException
	{
		if (this.getDirtySectors() > 0)
			this.write();
	}

	
	
	public static int considerImage(byte[] header, long fobjsize)
	{
		// is it right size for raw sectors
		if (fobjsize % DWDefs.DISK_SECTORSIZE == 0) 
		{
			
			// is it an os9 filesystem
			if (fobjsize > 3)
			{
				
				if (fobjsize == ((0xFF & header[0]) * 65535 + (0xFF & header[1]) * 256 + (0xFF & header[2]))*256)
				{
					// exact match, lets claim it
					return(DWDefs.DISK_CONSIDER_YES);
				}
			}
			
			// not os9 so can't be sure?
			return(DWDefs.DISK_CONSIDER_MAYBE);
		}
		
		// not /256
		return(DWDefs.DISK_CONSIDER_NO);
	}
	
}
