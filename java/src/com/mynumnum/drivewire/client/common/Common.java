/**
 * 
 */
package com.mynumnum.drivewire.client.common;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
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
	private static DialogBox errorBox = new DialogBox();
	public Common() {
		// Nothing to do here
	}
	
	public static void showErrorMessage() {
		errorBox.setWidget(new Label(DriveWireGWT.ERROR_MESSAGE));
		showMessage();
	}
	
	public static void showErrorMessage(String errorMessage) {
		errorBox.setWidget(new HTML(errorMessage));
		showMessage();
		
	}
	
	private static void showMessage() {
		errorBox.setHTML("<b>Error:</b>");
		errorBox.setAnimationEnabled(true);
		errorBox.setAutoHideEnabled(true);
		errorBox.setGlassEnabled(true);
		errorBox.center();
		
	}
}
