package com.groupunix.drivewireserver.virtualserial;

import java.net.Socket;

public class DWVPortListenerPool {

	private static Socket[] sockets = new Socket[256];
	private static int[] modes = new int[256];
	
	public static int addConn(Socket skt, int mode) 
	{
		for (int i = 0; i< 256;i++)
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
	
}
