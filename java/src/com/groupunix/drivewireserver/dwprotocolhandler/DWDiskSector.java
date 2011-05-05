package com.groupunix.drivewireserver.dwprotocolhandler;


public class DWDiskSector 
{
	private int LSN;
	private byte[] data;
	private byte[] dirtydata;
	private boolean dirty = false;
	private DWDisk disk;
	

	public DWDiskSector(DWDisk disk, int lsn)
	{
		this.LSN = lsn;
		this.disk = disk;
		this.data = new byte[disk.getSectorSize()];
		this.dirtydata = new byte[disk.getSectorSize()];
	}

	public synchronized int getLSN() {
		return this.LSN;
	}

	public synchronized void setData(byte[] newdata) 
	{

		this.dirty = true;
		System.arraycopy(newdata, 0, this.dirtydata, 0, disk.getSectorSize());
	}

	public synchronized void setData(byte[] newdata, boolean dirty) 
	{

		this.dirty = dirty;
		
		if (dirty == true)
		{
			System.arraycopy(newdata, 0, this.dirtydata, 0, disk.getSectorSize());
		}
		else
		{
			System.arraycopy(newdata, 0, this.data, 0, disk.getSectorSize());
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
			System.arraycopy(this.dirtydata, 0, this.data, 0, disk.getSectorSize());
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
