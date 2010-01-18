package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;

public class DWUtilURLThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilURLThread");
	
	private int vport = -1;
	private String url = null;
	private String action = null;
	
	private boolean wanttodie = false;
	
	public DWUtilURLThread(int vport, String theurl, String theaction)
	{
		logger.debug("init url thread");	
		this.vport = vport;
		this.url = theurl;
		this.action = theaction;
		
		
	}
	

	public void run() 
	{
		Thread.currentThread().setName("urlutil-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
				
		logger.debug("exiting");
	}

}

	