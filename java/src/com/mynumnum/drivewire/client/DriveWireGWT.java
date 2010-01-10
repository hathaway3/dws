package com.mynumnum.drivewire.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.mynumnum.drivewire.client.ports.Ports;
import com.mynumnum.drivewire.client.rpc.DriveWireService;
import com.mynumnum.drivewire.client.rpc.DriveWireServiceAsync;
import com.mynumnum.drivewire.client.status.Status;
import com.mynumnum.drivewire.client.tabs.About;
import com.mynumnum.drivewire.client.tabs.Drives;
import com.mynumnum.drivewire.client.tabs.Settings;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DriveWireGWT implements EntryPoint, ValueChangeHandler<String> {
	private static TabPanel tp = new TabPanel();
	private String initToken = History.getToken();
	public static final int REFRESH_RATE_IN_MS = 1000;
	public static final int ERROR_REFRESH_RATE_IN_MS = 5000;
	public static final String ERROR_MESSAGE = "There was an error contacting the server.";

	/**
	 * Create a remote service proxy to talk to the server-side DriveWire service.
	 */
	public static final DriveWireServiceAsync driveWireService = GWT
			.create(DriveWireService.class);


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// If we don't have a token then set our token to the status tab
		if (initToken.length() == 0) {
              History.newItem(Status.getTabname());
        }
		
		// Build the tabs
		tp.add(new Status(), Status.getTabname());
		tp.add(new Drives(), Drives.getTabname());
		tp.add(new Ports(), Ports.getTabname());
		tp.add(new Settings("settings tab"), Settings.getTabname());
		tp.add(new About(), About.getTabname());
		// Show the user our new tabPanel
		RootPanel.get().add(tp);
		// Selection handler will add the history to our browser as the user clicks the tabs
		tp.addSelectionHandler(new SelectionHandler<Integer>(){
	            public void onSelection(SelectionEvent<Integer> event) {
	            	History.newItem(tp.getTabBar().getTabHTML(event.getSelectedItem()).toString());
	            }});
		// Our handler will fire when the value in the browser URL changes
		History.addValueChangeHandler(this);
		// This will move us to the correct tab based on the browser URL
        History.fireCurrentHistoryState();
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		String historyToken = event.getValue();
		GWT.log(historyToken, null);
		int x;
		for (x = 0; x < tp.getTabBar().getTabCount(); x++) {
			if (historyToken.equalsIgnoreCase(tp.getTabBar().getTabHTML(x).toString()))
				break;
		}
		tp.selectTab(x);
	}
	

}
