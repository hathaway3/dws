package com.mynumnum.drivewire.client.tabs;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mynumnum.drivewire.client.DriveWireGWT;
import com.mynumnum.drivewire.client.common.Common;
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
	public Drives() {
		initWidget(uiBinder.createAndBindUi(this));
		getDrives();
	}


	private void getDrives() {
		// TODO Auto-generated method stub
		// Fetch drive list from drivewire server
		// after successful fetch populate the listbox
		DriveWireGWT.driveWireService.getDrives(new AsyncCallback<ArrayList<String>>() {
			
			@Override
			public void onSuccess(ArrayList<String> result) {
				// Populate the drive list
				for (String s : result) {
					drivesListBox.addItem(s);
				}
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}
		});
		
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
	
	@UiField
	ListBox drivesListBox;

}
