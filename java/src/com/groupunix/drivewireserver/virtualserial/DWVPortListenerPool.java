package com.groupunix.drivewireserver.virtualserial;

import java.net.Socket;

public class DWVPortListenerPool {

	private static Socket[] sockets = new Socket[256];
	
	public static int addConn(Socket skt) 
	{
		for (int i = 0; i< 256;i++)
		{
			if (sockets[i] == null)
			{
				sockets[i] = skt;
				return(i);
			}
		}
		
		return(-1);
	}

	public static Socket getConn(int conno)
	{
		return(sockets[conno]);
	}
	
	public static void clearConn(int conno)
	{
		sockets[conno] = null;
	}
	
}
