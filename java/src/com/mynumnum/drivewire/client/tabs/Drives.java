package com.mynumnum.drivewire.client.tabs;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
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
		// Fetch drive list from drivewire server
		// after successful fetch populate the listbox
		// This should be the list of drives that can be assigned on the drivewire server
		// with a disk image
		DriveWireGWT.driveWireService.getDrives(new AsyncCallback<ArrayList<Integer>>() {
			
			@Override
			public void onSuccess(ArrayList<Integer> result) {
				// Populate the drive list
				for (Integer driveNumber : result) {
					drivesListBox.addItem("Drive " + driveNumber, driveNumber.toString());
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

	@UiField
	CheckBox writeProtectCheckBox;
	
	@UiField
	FlexTable drivesListFlexTable;

	@UiHandler ("writeProtectCheckBox")
	void onClick2(ClickEvent e) {
		// Get selected drive #
		Integer driveNumber = Integer.valueOf(drivesListBox.getValue(drivesListBox.getSelectedIndex()));
		// check box setting (t/f)
		boolean writeProtect = writeProtectCheckBox.getValue();
		// Send to server for processing
		DriveWireGWT.driveWireService.setDriveWriteProtect(driveNumber, writeProtect, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}

			@Override
			public void onSuccess(String result) {
				// Nothing to do here
				
			}
		});
		
	}
	
	@UiHandler ("insertDiskButton")
	void onClick3(ClickEvent e) {
		// Get selected drive #
		Integer driveNumber = Integer.valueOf(drivesListBox.getValue(drivesListBox.getSelectedIndex()));
		DriveWireGWT.driveWireService.loadDiskFromFile(driveNumber, selectedFile.getText(), new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}

			@Override
			public void onSuccess(String result) {
				// If we receive an error message from the server then we will let the user know
				if (!result.equals("none")) {
					Label error = new Label();
					PopupPanel pp = new PopupPanel();
					pp.add(error);
					pp.setAnimationEnabled(true);
					pp.setAutoHideEnabled(true);
					error.setText(result);
					pp.center();
				}
				updateFilesTable();
				
			}

		});
		// send to server so it can be processed
		
	}
	
	private void updateFilesTable() {
		// TODO Auto-generated method stub
		
		
	}
	

}
