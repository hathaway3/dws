package com.mynumnum.drivewire.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.mynumnum.drivewire.client.serializable.FileListData;
import com.mynumnum.drivewire.client.serializable.SerialPortData;
import com.mynumnum.drivewire.client.serializable.StatusData;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("driveWire")
public interface DriveWireService extends RemoteService {
	String allowIncoming(String serialPort, boolean isChecked);
	String requirePassword(String serialPort, boolean isChecked);
	String serialPortDestination(String serialPort, String destination);
	StatusData getStatusData();
	ArrayList<SerialPortData> getPortData();
	String resetLogFile();
	ArrayList<String> getLogFileData(int numberOfLines);
	ArrayList<FileListData> getFileList();
	
}
