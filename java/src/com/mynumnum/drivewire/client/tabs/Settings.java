package com.mynumnum.drivewire.client.tabs;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.mynumnum.drivewire.client.DriveWireGWT;
import com.mynumnum.drivewire.client.common.Common;

public class Settings extends Composite {
	private final static String TABNAME = "Settings"; 

	private static SettingsUiBinder uiBinder = GWT
			.create(SettingsUiBinder.class);

	interface SettingsUiBinder extends UiBinder<Widget, Settings> {
	}


	public Settings(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
		getPorts();
		getModels();
		getLogLevels();
		getSettings();
		
		
	}
	private void getLogLevels() {
		logLevelListBox.setTitle("Select the logging detail level");
		logLevelListBox.addItem("ALL");
		logLevelListBox.addItem("DEBUG");
		logLevelListBox.addItem("INFO");
		logLevelListBox.addItem("WARN");
		logLevelListBox.addItem("ERROR");
		logLevelListBox.addItem("FATAL");
		logLevelListBox.addItem("NONE");

	}
	/**
	 * Populate the models drop down list box
	 */
	private void getModels() {
		cocoModelListBox.setTitle("Select your CoCo Model");		
		cocoModelListBox.addItem("CoCo 1 at 38400 bps", "1");
		cocoModelListBox.addItem("CoCo 2 at 57600 bps", "2");
		cocoModelListBox.addItem("CoCo 3 at 115200 bps", "3");
		
	}
	/**
	 * This method will make a request to the server to get the values that we need to 
	 * setup initially on this tab
	 */
	private void getSettings() {
		// TODO Auto-generated method stub
		DriveWireGWT.driveWireService.getSettings(new AsyncCallback<com.mynumnum.drivewire.client.serializable.SettingsData>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}

			@Override
			public void onSuccess(com.mynumnum.drivewire.client.serializable.SettingsData result) {
				setSerialDevice(result.getPort());
				setModel(result.getModel());
				tcpServerEnabledCheckBox.setValue(result.isTcpServerEnabled());
				portTextBox.setText(String.valueOf(result.getTcpPort()));
				setLogLevel(result.getLogLevel());
				writeToFileCheckBox.setValue(result.isWriteToFile());
				logFileNameLabel.setValue(result.getLogFileName());
				
			}

		});
		
	}
	/**
	 * Loop through the port list box and find the selected port and select that port
	 * @param port
	 */
	private void setSerialDevice(String port) {
		for (int x = 0; x < serialDeviceListBox.getItemCount()-1; x++) {
			if (serialDeviceListBox.getValue(x).equals(port)) {
				serialDeviceListBox.setSelectedIndex(x);
				break;
			}
		}
		
	}
	/**
	 * Loop through the model list box and find the selected model and select that model
	 * @param model
	 */
	private void setModel(int model) {
		for (int x = 0; x < cocoModelListBox.getItemCount()-1; x++) {
			if (cocoModelListBox.getValue(x).equals(model)) {
				cocoModelListBox.setSelectedIndex(x);
				break;
			}
		}
		
	}
	/**
	 * Loop through the log level list box and find the selected log level
	 * @param logLevel
	 */
	private void setLogLevel(String logLevel) {
		for (int x = 0; x < logLevelListBox.getItemCount()-1; x++) {
			if (logLevelListBox.getItemText(x).equals(logLevel)) {
				logLevelListBox.setSelectedIndex(x);
				break;
			}
		}
		
	}

	/**
	 * Fetch a list of serial ports available on the server and populate the list box
	 */
	private void getPorts() {
		serialDeviceListBox.setTitle("Select the serial port you want to use");
		DriveWireGWT.driveWireService.getPorts(new AsyncCallback<ArrayList<String>>() {
			
			@Override
			public void onSuccess(ArrayList<String> result) {
				for (String s : result) {
					serialDeviceListBox.addItem(s);
				}
				serialDeviceListBox.setTitle("Select Serial Port");
				// Disable the serial port drop down list box if there are no serial ports found!
				if (result.size() == 0) {
					serialDeviceListBox.setTitle("No Serial Ports Found!");
					serialDeviceListBox.setEnabled(false);
				} else {
					setServerPort(serialDeviceListBox.getItemText(serialDeviceListBox.getSelectedIndex()));
				}
			}
			

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}
		});
		
	}


	/**
	 * @return the tabname
	 */
	public static String getTabname() {
		return TABNAME;
	}
	@UiField
	ListBox serialDeviceListBox;
	
	@UiField
	ListBox cocoModelListBox;
	
	@UiField
	CheckBox tcpServerEnabledCheckBox;
	
	@UiField
	TextBox portTextBox;
	
	@UiField
	ListBox logLevelListBox;
	
	@UiField
	TextBox logFileNameLabel;
	
	@UiField
	CheckBox writeToFileCheckBox;
	
	@UiHandler("serialDeviceListBox")
	void onSerialPortChange(ChangeEvent e) {
		// Make request to server to change serial port
		setServerPort(serialDeviceListBox.getItemText(serialDeviceListBox.getSelectedIndex()));
	}
	/**
	 * Make the RPC call to the server to set the selected serial port
	 * @param port
	 */
	private void setServerPort(String port) {
		// TODO Auto-generated method stub
		DriveWireGWT.driveWireService.setPort(port, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}

			@Override
			public void onSuccess(String result) {
				// Nothing to do here
				
			}
		});
		
	}
	/**
	 * Set the new CoCo model on the server
	 * @param e
	 */
	@UiHandler("cocoModelListBox")
	void onCoCoModelChange(ChangeEvent e) {
		setServerModel(cocoModelListBox.getValue(cocoModelListBox.getSelectedIndex()));
	}
	

	private void setServerModel(String value) {
		DriveWireGWT.driveWireService.setModel(Integer.valueOf(value), new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}

			@Override
			public void onSuccess(String result) {
				if (!result.equals("none")) {
					Common.showErrorMessage(result);
				}
				
			}
		});
		
	}
	@UiHandler("logLevelListBox")
	void onLogLevelChange(ChangeEvent e) {
		setServerLogLevel(logLevelListBox.getItemText(logLevelListBox.getSelectedIndex()));
	}
	private void setServerLogLevel(String itemText) {
		DriveWireGWT.driveWireService.setLogLevel(itemText, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Common.showErrorMessage(caught.toString());
				
			}

			@Override
			public void onSuccess(String result) {
				// Nothing to do here
				
			}
		});
		
	}


}
