/**
 * 
 */
package com.mynumnum.drivewire.client.serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Jim Hathaway
 *
 */
public class DriveListData implements IsSerializable {
	private String fileName;
	private boolean writeProtect;
	private long diskSectors;
	private int driveNumber;
	public DriveListData() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param fileName
	 * @param writeProtect
	 */
	public DriveListData(String fileName, boolean writeProtect, int driveNumber) {
		super();
		this.fileName = fileName;
		this.writeProtect = writeProtect;
		this.driveNumber = driveNumber;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return the writeProtect
	 */
	public boolean isWriteProtect() {
		return writeProtect;
	}
	/**
	 * @param writeProtect the writeProtect to set
	 */
	public void setWriteProtect(boolean writeProtect) {
		this.writeProtect = writeProtect;
	}

	public void setDiskSectors(long diskSectors) {
		this.diskSectors = diskSectors;
		
	}
	public long getDiskSectors() {
		return diskSectors;
	}

	public int getDriveNumber() {
		// TODO Auto-generated method stub
		return driveNumber;
	}
	
	public void setDriveNumber(int driveNumber) {
		this.driveNumber = driveNumber;
	}
}
