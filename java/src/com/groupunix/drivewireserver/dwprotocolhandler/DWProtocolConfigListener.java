package com.groupunix.drivewireserver.dwprotocolhandler;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

public class DWProtocolConfigListener implements ConfigurationListener 
{

	@Override
	public void configurationChanged(ConfigurationEvent event) 
	{
		if (!event.isBeforeUpdate())
        {
            if (event.getPropertyName() != null)
            {
            	
            	//System.out.println("CONF CHG:: " + event.getPropertyName() + " to " + event.getPropertyValue());
            	
            }
        }
            
		
	}

}
