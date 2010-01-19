package com.mynumnum.drivewire.server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author Jim Hathaway
 *
 */
public class Jetty {
	private static final String PACKAGE_BASE ="com/mynumnum";
	private static final int DEFAULT_PORT = 8080;
	public Jetty() {
		startWebInterface(DEFAULT_PORT);
	}
	public Jetty(Integer port) {
		startWebInterface(port);
	}

	private void startWebInterface(int webPort) {
		
		Server server = new Server(webPort);

		String webDir = Jetty.class.getClassLoader().getResource(PACKAGE_BASE).toExternalForm();
		try {
			webDir = webDir.substring(0, webDir.lastIndexOf("WEB-INF/classes/" + PACKAGE_BASE));
		} catch (Exception e1) {
			// This is not an error, need to know this for debugging
			System.out.println("working in jar");
		}
		try {
			webDir = webDir.substring(0, webDir.lastIndexOf(PACKAGE_BASE));
		} catch (Exception e1) {
			// This is not an error, need to know this for debugging
			System.out.println("working in eclipse");
		}
		//System.out.println(webDir);
		// time to create a new context, set the base location, and the welcome files, etc
		final Context context = new Context(server, "/" , Context.SESSIONS);
		context.setResourceBase(webDir);
		context.setWelcomeFiles(new String[]{"DriveWireTest.html"});
		// this servlet will serve up the static content
		context.addServlet(new ServletHolder(new DefaultServlet()), "/");
		// This is the servlet that will send data back and forth between the client and the server
		context.addServlet(new ServletHolder(new DriveWireServiceImpl()), "/drivewiregwt/driveWire");
		          
		try {
			server.start();
			server.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	/**
	 * @param args
	 */
	public static void main(Integer port) {
	}

}
