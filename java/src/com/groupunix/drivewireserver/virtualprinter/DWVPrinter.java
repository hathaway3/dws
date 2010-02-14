package com.groupunix.drivewireserver.virtualprinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.virtualserial.DWVSerialCircularBuffer;
import com.groupunix.fx80img.fx80img;

public class DWVPrinter {

	private DWVSerialCircularBuffer printBuffer = new DWVSerialCircularBuffer(-1, true);
	
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
		
		logger.debug("Printer flush");
		
		try 
		{
			while (this.printBuffer.getInputStream().available() > 0)
			{
				byte databyte = (byte) this.printBuffer.getInputStream().read();
				tmp += Character.toString((char) databyte);
			}
			
			if (DriveWireServer.config.containsKey("PrinterDir"))
			{
				if (DirExistsOrCreate(DriveWireServer.config.getString("PrinterDir")))
				{
					// create printer output
					
					if (DriveWireServer.config.getString("PrinterType","TEXT").equalsIgnoreCase("TEXT"))
					{
						File theDir = new File(DriveWireServer.config.getString("PrinterDir"));
						File theFile = File.createTempFile("dw_print_",".txt",theDir);
						
						FileOutputStream theOS = new FileOutputStream(theFile);
						
						theOS.write(tmp.getBytes());
						theOS.close();
						
						logger.info("Flushed print buffer to text file: '" + theFile.getAbsolutePath() +"'");
					}
					else if (DriveWireServer.config.getString("PrinterType","TEXT").equalsIgnoreCase("FX80"))
					{
						// FX80 simulator output
						File theDir = new File(DriveWireServer.config.getString("PrinterDir"));
						File theFile = File.createTempFile("dw_print_",".png",theDir);
						
						fx80img.print(tmp,theFile);
						
					}
				}
				else
				{
					logger.error("Could not open or create PrinterDir '" + DriveWireServer.config.getString("PrinterDir") + "'");
				}
			}
			else
			{	
				logger.debug("PRINT: " + tmp);
			}
		} 
		catch (IOException e) 
		{
			logger.warn("error flushing print buffer: " + e.getMessage());
		}
	}

	private boolean DirExistsOrCreate(String directoryName)
	{
		  File theDir = new File(directoryName);

		  // if the directory does not exist, create it
		  if (!theDir.exists())
		  {
		    logger.info("creating printer directory: " + directoryName);
		    return(theDir.mkdir());
		  }
		  else
		  {
			  return(true);
		  }
	}
	
}
