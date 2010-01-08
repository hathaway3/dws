/**
 * 
 */
package com.mynumnum.drivewire.client.filemanager;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.mynumnum.drivewire.client.DriveWireGWT;
import com.mynumnum.drivewire.client.common.Common;
import com.mynumnum.drivewire.client.serializable.FileListData;
import com.mynumnum.drivewire.client.tabs.Drives;

/**
 * @author Jim Hathaway
 *
 */
public class FileManager extends Composite {
	private static String fileName;
	private static String fileFolder;
	
	private static FileManagerUiBinder uiBinder = GWT
			.create(FileManagerUiBinder.class);

	interface FileManagerUiBinder extends UiBinder<Widget, FileManager> {
	}


	public FileManager() {
		
		initWidget(uiBinder.createAndBindUi(this));
		// TODO Fetch the Directory listing from the 'server' and add it to the tree
		getServerFileList();
		// The following code was only used for testing
		/*
		TreeItem ti = new TreeItem();
		ti.setText("this is a test");
		ti.addItem("item 1");
		ti.addItem("item 2");
		ti.addItem("item 3");
		TreeItem ti2 = new TreeItem();
		ti2.setText("this is a test2");
		ti2.addItem("item 1");
		ti2.addItem("item 2");
		ti2.addItem("item 3");
		fileTree.addItem(ti);
		fileTree.addItem(ti2);
		*/
		
	}
	private void getServerFileList() {
		// TODO Auto-generated method stub
		DriveWireGWT.driveWireService.getFileList(new AsyncCallback<ArrayList<FileListData>>() {

			@Override
			public void onFailure(Throwable caught) {
				new Common().showErrorMessage();
				
			}

			@Override
			public void onSuccess(ArrayList<FileListData> result) {
				populateTreeList(result);
				
			}

		});
	}
	// Populate and show the tree list to the user
	private void populateTreeList(ArrayList<FileListData> fileData) {
		// For each folder we will add a new top level tree value
		for (FileListData fld : fileData) {
			TreeItem ti = new TreeItem();
			fileTree.addItem(ti);
			// For each folder we will add all the folder 'children'
			for (String file : fld.getFileNames()) {
				ti.addItem(file);
			}
		}
		
	}
	@UiField
	Tree fileTree;
	@UiHandler("okButton")
	void onClick2(ClickEvent e) {
		// Do something when the user selects ok
		// TODO populate the file box with the name of the file selected by the user
		Drives.setFileName(getFileName());
	}
	// TODO make this method return the correct fileFolder and filename path based on the OS
	private String getFileName() {
		return fileFolder + "/" + fileName;
	}
	@UiHandler("cancelButton")
	void onClick3(ClickEvent e) {
		// Close the 'file manager'
		Drives.hidePopup();
	}

	@UiHandler("fileTree") 
	void onSelection(SelectionEvent<TreeItem> event) {
		fileFolder = event.getSelectedItem().getParentItem().getText();
		fileName = event.getSelectedItem().getText();
	}
}
