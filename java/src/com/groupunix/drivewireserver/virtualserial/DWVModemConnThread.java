package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

public class DWVModemConnThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVModemConnThread");
	
	private Socket skt; 
	private String clientHost = "none";
	private int clientPort = -1;
	private int vport = -1;
	
	public DWVModemConnThread(int vport, String host, int tcpport) 
	{
		this.vport = vport;
 		this.clientHost = host;
		this.clientPort = tcpport;
	}

	public void run() 
	{
		Thread.currentThread().setName("mdmconn-" + Thread.currentThread().getId());
		logger.debug("thread run for connection to " + this.clientHost + ":" + clientPort);
		
		try 
		{
			
			skt = new Socket(clientHost,clientPort);
			
			DWVSerialPorts.markConnected(vport);
			DWVSerialPorts.setPortOutput(vport, skt.getOutputStream());
				
			int lastbyte = 0;
				
			while (skt.isConnected())
			{
			
				int data = skt.getInputStream().read();
				if (data >= 0)
				{
					// write it to the serial port
					DWVSerialPorts.getPortInput(vport).write((byte) data);
					lastbyte = data;
				}
				else
				{
					logger.info("end of stream from TCP client at " + this.clientHost + ":" + this.clientPort);
					if (skt.isConnected())
					{
						logger.debug("closing socket");
						skt.close();
					}
						
				}
			}
			
		} 
		catch (IOException e) 
		{
			logger.warn("IO error in connection to " + this.clientHost + ":" + this.clientPort + " = " + e.getMessage());
		} 
		finally
		{
			if (this.vport > -1)
			{
				DWVSerialPorts.markDisconnected(this.vport);
				// TODO: this is all wrong
				try {
					DWVSerialPorts.getPortInput(vport).write("\r\n\r\nNO CARRIER\r\n".getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.debug("thread exiting");
		}
	}

}
