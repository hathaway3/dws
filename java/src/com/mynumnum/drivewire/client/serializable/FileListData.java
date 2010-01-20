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
	private String directoryName;
	private ArrayList<FileDetails> fileNames;
	private String fileSeparator;
	public FileListData() {
	}
	/**
	 * @param fileFolder
	 * @param fileNames
	 */
	public FileListData(String fileFolder, ArrayList<FileDetails> fileNames) {
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
	public ArrayList<FileDetails> getFileNames() {
		return fileNames;
	}
	public void setFileFolder(String fileFolder) {
		// TODO Auto-generated method stub
		this.fileFolder = fileFolder;
		
	}
	public void setFileNames(ArrayList<FileDetails> files) {
		// TODO Auto-generated method stub
		this.fileNames = files;
		
	}
	public static class FileDetails implements IsSerializable {
		private String fileName;
		private String diskName;
		public FileDetails() {
			// TODO Auto-generated constructor stub
		}
		public String getDiskName() {
			return diskName;
		}
		public String getFileName() {
			return fileName;
		}
		public void setDiskName(String diskName) {
			this.diskName = diskName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
	}
	public void setDirectoryName(String name) {
		// TODO Auto-generated method stub
		this.directoryName = name;
		
	}
	public String getDirectoryName() {
		return directoryName;
	}
	public String getFileSeparator() {
		return fileSeparator;
	}
	public void setFileSeparator(String fileSeparator) {
		this.fileSeparator = fileSeparator;
	}
}
