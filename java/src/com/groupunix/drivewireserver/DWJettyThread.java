package com.groupunix.drivewireserver;

import org.apache.log4j.Logger;

public class DWJettyThread implements Runnable 
{
	private int port;
	private static final Logger logger = Logger.getLogger("DWJettyThread");
	
	
	DWJettyThread(int port)
	{
		this.port = port;
	}

	public void run()
	{
		Thread.currentThread().setName("jetty-" + Thread.currentThread().getId());
		
		logger.debug("Starting Jetty thread, port " + port);
		//new Jetty(port);
		logger.debug("Jetty thread exits");
	}

}
