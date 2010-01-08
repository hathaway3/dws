/**
 * 
 */
package com.mynumnum.drivewire.client.bundle;

import com.google.gwt.core.client.GWT;

/**
 * @author Jim Hathaway
 *
 */
public interface ClientBundle extends
		com.google.gwt.resources.client.ClientBundle {
	public static final ClientBundle INSTANCE =  GWT.create(ClientBundle.class);


	@Source("com/mynumnum/drivewire/shared/DriveWire.css")
	DriveWire driveWire();

}
