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

public class DWVDKDisk extends DWDisk
{
	private static final Logger logger = Logger.getLogger("DWServer.DWVDKDisk");
	private DWVDKDiskHeader header;
	
	
	public DWVDKDisk(DWProtocol dwproto, FileObject fileobj) throws IOException, DWImageFormatException
	{
		super(dwproto,fileobj);
		
		this.setParam("_format", "vdk");
		
		load();
		
		logger.debug("New VDK disk for " + fileobj.getName().getURI());
	}
	
	

	public int getDiskFormat()
	{
		return DWDefs.DISK_FORMAT_VDK;
	}



	public void load() throws IOException, DWImageFormatException
	{
		// load file into sector array
	    InputStream fis;
		    
	    fis = this.fileobj.getContent().getInputStream();
	    
	    this.setLastModifiedTime(this.fileobj.getContent().getLastModifiedTime()); 
	    
	    // read disk header
	    this.header = readHeader(fis);

	 
	    this.setParam("writeprotect", header.isWriteProtected());
	    this.setParam("_tracks", header.getTracks());
	    this.setParam("_sides", header.getSides());
	    this.setParam("_sectors", (header.getTracks() * header.getSides() * 18));
	    
	    if ( this.fileobj.getContent().getSize() != (this.params.getInt("_sectors")*256 + this.header.getHeaderLen() ))
	    {
	    	throw new DWImageFormatException("Invalid VDK image, wrong file size");
	    }
	    
	    this.sectors.setSize(this.params.getInt("_sectors"));
	    
	    byte[] buf = new byte[256];
	    int readres;
	    
	    for (int i =0;i<this.params.getInt("_sectors") ;i++)
	    {
	    	readres = 0;
	    	
	    	while (readres < 256)
		    	readres += fis.read(buf, readres, 256 - readres);
	    	
	    	this.sectors.set(i, new DWDiskSector(i, 256));
	    	this.sectors.get(i).setData(buf, false);
	    	
	    	
	    }
	    	    
		fis.close();
		
		
	}


	private static DWVDKDiskHeader readHeader(InputStream fis) throws IOException, DWImageFormatException
	{
	    // read sig and hdr length
	    int readres = 0;
	    byte[] hbuff = new byte[4];
	    
	    while (readres < 4)
	    	readres += fis.read(hbuff, readres, 4 - readres);
	    
	    // check sanity
	    if (((0xFF & hbuff[0]) != 'd') || ((0xFF & hbuff[1]) != 'k'))
	    {
	    	throw new DWImageFormatException("Invalid VDK header: " + hbuff[0] + " " +  hbuff[1]);
	    }
	    
	    int headerlen = (0xFF & hbuff[2]) + ((0xFF & hbuff[3]) * 256);
	    
	    hbuff = new byte[headerlen - 4];
	    readres = 0;
	    
	    while (readres < headerlen - 4)
	    	readres += fis.read(hbuff, readres, (headerlen - 4) - readres);
	    
	    return(new DWVDKDiskHeader(hbuff));
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
		
			long fobjsize = fobj.getContent().getSize();
	    
			// 	is it big enough to have a header
			if (fobjsize > 11)
			{
				InputStream fis = fobj.getContent().getInputStream();
			
				// 	make a header object
			
				DWVDKDiskHeader header;
			
				header = readHeader(fis);
			
				fis.close();
			
			
				// is the size right?
				if (fobjsize ==  (header.getHeaderLen() + ( header.getSides() * header.getTracks() * 256 * 18) ))
				{
					return DWDefs.DISK_CONSIDER_YES;
				}
			
			}
		}
		catch (IOException e)
		{
		} 
		catch (DWImageFormatException e)
		{
		}
		
		return DWDefs.DISK_CONSIDER_NO;
	}

}
