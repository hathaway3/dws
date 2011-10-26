package com.groupunix.drivewireserver.dwprotocolhandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.util.RandomAccessMode;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;


public class DWDisk {


	
	private static final Logger logger = Logger.getLogger("DWServer.DWDisk");
	private ArrayList<DWDiskSector> sectors = new ArrayList<DWDiskSector>();
		
	
	
	private FileSystemManager fsManager;
	private FileObject fileobj;
	
	private HierarchicalConfiguration params;
	
	// these cannot change for this disk while it is loaded
	private int sectorsize;
	private int maxsectors;

	
	
	public DWDisk(String path, int sectorsize, int maxsectors) throws IOException
	{
		this.sectorsize = sectorsize;
		this.maxsectors = maxsectors;

		
		this.fsManager = VFS.getManager();
		
		try
		{
			fileobj = fsManager.resolveFile(path);
		}
		catch (org.apache.commons.vfs.FileSystemException e)
		{
			logger.error("FileSystemException: " + e.getMessage());
			throw new IOException(e.getMessage());
		}
		
		
		this.params = new HierarchicalConfiguration();
		this.params.setProperty("path", fileobj.getName().toString());
		
		logger.info("New DWDisk for '" + path + "'");
		
	
		if (fileobj.isReadable())
		{
			loadSectors();
		}
		else
		{
			logger.error("Unreadable path '" + path + "'");
			throw new IOException("Unreadable path");
		}
	}
	
	

	void setParam(String key, Object val) 
	{
		if (val == null)
		{
			this.params.clearProperty(key);
		}
		else
		{
			this.params.setProperty(key, val);
		}
	}


	
	// OS9 disk format stuff

	public int DD_TOT()
	{
		byte[] dd_tot = new byte[3];
		System.arraycopy( sectors.get(0).getData(), 0, dd_tot, 0, 3 ); 
		return(DWUtils.int3(dd_tot));
	}
	
	public int DD_TKS()
	{
		return((int) sectors.get(0).getData()[3]);
	}
	
	public int DD_MAP()
	{
		byte[] dd_map = new byte[2];
		System.arraycopy( sectors.get(0).getData(), 4, dd_map, 0, 2 ); 
		return(DWUtils.int2(dd_map));
	}
	
	public int DD_BIT()
	{
		byte[] dd_bit = new byte[2];
		System.arraycopy( sectors.get(0).getData(), 6, dd_bit, 0, 2 ); 
		return(DWUtils.int2(dd_bit));
	}
	
	public int DD_DIR()
	{
		byte[] dd_dir = new byte[3];
		System.arraycopy( sectors.get(0).getData(), 8, dd_dir, 0, 3 ); 
		return(DWUtils.int3(dd_dir));
	}
	
	public int DD_OWN()
	{
		byte[] dd_own = new byte[2];
		System.arraycopy( sectors.get(0).getData(), 11, dd_own, 0, 2 ); 
		return(DWUtils.int2(dd_own));
	}
	
	public byte DD_ATT()
	{
		return(sectors.get(0).getData()[13]);
	}
	
	public long DD_DSK()
	{
		byte[] dd_dsk = new byte[2];
		System.arraycopy( sectors.get(0).getData(), 14, dd_dsk, 0, 2 ); 
		return(DWUtils.int2(dd_dsk));
	}
	
	public byte DD_FMT()
	{
		return(sectors.get(0).getData()[16]);
	}
	
	public long DD_SPT()
	{
		byte[] dd_spt = new byte[2];
		System.arraycopy( sectors.get(0).getData(), 17, dd_spt, 0, 2 ); 
		return(DWUtils.int2(dd_spt));
	}
	
	public long DD_BT()
	{
		byte[] dd_bt = new byte[3];
		System.arraycopy( sectors.get(0).getData(), 21, dd_bt, 0, 3 ); 
		return(DWUtils.int3(dd_bt));
	}
	
