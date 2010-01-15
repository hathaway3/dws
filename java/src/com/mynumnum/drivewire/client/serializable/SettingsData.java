/**
 * 
 */
package com.mynumnum.drivewire.client.serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Jim Hathaway
 * This class will be used to transfer the settings of the 'Settings' tab from the server to the client
 *
 */
public class SettingsData implements IsSerializable {
	private String port;
	private int model;
	private boolean tcpServerEnabled;
	private int tcpPort;
	private String logLevel;
	private boolean writeToFile;
	private String logFileName;
	public SettingsData() {
		// This must be here or GWT will not serialize
	}
	/**
	 * @param port
	 * @param model
	 * @param tcpServerEnabled
	 * @param tcpPort
	 * @param logLevel
	 * @param writeToFile
	 * @param logFileName
	 */
	public SettingsData(String port, int model, boolean tcpServerEnabled,
			int tcpPort, String logLevel, boolean writeToFile,
			String logFileName) {
		super();
		this.port = port;
		this.model = model;
		this.tcpServerEnabled = tcpServerEnabled;
		this.tcpPort = tcpPort;
		this.logLevel = logLevel;
		this.writeToFile = writeToFile;
		this.logFileName = logFileName;
	}
	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * @return the model
	 */
	public int getModel() {
		return model;
	}
	/**
	 * @param model the model to set
	 */
	public void setModel(int model) {
		this.model = model;
	}
	/**
	 * @return the tcpServerEnabled
	 */
	public boolean isTcpServerEnabled() {
		return tcpServerEnabled;
	}
	/**
	 * @param tcpServerEnabled the tcpServerEnabled to set
	 */
	public void setTcpServerEnabled(boolean tcpServerEnabled) {
		this.tcpServerEnabled = tcpServerEnabled;
	}
	/**
	 * @return the tcpPort
	 */
	public int getTcpPort() {
		return tcpPort;
	}
	/**
	 * @param tcpPort the tcpPort to set
	 */
	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}
	/**
	 * @return the logLevel
	 */
	public String getLogLevel() {
		return logLevel;
	}
	/**
	 * @param logLevel the logLevel to set
	 */
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	/**
	 * @return the writeToFile
	 */
	public boolean isWriteToFile() {
		return writeToFile;
	}
	/**
	 * @param writeToFile the writeToFile to set
	 */
	public void setWriteToFile(boolean writeToFile) {
		this.writeToFile = writeToFile;
	}
	/**
	 * @return the logFileName
	 */
	public String getLogFileName() {
		return logFileName;
	}
	/**
	 * @param logFileName the logFileName to set
	 */
	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}
	
	

}
