package com.groupunix.drivewireui;

public class DiskDef 
{
	private int drive = -1;
	private String path = "";
	private int offset = 0;
	private int sizelimit = -1;
	private boolean sync = true;
	private boolean expand = true;
	private boolean writeprotect = false;
	
	private boolean fswriteable = false;
	private boolean writeable = false;
	private boolean randomwriteable = false;
	private boolean namedobject = false;
	private boolean syncfromsource = false;
	
	private int sectors = 0;
	private int dirty = 0;
	private int lsn = 0;
	private int reads = 0;
	private int writes = 0;
	
	private boolean loaded = false;
	
	
	public void setDrive(int drive) {
		this.drive = drive;
	}
	public int getDrive() {
		return drive;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getOffset() {
		return offset;
	}
	public void setSizelimit(int sizelimit) {
		this.sizelimit = sizelimit;
	}
	public int getSizelimit() {
		return sizelimit;
	}
	public void setSync(boolean sync) {
		this.sync = sync;
	}
	public boolean isSync() {
		return sync;
	}
	public void setExpand(boolean expand) {
		this.expand = expand;
	}
	public boolean isExpand() {
		return expand;
	}
	public void setWriteprotect(boolean writeprotect) {
		this.writeprotect = writeprotect;
	}
	public boolean isWriteprotect() {
		return writeprotect;
	}
	public void setFswriteable(boolean fswriteable) {
		this.fswriteable = fswriteable;
	}
	public boolean isFswriteable() {
		return fswriteable;
	}
	public void setWriteable(boolean writeable) {
		this.writeable = writeable;
	}
	public boolean isWriteable() {
		return writeable;
	}
	public void setRandomwriteable(boolean randomwriteable) {
		this.randomwriteable = randomwriteable;
	}
	public boolean isRandomwriteable() {
		return randomwriteable;
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
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	public boolean isLoaded() {
		return loaded;
	}

	public void setNamedobject(boolean namedobject) {
		this.namedobject = namedobject;
	}
	public boolean isNamedobject() {
		return namedobject;
	}
	public void setSyncfromsource(boolean syncfromsource) {
		this.syncfromsource = syncfromsource;
	}
	public boolean isSyncfromsource() {
		return syncfromsource;
	}


	
	
}

