package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public abstract class DWDisk 
{
	private static final Logger logger = Logger.getLogger("DWServer.DWDisk");
	
	protected HierarchicalConfiguration params;
	protected DWProtocol dwproto;
	protected Vector<DWDiskSector> sectors = new Vector<DWDiskSector>();	
	protected int diskno = -1;
	protected FileObject fileobj;
	protected FileSystemManager fsManager;
	protected DWDiskConfigListener configlistener;
	
	
	// required for format implementation:

	public abstract void seekSector(int lsn) throws DWInvalidSectorException, DWSeekPastEndOfDeviceException;
	public abstract void writeSector(byte[] data) throws DWDriveWriteProtectedException, IOException;
	public abstract byte[] readSector() throws IOException, DWImageFormatException;
	protected abstract void load() throws IOException, DWImageFormatException;
	
	
	public DWDisk(DWProtocol dwproto, FileObject fileobj) throws IOException, DWImageFormatException
	{
		this.dwproto = dwproto;
		this.fileobj = fileobj;
		
		this.params = new HierarchicalConfiguration();
		
		// internal 
		this.setParam("_path", fileobj.getName().getURI());
		this.setLastModifiedTime(fileobj.getContent().getLastModifiedTime());
		
		// user options
		this.setParam("writeprotect",DWDefs.DISK_DEFAULT_WRITEPROTECT);
		
	}
	
	
	
	public HierarchicalConfiguration getParams()
	{
		return this.params;
	}
	
	
	public void setParam(String key, Object val) 
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
	
	public void incParam(String key)
	{
		this.setParam(key, this.params.getInt(key,0) + 1);
	}
	
	
	public String getFilePath()
	{
		return this.fileobj.getName().getURI();
	}

	public FileObject getFileObject()
	{
		return(this.fileobj);
	}

	
	public void setLastModifiedTime(long lastModifiedTime) 
	{
		this.params.setProperty("_last_modified", lastModifiedTime);
	}
	
	public long getLastModifiedTime() 
	{
		return this.params.getLong("_last_modified", 0);
	}
	
	public int getLSN()
	{
		return(this.params.getInt("_lsn",0));
	}
	
	
	
	
	public synchronized int getDiskSectors()
	{
		return(this.sectors.size());
	}
	
	
	public void setDiskNo(int d)
	{
		
		
		if (d < 0)
		{
			// disk is not in play
			
			// remove listener
			if (this.configlistener != null)
				this.params.removeConfigurationListener(this.configlistener);
			// send null path event
			DriveWireServer.submitDiskEvent(this.dwproto.getHandlerNo(), this.diskno,  "_path","");
			
		}
		else
		{
			// disk is active in drive d
			
			// remove any existing listeners
			@SuppressWarnings("unchecked")
			Iterator<ConfigurationListener> citr = this.params.getConfigurationListeners().iterator();
			while (citr.hasNext())
			{
				this.params.removeConfigurationListener(citr.next());
			}

			// add for this drive
			this.configlistener = new DWDiskConfigListener(this.dwproto.getHandlerNo(), d);
			this.params.addConfigurationListener(this.configlistener);
		
			// announce drive info to any event listeners
			@SuppressWarnings("unchecked")
			Iterator<String> itr = this.params.getKeys();
			while(itr.hasNext())
			{
				String key = itr.next();
				DriveWireServer.submitDiskEvent(this.dwproto.getHandlerNo(), d,  key, this.params.getProperty(key).toString());
			}
		}
		
		this.diskno = d;
	}
	
	
	public void reload() throws IOException, DWImageFormatException
	{
		logger.debug("reloading disk sectors from " + this.getFilePath());

		this.sectors.clear();
		
		// load from path 
		load();
	}

	
	public void eject()
	{
		
		try
		{
			sync();
		} 
		catch (IOException e)
		{
			logger.warn("While ejecting disk in drive " + this.diskno + ": " + e.getMessage());
		}
		
		this.setDiskNo(-1);
	}

	
	public void sync() throws IOException
	{
		// NOP on readonly image formats
	}

	public void write() throws IOException
	{
		// Fail on readonly image formats
		throw new IOException("Image is read only");
	}

	
	public synchronized void writeTo(String path) throws IOException
	{
		// write in memory image to specified path (raw format)
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
	
	
	 public void writeSectors(FileObject fobj) throws IOException
	 {
		 // write out all sectors
		   
		 OutputStream fos;
		   
		 logger.debug("Writing out all sectors from cache to " + fobj.getName());
		 
		 fos = fobj.getContent().getOutputStream();

		 int sector = 0;
	       
		 while (sector < this.sectors.size()) 
		 {
		    
			   fos.write(this.sectors.get(sector).getData(), 0, this.sectors.get(sector).getData().length);
			   this.sectors.get(sector).makeClean();
			   sector++;
		 }
			   
		 fos.close();
		   
		 this.setLastModifiedTime(this.fileobj.getContent().getLastModifiedTime()); 
	   
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
	 
	 
	public synchronized boolean getWriteProtect()
	{
		return(this.params.getBoolean("writeprotect",DWDefs.DISK_DEFAULT_WRITEPROTECT));
	}
	

	
}
