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

	public String[] getLastEvents(int num)
	{
		
		if (events.size() > num)
		{
			String[] eventstxt = new String[num];
			for (int i = (events.size() - num);i<events.size();i++)
			{
				eventstxt[i - (events.size() - num)] = events.get(i).toString();
			}
			
			return(eventstxt);
		}
		else
		{
			String[] eventstxt = new String[events.size()];

			for (int i = 0; i<events.size();i++)
			{
				eventstxt[i] = events.get(i).toString();
			}
			
			return(eventstxt);
		}
		
	}
	
	public void close()
	{
		
	}
}