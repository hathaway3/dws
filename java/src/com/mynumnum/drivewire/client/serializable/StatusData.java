/**
 * 
 */
package com.mynumnum.drivewire.client.serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Jim Hathaway
 * This serializable class is used to transfer data via RPC from the
 * server to the client in one nice package.
 *
 */
public class StatusData implements IsSerializable {
	private byte lastDrive = 0;
	private int readRetries = 0;
	private int writeRetries = 0;
	private int sectorsRead = 0;
	private int sectorsWritten = 0;
	private String lastOpcode = null;
	private String lastGetStat = null;
	private String lastSetStat = null;
	private byte[] lastLSN = new byte[3];
	private String device = "";
	private int model = 0;
	private VersionData version;
	
	
	public StatusData(byte lastDrive, int readRetries, int writeRetries,
			int sectorsRead, int sectorsWritten, String lastOpcode,
			String lastGetStat, String lastSetStat, int lastChecksum,
			int lastError, byte[] lastLSN, String lastMessage,
			String device, int model, VersionData version) {
		super();
		
		// Set the new vales
		this.lastDrive = lastDrive;
		this.readRetries = readRetries;
		this.writeRetries = writeRetries;
		this.sectorsRead = sectorsRead;
		this.sectorsWritten = sectorsWritten;
		this.lastOpcode = lastOpcode;
		this.lastGetStat = lastGetStat;
		this.lastSetStat = lastSetStat;
		this.lastLSN = lastLSN;
		this.device = device;
		this.model = model;
		this.setVersion(version);
		
	}
	public StatusData() {
		// TODO Auto-generated constructor stub
	}
	public String getLastDrive() {
		return String.valueOf(lastDrive);
	}
	public void setLastDrive(byte lastDrive) {
		this.lastDrive = lastDrive;
	}
	public String getReadRetries() {
		return String.valueOf(readRetries);
	}
	public void setReadRetries(int readRetries) {
		this.readRetries = readRetries;
	}
	public String getWriteRetries() {
		return String.valueOf(writeRetries);
	}
	public void setWriteRetries(int writeRetries) {
		this.writeRetries = writeRetries;
	}
	public String getSectorsRead() {
		return String.valueOf(sectorsRead);
	}
	public void setSectorsRead(int sectorsRead) {
		this.sectorsRead = sectorsRead;
	}
	public String getSectorsWritten() {
		return String.valueOf(sectorsWritten);
	}
	public void setSectorsWritten(int sectorsWritten) {
		this.sectorsWritten = sectorsWritten;
	}
	public String getLastOpcode() {
		return String.valueOf(lastOpcode);
	}
	public void setLastOpcode(String string) {
		this.lastOpcode = string;
	}
	public String getLastGetStat() {
		return String.valueOf(lastGetStat);
	}
	public void setLastGetStat(String string) {
		this.lastGetStat = string;
	}
	public String getLastSetStat() {
		return String.valueOf(lastSetStat);
	}
	public void setLastSetStat(String string) {
		this.lastSetStat = string;
	}
	public String getLastLSN() {
		long l = ((lastLSN[0] & 0xFF) << 16) + ((lastLSN[1] & 0xFF) << 8) + (lastLSN[2] & 0xFF);
		return String.valueOf(l);
	}
	public void setLastLSN(byte[] lastLSN) {
		this.lastLSN = lastLSN;
	}

	public String getDevice() {
		// TODO Auto-generated method stub
		return device;
	}
	public String getModel() {
		String model = "";
		switch(this.model)
		{
			case 1:
				model = "CoCo 1 at 38400 bps";
				break;
			case 2:
				model = "CoCo 2 at 57600 bps";
				break;
			default:
				model = "CoCo 3 at 115200 bps";
				break;
			
		}
		return model;
	}
	public String getWriteGood() {
		return String.valueOf(this.sectorsWritten - this.writeRetries);
	}
	public String getReadGood() {
		// TODO Auto-generated method stub
		return String.valueOf(this.sectorsRead - this.readRetries);
	}

	
	public void setDevice(String device) {
		this.device = device;
	}
	public void setModel(int model) {
		this.model = model;
	}
	public void setVersion(VersionData version)
	{
		this.version = version;
	}
	public VersionData getVersion()
	{
		return version;
	}



}
