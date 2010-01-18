package com.groupunix.drivewireserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

// adds logging events to an internal list so they can be retrieved for web UI
// very basic for now

public class DWLogAppender extends AppenderSkeleton 
{


	private List<LoggingEvent> events = new ArrayList<LoggingEvent>();

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
		synchronized (events) 
		{
			events.add(event);
		}
	}

	public ArrayList<String> getLastEvents(int num)
	{
		ArrayList<String> eventstxt = new ArrayList<String>();
		int start = 0;
		
		if (events.size() > num)
		{
			start = events.size() - num;
		}
		
		for (int i = start;i<events.size();i++)
		{
			eventstxt.add(events.get(i).toString());
		}
		
		return(eventstxt);
	}
	
	public void close()
	{
		
	}
}