/**
 * 
 */
package com.mynumnum.drivewire.client.serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Jim Hathaway
 *
 */
public class VersionData implements IsSerializable {
	private String version;
	private String date;
	public VersionData() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param version
	 * @param date
	 */
	public VersionData(String version, String date) {
		super();
		this.version = version;
		this.date = date;
	}

	public String getDate() {
		return date;
	}
	public String getVersion() {
		return version;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setVersion(String version) {
		this.version = version;
	}

}
