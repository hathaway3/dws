package com.mynumnum.drivewire.server;

import gnu.io.UnsupportedCommOperationException;

import java.io.File;
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
import com.groupunix.drivewireserver.tcpserver.DWTCPServer;
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
@SuppressWarnings("serial")
public class DriveWireServiceImpl extends RemoteServiceServlet implements
		DriveWireService {
	private CharSequence filterString;
	private Integer counter = 0;
	public String greetServer(String input) {
		counter++;
		return (counter.toString());

	}
	public String allowIncoming(String serialPort, boolean isChecked) {
		// Make call to appropriate method in drivewire server
		System.out.println("Just got an allow incoming change for serial port " + serialPort + " value= " + isChecked );
		return null;
	}
	public String requirePassword(String serialPort, boolean isChecked) {
		// Make call to appropriate method in drivewire server
		System.out.println("Just got an require password change for serial port " + serialPort + " value= " + isChecked );
		return null;
	}
	public String serialPortDestination(String serialPort, String destination) {
		// Make call to appropriate method in drivewire server
		System.out.println("Just got a serial port destination change for serial port " + serialPort + " destination= " + destination );
		return null;
	}
	public StatusData getStatusData() {
		StatusData sd = new StatusData();
		sd.setLastDrive(DWProtocolHandler.getLastDrive());
		sd.setLastGetStat(DWProtocolHandler.getLastGetStat());
		sd.setLastLSN(DWProtocolHandler.getLastLSN());
		sd.setLastMessage(DWProtocolHandler.getLastMessage());
		sd.setLastOpcode(DWProtocolHandler.getLastOpcode());
		sd.setLastSetStat(DWProtocolHandler.getLastSetStat());
		sd.setReadRetries(DWProtocolHandler.getReadRetries());
		sd.setSectorsRead(DWProtocolHandler.getSectorsRead());
		sd.setSectorsWritten(DWProtocolHandler.getSectorsWritten());
		sd.setWriteRetries(DWProtocolHandler.getWriteRetries());
		sd.setModel(DWProtocolHandler.getCocoModel());
		// Need to add the rest of the getters and setters.
		return sd;
	}
	public ArrayList<SerialPortData> getPortData() {
		ArrayList<SerialPortData> portData = new ArrayList<SerialPortData>();
		for (int port = 0; port < DWVSerialPorts.MAX_PORTS; port++) {
			SerialPortData spd = new SerialPortData();
			// set all the fields of the SerialPortData class so the client will have access to the data
			// spd.setActionFileDefined(DWVSerialPorts.getActionFileDefined(port));
			spd.setCocoInit(DWVSerialPorts.isCocoInit(port));
			spd.setConnected(DWVSerialPorts.isConnected(port));
			spd.setMode(DWVSerialPorts.prettyMode(port));
			// spd.setPasswordSet(DWVSerialPorts.isPasswordRequired(port));
			spd.setPD_INT(DWVSerialPorts.getPD_INT(port));
			spd.setPD_QUT(DWVSerialPorts.getPD_QUT(port));
			spd.setPrettyPort(DWVSerialPorts.prettyPort(port));
			spd.setPort(port);
			spd.setHostIP(DWVSerialPorts.getHostIP(port));
			spd.setHostPort(DWVSerialPorts.getHostPort(port));
			// Add this instance of spd to our array list
			portData.add(spd);
			
			 	
		}
		
		return portData;
	}
	// TODO Need method created in DriveWireServer that can return this data
	public ArrayList<String> getLogFileData(int numberOfLines) {
		// return DriveWireServer.getLogFileData(numberOfLines);
		return null;
	}
	// TODO need a method in DriveWireServer that can reset the log file.
	public String resetLogFile() {
		// DriveWireServer.resetLogFile();
		return "Success";
	}
	// TODO fetch the file and folder data and return to client
	public ArrayList<FileListData> getFileList(String fileType) {
		File dir = new File(".");
		if (fileType.equals("disk"))
			filterString = ".dsk";
		if (fileType.equals("set"))
			filterString = ".set";
		System.out.println("the value of fileType is " + filterString);
		// Define filter for the types of files we want to see
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
				return name.contains(filterString);
		    }
		};

		// Get the current folder in the file system
		String fileFolder = System.getProperty("user.dir");

		ArrayList<FileListData> afld = new ArrayList<FileListData>();
		FileListData fld = new FileListData();
		fld.setFileFolder(fileFolder);
		ArrayList<FileListData.FileDetails> files = new ArrayList<FileListData.FileDetails>();
		for (String s : dir.list(filter)) {
			DWDisk disk = new DWDisk();
			try {
				disk.setFilePath(fileFolder + "/" + s);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			FileListData.FileDetails file = new FileListData.FileDetails();
			try {
				file.setDiskName(disk.getDiskName());
				System.out.println("the disk name is " + disk.getDiskName());
			} catch (Exception e) {
				file.setDiskName("unknown");
			}
			file.setFileName(s);
			files.add(file);
			
		}
		// Add the files to the file list
		fld.setFileNames(files);
		// add this directory to the directory list
		afld.add(fld);
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
		DWDiskDrives.setWriteProtect(driveNumber, writeProtect);
		return ("Success");
		
	}
	@Override
	public String loadDiskFromFile(Integer drive, String path) {
		String error = "none";
		// Call DWDiskDrives loadDiskFromFile
		try {
			DWDiskDrives.LoadDiskFromFile(drive, path);
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
	@Override
	public ArrayList<DriveListData> getDrivesList() {
		// Get the list of drives, paths, and write protect, and disk sector information and return to the client
		ArrayList<DriveListData> adld = new ArrayList<DriveListData>();
		for (int drive = 0; drive < DWDiskDrives.MAX_DRIVES; drive++) {
			if (DWDiskDrives.diskLoaded(drive)) {
				DriveListData dld = new DriveListData();
				dld.setDriveNumber(drive);
				dld.setFileName(DWDiskDrives.getDiskFile(drive));
				dld.setWriteProtect(DWDiskDrives.getWriteProtect(drive));
				dld.setDiskSectors(DWDiskDrives.getDiskSectors(drive));
				dld.setDiskName(DWDiskDrives.getDiskName(drive));
				adld.add(dld);
				
			}
		}
		return adld;
	}
	public VersionData getServerVersion() {
		return(new VersionData(DriveWireServer.DWServerVersion, DriveWireServer.DWServerVersionDate));
	}
	@Override
	public String openDiskSet(String fileName) {
		DWDiskDrives.loadDiskSet(fileName);
		return null;
	}
	@Override
	public String saveDiskSet(String fileName) {
		DWDiskDrives.saveDiskSet(fileName);
		return null;
	}
	@Override
	public String ejectDisk(Integer driveNumber) {
		String errorMessage = "none";
		try {
			DWDiskDrives.EjectDisk(driveNumber);
		} catch (DWDriveNotValidException e) {
			errorMessage = "Drive not valid.";
		} catch (DWDriveNotLoadedException e) {
			errorMessage = "Drive not loaded.";
		}
		return errorMessage;
	}
	@Override
	public ArrayList<String> getPorts() {
		return DWProtocolHandler.getPortNames();
	}
	@Override
	public String setPort(String port) {
		DWProtocolHandler.setPort(port);
		return ("Success");
	}
	/**
	 * Return the current settings for the 'Settings' tab to the client
	 */
	@Override
	public SettingsData getSettings() {
		SettingsData settings = new SettingsData();
		settings.setPort(DWProtocolHandler.getSerialPort().getName());
		settings.setModel(DWProtocolHandler.getCocoModel());
		settings.setTcpServerEnabled(DriveWireServer.isTcpEnabled());
		settings.setTcpPort(DWTCPServer.getTcpPort());
		settings.setLogLevel(DriveWireServer.getLogLevel());
		settings.setWriteToFile(DriveWireServer.isWriteToFileEnabled());
		settings.setLogFileName(DriveWireServer.getLogFileName());
		return settings;
	}
	@Override
	public String setLogLevel(String level) {
		DriveWireServer.setLogLevel(level);
		return "Success";
	}
	@Override
	public String setModel(int model) {
		String error = "none";
		try {
			DWProtocolHandler.setCocoModel(model);
		} catch (UnsupportedCommOperationException e) {
			error = e.toString();
		}
		return error;
	}
	
	
}
