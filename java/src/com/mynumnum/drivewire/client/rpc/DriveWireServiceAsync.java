package com.mynumnum.drivewire.client.rpc;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.mynumnum.drivewire.client.serializable.FileListData;
import com.mynumnum.drivewire.client.serializable.SerialPortData;
import com.mynumnum.drivewire.client.serializable.StatusData;

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

	void getFileList(AsyncCallback<ArrayList<FileListData>> asyncCallback);

	void getDrives(AsyncCallback<ArrayList<Integer>> callback);

	void setDriveWriteProtect(Integer driveNumber, boolean writeProtect,
			AsyncCallback<String> callback);

	void loadDiskFromFile(Integer drive, String path,
			AsyncCallback<String> callback);
}
