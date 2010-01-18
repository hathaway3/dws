package com.groupunix.drivewireserver.dwprotocolhandler;

// import org.apache.log4j.Logger;

public class DWDiskSector 
{
	private int LSN;
	private byte[] data = new byte[256];
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
		this.data = newdata;
	}

	public synchronized void setData(byte[] newdata, boolean dirty) 
	{
		this.dirty = dirty;
		this.data = newdata;
	}
	
	public synchronized byte[] getData() 
	{
		return this.data;
	}

	public synchronized void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public synchronized boolean isDirty() {
		return dirty;
	}
	
	
}
