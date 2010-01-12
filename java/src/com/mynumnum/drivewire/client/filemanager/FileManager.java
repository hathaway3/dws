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
import com.google.gwt.user.client.ui.Label;
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
	private static String fileType;
	
	private static FileManagerUiBinder uiBinder = GWT
			.create(FileManagerUiBinder.class);

	interface FileManagerUiBinder extends UiBinder<Widget, FileManager> {
	}


	public FileManager() {
		initWidget(uiBinder.createAndBindUi(this));
		// TODO Fetch the Directory listing from the 'server' and add it to the tree
		
	}
	/**
	 * Param type should be either 'disk' or 'set'
	 * @param fileType
	 */
	public void getServerFileList(String fileType) {
		// TODO Add option to filter file type (so we can use disk images or disk sets)
		FileManager.fileType = fileType;
		DriveWireGWT.driveWireService.getFileList(fileType, new AsyncCallback<ArrayList<FileListData>>() {

			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}

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
			// Set the file folder for this tree element
			ti.setText(fld.getFileFolder());
			fileTree.addItem(ti);
			// For each folder we will add all the folder 'children'
			for (FileListData.FileDetails file : fld.getFileNames()) {
				Label fileLabel = new Label();
				fileLabel.setTitle(file.getDiskName());
				fileLabel.setText(file.getFileName());
				ti.addItem(fileLabel);
			}
		}
		
	}
	@UiField
	Tree fileTree;
	@UiHandler("okButton")
	void onClick2(ClickEvent e) {
		// Do something when the user selects ok
		if (fileType.equals("disk"))
			Drives.setFileName(getFileName());
		if (fileType.equals("set"))
			Drives.openDiskSet(getFileName());
		// Close the file manager - always to this for either disk sets or disk images
		Drives.hidePopup();
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
		Label fileLabel = (Label) event.getSelectedItem().getWidget(); 
		fileName = fileLabel.getText();
	}
	
}