	public long DD_BSZ()
	{
		byte[] dd_bsz = new byte[2];
		System.arraycopy( sectors.get(0).getData(), 24, dd_bsz, 0, 2 ); 
		return(DWUtils.int2(dd_bsz));
	}
	
	public byte[] DD_DAT()
	{
		byte[] dd_dat = new byte[5];
		System.arraycopy( sectors.get(0).getData(), 26, dd_dat, 0, 5 ); 
		return(dd_dat);
	}
	
	public byte[] DD_NAM()
	{
		byte[] dd_nam = new byte[32];
		
		if (!sectors.isEmpty())
		{
			System.arraycopy( sectors.get(0).getData(), 31, dd_nam, 0, 32 );
		}
		else
		{
			dd_nam = "  **** DISK HAS NO LSN 0 ****   ".getBytes();
		}
		return(dd_nam);
	}
	
	public byte[] DD_OPT()
	{
		byte[] dd_opt = new byte[32];
		System.arraycopy( sectors.get(0).getData(), 63, dd_opt, 0, 32 ); 
		return(dd_opt);
	}

	
	
	
	public synchronized String getDiskName()
	{
		return(cocoString(DD_NAM()));
	}

	
	
	public synchronized int getDiskSectors()
	{
		return(this.sectors.size());
	}
	

	
	private String cocoString(byte[] bytes) 
	{
		// return string from 6809 style string
		String ret = new String();
		
		int i = 0;
		
		// thanks Christopher Hawks
		while ((i < bytes.length - 1) && (bytes[i] > 0))
		{
			ret += Character.toString((char) bytes[i]);
			i++;
		}
		
		ret += Character.toString((char) (bytes[i] + 128));
		
		return(ret);
	}

	
	
	public synchronized void seekSector(int newLSN) throws DWInvalidSectorException, DWSeekPastEndOfDeviceException
	{
	
		
		if ((newLSN < 0) || (newLSN > this.maxsectors))
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

	
	
	public int getLSN()
	{
		return(this.params.getInt("_lsn",0));
	}
	
	
	
	
	public synchronized boolean getWriteProtect()
	{
		return(this.params.getBoolean("writeprotect",DWDefs.DISK_DEFAULT_WRITEPROTECT));
	}
	
	
	
	private void loadSectors() throws IOException 
	{
		// load file into sector array
	    InputStream fis;
	
	    
	    fis = this.fileobj.getContent().getInputStream();
	    
	    this.setLastModifiedTime(this.fileobj.getContent().getLastModifiedTime()); 
	    
	    int sector = 0;
	    int sectorsize = this.sectorsize;
	    int readres = 0;
	    int bytesRead = 0;
	    byte[] buffer = new byte[sectorsize];
	   		    
	    readres = fis.read(buffer, 0, sectorsize);
	    
		while (readres > -1)
		{
			bytesRead += readres; 
			
		   	if (bytesRead == sectorsize)
		   	{
		   		
		   		this.sectors.add(sector, new DWDiskSector(sector, this.sectorsize));
		   		this.sectors.get(sector).setData(buffer, false);
		   		sector++;
		   		bytesRead = 0;
		   		buffer = new byte[sectorsize];
		   	}	
		   	
		   	readres = fis.read(buffer, bytesRead, (sectorsize - bytesRead));
		}
		
		if (bytesRead > 0)
		{
			
			if (this.isPadPartialSectors())
			{
				this.sectors.add(sector, new DWDiskSector(sector, this.sectorsize));
		   		this.sectors.get(sector).setData(buffer, false);
		   		sector++;
		   		logger.info("File length doesn't match current sector size of " + sectorsize + ", the last " + bytesRead + " bytes were padded to form a full sector.");
			}
			else
			{
				logger.warn("File length doesn't match current sector size of " + sectorsize + ", the last " + bytesRead + " bytes are ignored and may be overwritten.");
			}
		}
		
		
		logger.debug("read " + sector +" sectors from '" + this.fileobj.getName() + "'");
			
		//logger.error("Encoding: " + this.fileobj.getContent().getContentInfo().getContentEncoding() + "  Type: " + this.fileobj.getContent().getContentInfo().getContentType());
		
		this.setParam("_sectors", sector);
		fis.close();
			
	}

	
	
	public String getFilePath()
	{
		return(this.fileobj.getName().toString());
	}
	

	
	public synchronized byte[] readSector() throws IOException
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
					this.writeDisk();
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
			this.sectors.add(effLSN, new DWDiskSector(effLSN, this.sectorsize));
		}
		
