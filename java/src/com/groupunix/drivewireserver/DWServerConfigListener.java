package com.groupunix.drivewireserver;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;



public class DWServerConfigListener implements ConfigurationListener
{
	private static final Logger logger = Logger.getLogger("DWServer.DWServerConfigListener");

	
    public void configurationChanged(ConfigurationEvent event)
    {
        if (!event.isBeforeUpdate())
        {
            if (event.getPropertyName() != null)
            {
            	// logging changes
            	if (event.getPropertyName().startsWith("Log"))
            	{
            		logger.info("Restarting logging due to config changes");
            		DriveWireServer.applyLoggingSettings();
            	}
            	
            	// UI thread
            	if (event.getPropertyName().startsWith("UI"))
            	{
            		logger.info("Restarting UI thread due to config changes");
            		DriveWireServer.applyUISettings();
            	}
            	
            	
            }
        }
    }
}
