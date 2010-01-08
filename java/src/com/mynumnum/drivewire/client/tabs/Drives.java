package com.mynumnum.drivewire.client.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mynumnum.drivewire.client.filemanager.FileManager;

public class Drives extends Composite {
	private final static String TABNAME = "Drives"; 
	private static PopupPanel pp = new PopupPanel();

	private static DrivesUiBinder uiBinder = GWT.create(DrivesUiBinder.class);

	interface DrivesUiBinder extends UiBinder<Widget, Drives> {
	}

	@UiField
	static Label selectedFile;
	
	@UiConstructor
	public Drives(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
	}


	/**
	 * @return the tabname
	 */
	public static String getTabname() {
		return TABNAME;
	}
	
	@UiHandler ("chooseButton")
	void onClick(ClickEvent e) {
		pp.setWidget(new FileManager());
		pp.center();
		
	}
	public static void hidePopup() {
		pp.hide();
	}
	
	// This method will set the file name that the user sees on the web page
	// this might need to also include the path
	public static void setFileName(String fileName) {
		selectedFile.setText(fileName);
	}

}
