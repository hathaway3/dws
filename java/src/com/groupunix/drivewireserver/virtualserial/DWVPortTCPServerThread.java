package com.groupunix.drivewireserver.virtualserial;



import java.io.IOException;
import java.net.Socket;


import org.apache.log4j.Logger;

public class DWVPortTCPServerThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTCPServerThread");
	
	private int vport = -1;
	private Socket skt; 
	private int conno;
	private boolean wanttodie = false;
	
	public DWVPortTCPServerThread(int vport, int conno)
	{
		logger.debug("init tcp server thread for conn " + conno);	
		this.vport = vport;
		this.conno = conno;
		this.skt = DWVPortListenerPool.getConn(conno);
		
	}
	

	public void run() 
	{
		Thread.currentThread().setName("tcpserv-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		// connection mode
			
		// set pass through mode
		DWVSerialPorts.markConnected(vport);	
		try {
			DWVSerialPorts.setPortOutput(vport, skt.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while ((wanttodie == false) && (skt.isClosed() == false) && (DWVSerialPorts.isOpen(this.vport)))
		{
			try 
			{
				int tcpAvail = skt.getInputStream().available();
				if (tcpAvail > 1)
				{
					// read block
					byte[] buffer = new byte[tcpAvail];
					skt.getInputStream().read(buffer, 0, tcpAvail);
					DWVSerialPorts.write(this.vport, new String(buffer));
						
				}
				else
				{
					//wait for data/read one
					int databyte = skt.getInputStream().read();
					if (databyte == -1)
					{
						wanttodie = true;
					}
					else
					{
						DWVSerialPorts.write1(this.vport,(byte)databyte);
					}
				}
			} 
			catch (IOException e) 
			{
					logger.debug("IO error reading tcp: " + e.getMessage());
					wanttodie = true;
			}
				
		}
			
		DWVSerialPorts.markDisconnected(this.vport);
		DWVSerialPorts.setPortOutput(vport, null);
			
		if (skt.isClosed() == false)
		{
			// close socket
			try 
			{
				skt.close();
			} 
			catch (IOException e) 
			{
				logger.debug("error closing socket: " + e.getMessage());
			}
		}
			
		
		// only if we got connected..
		if (skt != null)
		{
			if (skt.isConnected())
			{
		
				logger.debug("exit stage 1, flush buffer");
		
				// 	flush buffer, term port
				try {
					while ((DWVSerialPorts.bytesWaiting(this.vport) > 0) && (DWVSerialPorts.isOpen(this.vport)))
					{
						Thread.sleep(100);
					}
				} 
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				logger.debug("exit stage 2, send peer signal");
		
				DWVSerialPorts.closePort(this.vport);
			}
			
			DWVPortListenerPool.clearConn(this.conno);	
		}
		
		logger.debug("thread exiting");
	}	
}

	