		return(this.sectors.get(effLSN).getData());	
	}
	
	
	

	public boolean isNamedObject() 
	{
		return this.params.getBoolean("namedobject",DWDefs.DISK_DEFAULT_NAMEDOBJECT);
	}



	public boolean isSyncFrom() 
	{
		return this.params.getBoolean("syncfrom",DWDefs.DISK_DEFAULT_SYNCFROM);
	}

	public boolean isSyncTo() 
	{
		return this.params.getBoolean("syncto",DWDefs.DISK_DEFAULT_SYNCTO);
	}
	
	public boolean isPadPartialSectors() 
	{
		return this.params.getBoolean("padsectors",DWDefs.DISK_DEFAULT_PADSECTORS);
	}

	public void incParam(String key)
	{
		this.setParam(key, this.params.getInt(key,0) + 1);
	}
	

	private void expandDisk(int target) 
	{
		for (int i = this.sectors.size();i < target;i++)
		{
			this.sectors.add(i, new DWDiskSector(0, this.sectorsize));
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
				this.sectors.add(effLSN, new DWDiskSector(effLSN, this.sectorsize));
				logger.debug("new sector " + effLSN);
			}
			
			this.sectors.get(effLSN).setData(data);
			
			this.incParam("_writes");
			
			// logger.debug("write sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));

			
		}
	}

	public int getReads() {
		return this.params.getInt("_reads",0);
	}


	public int getWrites() {
		return this.params.getInt("_writes",0);
	}

	public synchronized int getDirtySectors() 
	{
		int drt = 0;
		
		for (int i=0;i<this.sectors.size();i++)
		{
			if (this.sectors.get(i) != null)
			{
				if (this.sectors.get(i).isDirty())
				{
					drt++;
				}
			}
		}
		
		return(drt);
	}
	
	
	
	public synchronized DWDiskSector getSector(int no)
	{
		return(this.sectors.get(no));
	}

	
	
	public synchronized void writeDisk() throws IOException
	{
		// write in memory image to source 
		// using most efficient method available
		
		
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
				logger.warn("Filesystem is unwritable for path '"+ this.fileobj.getName() + "'");
				throw new FileSystemException("Filesystem is unwriteable");
			}
		}
		else
		{
			logger.warn("File is unwriteable for path '" + this.fileobj.getName() + "'");
			throw new IOException("File is unwriteable");
		}
		
		
	}
	
	
	
	public synchronized void writeDisk(String path) throws IOException
	{
		// write in memory image to specified path
		// using most efficient method available
		
		FileObject altobj = fsManager.resolveFile(path);

		
		if (altobj.isWriteable())
		{
			if (altobj.getFileSystem().hasCapability(Capability.WRITE_CONTENT))
			{
				// we always rewrite the entire object
				writeSectors(altobj);
			}
			else
			{
				// no way to write to this filesystem
				logger.warn("Filesystem is unwritable for path '"+ altobj.getName() + "'");
				throw new FileSystemException("Filesystem is unwriteable");
			}
		}
		else
		{
			logger.warn("File is unwriteable for path '" + altobj.getName() + "'");
			throw new IOException("File is unwriteable");
		}
	}
	
	
	
	
	public synchronized void syncSectors() 
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
						long pos = i * this.sectorsize;
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
	
	

   
   public void writeSectors(FileObject fobj) throws IOException
   {
	   // write out all sectors
	
	   
	   OutputStream fos;
	   
	   logger.debug("Writing out all sectors from cache to " + fobj.getName());
	   
	   fos = fobj.getContent().getOutputStream();

	   int sector = 0;
       
	   while (sector < this.sectors.size()) 
	   {
	    
		   fos.write(this.sectors.get(sector).getData(), 0, this.sectorsize);
		   this.sectors.get(sector).makeClean();
		   sector++;
	   }
		   
	   fos.close();
	   
	   this.setLastModifiedTime(this.fileobj.getContent().getLastModifiedTime()); 
   }

   
   
   public boolean isWriteable()
   {

	   if (this.fileobj.getFileSystem().hasCapability(Capability.WRITE_CONTENT) == false)
	   {
		   return(false);
	   }
	   else
	   {
		   boolean isw = false;
	   
		   try
		   {
			   isw = this.fileobj.isWriteable();
		   } 
		   catch (FileSystemException e)
		   {
			   logger.error("While checking FS writable: " + e.getMessage());
		   }
		   
		   return(isw);
	   }
	
   }

   public boolean isFSWriteable()
   {
	   return(this.fileobj.getFileSystem().hasCapability(Capability.WRITE_CONTENT));
   }
   
   public boolean isRandomWriteable()
   {
	  return(this.fileobj.getFileSystem().hasCapability(Capability.RANDOM_ACCESS_WRITE));
   }



