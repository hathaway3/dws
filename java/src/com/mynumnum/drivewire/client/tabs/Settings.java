package com.mynumnum.drivewire.client.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Settings extends Composite {
	private final static String TABNAME = "Settings"; 

	private static SettingsUiBinder uiBinder = GWT
			.create(SettingsUiBinder.class);

	interface SettingsUiBinder extends UiBinder<Widget, Settings> {
	}


	public Settings(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
	}


	/**
	 * @return the tabname
	 */
	public static String getTabname() {
		return TABNAME;
	}

}
