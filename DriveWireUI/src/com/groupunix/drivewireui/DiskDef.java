package com.groupunix.drivewireui;

public class DiskDef 
{
	private int drive;
	private String path;
	private int offset;
	private int sizelimit;
	private boolean sync;
	private boolean expand;
	private boolean writeprotect;
	
	
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
	
	
}

