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

public class Log extends Composite {
	private final static String TABNAME = "Log"; 

	private static LogUiBinder uiBinder = GWT.create(LogUiBinder.class);

	interface LogUiBinder extends UiBinder<Widget, Log> {
	}

	@UiField
	Button button;

	public Log(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
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
