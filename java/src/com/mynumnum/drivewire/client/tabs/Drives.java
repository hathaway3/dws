package com.mynumnum.drivewire.client.tabs;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mynumnum.drivewire.client.DriveWireGWT;
import com.mynumnum.drivewire.client.common.Common;
import com.mynumnum.drivewire.client.filemanager.FileManager;
import com.mynumnum.drivewire.client.serializable.DriveListData;

public class Drives extends Composite {
	private final static String TABNAME = "Drives"; 
	private static DialogBox pp = new DialogBox();
	private static DialogBox db = new DialogBox();
	private static final int DRIVE_COLUMN = 0;

	private static int gridDriveNumber = 0;
	private static int selectedRow = 0;
	private static int previousRow = 0;
	private static String previousStyle;

	private static DrivesUiBinder uiBinder = GWT.create(DrivesUiBinder.class);

	interface DrivesUiBinder extends UiBinder<Widget, Drives> {
	}

	@UiField
	static Label selectedFile;
	
	@UiField
	VerticalPanel drivesPanel;
	
	@UiConstructor
	public Drives() {
		initWidget(uiBinder.createAndBindUi(this));
		drivesPanel.setSpacing(10);
		getDrives();
		updateFilesTable();
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
		FileManager fm = new FileManager();
		fm.getServerFileList("disk");
		pp.setWidget(fm);
		pp.setAnimationEnabled(true);
		pp.setGlassEnabled(true);
		pp.setText("Select disk to load");
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
	static
	FlexTable drivesListFlexTable;
	
	@UiHandler ("drivesListFlexTable")
	void onTableClick(ClickEvent event) {
		// Set the currently seleted row in the table
		selectedRow = drivesListFlexTable.getCellForEvent(event).getRowIndex();
		// If the drive number is 0 then the user selected the header of the table (row 0) and we dont care about that row
		if (selectedRow > 0) {
			int selectedDrive = Integer.valueOf(drivesListFlexTable.getText(selectedRow, DRIVE_COLUMN));
			// the real drive number is the row -1
			gridDriveNumber = selectedDrive; 
			selectRow(selectedRow);
			
		}

	}
	
	@UiHandler ("writeEnableButton")
	void onClick4(ClickEvent e) {
		setWriteProtect(gridDriveNumber, false);
		
	}
	/**
	 * Set the write proctect for the given drive number 
	 * @param driveNumber
	 * @param writeProtect
	 */
	private void setWriteProtect(int driveNumber, boolean writeProtect) {
		// Send to server for processing
		DriveWireGWT.driveWireService.setDriveWriteProtect(driveNumber, writeProtect, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}

			@Override
			public void onSuccess(String result) {
				// After we update the disk image status we need to redraw the disk grid
				updateFilesTable();
				
			}
		});
		
	}
	// TODO Set the gridDRiveNumber from the flex table (selected row)
	@UiHandler ("writeProtectButton")
	void onClick2(ClickEvent e) {
		setWriteProtect(gridDriveNumber, true);
		
	}

	@UiHandler ("insertDiskButton")
	void onClick3(ClickEvent e) {
		// Get selected drive #
		final Integer driveNumber = Integer.valueOf(drivesListBox.getValue(drivesListBox.getSelectedIndex()));
		DriveWireGWT.driveWireService.loadDiskFromFile(driveNumber, selectedFile.getText(), new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}

			@Override
			public void onSuccess(String result) {
				// If we receive an error message from the server then we will let the user know
				if (!result.equals("none")) {
					Common.showErrorMessage(result);
				} else {
					setWriteProtect(driveNumber, writeProtectCheckBox.getValue());
				}
				
			}

		});
		// send to server so it can be processed
		
	}
	
	private static void updateFilesTable() {
		DriveWireGWT.driveWireService.getDrivesList(new AsyncCallback<ArrayList<DriveListData>>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}

			public void onSuccess(ArrayList<DriveListData> result) {
				drivesListFlexTable.removeAllRows();
				// Set styles of flex table
				StyleInjector.inject(com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().getText());
				drivesListFlexTable.setStyleName(com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().sample());

				// Set header row style
				drivesListFlexTable.getRowFormatter().setStyleName(0, com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().h1());

				// Set text of header
				int column = 0;
				drivesListFlexTable.setText(0, column++, "Drive");
				drivesListFlexTable.setText(0, column++, "File Name");
				drivesListFlexTable.setText(0, column++, "Disk Name");
				drivesListFlexTable.setText(0, column++, "WP");
				drivesListFlexTable.setText(0, column++, "Sectors");
				int row = 1;
				column = 0;
				// Loop for each returned disk
				for (DriveListData dld : result) {
					drivesListFlexTable.setText(row, column++, String.valueOf(dld.getDriveNumber()));
					drivesListFlexTable.setText(row, column++, dld.getFileName());
					drivesListFlexTable.setText(row, column++, dld.getDiskName());
					drivesListFlexTable.setText(row, column++, String.valueOf(dld.isWriteProtect()));
					drivesListFlexTable.setText(row++, column++, String.valueOf(dld.getDiskSectors()));
					column = 0;
					if (row % 2 == 0)
						drivesListFlexTable.getRowFormatter().setStyleName(row, com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().d0());
					else
						drivesListFlexTable.getRowFormatter().setStyleName(row, com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().d1());

					
				}
				findDriveAfterUpdate();
				
			}

		});
		
		
	}

	private static void findDriveAfterUpdate() {
		// TODO Auto-generated method stub
		int totalRows = drivesListFlexTable.getRowCount();
		for (int row = 1; row <= totalRows; row++) {
			if (Integer.valueOf(drivesListFlexTable.getText(row, DRIVE_COLUMN)) == gridDriveNumber) {
				selectRow(row);
			}
		}
		
	}

	private static void selectRow(int row) {
		if (previousRow != 0) {
			drivesListFlexTable.getRowFormatter().setStyleName(previousRow, previousStyle);
		}
		previousRow = row;
		previousStyle = drivesListFlexTable.getRowFormatter().getStyleName(row);
		drivesListFlexTable.getRowFormatter().setStyleName(row, com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().green());
	}

	@UiHandler ("openSetButton")
	void onOpenSetButtonClick(ClickEvent e) {
		FileManager fm = new FileManager();
		fm.getServerFileList("set");
		pp.setWidget(fm);
		pp.setAnimationEnabled(true);
		pp.setGlassEnabled(true);
		pp.setText("Select the disk set to load");
		pp.center();

		
	}

	@UiHandler ("saveSetButton")
	void onSaveSetButtonClick(ClickEvent e) {
		// Open box to get the file name
		db.setAnimationEnabled(true);
		db.setGlassEnabled(true);
		db.setText("Enter file name for this disk set");
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(10);
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new Label("File Name:"));
		hp.setSpacing(10);
		final TextBox fileTextBox = new TextBox();
		hp.add(fileTextBox);
		vp.add(hp);
		HorizontalPanel hp2 = new HorizontalPanel();
		hp2.setSpacing(10);
		Button ok = new Button("OK");
		Button cancel = new Button("Cancel");
		hp2.add(ok);
		hp2.add(cancel);
		vp.add(hp2);
		db.setWidget(vp);
		ok.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				saveDiskSet(fileTextBox.getText());
				
			}
		});
		cancel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				db.hide();
				
			}
		});
		
		db.setWidget(vp);
		db.center();
		fileTextBox.setFocus(true);
		
		
	}

	/**
	 * Open the disk set on the server
	 * and refresh the files table when the call is complete
	 * @param fileName
	 */
	public static void openDiskSet(String fileName) {
		DriveWireGWT.driveWireService.openDiskSet(fileName, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}

			@Override
			public void onSuccess(String result) {
				updateFilesTable();
				
			}
		});
		
	}
	
	public static void saveDiskSet(String fileName) {
		DriveWireGWT.driveWireService.saveDiskSet(fileName, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage();
				
			}

			@Override
			public void onSuccess(String result) {
				db.hide();
				
			}
		});
	}
	
	@UiHandler("ejectButton")
	void onEjectButtonClick(ClickEvent e) {
		ejectDisk(gridDriveNumber);
		
	}
	/**
	 * Request that the server eject the requested disk
	 * @param driveNumber
	 */
	private static void ejectDisk(Integer driveNumber) {
		DriveWireGWT.driveWireService.ejectDisk(driveNumber, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}

			@Override
			public void onSuccess(String result) {
				gridDriveNumber = 0;
				updateFilesTable();
				
			}
		});
	}

}
