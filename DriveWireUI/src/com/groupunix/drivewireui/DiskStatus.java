package com.groupunix.drivewireui;

public class DiskStatus 
{
	private int serial = -1;
	private Boolean loaded = false;
	private int sectors = 0;
	private int dirty = 0;
	private int lsn = 0;
	private int reads = 0;
	private int writes = 0;
	
	public void setSerial(int serial) {
		this.serial = serial;
	}
	public int getSerial() {
		return serial;
	}
	public void setLoaded(Boolean loaded) {
		this.loaded = loaded;
	}
	public Boolean getLoaded() {
		return loaded;
	}
	public void setSectors(int sectors) {
		this.sectors = sectors;
	}
	public int getSectors() {
		return sectors;
	}
	public void setDirty(int dirty) {
		this.dirty = dirty;
	}
	public int getDirty() {
		return dirty;
	}
	public void setLsn(int lsn) {
		this.lsn = lsn;
	}
	public int getLsn() {
		return lsn;
	}
	public void setReads(int reads) {
		this.reads = reads;
	}
	public int getReads() {
		return reads;
	}
	public void setWrites(int writes) {
		this.writes = writes;
	}
	public int getWrites() {
		return writes;
	}
	
	
}
