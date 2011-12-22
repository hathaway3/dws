package com.groupunix.drivewireserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;


public class DWUIThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWUIThread");

	private int tcpport;
	private boolean wanttodie = false;
	private ServerSocket srvr = null;
	
	private LinkedList<DWUIClientThread> clientThreads = new LinkedList<DWUIClientThread>();

	private int dropppedevents = 0;
	
	public DWUIThread(int port) 
	{
		this.tcpport = port;
	}

	
	public void die()
	{
		this.wanttodie = true;
		try 
		{
			for (DWUIClientThread ct : this.clientThreads)
			{

					ct.die();
				
			}
			
			if (this.srvr != null)
			{
				this.srvr.close();
			}
		} 
		catch (IOException e) 
		{
			logger.warn("IO Error closing socket: " + e.getMessage());
		}
		catch (ConcurrentModificationException e)
		{
			// whatever
		}
		
	}
	
	
	public void run() 
	{
		Thread.currentThread().setName("dwUIserver-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		// open server socket
		
		try 
		{
			// check for listen address
			
			srvr = new ServerSocket(this.tcpport);
			logger.info("UI listening on port " + srvr.getLocalPort());
			
			
			
		} 
		catch (IOException e2) 
		{
			logger.error("Error opening UI socket on port " + this.tcpport +": " + e2.getMessage());
			wanttodie = true;
		}
				
		while ((wanttodie == false) && (srvr.isClosed() == false))
		{
			//logger.debug("UI waiting for connection");
			Socket skt = null;
			try 
			{
				skt = srvr.accept();
				
				if (DriveWireServer.serverconfig.getBoolean("LogUIConnections", false))
					logger.debug("new UI connection from " + skt.getInetAddress().getHostAddress());
				
				Thread uiclientthread = new Thread(new DWUIClientThread(skt, this.clientThreads));
				uiclientthread.setDaemon(true);
				uiclientthread.start();
			
				
			} 
			catch (IOException e1) 
			{
				if (wanttodie)
					logger.debug("IO error while dying: " + e1.getMessage());
				else
					logger.warn("IO error: " + e1.getMessage());
				
				wanttodie = true;
			}
			
		}
			
		if (srvr != null)
		{
			try 
			{
				srvr.close();
			} 
			catch (IOException e) 
			{
				logger.error("error closing server socket: " + e.getMessage());
			}
		}
		
		
		logger.debug("exiting");
	}


	public void submitEvent(DWEvent evt) 
	{
		
		//System.out.println("Event " + evt.getEventType() + " " + evt.getParam("k") + " " + evt.getParam("v"));
		
		synchronized(this.clientThreads)
		{
			Iterator<DWUIClientThread> itr = this.clientThreads.iterator(); 
			
			while(itr.hasNext()) 
			{	
				LinkedBlockingQueue<DWEvent> queue = (LinkedBlockingQueue<DWEvent>) itr.next().getEventQueue(); 
		    
				synchronized(queue)
				{
					if (queue != null)
					{
						if (queue.size() < DWDefs.EVENT_QUEUE_LOGDROP_SIZE)
						{
							queue.add(evt);
						}
						else if ((queue.size() < DWDefs.EVENT_MAX_QUEUE_SIZE) && (evt.getEventType() != DWDefs.EVENT_TYPE_LOG))
						{
							queue.add(evt);
						}
						else
						{
							this.dropppedevents++;
							System.out.println("queue drop: " + queue.size() + "/" + this.dropppedevents);
						}
					}
				}
			}

		} 
	}

}
