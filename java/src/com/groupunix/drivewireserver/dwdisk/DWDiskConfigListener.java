package com.groupunix.drivewireserver.dwdisk;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWDiskConfigListener implements ConfigurationListener 
{
	private int instance;
	private int diskno;

	public DWDiskConfigListener(int instance, int diskno)
	{
		super();
		this.instance = instance;
		this.diskno = diskno;
	}
	
	@Override
	public void configurationChanged(ConfigurationEvent event) 
	{
		if (!event.isBeforeUpdate())
        {
            if (event.getPropertyName() != null)
            {
            	if (event.getPropertyValue() != null)
            		DriveWireServer.submitDiskEvent(this.instance, this.diskno,  event.getPropertyName(), event.getPropertyValue().toString());
            	else
            		DriveWireServer.submitDiskEvent(this.instance, this.diskno,  event.getPropertyName(), "");
            }
        }
            
		
	}

}