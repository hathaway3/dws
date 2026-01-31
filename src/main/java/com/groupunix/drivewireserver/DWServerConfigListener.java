package com.groupunix.drivewireserver;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;

public class DWServerConfigListener implements ConfigurationListener {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("DWServer.DWServerConfigListener");

	public void configurationChanged(ConfigurationEvent event) {

		if (!event.isBeforeUpdate()) {
			// indicate changed config for UI poll
			DriveWireServer.incrementConfigSerial();

			if ((event.getPropertyName() != null) && (event.getPropertyValue() != null)) {
				DriveWireServer.submitServerConfigEvent(event.getPropertyName(), event.getPropertyValue().toString());

			}
		}
	}
}
