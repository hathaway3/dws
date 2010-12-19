package com.groupunix.drivewireserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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
		this.dwuiref = dwuiref;
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
			if (!outskt.isClosed())
				this.outskt.getOutputStream().write((layout.format(evt) + "\r\n").getBytes());
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
			try {
				outskt.close();
			} 
			catch (IOException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}


}
