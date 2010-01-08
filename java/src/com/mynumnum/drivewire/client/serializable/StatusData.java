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
	private byte lastOpcode = 0;
	private byte lastGetStat = (byte) 255;
	private byte lastSetStat = (byte) 255;
	private byte[] lastLSN = new byte[3];
	private String lastMessage = "";
	private String device = "";
	private int model = 0;

	public StatusData(byte lastDrive, int readRetries, int writeRetries,
			int sectorsRead, int sectorsWritten, byte lastOpcode,
			byte lastGetStat, byte lastSetStat, int lastChecksum,
			int lastError, byte[] lastLSN, String lastMessage,
			String device, int model) {
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
		this.lastMessage = lastMessage;
		this.device = device;
		this.model = model;
		
		
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
	public void setLastOpcode(byte lastOpcode) {
		this.lastOpcode = lastOpcode;
	}
	public String getLastGetStat() {
		return String.valueOf(lastGetStat);
	}
	public void setLastGetStat(byte lastGetStat) {
		this.lastGetStat = lastGetStat;
	}
	public String getLastSetStat() {
		return String.valueOf(lastSetStat);
	}
	public void setLastSetStat(byte lastSetStat) {
		this.lastSetStat = lastSetStat;
	}
	public String getLastLSN() {
		long l = ((lastLSN[0] & 0xFF) << 16) + ((lastLSN[1] & 0xFF) << 8) + (lastLSN[2] & 0xFF);
		return String.valueOf(l);
	}
	public void setLastLSN(byte[] lastLSN) {
		this.lastLSN = lastLSN;
	}
	public String getLastMessage() {
		return lastMessage;
	}
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
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



}
