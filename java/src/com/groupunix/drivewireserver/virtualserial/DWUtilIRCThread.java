package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;

import org.apache.log4j.Logger;

public class DWUtilIRCThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilIRCThread");
	
	private DWVSerialCircularBuffer input;
	private String username = new String();
	
	private int vport = -1;
	
	private boolean wanttodie = false;
	
	public DWUtilIRCThread(int vport, DWVSerialCircularBuffer utilstream)
	{
		this.vport = vport;
		


		this.input =utilstream;

		logger.debug("init dw IRC thread");	
	}
	
	public void run() 
	{
		
		Thread.currentThread().setName("dwIRC-" + Thread.currentThread().getId());
		
		logger.debug("run");
				
		DWVSerialPorts.write(this.vport, "DriveWire IRC Client\r\n\n");
		
		DWVSerialPorts.write(this.vport, "Please enter your nickname (14 chars or less): ");
		
		this.username = getInputLine();
	
		if ((username.length() > 0) && (username.length() < 15))
		{
			DWVSerialPorts.write(this.vport, "\r\n\nOk, " + username + ", lets get you connected...\r\n");
	        
			DWUtilIRCServer ircServer = new DWUtilIRCServer(this.vport, username);
		
			while (wanttodie == false)
			{
				String strinp = getInputLine();
			
				if (strinp.equalsIgnoreCase("/quit"))
				{
					// ircServer.stop();
										
					wanttodie = true;
				}
				else
				{
					ircServer.sendMsg(strinp);
					DWVSerialPorts.write(this.vport, "\r\n" + username + ": " + strinp + "\r\n");
				}
			
			}
			
			ircServer.stop();
			
		}
		
		
		logger.debug("exiting");
		DWVSerialPorts.write1(this.vport,(byte) 0);
		DWVSerialPorts.setUtilMode(this.vport, 0);
		
	}

	
	private String getInputLine() 
	{
		String inpstr = new String();
		
		try 
		{
			int inbyte = 0;
			
			
			while (inbyte != 13)
			{

				inbyte = input.getInputStream().read();
				if (inbyte != 13)
				{
					if ((inbyte == 8) || (inbyte == 127))
					{
						if (inpstr.length() > 0)
						{
							inpstr = inpstr.substring(0, inpstr.length() - 1);
						}
					}
					else
					{
						inpstr += Character.toString((char) inbyte);
					}
				}
			}
			
			logger.debug("got input: " + inpstr);
			
			return(inpstr);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		
		return(inpstr);
	}

	
}
