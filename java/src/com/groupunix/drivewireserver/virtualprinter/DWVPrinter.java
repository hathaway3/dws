package com.groupunix.drivewireserver.virtualprinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.virtualserial.DWVSerialCircularBuffer;
import com.groupunix.fx80img.fx80img;

public class DWVPrinter {

	private DWVSerialCircularBuffer printBuffer = new DWVSerialCircularBuffer(-1, true);
	
	private static final Logger logger = Logger.getLogger("DWServer.DWVPrinter");
	private int handlerno;
	
	
	public DWVPrinter(int handlerno)
	{
		logger.debug("initialized by handler #" + handlerno);
		this.handlerno = handlerno;
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
			
			if (DriveWireServer.getHandler(handlerno).getConfig().containsKey("PrinterFile"))
			{
				if (FileExistsOrCreate(DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterFile")))
				{
					// append printer output to file
					if (DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterType","TEXT").equalsIgnoreCase("TEXT"))
					{
						FileWriter fstream = new FileWriter(DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterFile"),true);
				        BufferedWriter out = new BufferedWriter(fstream);
				   	
						out.write(tmp);
						out.close();
						
						logger.info("Flushed print buffer to text file: '" + DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterFile") +"'");
					}
					else
					{
						logger.error("Only TEXT mode printing is supported if PrinterFile is specified in config");
					}
					
					
				}
			} 
			else if (DriveWireServer.getHandler(handlerno).getConfig().containsKey("PrinterDir"))
			{
				if (DirExistsOrCreate(DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterDir")))
				{
					// create printer output in seperate file
					
					if (DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterType","TEXT").equalsIgnoreCase("TEXT"))
					{
						File theDir = new File(DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterDir"));
						File theFile = File.createTempFile("dw_print_",".txt",theDir);
						
						FileOutputStream theOS = new FileOutputStream(theFile);
						
						theOS.write(tmp.getBytes());
						theOS.close();
						
						
						logger.info("Flushed print buffer to text file: '" + theFile.getAbsolutePath() +"'");
					}
					else if (DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterType","TEXT").equalsIgnoreCase("FX80"))
					{
						// FX80 simulator output
						File theDir = new File(DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterDir"));
						
						Thread fx80thread = new Thread(new fx80img(handlerno, tmp, theDir));
						fx80thread.start();
						
					}
				}
				else
				{
					logger.error("Could not open or create PrinterDir '" + DriveWireServer.getHandler(handlerno).getConfig().getString("PrinterDir") + "'");
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
	
	private boolean FileExistsOrCreate(String fileName) throws IOException
	{
		  File theFile = new File(fileName);

		  // if the directory does not exist, create it
		  if (!theFile.exists())
		  {
		    logger.info("creating printer file: " + fileName);
		    return(theFile.createNewFile());
		  }
		  else
		  {
			  return(true);
		  }
	}
	
}
