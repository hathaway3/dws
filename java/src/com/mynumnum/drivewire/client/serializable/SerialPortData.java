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
	private boolean isConnected;
	private int opens;
	private byte PD_INT;
	private byte PD_QUT;
	private String prettyPort;
	private int port;
	private String hostIP;
	private int hostPort;
	private String username;
	private String procd;
	
	public SerialPortData() {
		// Nothing to do here, but this is required if you expect GWT to serialize this
		// data, so please don't remove this!
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

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}

	public int getHostPort() {
		return hostPort;
	}

	public String getPrettyOpen()
	{
		String tmp = new String();
		
		if (this.opens > 0)
		{
			tmp = this.opens + " opens";
		}
		else
		{
			tmp = "none";
		}
		
		return(tmp);
	}
	
	public void setOpens(int ops)
	{
		this.opens = ops;
	}

	public String getUsername()
	{
		return(this.username);
	}

	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getPrettyPD()
	{
		return(this.procd);
	}
	
	public void setPrettyPD(String pd)
	{
		this.procd = pd;
	}



}
