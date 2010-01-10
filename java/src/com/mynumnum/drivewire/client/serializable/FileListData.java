/**
 * 
 */
package com.mynumnum.drivewire.client.serializable;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Jim Hathaway
 * This class is used to serialize the fileFolders and file names from the
 * server.
 *
 */
public class FileListData implements IsSerializable{
	private String fileFolder;
	private ArrayList<String> fileNames;
	public FileListData() {
	}
	/**
	 * @param fileFolder
	 * @param fileNames
	 */
	public FileListData(String fileFolder, ArrayList<String> fileNames) {
		super();
		this.fileFolder = fileFolder;
		this.fileNames = fileNames;
	}
	/**
	 * @return the fileFolder
	 */
	public String getFileFolder() {
		return fileFolder;
	}
	/**
	 * @return the fileNames
	 */
	public ArrayList<String> getFileNames() {
		return fileNames;
	}
	
}
