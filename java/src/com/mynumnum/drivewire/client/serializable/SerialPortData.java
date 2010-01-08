/**
 * 
 */
package com.mynumnum.drivewire.client.serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Jim Hathaway
 *
 */
public class SerialPortData implements IsSerializable {
	private String mode;
	private boolean isPasswordSet;
	private boolean isConnected;
	private boolean isCocoInit;
	// This field is optional for a serial port
	private boolean isActionFileDefined;
	private byte PD_INT;
	private byte PD_QUT;
	private String prettyPort;
	private int port;
	
	public SerialPortData() {
		// Nothing to do here, but this is required if you expect GWT to serialize this
		// data, so please don't remove this!
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @param isPasswordSet the isPasswordSet to set
	 */
	public void setPasswordSet(boolean isPasswordSet) {
		this.isPasswordSet = isPasswordSet;
	}

	/**
	 * @return the isPasswordSet
	 */
	public boolean isPasswordSet() {
		return isPasswordSet;
	}

	/**
	 * @param isConnected the isConnected to set
	 */
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	/**
	 * @return the isConnected
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * @param isCocoInit the isCocoInit to set
	 */
	public void setCocoInit(boolean isCocoInit) {
		this.isCocoInit = isCocoInit;
	}

	/**
	 * @return the isCocoInit
	 */
	public boolean isCocoInit() {
		return isCocoInit;
	}

	/**
	 * @param isActionFileDefined the isActionFileDefined to set
	 */
	public void setActionFileDefined(boolean isActionFileDefined) {
		this.isActionFileDefined = isActionFileDefined;
	}

	/**
	 * @return the isActionFileDefined
	 */
	public boolean isActionFileDefined() {
		return isActionFileDefined;
	}

	/**
	 * @param pD_INT the pD_INT to set
	 */
	public void setPD_INT(byte pD_INT) {
		PD_INT = pD_INT;
	}

	/**
	 * @return the pD_INT
	 */
	public byte getPD_INT() {
		return PD_INT;
	}

	/**
	 * @param pD_QUT the pD_QUT to set
	 */
	public void setPD_QUT(byte pD_QUT) {
		PD_QUT = pD_QUT;
	}

	/**
	 * @return the pD_QUT
	 */
	public byte getPD_QUT() {
		return PD_QUT;
	}

	/**
	 * @return the prettyPort
	 */
	public String getPrettyPort() {
		return prettyPort;
	}

	/**
	 * @param prettyPort the prettyPort to set
	 */
	public void setPrettyPort(String prettyPort) {
		this.prettyPort = prettyPort;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}



}
