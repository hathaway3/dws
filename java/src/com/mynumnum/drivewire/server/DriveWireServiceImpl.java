package com.mynumnum.drivewire.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;
import com.mynumnum.drivewire.client.rpc.DriveWireService;
import com.mynumnum.drivewire.client.serializable.FileListData;
import com.mynumnum.drivewire.client.serializable.SerialPortData;
import com.mynumnum.drivewire.client.serializable.StatusData;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class DriveWireServiceImpl extends RemoteServiceServlet implements
		DriveWireService {

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
			spd.setActionFileDefined(DWVSerialPorts.getActionFileDefined(port));
			spd.setCocoInit(DWVSerialPorts.isCocoInit(port));
			spd.setConnected(DWVSerialPorts.isConnected(port));
			spd.setMode(DWVSerialPorts.prettyMode(port));
			spd.setPasswordSet(DWVSerialPorts.isPasswordRequired(port));
			spd.setPD_INT(DWVSerialPorts.getPD_INT(port));
			spd.setPD_QUT(DWVSerialPorts.getPD_QUT(port));
			spd.setPrettyPort(DWVSerialPorts.prettyPort(port));
			spd.setPort(port);
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
	public ArrayList<FileListData> getFileList() {
		ArrayList<FileListData> fileList = new ArrayList<FileListData>();
		File dir = new File(".");

		String[] children = dir.list();
		if (children == null) {
		    // Either dir does not exist or is not a directory
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        String filename = children[i];
		    }
		}

		// It is also possible to filter the list of returned files.
		// This example does not return any files that start with `.'.
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".");
		    }
		};
		children = dir.list(filter);

		String fileFolder = System.getProperty("user.dir");

		ArrayList<FileListData> afld = new ArrayList<FileListData>();
		FileListData fld = new FileListData();
		fld.setFileFolder(fileFolder);
		ArrayList<String> files = new ArrayList<String>();
		for (String s : dir.list()) {
			files.add(s);
		}
		fld.setFileNames(files);
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
}
