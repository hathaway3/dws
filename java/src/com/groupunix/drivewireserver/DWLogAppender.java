package com.groupunix.drivewireserver;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

// adds logging events to an internal list so they can be retrieved for web UI
// very basic for now

public class DWLogAppender extends AppenderSkeleton 
{

	public static final int MAX_EVENTS = 500;

	private LinkedList<LoggingEvent> events = new LinkedList<LoggingEvent>();

	public DWLogAppender(Layout layout) 
	{
		setLayout(layout);
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public Layout getLayout() {
		return layout;
	}

	public boolean requiresLayout() {
		return true;
	}


	public synchronized void shutdown() 
	{
	}

	protected void append(LoggingEvent event) 
	{
		// ignore those pesky XMLConfiguration debug messages
		if (!event.getMessage().equals("ConfigurationUtils.locate(): base is null, name is null"))
		{
			synchronized (events) 
			{
				if (events.size() == MAX_EVENTS)
				{
					events.removeFirst();
				}
			
				events.addLast(event);
				DriveWireServer.submitLogEvent(event);
			}
		}
	}

	public ArrayList<String> getLastEvents(int num)
	{
		ArrayList<String> eventstxt = new ArrayList<String>();
		int start = 0;
		
		if (num > events.size())
			num = events.size();
		
		if (events.size() > num)
		{
			start = events.size() - num;
		}
		
		for (int i = start;i<events.size();i++)
		{
			
			eventstxt.add(layout.format(events.get(i)));
		}
		
		return(eventstxt);
	}
	
	public void close()
	{
		
	}

	public int getEventsSize()
	{
		return(events.size());
	}
}