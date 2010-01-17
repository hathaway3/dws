package com.mynumnum.drivewire.client.rpc;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.mynumnum.drivewire.client.serializable.DriveListData;
import com.mynumnum.drivewire.client.serializable.FileListData;
import com.mynumnum.drivewire.client.serializable.SerialPortData;
import com.mynumnum.drivewire.client.serializable.SettingsData;
import com.mynumnum.drivewire.client.serializable.StatusData;
import com.mynumnum.drivewire.client.serializable.VersionData;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface DriveWireServiceAsync {
	void allowIncoming(String serialPort, boolean isChecked,
			AsyncCallback<String> callback);

	void requirePassword(String serialPort, boolean isChecked,
			AsyncCallback<String> callback);

	void serialPortDestination(String serialPort, String destination,
			AsyncCallback<String> callback);

	void getStatusData(AsyncCallback<StatusData> callback);

	void getPortData(AsyncCallback<ArrayList<SerialPortData>> callback);

	void resetLogFile(AsyncCallback<String> asyncCallback);

	void getLogFileData(int numberOfLines, AsyncCallback<ArrayList<String>> asyncCallback);

	void getDrives(AsyncCallback<ArrayList<Integer>> callback);

	void setDriveWriteProtect(Integer driveNumber, boolean writeProtect,
			AsyncCallback<String> callback);

	void loadDiskFromFile(Integer drive, String path,
			AsyncCallback<String> callback);

	void getDrivesList(AsyncCallback<ArrayList<DriveListData>> asyncCallback);

	void getFileList(String fileType,
			AsyncCallback<ArrayList<FileListData>> callback);

	void getServerVersion(AsyncCallback<VersionData> callback);

	void openDiskSet(String fileName, AsyncCallback<String> callback);

	void saveDiskSet(String fileName, AsyncCallback<String> callback);

	void ejectDisk(Integer driveNumber, AsyncCallback<String> callback);

	void getPorts(AsyncCallback<ArrayList<String>> callback);

	void setPort(String port, AsyncCallback<String> callback);

	void getSettings(AsyncCallback<SettingsData> callback);

	void setModel(int model, AsyncCallback<String> callback);

	void setLogLevel(String level, AsyncCallback<String> callback);

	void setLogFileName(String fileName, AsyncCallback<String> callback);

	void setLogToFile(boolean logToFile, AsyncCallback<String> callback);

}
