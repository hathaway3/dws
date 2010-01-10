/**
 * 
 */
package com.mynumnum.drivewire.client.common;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.mynumnum.drivewire.client.DriveWireGWT;

/**
 * @author Jim Hathaway
 * This class contains common code that can be used across the client GWT code.
 * There is nothing special about this class or the package, I just wanted to 
 * have a place to store common code.  The only restriction is that it must be
 * located somewhere under com.mynumnum.client
 *
 */
public class Common {
	public Common() {
		// Nothing to do here
	}
	public static void showErrorMessage() {
		PopupPanel p = new PopupPanel();
		p.add(new Label(DriveWireGWT.ERROR_MESSAGE));
		p.center();
	}
}
