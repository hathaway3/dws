package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

public class DWVPortListenerPool {

	public static final int MAX_CONN = 256;
	private static Socket[] sockets = new Socket[MAX_CONN];
	private static int[] modes = new int[MAX_CONN];
	
	private static final Logger logger = Logger.getLogger("DWServer.DWVPortListenerPool");
	
	public static int addConn(Socket skt, int mode) 
	{
		for (int i = 0; i< MAX_CONN;i++)
		{
			if (sockets[i] == null)
			{
				sockets[i] = skt;
				modes[i] = mode;
				return(i);
			}
		}
		
		return(-1);
	}

	public static Socket getConn(int conno)
	{
		return(sockets[conno]);
	}
	
	// temporary crap to make telnetd work
	public static int getMode(int conno)
	{
		return(modes[conno]);
	}
	
	public static void clearConn(int conno)
	{
		sockets[conno] = null;
	}

	public static void killConn(int conno)
	{
		try
		{
			sockets[conno].close();
			logger.debug("killed conn #" + conno);
		} 
		catch (IOException e)
		{
			logger.debug("IO error closing conn #" + conno + ": " + e.getMessage());
		}
		
		clearConn(conno);
		
	}
	
}
