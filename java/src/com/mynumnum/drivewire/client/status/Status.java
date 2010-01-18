/**
 * 
 */
package com.mynumnum.drivewire.client.status;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mynumnum.drivewire.client.DriveWireGWT;
import com.mynumnum.drivewire.client.common.Common;
import com.mynumnum.drivewire.client.serializable.DriveListData;
import com.mynumnum.drivewire.client.serializable.StatusData;

/**
 * @author Jim Hathaway
 * This class is used to display the Status tab in the web client.
 *
 */
public class Status extends Composite {

	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	private Timer refreshTimer;
	private final static String TABNAME = "Status"; 

	interface PanelUiBinder extends UiBinder<Widget, Status> {
	}

	@UiField
	HorizontalPanel driveStatusPanel;
	
	@UiField
	Label lastOpCode, lastLsn, lastDrive, lastGetStat, lastSetStat, device, model;
	
	@UiField
	static
	FlexTable driveStatusFlexTable;
	
	@UiField
	static
	TextArea logLines;
	
	@UiConstructor
	public Status() {
		initWidget(uiBinder.createAndBindUi(this));
		// Can access @UiField after calling createAndBindUi
		// Setup our timer to do GWT Async callbacks
		defineTimer();
		// Start the timer with the default refresh rate
		startTimer(DriveWireGWT.REFRESH_RATE_IN_MS);
		
	}

	public void startTimer(int refreshRateInMs) {
		refreshTimer.scheduleRepeating(refreshRateInMs);
		
	}
	
	public static String getTabname() {
		return TABNAME;
	}
	
	private void refreshClientData(StatusData result) {
		// Refresh all the client data
		device.setText(result.getDevice());
		lastDrive.setText(result.getLastDrive());
		lastGetStat.setText(result.getLastGetStat());
		lastLsn.setText(result.getLastLSN());
		lastOpCode.setText(result.getLastOpcode());
		model.setText(result.getModel());
		lastSetStat.setText(result.getLastSetStat());
		device.setText(result.getDevice());
		updateDriveStatusTable();
		updateLogLines();
	}

	private void defineTimer() {
		refreshTimer = new Timer() {
			
			public void run() {
				// Make RPC call only if this tab is visible
				if (Status.this.isVisible()) {
					getServerData();
				}
				
			}

			
		};
		
	}
	
	/**
	 * Fetch the Status data from the server via GWT RPC
	 */
	private void getServerData() {
		DriveWireGWT.driveWireService.getStatusData(new AsyncCallback<StatusData>() {
			
			public void onSuccess(StatusData result) {
				// Refresh all the client labels with the data
				refreshClientData(result);
				
			}
			
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				refreshTimer.cancel();
				
			}
			
		});
		
		
		
	}
	
	// This will handle the click of the reset button by sending an RPC
	// request to the server to reset the log file

	
	// This will handle the click request to display log results
	// it will request x number of rows from the log file and display
	// the results in a popup window with a close button
	@UiHandler("viewLog")
	void onClick2(ClickEvent e) {
		// Fetch the log file data from the server using GWT RPC
		int numberOfLines = 50;
		DriveWireGWT.driveWireService.getLogFileData(numberOfLines, new AsyncCallback<ArrayList<String>>() {

			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());

			}

			public void onSuccess(ArrayList<String> result) {
				final PopupPanel p = new PopupPanel();
				VerticalPanel v = new VerticalPanel();
				for (String s : result) {
					v.add(new Label(s));
				}
				Button close = new Button();
				v.add(close);
				close.addClickHandler(new ClickHandler() {
					
					public void onClick(ClickEvent event) {
						p.hide();
						
					}
				});
				p.setWidget(v);
				p.center();
				
			}
		});
		
	}

	private static void updateLogLines() 
	{
		int numberOfLines = 50;
		DriveWireGWT.driveWireService.getLogFileData(numberOfLines, new AsyncCallback<ArrayList<String>>() {

			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());

			}

			public void onSuccess(ArrayList<String> result) 
			{
				String lines = new String();
				
				for (String logline : result) 
				{
					lines += logline;
				}
				
				logLines.setText(lines);
			}
			
		});
		
	}
	
	private static void updateDriveStatusTable() {
		DriveWireGWT.driveWireService.getDrivesList(new AsyncCallback<ArrayList<DriveListData>>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}

			public void onSuccess(ArrayList<DriveListData> result) {
				driveStatusFlexTable.removeAllRows();
				// Set styles of flex table
				StyleInjector.inject(com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().getText());
				driveStatusFlexTable.setStyleName(com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().sample());

				// Set header row style
				driveStatusFlexTable.getRowFormatter().setStyleName(0, com.mynumnum.drivewire.client.bundle.ClientBundle.INSTANCE.driveWire().h1());

				// Set text of header
				int column = 0;
				driveStatusFlexTable.setText(0, column++, "Drive");
				driveStatusFlexTable.setText(0, column++, "Sectors");
				driveStatusFlexTable.setText(0, column++, "LSN");
				driveStatusFlexTable.setText(0, column++, "Reads");
				driveStatusFlexTable.setText(0, column++, "Writes");
				driveStatusFlexTable.setText(0, column++, "Dirty");
				int row = 1;
				column = 0;
				// Loop for each returned disk
				for (DriveListData dld : result) {
					driveStatusFlexTable.setText(row, column++, String.valueOf(dld.getDriveNumber()));
					driveStatusFlexTable.setText(row, column++, String.valueOf(dld.getDiskSectors()));
					driveStatusFlexTable.setText(row, column++, String.valueOf(dld.getLSN()));
					driveStatusFlexTable.setText(row, column++, String.valueOf(dld.getReads()));
					driveStatusFlexTable.setText(row, column++, String.valueOf(dld.getWrites()));
					driveStatusFlexTable.setText(row++, column++, String.valueOf(dld.getDirty()));
					column = 0;
					
					
				}
				
			}

		});
		
		
	}
	
	
}
