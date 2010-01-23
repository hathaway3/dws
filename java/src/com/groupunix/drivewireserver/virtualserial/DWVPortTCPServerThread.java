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
	private int mode = 0;
	
	private static final int MODE_TELNET = 1;
	private static final int MODE_TERM = 3;
	
	//private String tmpbuf = new String();

	
	
	public DWVPortTCPServerThread(int vport, int conno)
	{
		logger.debug("init tcp server thread for conn " + conno);	
		this.vport = vport;
		this.conno = conno;
		this.mode = DWVPortListenerPool.getMode(conno);
		this.skt = DWVPortListenerPool.getConn(conno);
		
	}
	

	public void run() 
	{
		Thread.currentThread().setName("tcpserv-" + Thread.currentThread().getId());
		
		logger.debug("run for conn " + this.conno);
				
		if (skt == null)
		{
			logger.warn("got a null socket, bailing out");
			return;
		}
		
		// set pass through mode
		DWVSerialPorts.markConnected(vport);	
		try {
			DWVSerialPorts.setPortOutput(vport, skt.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int lastbyte = -1;
		
		while ((wanttodie == false) && (skt.isClosed() == false) && (DWVSerialPorts.isOpen(this.vport) || (mode == MODE_TERM)))
		{
			
			try 
			{
				int databyte = skt.getInputStream().read();
				if (databyte == -1)
				{
					wanttodie = true;
				}
				else
				{
					if ((mode == MODE_TELNET) || (mode == MODE_TERM))
					{
						// logger.debug("telnet in : " + databyte);
						// TODO filter CR/LF.. should do this better
						if (!((lastbyte == 13) && ((databyte == 10) || (databyte == 0))))
						{
							// write it to the serial port
							// logger.debug("passing : " + databyte);
							DWVSerialPorts.writeToCoco(this.vport,(byte)databyte);
							lastbyte = databyte;
						}
						
					}
					else
					{
						
						DWVSerialPorts.writeToCoco(this.vport,(byte)databyte);
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
			
		
		// only if we got connected.. and its not term
		if ((skt != null) && (mode != MODE_TERM))
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

	