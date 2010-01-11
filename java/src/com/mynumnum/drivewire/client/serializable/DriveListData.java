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
	private String diskName;
	public DriveListData() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param fileName
	 * @param writeProtect
	 * @param diskName 
	 */
	public DriveListData(String fileName, boolean writeProtect, int driveNumber, String diskName) {
		super();
		this.fileName = fileName;
		this.writeProtect = writeProtect;
		this.driveNumber = driveNumber;
		this.diskName = diskName;
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
	public String getDiskName() {
		return diskName;
	}
	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}
}
