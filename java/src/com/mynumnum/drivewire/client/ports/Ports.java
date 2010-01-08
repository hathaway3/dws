/**
 * 
 */
package com.mynumnum.drivewire.client.ports;

import java.util.ArrayList;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.mynumnum.drivewire.client.DriveWireGWT;
import com.mynumnum.drivewire.client.serializable.SerialPortData;

/**
 * @author Jim Hathaway
 * This is the code that is required to implement the Ports tab on the Web interface
 * For each listbox and checkbox we must use the serSerial method so that it knows
 * what serial port is it responsible for.
 * 
 *
 */
public class Ports extends Composite {
	private static FlexTable portsTable = new FlexTable();
	private final static String TABNAME = "Ports"; 
	private Timer refreshTimer;
		
	
	public Ports() {
		initWidget(portsTable);
		// Setup our timer to do GWT Async callbacks
		defineTimer();
		// Start the timer with the default refresh rate
		startTimer(DriveWireGWT.REFRESH_RATE_IN_MS);
		
	}


	private void defineTimer() {
		refreshTimer = new Timer() {
			
			public void run() {
				// TODO Auto-generated method stub
				// Make RPC call
				DriveWireGWT.driveWireService.getPortData(new AsyncCallback<ArrayList<SerialPortData>>() {
					
					public void onSuccess(ArrayList<SerialPortData> result) {
						// Refresh all the client labels with the data
						refreshClientData(result);
						
					}
					
					public void onFailure(Throwable caught) {
						// schedule refresh in the future to try again
						startTimer(DriveWireGWT.ERROR_REFRESH_RATE_IN_MS);
						
					}
					
				});
				
			}
			
		};
		
	}

	public void startTimer(int refreshRateInMs) {
		refreshTimer.schedule(refreshRateInMs);
		
	}
	
	public void cancelTimer() {
		refreshTimer.cancel();
	}

	public static String getTabname() {
		return TABNAME;
	}
	
	private void refreshClientData(ArrayList<SerialPortData> result) {
		// Refresh all the client data
		int column = 0;
		portsTable.setHTML(0, column++, "Port");
		portsTable.setHTML(0, column++, "Mode");
		portsTable.setHTML(0, column++, "Connected");
		portsTable.setHTML(0, column++, "Coco Init");
		portsTable.setHTML(0, column++, "Password Set");
		portsTable.setHTML(0, column++, "Action File Set");
		portsTable.setHTML(0, column++, "PD_INT");
		portsTable.setHTML(0, column++, "PD_QUT");
		int row;
		for (SerialPortData spd : result) {
			column = 0;
			// For each serial port we will set the text of the label
			// Use the port number as the offset to the row in the flextable
			row = spd.getPort() + 1;
			portsTable.setHTML(row, column++, String.valueOf(spd.getPort()));
			portsTable.setHTML(row, column++, spd.getMode());
			portsTable.setHTML(row, column++, String.valueOf(spd.isConnected()));
			portsTable.setHTML(row, column++, String.valueOf(spd.isCocoInit()));
			portsTable.setHTML(row, column++, String.valueOf(spd.isPasswordSet()));
			portsTable.setHTML(row, column++, String.valueOf(spd.isActionFileDefined()));
			portsTable.setHTML(row, column++, String.valueOf(spd.getPD_INT()));
			portsTable.setHTML(row, column++, String.valueOf(spd.getPD_QUT()));
			
		}
				
	}

}
