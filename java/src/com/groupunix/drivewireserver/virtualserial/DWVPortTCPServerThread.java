package com.groupunix.drivewireserver.virtualserial;



import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWVPortTCPServerThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTCPServerThread");
	
	private int vport = -1;
	private Socket skt; 
	private int conno;
	private boolean wanttodie = false;
	private int mode = 0;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	
	
	private static final int MODE_TELNET = 1;
	private static final int MODE_TERM = 3;
	
	//private String tmpbuf = new String();

	
	
	public DWVPortTCPServerThread(int handlerno, int vport, int conno)
	{
		logger.debug("init tcp server thread for conn " + conno);	
		this.vport = vport;
		this.conno = conno;
		this.mode = DWVPortListenerPool.getMode(conno);
		this.skt = DWVPortListenerPool.getConn(conno);
		this.handlerno = handlerno;
		this.dwVSerialPorts = DriveWireServer.getHandler(this.handlerno).getVPorts();
		
	}
	

	public void run() 
	{
		Thread.currentThread().setName("tcpserv-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		// setup ties
		DWVPortListenerPool.setConnPort(this.conno, this.vport);
		dwVSerialPorts.setConn(this.vport,this.conno);
		
		
		logger.debug("run for conn " + this.conno);
				
		if (skt == null)
		{
			logger.warn("got a null socket, bailing out");
			return;
		}
		
		// set pass through mode
		dwVSerialPorts.markConnected(vport);	
		try {
			dwVSerialPorts.setPortOutput(vport, skt.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int lastbyte = -1;
		
		while ((wanttodie == false) && (skt.isClosed() == false) && (dwVSerialPorts.isOpen(this.vport) || (mode == MODE_TERM)))
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
					// filter CR,NULL if in telnet or term mode unless PD.INT and PD.QUT = 0
					if (((mode == MODE_TELNET) || (mode == MODE_TERM)) && ((dwVSerialPorts.getPD_INT(this.vport) != 0) || (dwVSerialPorts.getPD_QUT(this.vport) != 0)))
					{
						// logger.debug("telnet in : " + databyte);
						// TODO filter CR/LF.. should do this better
						if (!((lastbyte == 13) && ((databyte == 10) || (databyte == 0))))
						{
							// write it to the serial port
							// logger.debug("passing : " + databyte);
							dwVSerialPorts.writeToCoco(this.vport,(byte)databyte);
							lastbyte = databyte;
						}
						
					}
					else
					{
						
						dwVSerialPorts.writeToCoco(this.vport,(byte)databyte);
					}
				}
				
			} 
			catch (IOException e) 
			{
					logger.debug("IO error reading tcp: " + e.getMessage());
					wanttodie = true;
			}
				
		}
			
		dwVSerialPorts.markDisconnected(this.vport);
		dwVSerialPorts.setPortOutput(vport, null);
			
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
					while ((dwVSerialPorts.bytesWaiting(this.vport) > 0) && (dwVSerialPorts.isOpen(this.vport)))
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
		
				dwVSerialPorts.closePort(this.vport);
			}
			
			DWVPortListenerPool.clearConn(this.conno);	
		}
		
		logger.debug("thread exiting");
	}	
	
	
	
	
	
}

	