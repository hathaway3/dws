package com.groupunix.drivewireserver.dwprotocolhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWProtocolEventHandler {

	private static final Logger logger = Logger.getLogger("DWServer.DWProtocolEventHandler");
	
	private int handlerno;
	private Map<String,List<Integer>> events = new HashMap<String,List<Integer>>();
		
	
	
	public DWProtocolEventHandler(int handlerno) 
	{
		this.handlerno = handlerno;
	}


	public void notifyEvent(String event, String detail) 
	{
		//logger.debug("notifyEvent in handler #"+this.handlerno + ": " + event+" - "+detail);
		
		// send to registered listeners
		
		if (this.events.containsKey(event))
		{
			List<Integer> listeners = (List<Integer>)this.events.get(event);
			
			Iterator<Integer> itr = listeners.iterator();
			
			while(itr.hasNext())
				notifyListener((int) itr.next(),event,detail);
		}
			
	}
	
	
	private void notifyListener(int vport, String event, String detail) 
	{
		//logger.debug("notifying port " + vport + " for event " + event);
		DriveWireServer.getHandler(this.handlerno).getVPorts().writeToCoco(vport, event + "," + detail + "\r\n");
	}



	public void unregisterEvent(String event, int vport)
	{
		if (this.events.containsKey(event))
		{
			// existing event, add this port as listener
			
			
			List<Integer> listeners = (List<Integer>)this.events.get(event);
			
			if (listeners.contains(vport))
			{
				listeners.remove(listeners.indexOf(vport));
				this.events.put(event, listeners);
				logger.debug("unregistration for event '" + event +"' by port " + vport);
			}

		}
		else
		{
			logger.debug("port " + vport +" attempted unregister for unknown event '" + event + "'");
		}
	}
	
	public void registerEvent(String event, int vport)
	{
		if (this.events.containsKey(event))
		{
			// existing event, add this port as listener
			
			
			List<Integer> listeners = (List<Integer>)this.events.get(event);
			
			if (listeners.contains(vport))
			{
				// duplicate registration.. ok or not ok?
				logger.debug("duplicate registration for event '" + event +"' by port " + vport);
			}
			else
			{
				listeners.add(vport);
				this.events.put(event, listeners);
				logger.debug("port " + vport +" registered for exisiting event '" + event + "'");
			}
		}
		else
		{
			// new event, create and add this port as listener
			List<Integer> listeners = new ArrayList<Integer>();
			listeners.add(vport);
			this.events.put(event,listeners);
			
			logger.debug("port " + vport +" registered for new event '" + event + "'");
		}
	}


	public void unregisterAllEvents(int uiport) 
	{
		logger.debug("unregistering " + uiport +" for all events");
		
		Iterator<String> itr = this.events.keySet().iterator();
		
		while(itr.hasNext())
		{
			unregisterEvent((String) itr.next(), uiport);
		}
		
	}
	
}
