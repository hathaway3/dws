package com.groupunix.drivewireserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class DWUILogAppender extends AppenderSkeleton
{
	private Socket outskt;
	private DWUIClientThread dwuiref;
	
	public DWUILogAppender(Layout layout, Socket outskt, DWUIClientThread dwuiref )
	{
		this.outskt = outskt;
		this.setDwuiref(dwuiref);
		setLayout(layout);
		
		if (DriveWireServer.getLogEventsSize() > 0)
		{
			ArrayList<String> exist = DriveWireServer.getLogEvents(DriveWireServer.getLogEventsSize());
			
			for (String l : exist)
			{
				try 
				{
					this.outskt.getOutputStream().write(l.getBytes());
				} 
				catch (IOException e) 
				{
					
				}
			}
		}
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
	
	@Override
	protected void append(LoggingEvent evt) 
	{
		try 
		{
			if (!outskt.isClosed() && !evt.getMessage().equals("ConfigurationUtils.locate(): base is null, name is null"))
				this.outskt.getOutputStream().write((layout.format(evt)).getBytes());
		} 
		catch (IOException e) 
		{
			
			
			try {
				outskt.close();
			} 
			catch (IOException e1) 
			{
				
			}
			
		}
	}

	@Override
	public void close() 
	{
		this.dwuiref.die();
	}

	public void setDwuiref(DWUIClientThread dwuiref) {
		this.dwuiref = dwuiref;
	}

	public DWUIClientThread getDwuiref() {
		return dwuiref;
	}


}
