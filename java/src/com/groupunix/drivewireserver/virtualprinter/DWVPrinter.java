package com.groupunix.drivewireserver.virtualprinter;

import java.io.IOException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWPrinterFileError;
import com.groupunix.drivewireserver.dwexceptions.DWPrinterNotDefinedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWVPrinter 
{

	private DWVPrinterDriver[] drivers;
	
	
	private static final Logger logger = Logger.getLogger("DWServer.DWVPrinter");
	
	private DWProtocol dwProto;
	
	public DWVPrinter(DWProtocol dwProto)
	{
		this.dwProto = dwProto;
		
		logger.debug("initialized by handler #" + dwProto.getHandlerNo());
		
		// load drivers
		
		drivers = new DWVPrinterDriver[2];
		drivers[0] = new DWVPrinterText(this);
		drivers[1] = new DWVPrinterFX80(this);
		
		for (int i = 0;i<drivers.length;i++)
		{
			logger.info("Print driver " + drivers[i].getDriverName() + " loaded");
		}
		
	}
	
	public void addByte(byte data)
	{
		try 
		{
			getCurrentDriver().addByte(data);
		} 
		catch (IOException e) 
		{
			logger.warn("error writing to print buffer: " + e.getMessage());
		} 
		catch (DWPrinterNotDefinedException e) 
		{
			logger.warn("error writing to print buffer: " + e.getMessage());
		}
	}
	
	private DWVPrinterDriver getCurrentDriver() throws DWPrinterNotDefinedException
	{
		
		for (int i = 0;i<drivers.length;i++)
		{
			if (getConfig().getString("PrinterType","TEXT").equalsIgnoreCase(drivers[i].getDriverName()))
			{
				return(drivers[i]);
			}
		}
		
		throw new DWPrinterNotDefinedException("Cannot find driver for printer type '" + getConfig().getString("PrinterType","TEXT") + "'");
	}

	
	public void flush()
	{
		logger.debug("Printer flush");
		
		try 
		{
			getCurrentDriver().flush();
		} 
		catch (DWPrinterNotDefinedException e) 
		{
			logger.warn("error flushing print buffer: " + e.getMessage());
		} 
		catch (IOException e) 
		{
			logger.warn("error flushing print buffer: " + e.getMessage());
		} 
		catch (DWPrinterFileError e) 
		{
			logger.warn("error flushing print buffer: " + e.getMessage());
		}

	}

	

	
	public HierarchicalConfiguration getConfig()
	{
		return dwProto.getConfig();
	}

	public Logger getLogger() 
	{
		return dwProto.getLogger();
	}
	
}
