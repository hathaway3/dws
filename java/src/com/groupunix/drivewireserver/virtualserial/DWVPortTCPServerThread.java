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
		
		logger.debug("run");
		
		// connection mode
		if (mode == 1)
		{
			// telnet processing
			
			// ask telnet to turn off echo, should probably be a setting or left to the client
			byte[] buf = new byte[9];
			
			buf[0] = (byte) 255;
			buf[1] = (byte) 251;
			buf[2] = (byte) 1;
			buf[3] = (byte) 255;
			buf[4] = (byte) 251;
			buf[5] = (byte) 3;
			buf[6] = (byte) 255;
			buf[7] = (byte) 253;
			buf[8] = (byte) 243;
			
			try
			{
				skt.getOutputStream().write(buf, 0, 9);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		
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
			int lastbyte = -1;
			
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
						if (mode == 1)
						{
							// filter CR/LF.. should really do this in the client or at least make it a setting
							if (!((lastbyte == 13) && ((databyte == 10) || (databyte == 0))))
							{
								// write it to the serial port
								DWVSerialPorts.write1(this.vport,(byte)databyte);
								lastbyte = databyte;
							}
						}
						else
						{
						
							DWVSerialPorts.write1(this.vport,(byte)databyte);
						}
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

	