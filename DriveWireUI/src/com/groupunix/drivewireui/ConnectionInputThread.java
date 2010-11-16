package com.groupunix.drivewireui;

import java.io.IOException;
import java.io.InputStream;

public class ConnectionInputThread implements Runnable 
{
	private InputStream input;
	
	public ConnectionInputThread(InputStream inp)
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
			
			while (data != -1)
			{
				
				if (data != 10)
				{
					line += Character.toString((char) data);
				}
				else
				{
					MainWin.addToDisplay(line);
					line = "";
				}
				
				data = this.input.read();
			}
			
			
		} 
		catch (IOException e) 
		{
			MainWin.addToDisplay(e.getMessage());
		}
		
		
		
	}

}
