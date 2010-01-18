package com.groupunix.drivewireserver.virtualprinter;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.virtualserial.DWVSerialCircularBuffer;

public class DWVPrinter {

	private DWVSerialCircularBuffer printBuffer = new DWVSerialCircularBuffer();
	
	private static final Logger logger = Logger.getLogger("DWServer.DWVPrinter");
	
	public DWVPrinter()
	{
		logger.debug("initialized");
		
	}
	
	public void addByte(byte data)
	{
		try 
		{
			this.printBuffer.getOutputStream().write(data);
		} 
		catch (IOException e) 
		{
			logger.warn("error writing to print buffer: " + e.getMessage());
		}
	}
	
	public void flush()
	{
		String tmp = new String();
		
		try 
		{
			while (this.printBuffer.getInputStream().available() > 0)
			{
				byte databyte = (byte) this.printBuffer.getInputStream().read();
				tmp += Character.toString((char) databyte);
			}
			
			logger.debug("PRINT: " + tmp);
		} 
		catch (IOException e) 
		{
			logger.warn("error reading from print buffer: " + e.getMessage());
		}
	}
	
}
