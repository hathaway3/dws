package com.groupunix.drivewireserver.virtualserial;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class DWUtilURLThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilURLThread");
	
	private int vport = -1;
	private String url = null;
	private String action = null;

	
	public DWUtilURLThread(int vport, String theurl, String theaction)
	{
		logger.debug("init url thread");	
		this.vport = vport;
		this.url = theurl;
		this.action = theaction;
		
		
	}
	

	@SuppressWarnings("deprecation")
	public void run() 
	{
		Thread.currentThread().setName("urlutil-" + Thread.currentThread().getId());
		
		String text = new String();
		
		logger.debug("run");
		
		logger.info("URL - " + this.action + " - " + this.url + " for port " + this.vport);
		
		URL url;
		try
		{
			url = new URL(this.url);
			
			int databyte = -1;
			
			DataInputStream theHTML = new DataInputStream(url.openStream());
			
			while ((databyte = theHTML.read()) >= 0) 
		    {
		        text += Character.toString((char) databyte);
		        
		    } 
			
			DWVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
			DWVSerialPorts.writeToCoco(this.vport, text);	
		} 
		catch (MalformedURLException e)
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Malformed URL: " + e.getMessage());
			
		} 
		catch (IOException e1)
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "IO Error: " + e1.getMessage());
			
		}
		
		
		// wait for output
		try {
			while ((DWVSerialPorts.bytesWaiting(this.vport) > 0) && (DWVSerialPorts.isOpen(this.vport)))
			{
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		DWVSerialPorts.closePort(this.vport);
		logger.debug("exiting");
	}

}

	