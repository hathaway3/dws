package com.groupunix.drivewireserver;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class DWUILogAppender extends AppenderSkeleton
{
	private OutputStream output;
	
	public DWUILogAppender(Layout layout, OutputStream outputStream)
	{
		this.output = outputStream;
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
	
	@Override
	protected void append(LoggingEvent evt) 
	{
		try 
		{
			this.output.write((layout.format(evt) + "\r\n").getBytes());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}


}
