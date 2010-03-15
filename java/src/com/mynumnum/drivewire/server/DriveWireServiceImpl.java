package com.mynumnum.drivewire.server;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDisk;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;
import com.mynumnum.drivewire.client.rpc.DriveWireService;
import com.mynumnum.drivewire.client.serializable.DriveListData;
import com.mynumnum.drivewire.client.serializable.FileListData;
import com.mynumnum.drivewire.client.serializable.SerialPortData;
import com.mynumnum.drivewire.client.serializable.SettingsData;
import com.mynumnum.drivewire.client.serializable.StatusData;
import com.mynumnum.drivewire.client.serializable.VersionData;

/**
 * The server side implementation of the RPC service.
 */

/*
@SuppressWarnings("serial")
public class DriveWireServiceImpl extends RemoteServiceServlet implements
		DriveWireService {
	private CharSequence filterString;
	public StatusData getStatusData() {
		StatusData sd = new StatusData();
		sd.setLastDrive(DWProtocolHandler.getLastDrive());
		sd.setLastGetStat(DWProtocolHandler.prettySS(DWProtocolHandler.getLastGetStat()));
		sd.setLastLSN(DWProtocolHandler.getLastLSN());
		sd.setLastOpcode(DWProtocolHandler.prettyOP(DWProtocolHandler.getLastOpcode()));
		sd.setLastSetStat(DWProtocolHandler.prettySS(DWProtocolHandler.getLastSetStat()));
		sd.setReadRetries(DWProtocolHandler.getReadRetries());
		sd.setSectorsRead(DWProtocolHandler.getSectorsRead());
		sd.setSectorsWritten(DWProtocolHandler.getSectorsWritten());
		sd.setWriteRetries(DWProtocolHandler.getWriteRetries());
		sd.setModel(DriveWireServer.getCocoModel());
		sd.setDevice(DriveWireServer.getPortName());
		sd.setVersion(new VersionData(DriveWireServer.DWServerVersion, DriveWireServer.DWServerVersionDate));
		return sd;
	}
	public ArrayList<SerialPortData> getPortData() {
		ArrayList<SerialPortData> portData = new ArrayList<SerialPortData>();
		for (int port = 0; port < DWVSerialPorts.MAX_PORTS; port++) {
			SerialPortData spd = new SerialPortData();
				
			spd.setConnected(DWVSerialPorts.isConnected(port));
			spd.setPD_INT(DWVSerialPorts.getPD_INT(port));
			spd.setPD_QUT(DWVSerialPorts.getPD_QUT(port));
			spd.setPrettyPort(DWVSerialPorts.prettyPort(port));
			spd.setPort(port);
			spd.setHostIP(DWVSerialPorts.getHostIP(port));
			spd.setHostPort(DWVSerialPorts.getHostPort(port));
			
			spd.setOpens(DWVSerialPorts.getOpen(port));
			spd.setPrettyPD(DWProtocolHandler.byteArrayToHexString(DWVSerialPorts.getDD(port)));
			
			// Add this instance of spd to our array list
			portData.add(spd);
			 	
		}
		
		return portData;
	}
	// TODO Need method created in DriveWireServer that can return this data
	public ArrayList<String> getLogFileData(int numberOfLines) {
		return(DriveWireServer.getLogEvents(numberOfLines));
	}
	// TODO need a method in DriveWireServer that can reset the log file.
	public String resetLogFile() {
		DriveWireServer.resetLogFile();
		return "Success";
	}
	// TODO fetch the file and folder data and return to client
	public ArrayList<FileListData> getFileList(String fileType) {
		File dir = new File(".");
		File fileList;
		if (fileType.equals("disk"))
			filterString = ".dsk";
		if (fileType.equals("set"))
			filterString = ".set";
		//System.out.println("the value of fileType is " + filterString);
		
		// Define filter for the types of files we want to see
		FilenameFilter fileFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
				return name.contains(filterString);
		    }
		};
		
		// This filter only returns directories
		FileFilter directoryFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		};
		
		// Get the current folder in the file system
		String rootFolder = System.getProperty("user.dir");

		ArrayList<FileListData> afld = new ArrayList<FileListData>();
		// Loop through each directory
		for (File name : dir.listFiles(directoryFilter)) {
			rootFolder = dir.getAbsolutePath() + java.io.File.separator + name.getName();
			FileListData fld = new FileListData();
			fld.setFileFolder(rootFolder);
			fld.setDirectoryName(name.getName());
			fld.setFileSeparator(java.io.File.separator);
			ArrayList<FileListData.FileDetails> files = new ArrayList<FileListData.FileDetails>();
			fileList = new File(rootFolder);
			// Loop through all files in the folder that match the appropriate filter
			for (String s : fileList.list(fileFilter)) {
				// See if we can determine the OS9 Disk Name
				//DWDisk disk = new DWDisk();
				//try {
				//	disk.setFilePath(rootFolder + java.io.File.separator + s);
				//} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				//}
				// Create a new file that will contain the disk name and file name
				FileListData.FileDetails file = new FileListData.FileDetails();
				//try {
				//	file.setDiskName(disk.getDiskName());
					//System.out.println("the disk name is " + disk.getDiskName());
				//} catch (Exception e) {
				//	file.setDiskName("unknown");
				//}
				file.setFileName(s);
				files.add(file);
				
			}
			// Add the files to the file list
			fld.setFileNames(files);
			// add this directory to the directory list
			afld.add(fld);
		}
		// return all the directories and files under the user.dir folder 
		return afld;
	}
	// This method will return a list of the drives that are available for assignment by the user with a disk image
	public ArrayList<Integer> getDrives() {
		ArrayList<Integer> drivesList = new ArrayList<Integer>();
		for (Integer drive = 0; drive < DWDiskDrives.MAX_DRIVES; drive++)
			drivesList.add(drive);
		return drivesList;
	}
	public String setDriveWriteProtect(Integer driveNumber, boolean writeProtect) {
		DWProtocolHandler.getDiskDrives().setWriteProtect(driveNumber, writeProtect);
		return ("Success");
		
	}
	public String loadDiskFromFile(Integer drive, String path) {
		String error = "none";
		// Call DWDiskDrives loadDiskFromFile
		/* try {
			DWProtocolHandler.getDiskDrives().LoadDiskFromFile(drive, path);
		} catch (FileNotFoundException e) {
			error = "Could not find the specified file.";
			//e.printStackTrace();
		} catch (DWDriveNotValidException e) {
			error = "The Drive specified is not valid.";
			//e.printStackTrace();
		} catch (DWDriveAlreadyLoadedException e) {
			error = "This drive already has a disk loaded.";
			//e.printStackTrace();
		} 
		return error;
	}
	public ArrayList<DriveListData> getDrivesList() {
		// Get the list of drives, paths, and write protect, and disk sector information and return to the client
		ArrayList<DriveListData> adld = new ArrayList<DriveListData>();
		for (int drive = 0; drive < DWDiskDrives.MAX_DRIVES; drive++) {
			if (DWProtocolHandler.getDiskDrives().diskLoaded(drive)) {
				DriveListData dld = new DriveListData();
				dld.setDriveNumber(drive);
				dld.setFileName(DWProtocolHandler.getDiskDrives().getDiskFile(drive));
				dld.setWriteProtect(DWProtocolHandler.getDiskDrives().getWriteProtect(drive));
				dld.setDiskSectors(DWProtocolHandler.getDiskDrives().getDiskSectors(drive));
				dld.setDiskName(DWProtocolHandler.getDiskDrives().getDiskName(drive));
				dld.setLSN(DWProtocolHandler.getDiskDrives().getLSN(drive));
				dld.setReads(DWProtocolHandler.getDiskDrives().getReads(drive));
				dld.setWrites(DWProtocolHandler.getDiskDrives().getWrites(drive));
				dld.setDirty(DWProtocolHandler.getDiskDrives().getDirtySectors(drive));
				adld.add(dld);
			}
		}
		return adld;
	}
	public VersionData getServerVersion() {
		return(new VersionData(DriveWireServer.DWServerVersion, DriveWireServer.DWServerVersionDate));
	}
	public String openDiskSet(String fileName) {
		DWProtocolHandler.getDiskDrives().LoadDiskSet(fileName);
		return null;
	}
	public String saveDiskSet(String fileName) {
		DWProtocolHandler.getDiskDrives().saveDiskSet(fileName);
		return null;
	}
	public String ejectDisk(Integer driveNumber) {
		String errorMessage = "none";
		try {
			DWProtocolHandler.getDiskDrives().EjectDisk(driveNumber);
		} catch (DWDriveNotValidException e) {
			errorMessage = "Drive not valid.";
		} catch (DWDriveNotLoadedException e) {
			errorMessage = "Drive not loaded.";
		}
		return errorMessage;
	}
	public ArrayList<String> getPorts() {
		return DWProtocolHandler.getPortNames();
	}
	public String setPort(String port) {
		// should never call setPort like this, will do something better eventually
		// DWProtocolHandler.setPort(port);
		return ("Success");
	}

	/**
	 * Return the current settings for the 'Settings' tab to the client
	
	public SettingsData getSettings() {

		SettingsData settings = new SettingsData();
		
		settings.setPort(DriveWireServer.getPortName());
		settings.setModel(DriveWireServer.getCocoModel());
		settings.setLogLevel(DriveWireServer.getLogLevel());
		settings.setWriteToFile(DriveWireServer.isWriteToFileEnabled());
		settings.setLogFileName(DriveWireServer.getLogFileName());
		return settings;
	}

	public String setLogLevel(String level) {
		DriveWireServer.setLogLevel(level);
		return "Success";
	}
	public String setModel(int model) {
		String error = "none";
		//try {
			DriveWireServer.setCocoModel(model);
		//} catch (UnsupportedCommOperationException e) {
		//	error = e.toString();
		//}
		return error;
	}
	public String setLogFileName(String fileName) {
		DriveWireServer.setLogFileName(fileName);
		return "Success";
	}
	public String setLogToFile(boolean logToFile) {
		DriveWireServer.logToFile(logToFile);
		return "success";
	}
	
	
}

*/