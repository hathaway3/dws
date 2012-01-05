package com.groupunix.drivewireserver;

import java.util.HashMap;
import java.util.Set;

public class DWEvent {

	private byte eventType;
	private HashMap<String, String> params = new HashMap<String, String>();
	
	public DWEvent(byte eventType) 
	{
		this.setEventType(eventType);
	}
	
	public void setParam(String key, String val)
	{
		this.params.put(key,val);
	}
	
	public boolean hasParam(String key)
	{
		return(this.params.containsKey(key));
	}
	
	public String getParam(String key)
	{
		if (this.params.containsKey(key))
			return(this.params.get(key));
		return(null);
	}
	
	public Set<String> getParamKeys()
	{
		return(this.params.keySet());
	}

	public void setEventType(byte eventType) {
		this.eventType = eventType;
	}

	public byte getEventType() {
		return eventType;
	}

}
