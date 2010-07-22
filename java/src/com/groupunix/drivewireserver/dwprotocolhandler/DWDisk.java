package com.groupunix.drivewireserver.dwprotocolhandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.util.RandomAccessMode;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;


public class DWDisk {

	private static final Logger logger = Logger.getLogger("DWServer.DWDisk");
	
	public static final int MAX_SECTORS = 16777215; // what is coco's max? 24 bits?
	private int LSN = 0;
	private boolean	wrProt = false;
	private ArrayList<DWDiskSector> sectors = new ArrayList<DWDiskSector>();
	
	private int reads = 0;
	private int writes = 0;
	
	private FileSystemManager fsManager;
	private FileObject fileobj;
	
	
	
	public DWDisk(String path) throws IOException
	{
		this.fsManager = VFS.getManager();
		fileobj = fsManager.resolveFile(path);

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

	
	
	public synchronized void seekSector(int newLSN)
	{
		if ((newLSN < 0) || (newLSN > MAX_SECTORS))
		{
			logger.error("Seek out of range: sector " + newLSN + " ?");
		}
		else
		{
			this.LSN = newLSN;
			
			// logger.debug("seek to sector " + newLSN + " for '" + this.filePath + "'");
		
		}
	}

	
	
	public int getLSN()
	{
		return(this.LSN);
	}
	
	
	
	public synchronized void setWriteProtect(boolean wp)
	{
		if (wp)
		{
			logger.debug("write protecting '" + this.fileobj.getName() + "'");
		}
		else
		{
			logger.debug("write enabling '" + this.fileobj.getName() + "'");
		}
		
		this.wrProt = wp;
	}
	
	
	
	public synchronized boolean getWriteProtect()
	{
		return(this.wrProt);
	}
	
	
	
	private void loadSectors() throws IOException 
	{
		// load file into sector array
	    InputStream fis;
	

	    fis = this.fileobj.getContent().getInputStream();
	
	    int sector = 0;
	    int bytesRead = 0;
	    byte[] buffer = new byte[256];
	   		    
	    int databyte = fis.read();
	    	    
		while (databyte != -1)
		{
			
			buffer[bytesRead] = (byte)databyte;
			bytesRead++;
			
		   	if (bytesRead == 256)
		   	{
		   		
		   		this.sectors.add(sector, new DWDiskSector(sector));
		   		this.sectors.get(sector).setData(buffer, false);
		   		sector++;
		   		bytesRead = 0;
		   	}	
		   	
		   	databyte = fis.read();
		}
		   
		
		logger.debug("read " + sector +" sectors from '" + this.fileobj.getName() + "'");
			
		//logger.error("Encoding: " + this.fileobj.getContent().getContentInfo().getContentEncoding() + "  Type: " + this.fileobj.getContent().getContentInfo().getContentType());
		
		fis.close();
			
	}

	
	
	public String getFilePath()
	{
		return(this.fileobj.getName().toString());
	}
	

	
	public synchronized byte[] readSector() throws IOException
	{
		// logger.debug("Read sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));
		this.reads++;
		
		// we can read beyond the current size of the image
		if (this.LSN >= this.sectors.size())
		{
			// logger.debug("request for undefined sector " + this.LSN);
			this.sectors.add(this.LSN, new DWDiskSector(this.LSN));
		}
		
		return(this.sectors.get(this.LSN).getData());	
	}
	
	
	
	public synchronized void writeSector(byte[] data) throws DWDriveWriteProtectedException, IOException
	{
		
		if (this.wrProt)
		{
			throw new DWDriveWriteProtectedException("Disk is write protected");
		}
		else
		{
			if (sectors.get(this.LSN) == null)
			{
				// expand disk / add sector
				this.sectors.add(this.LSN, new DWDiskSector(this.LSN));
				logger.debug("new sector " + this.LSN);
			}
			this.sectors.get(this.LSN).setData(data);
			
			this.writes++;
			
			// logger.debug("write sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));

			
		}
	}

	public int getReads() {
		return reads;
	}


	public int getWrites() {
		return writes;
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
		// write in memory image to file
		// using most efficient method available
		
		if (getDirtySectors() > 0)
		{	
		
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
						long pos = i * 256;
						raf.seek(pos);
						raf.write(getSector(i).getData());
						logger.debug("wrote sector " + i + " in " + getFilePath() );
						getSector(i).makeClean();
					}
				}
			}
			
		
			raf.close();
			fileobj.close();
	
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	    
		   fos.write(this.sectors.get(sector).getData(), 0, 256);
		   this.sectors.get(sector).makeClean();
		   sector++;
	   }
		   
	   fos.close();
		
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
			   // TODO Auto-generated catch block
			   e.printStackTrace();
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
	logger.debug("reloading sectors from path");

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



	
	
}
