/**
 * 
 */
package com.mynumnum.drivewire.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.groupunix.drivewireserver.DriveWireServer;

/**
 * @author Jim Hathaway
 * This is used when you launch this project as a GWT project
 *
 */
public class MyServletContextListener implements ServletContextListener {

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Starting up the DriveWire Server.");
		//DriveWireServer.main(null);

	}

}
