package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class DWVPortListenerPool {

	public static final int MAX_CONN = 256;
	public static final int MAX_LISTEN = 64;
	private Socket[] sockets = new Socket[MAX_CONN];
	private ServerSocket[] server_sockets = new ServerSocket[MAX_LISTEN];
	private int[] serversocket_ports = new int[MAX_LISTEN];
	private int[] socket_ports = new int[MAX_CONN];
	private int[] modes = new int[MAX_CONN];
	
	private static final Logger logger = Logger.getLogger("DWServer.DWVPortListenerPool");
	
	public int addConn(int port, Socket skt, int mode) 
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

	public Socket getConn(int conno)
	{
		return(sockets[conno]);
	}
	
	public void setConnPort(int conno, int port)
	{
		socket_ports[conno] = port;
	}
	
	public int addListener(int port, ServerSocket srvskt)
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
	
	public ServerSocket getListener(int conno)
	{
		return(server_sockets[conno]);
	}
	
	
	public void closePortServerSockets(int port)
	{
		for (int i = 0;i<MAX_LISTEN;i++)
		{
			if (this.getListener(i) != null)
			{
				if (serversocket_ports[i] == port)
				{
					this.killListener(i);
				}
			}
		}
	}
	
	public void closePortConnectionSockets(int port)
	{
		for (int i = 0;i<DWVPortListenerPool.MAX_CONN;i++)
		{
			if (this.getConn(i) != null)
			{
				// don't reset term
				if (this.getMode(i) != DWVSerialPorts.MODE_TERM)
				{
					
					this.killConn(i);
				}
			}
		}

		
	}
	
	// temporary crap to make telnetd work
	public int getMode(int conno)
	{
		return(modes[conno]);
	}
	
	public void clearConn(int conno)
	{
		sockets[conno] = null;
		socket_ports[conno] = -1;
	}

	public void clearListener(int conno)
	{
		server_sockets[conno] = null;
		serversocket_ports[conno] = -1;
	}
	
	public void killConn(int conno)
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

	public void killListener(int conno)
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

	public int getListenerPort(int i)
	{
		return serversocket_ports[i];
	}

	public int getConnPort(int i)
	{
		return socket_ports[i];
	}
	
}
