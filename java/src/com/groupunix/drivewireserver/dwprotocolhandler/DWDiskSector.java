package com.groupunix.drivewireserver.dwprotocolhandler;


// import org.apache.log4j.Logger;

public class DWDiskSector 
{
	private int LSN;
	private byte[] data = new byte[256];
	private byte[] dirtydata = new byte[256];
	private boolean dirty = false;

	
	// private static final Logger logger = Logger.getLogger("DWServer.DWDiskSector");
	
	public DWDiskSector(int lsn)
	{
		this.LSN = lsn;
	}

	public synchronized int getLSN() {
		return this.LSN;
	}

	public synchronized void setData(byte[] newdata) 
	{

		this.dirty = true;
		System.arraycopy(newdata, 0, this.dirtydata, 0, 256);
	}

	public synchronized void setData(byte[] newdata, boolean dirty) 
	{

		this.dirty = dirty;
		
		if (dirty == true)
		{
			System.arraycopy(newdata, 0, this.dirtydata, 0, 256);
		}
		else
		{
			System.arraycopy(newdata, 0, this.data, 0, 256);
		}
	}
	
	
	public synchronized byte[] getData() 
	{
		if (this.dirty == true)
		{
			return this.dirtydata;
		}
		else
		{
			return this.data;
		}
	}

	public synchronized void makeClean()
	{
		if (this.dirty)
		{
			System.arraycopy(this.dirtydata, 0, this.data, 0, 256);
			this.dirty = false;
		}
	}

	public synchronized boolean isDirty() {
		return dirty;
	}

	public byte[] getCleanData()
	{
		return this.data;
	}
	
	public byte[] getDirtyData()
	{
		return this.dirtydata;
	}

	
}
