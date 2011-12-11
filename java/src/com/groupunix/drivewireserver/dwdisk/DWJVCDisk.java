package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWJVCDisk extends DWDisk
{
	private static final Logger logger = Logger.getLogger("DWServer.DWJVCDisk");
	private DWJVCDiskHeader header;
	
	
	public DWJVCDisk(DWProtocol dwproto, FileObject fileobj) throws IOException, DWImageFormatException
	{
		super(dwproto,fileobj);
		
		this.setParam("_format", "jvc");
		
		load();
		
		logger.debug("New JVC disk for " + fileobj.getName().getURI());
	}
	
	

	public int getDiskFormat()
	{
		return DWDefs.DISK_FORMAT_JVC;
	}



	public void load() throws IOException, DWImageFormatException
	{
		// load file into sector array
	    InputStream fis;
		    
	    fis = this.fileobj.getContent().getInputStream();
	    
	    this.setLastModifiedTime(this.fileobj.getContent().getLastModifiedTime()); 
	    
	    this.header = new DWJVCDiskHeader();
	    
	    int filelen = (int) this.fileobj.getContent().getSize();
	    int headerlen = (filelen % 256);
	    
	    if (headerlen > 0)
	    {
	    	int readres = 0;
	    	byte[] buf = new byte[headerlen];
	    	
	    	while (readres < headerlen)
		    	readres += fis.read(buf, readres, headerlen - readres);
	    	
	    	this.header.setData(buf);
	    }
	    
	    if (this.header.getSectorAttributes() > 0)
	    {
	    	throw new DWImageFormatException("JVC with sector attributes not supported");
	    }

	    this.setParam("_secpertrack", header.getSectorsPerTrack());
	    this.setParam("_sides", header.getSides());
	    this.setParam("_sectorsize", header.getSectorSize());
	    this.setParam("_firstsector", header.getFirstSector());
	    
	    int tracks = (filelen - headerlen) / (header.getSectorsPerTrack() * (header.getSectorSize())) / header.getSides();
	    this.setParam("_tracks", tracks);
	    
	    this.setParam("_sectors", (tracks * header.getSides() * header.getSectorsPerTrack()));
	    
	     
	    this.sectors.setSize(this.params.getInt("_sectors"));
	    
	    byte[] buf = new byte[header.getSectorSize()];
	    int readres;
	    
	    for (int i =0;i<this.params.getInt("_sectors") ;i++)
	    {
	    	readres = 0;
	    	
	    	while (readres < header.getSectorSize())
		    	readres += fis.read(buf, readres, header.getSectorSize() - readres);
	    	
	    	this.sectors.set(i, new DWDiskSector(i, header.getSectorSize()));
	    	this.sectors.get(i).setData(buf, false);
	    	
	    	
	    }
	    	    
		fis.close();
		
		
	}







	public void seekSector(int newLSN) throws DWInvalidSectorException, DWSeekPastEndOfDeviceException
	{
		if (newLSN < 0)
		{
			throw new DWInvalidSectorException("Sector " + newLSN + " is not valid");
		}
		else if (newLSN > (this.sectors.size()-1) )
		{
			throw new DWSeekPastEndOfDeviceException("Attempt to seek beyond end of image");
		}
		else
		{
			this.setParam("_lsn", newLSN);
		}
	}

	

	
	public void writeSector(byte[] data) throws DWDriveWriteProtectedException,	IOException
	{
		if (this.getWriteProtect())
		{
			throw new DWDriveWriteProtectedException("Disk is write protected");
		}
		else
		{
			
			this.sectors.get(this.getLSN()).setData(data);
			
			this.incParam("_writes");
			
		}
	}
	
	

	public byte[] readSector() throws IOException
	{
		this.incParam("_reads");
		return(this.sectors.get(this.getLSN()).getData() );
	}

	



	public static int considerImage(FileObject fobj)
	{
		try
		{
			 
			DWJVCDiskHeader header = new DWJVCDiskHeader();
			    
		
			int filelen = (int) fobj.getContent().getSize();
			int headerlen = (filelen % 256);
			   
			// only consider jvc with header
			if (headerlen > 0)
		    {
				InputStream fis;
			    
			    fis = fobj.getContent().getInputStream();
				
		    	int readres = 0;
		    	byte[] buf = new byte[headerlen];
		    	
		    	while (readres < headerlen)
			    	readres += fis.read(buf, readres, headerlen - readres);

		    	fis.close();
		    	
		    	header.setData(buf);
		    	
		    	// we dont read sector attributed files
		    	if (header.getSectorAttributes() == 0)
		    	{
		    		// is filesize right..
		    		if ((filelen - headerlen) % header.getSectorSize() == 0)
		    			return DWDefs.DISK_CONSIDER_MAYBE;
		    	}
		    	
		    }
		}
		catch (IOException e)
		{
		} 
		
		return DWDefs.DISK_CONSIDER_NO;
	}

}
