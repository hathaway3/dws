package com.mynumnum.drivewire.server;

import java.util.ArrayList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
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
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ArrayList<String> getDrives() {
		// TODO Auto-generated method stub
		return null;
	}
}
