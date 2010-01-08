package com.mynumnum.drivewire.server;

import java.util.ArrayList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;
import com.mynumnum.drivewire.client.DriveWireService;
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
	@Override
	public String allowIncoming(String serialPort, boolean isChecked) {
		// Make call to appropriate method in drivewire server
		System.out.println("Just got an allow incoming change for serial port " + serialPort + " value= " + isChecked );
		return null;
	}
	@Override
	public String requirePassword(String serialPort, boolean isChecked) {
		// Make call to appropriate method in drivewire server
		System.out.println("Just got an require password change for serial port " + serialPort + " value= " + isChecked );
		return null;
	}
	@Override
	public String serialPortDestination(String serialPort, String destination) {
		// Make call to appropriate method in drivewire server
		System.out.println("Just got a serial port destination change for serial port " + serialPort + " destination= " + destination );
		return null;
	}
	@Override
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
	@Override
	public ArrayList<SerialPortData> getPortData() {
		ArrayList<SerialPortData> portData = new ArrayList<SerialPortData>();
		for (int port = 0; port < DWVSerialPorts.MAX_PORTS; port++) {
			@SuppressWarnings("unused")
			SerialPortData spd = new SerialPortData();
			
			//spd.setActionFileDefined(isActionFileDefined);
			
			//portData.add(new SerialPortData(
			//		DWVSerialPorts.getPrettyMode(port),
			//		DWVSerialPorts.get));
			
			 	
		}
		
		return portData;
	}
	@Override
	// TODO Need method created in DriveWireServer that can return this data
	public ArrayList<String> getLogFileData(int numberOfLines) {
		// return DriveWireServer.getLogFileData(numberOfLines);
		return null;
	}
	@Override
	// TODO need a method in DriveWireServer that can reset the log file.
	public String resetLogFile() {
		// DriveWireServer.resetLogFile();
		return "Success";
	}
	// TODO fetch the file and folder data and return to client
	@Override
	public ArrayList<FileListData> getFileList() {
		// TODO Auto-generated method stub
		return null;
	}
}