public FileObject getFileObject()
{
	return(this.fileobj);
}



public void reload() throws IOException
{
	logger.debug("reloading disk sectors from " + this.getFilePath());

	this.sectors.clear();
	
	// load from path 
	loadSectors();
}



public void shutdown()
{
	try
	{
		writeDisk();
		this.fileobj.close();
		this.fileobj = null;
		this.fsManager = null;
	} 
	catch (IOException e)
	{
		logger.warn("While shutting down: " + e.getMessage());
	}
	
}



	public boolean isExpand() 
	{
		return this.params.getBoolean("expand", DWDefs.DISK_DEFAULT_EXPAND);
	}




	public int getOffset() 
	{
		return this.params.getInt("offset", DWDefs.DISK_DEFAULT_OFFSET);
	}



	public void setSizelimit(int size) 
	{
		this.params.setProperty("sizelimit", size);
	}



	public int getSizelimit() 
	{
		return this.params.getInt("sizelimit",DWDefs.DISK_DEFAULT_SIZELIMIT);
	}





	public int getReadErrors() 
	{
		return this.params.getInt("_read_errors",0);
	}

	public int getWriteErrors() 
	{
		return this.params.getInt("_write_errors",0);
	}



	public void setLastModifiedTime(long lastModifiedTime) 
	{
		this.params.setProperty("_last_modified_time", lastModifiedTime);
	}



	public long getLastModifiedTime() 
	{
		return this.params.getLong("_last_modified_time", 0);
	}



	public HierarchicalConfiguration getParams() 
	{
		return(this.params);
	}


	
	public String getDiskFSType()
	{
		
		if (!sectors.isEmpty())
		{
			// OS9 ?
			if ((sectors.get(0).getData()[3] == 18) && (sectors.get(0).getData()[73] == 18) && (sectors.get(0).getData()[75] == 18))
			{
				return("OS9");
			}
			
			// LWFS
			byte[] lwfs = new byte[4];
			System.arraycopy( sectors.get(0).getData(), 0, lwfs, 0, 4 );
			
			if (new String(lwfs).equals("LWFS") || new String(lwfs).equals("LW16"))
			{
				return("LWFS");
			}
			
			// cocoboot isave
			if (sectors.get(0).getData()[0] == (byte) 'f' && sectors.get(0).getData()[1] == (byte) 'c')
			{
				return("CoCoBoot isave");
			}
			
			// cocoboot script
			if (sectors.get(0).getData()[0] == (byte) 'f' && sectors.get(0).getData()[1] == (byte) 's')
			{
				return("CoCoBoot script");
			}
			
			// DECB?
		}
		
		return("Unknown");
	}
	
	
	
}
