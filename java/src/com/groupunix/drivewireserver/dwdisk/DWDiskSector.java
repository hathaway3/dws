package com.groupunix.drivewireserver.dwdisk;


public class DWDiskSector 
{
	private int LSN;
	private byte[] data;
	//private byte[] dirtydata;
	private boolean dirty = false;
	private int sectorsize;
	

	public DWDiskSector( int lsn, int sectorsize)
	{
		this.LSN = lsn;
		this.sectorsize = sectorsize;
		this.data = new byte[sectorsize];
		//this.dirtydata = new byte[sectorsize];
	}

	public synchronized int getLSN() {
		return this.LSN;
	}

	public synchronized void setData(byte[] newdata) 
	{

		this.dirty = true;
		System.arraycopy(newdata, 0, this.data, 0, this.sectorsize);
	}

	public synchronized void setData(byte[] newdata, boolean dirty) 
	{

		this.dirty = dirty;
		
		//if (dirty == true)
		//{
		//	System.arraycopy(newdata, 0, this.dirtydata, 0, this.sectorsize);
		//}
		//else
		//{
			System.arraycopy(newdata, 0, this.data, 0, this.sectorsize);
		//}
	}
	
	
	public synchronized byte[] getData() 
	{
		//if (this.dirty == true)
		//{
		//	return this.dirtydata;
		//}
		//else
		//{
			return this.data;
		//}
	}

	public synchronized void makeClean()
	{
		if (this.dirty)
		{
			//System.arraycopy(this.dirtydata, 0, this.data, 0, this.sectorsize);
			this.dirty = false;
		}
	}

	public synchronized boolean isDirty() {
		return dirty;
	}



	
}
