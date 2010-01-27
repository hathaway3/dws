package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class DWVPortListenerPool {

	public static final int MAX_CONN = 256;
	public static final int MAX_LISTEN = 64;
	private static Socket[] sockets = new Socket[MAX_CONN];
	private static ServerSocket[] server_sockets = new ServerSocket[MAX_LISTEN];
	private static int[] serversocket_ports = new int[MAX_LISTEN];
	private static int[] socket_ports = new int[MAX_CONN];
	private static int[] modes = new int[MAX_CONN];
	
	private static final Logger logger = Logger.getLogger("DWServer.DWVPortListenerPool");
	
	public static int addConn(int port, Socket skt, int mode) 
	{
		for (int i = 0; i< MAX_CONN;i++)
		{
			if (sockets[i] == null)
			{
				sockets[i] = skt;
				modes[i] = mode;
				socket_ports[i] = port;
				return(i);
			}
		}
		
		return(-1);
	}

	public static Socket getConn(int conno)
	{
		return(sockets[conno]);
	}
	
	public static void setConnPort(int conno, int port)
	{
		socket_ports[conno] = port;
	}
	
	public static int addListener(int port, ServerSocket srvskt)
	{
		for (int i = 0; i< MAX_LISTEN;i++)
		{
			if (server_sockets[i] == null)
			{
				server_sockets[i] = srvskt;
				serversocket_ports[i] = port;
				return(i);
			}
		}
	 	
		return(-1);
	}
	
	public static ServerSocket getListener(int conno)
	{
		return(server_sockets[conno]);
	}
	
	
	public static void closePortServerSockets(int port)
	{
		for (int i = 0;i<MAX_LISTEN;i++)
		{
			if (DWVPortListenerPool.getListener(i) != null)
			{
				if (serversocket_ports[i] == port)
				{
					DWVPortListenerPool.killListener(i);
				}
			}
		}
	}
	
	public static void closePortConnectionSockets(int port)
	{
		for (int i = 0;i<DWVPortListenerPool.MAX_CONN;i++)
		{
			if (DWVPortListenerPool.getConn(i) != null)
			{
				// don't reset term
				if (DWVPortListenerPool.getMode(i) != DWVSerialPorts.MODE_TERM)
				{
					
					DWVPortListenerPool.killConn(i);
				}
			}
		}

		
	}
	
	// temporary crap to make telnetd work
	public static int getMode(int conno)
	{
		return(modes[conno]);
	}
	
	public static void clearConn(int conno)
	{
		sockets[conno] = null;
		socket_ports[conno] = -1;
	}

	public static void clearListener(int conno)
	{
		server_sockets[conno] = null;
		serversocket_ports[conno] = -1;
	}
	
	public static void killConn(int conno)
	{
		
		if (sockets[conno] != null)
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
		else
		{
			logger.warn("asked to kill connection " + conno +" which does not exist");
		}
	}

	public static void killListener(int conno)
	{
		if (server_sockets[conno] != null)
		{
			try
			{
				server_sockets[conno].close();
				logger.debug("killed listener #" + conno);
			} 
			catch (IOException e)
			{
				logger.debug("IO error closing listener #" + conno + ": " + e.getMessage());
			}
		
			clearListener(conno);
			
		}
		else
		{
			logger.warn("asked to kill listener " + conno +" which does not exist");
		}
	}

	public static int getListenerPort(int i)
	{
		return serversocket_ports[i];
	}

	public static int getConnPort(int i)
	{
		return socket_ports[i];
	}
	
}
