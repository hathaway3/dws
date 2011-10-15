package com.groupunix.drivewireui;

import java.io.IOException;
import java.io.InputStream;

public class LogInputThread implements Runnable 
{
	private InputStream input;
	
	public LogInputThread(InputStream inp)
	{
		this.input = inp;
	}
	
	@Override
	public void run() 
	{
		int data;
		String line = new String();
		
		try 
		{
			data = this.input.read();
			
			while ((data != -1) && (!MainWin.shell.isDisposed()))
			{
				
				if (data != 10)
				{
					line += Character.toString((char) data);
				}
				else
				{
					MainWin.getLogViewerWin().addToDisplay(line);
					line = "";
				}
				
				data = this.input.read();
			}
			
			
		} 
		catch (IOException e) 
		{
			MainWin.addToDisplay("Log viewer: " + e.getMessage());
		}
		
		
		
	}

}
