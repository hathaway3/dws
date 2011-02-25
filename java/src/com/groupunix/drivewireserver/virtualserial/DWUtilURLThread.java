package com.groupunix.drivewireserver.virtualserial;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWUtilURLThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilURLThread");
	
	private int vport = -1;
	private String url = null;
	private String action = null;

	private DWVSerialPorts dwVSerialPorts;
	
	
	public DWUtilURLThread(DWProtocolHandler dwProto, int vport, String theurl, String theaction)
	{
		logger.debug("init url thread");	
		this.vport = vport;
		this.url = theurl;
		this.action = theaction;
	
		this.dwVSerialPorts = dwProto.getVPorts();
	}
	


	public void run() 
	{
		Thread.currentThread().setName("urlutil-" + Thread.currentThread().getId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		logger.debug("run");
		
		logger.info("URL - " + this.action + " - " + this.url + " for port " + this.vport);
		
		URL url;
		try
		{
			url = new URL(this.url);
			
			DataInputStream theHTML = new DataInputStream(url.openStream());
			
			byte[] buffer = new byte[256];
			int sz = 0;
			
			while ((sz = theHTML.read(buffer)) >= 0)
			{
				baos.write(buffer,0,sz);
			}
			
			theHTML.close();
			
			dwVSerialPorts.sendUtilityOKResponse(this.vport, baos.toByteArray());
			
		} 
		catch (MalformedURLException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Malformed URL: " + e.getMessage());
			
		} 
		catch (IOException e1)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "IO Error: " + e1.getMessage());
			
		}
		
		
		// wait for output
		try 
		{
			while ((dwVSerialPorts.bytesWaiting(this.vport) > 0) && (dwVSerialPorts.isOpen(this.vport)))
			{
				Thread.sleep(100);
			}
			
			dwVSerialPorts.closePort(this.vport);
		} 
		catch (InterruptedException e)
		{
			logger.error(e.getMessage());
		}
		catch (DWPortNotValidException e) 
		{
			logger.error(e.getMessage());
		}
		
				
		logger.debug("exiting");
	}

}

	