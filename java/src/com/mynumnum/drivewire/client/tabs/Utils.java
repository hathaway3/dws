/**
 * 
 */
package com.mynumnum.drivewire.client.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Jimbo
 *
 */
public class Utils extends Composite {
	private final static String TABNAME = "Utils"; 

	private static UtilsUiBinder uiBinder = GWT.create(UtilsUiBinder.class);

	interface UtilsUiBinder extends UiBinder<Widget, Utils> {
	}

	@UiField
	Button button;

	public Utils(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));

		// Can access @UiField after calling createAndBindUi
		button.setText(firstName);
	}

	@UiHandler("button")
	void onClick(ClickEvent e) {
		Window.alert("Hello!");
	}

	/**
	 * @return the tabname
	 */
	public static String getTabname() {
		return TABNAME;
	}

}
