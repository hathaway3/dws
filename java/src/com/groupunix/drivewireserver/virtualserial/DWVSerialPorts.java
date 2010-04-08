package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class DWVSerialPorts {

	private static final Logger logger = Logger.getLogger("DWServer.DWVSerialPorts");
	
	// should move multiread toggle and max ports to config file
	public static final int MULTIREAD_LIMIT = 3;
	public static final int TERM_PORT = 0;
	public static final int MODE_TERM = 3;
	public static final int MAX_COCO_PORTS = 15;
	public static final int MAX_UI_PORTS = 15;
	public static final int MAX_PORTS = MAX_COCO_PORTS + MAX_UI_PORTS;
	
	private int handlerno;
	
	
	private DWVSerialPort[] vserialPorts = new DWVSerialPort[MAX_PORTS];
	
	private int[] dataWait = new int[MAX_PORTS];
	
	
	public DWVSerialPorts(int handlerno)
	{
		this.handlerno = handlerno;
	}


	public void openPort(int port)
	{
		if (vserialPorts[port] == null)
		{
			//this happens in UI and on TERM/headless mode.. guess it doesn't matter
			//logger.error("WHY IS THIS PORT NULL? RESETING IT.");
			resetPort(port);
		}
		
 		vserialPorts[port].open();
	}


	public String prettyPort(int port) 
	{
		if (port == TERM_PORT)
		{
			return("Term");
		}
		else if (port < MAX_COCO_PORTS)
		{
			return("/N" + port);
		}
		else
		{
			return("UI:" + port);
		}
	}


	public void closePort(int port)
	{
		vserialPorts[port].close();	
	}
	

	public byte[] serRead() 
	{
		byte[] response = new byte[2];
		
		// redesigned to avoid bandwidth hogging
		
		// first look for termed ports
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if (vserialPorts[i].isTerm())
				{
					response[0] = (byte) 16;  // port status
					response[1] = (byte) i;   // 000 portnumber
					
					logger.info("sending terminated status to coco for port " + i);
					
					vserialPorts[i] = new DWVSerialPort(this.handlerno, i);
					
					return(response);
				}
			}
		}
		
		
		
		// first data pass, increment data waiters
		
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if (vserialPorts[i].bytesWaiting() > 0)
				{
					// increment wait count
					dataWait[i]++;
				}
			}
		}
		
		// second pass, look for oldest waiting ports
		
		int oldest1 = 0;
		int oldest1port = -1;
		int oldestM = 0;
		int oldestMport = -1;
		
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if (vserialPorts[i].bytesWaiting() < MULTIREAD_LIMIT)
				{
					if (dataWait[i] > oldest1)
					{
						oldest1 = dataWait[i];
						oldest1port = i;
					}
				}
				else
				{
					if (dataWait[i] > oldestM)
					{
						oldestM = dataWait[i];
						oldestMport = i;
					}
				}
			}
		}
		
		if (oldest1port > -1)
		{
			// if we have a small byte waiter, send serread for it
			
			dataWait[oldest1port] = 0;
			response[0] = (byte) (oldest1port + 1);     // add one
			response[1] = vserialPorts[oldest1port].read1();  // send data byte
		}
		else if (oldestMport > -1)
		{
			// send serream for oldest bulk
			
			dataWait[oldestMport] = 0;
			response[0] = (byte) (oldestMport + 16 + 1);     // add one and 16 for serreadm
			response[1] = (byte) vserialPorts[oldestMport].bytesWaiting(); //send data size
			// logger.debug("SERREADM RESPONSE: " + Integer.toBinaryString(response[0]) + " " + Integer.toBinaryString(response[1]));

		}
		else
		{
			// no waiting ports
			
			response[0] = (byte) 0;
			response[1] = (byte) 0;
		}
		
		// logger.debug("SERREAD RESPONSE: " + Integer.toBinaryString(response[0]) + " " + Integer.toBinaryString(response[1]));
		
		return(response);
	}


	public void serWriteM(int port, String str)
	{
		for (int i = 0;i<str.length();i++)
		{
			serWrite(port, str.charAt(i));
		}
	}
	

	public void serWrite(int port, int databyte) 
	{
		// logger.debug("write to port " + port + ": " + databyte);
		
		if (port < MAX_COCO_PORTS)
		{
			if (vserialPorts[port].isOpen())
			{
				// normal write
				vserialPorts[port].write(databyte);
			}
			else
			{
				logger.debug("write to closed port " + port);
			}
		}
		else
		{
			logger.error("asked to write to nonexistant port " + port);
		}
		
	}



	public byte[] serReadM(int tmpport, int tmplen) 
	{
		byte[] data = new byte[tmplen];
		
		data = vserialPorts[tmpport].readM(tmplen);
		
		return(data);
	}

	
	
	
	public OutputStream getPortInput(int vport) 
	{
		return (vserialPorts[vport].getPortInput());
	}

	public InputStream getPortOutput(int vport) 
	{
		return (vserialPorts[vport].getPortOutput());
	}
	
	public void setPortOutput(int vport, OutputStream output)
	{
		if (isNull(vport))
		{
			logger.debug("attempt to set output on null port " + vport);
		}
		else
		{
			vserialPorts[vport].setPortOutput(output);
		}
	}


	public void markConnected(int vport) 
	{
		if (vserialPorts[vport] == null)
		{
			logger.warn("mark connected on null port " + vport);
		}
		else
		{
			vserialPorts[vport].setConnected(true);
		}
	}


	public void markDisconnected(int vport) 
	{
		vserialPorts[vport].setConnected(false);
	}


	public boolean isConnected(int port)
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].isConnected());
		}
		return(false);
	}

	

	public void setUtilMode(int port, int mode)
	{
		vserialPorts[port].setUtilMode(mode);
	}
	
		
	
	
	public void write1(int port, byte data)
	{

		try 
		{
			getPortInput(port).write(data);
		} 
		catch (IOException e) 
		{
			logger.error("IO error writing to port " + prettyPort(port));
		}
	}

	public void write(int port, String str)
	{
		
			vserialPorts[port].writeM(str);
		
	}

	
	
	public void setPD_INT(int port, byte pD_INT) 
	{
		vserialPorts[port].setPD_INT(pD_INT);
	}



	public byte getPD_INT(int port) 
	{
		return(vserialPorts[port].getPD_INT());
	}



	public void setPD_QUT(int port, byte pD_QUT) 
	{
		vserialPorts[port].setPD_QUT(pD_QUT);
	}



	public byte getPD_QUT(int port) 
	{
		return(vserialPorts[port].getPD_QUT());
	}






	public void sendUtilityFailResponse(int vport, int code, String txt) 
	{
		logger.debug("API FAIL: port " + vport + " code " + code + ": " + txt);
		vserialPorts[vport].sendUtilityFailResponse(code, txt);
	}


	public void sendUtilityOKResponse(int vport, String txt) 
	{
		logger.debug("API OK: port " + vport + ": " + txt);
		vserialPorts[vport].sendUtilityOKResponse(txt);
	}


	public int bytesWaiting(int vport) 
	{
		return(vserialPorts[vport].bytesWaiting());
	}

	

	public void setDD(byte vport, byte[] devdescr)
	{
		vserialPorts[vport].setDD(devdescr);
	}


	public void resetAllPorts() 
	{
		logger.debug("Resetting all virtual serial ports - part 1, close all sockets");
		
		
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			DWVPortListenerPool.closePortConnectionSockets(i);
			DWVPortListenerPool.closePortServerSockets(i);
		}
		
		logger.debug("Resetting all virtual serial ports - part 2, init all ports");
		
		//vserialPorts = new DWVSerialPort[MAX_PORTS];
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			// dont reset term
			if (i != TERM_PORT)
				resetPort(i);
		}
	}

	public void resetPort(int i)
	{
		vserialPorts[i] = new DWVSerialPort(this.handlerno, i);
	}
	
	public boolean isOpen(int vport) 
	{
		if (vserialPorts[vport] != null)
			return(vserialPorts[vport].isOpen());
		
		return(false);
	}


	public int getOpen(int i) 
	{
		return(vserialPorts[i].getOpen());
	}


	public byte[] getDD(int i)
	{
		if (vserialPorts[i] != null)
		{
			return(vserialPorts[i].getDD());
		}
		return(null);
	}


	
	//public static void setSocket(int vport, Socket skt) 
	//{
	//	vserialPorts[vport].setSocket(skt);
	//}


	public void writeToCoco(int vport, byte databyte) 
	{
		vserialPorts[vport].writeToCoco(databyte);
	}
	
	public void writeToCoco(int vport, String str) 
	{
		vserialPorts[vport].writeToCoco(str);
	}



	public boolean hasOutput(int vport)
	{
		if (vserialPorts[vport] != null)
		{
			return(vserialPorts[vport].hasOutput());
		}

		return false;
	}
	
	public boolean isNull(int vport)
	{
		if (vserialPorts[vport] == null)
			return(true);
		
		return(false);
	}


	public boolean isValid(byte b)
	{
	  if ((b >= 0) && (b < MAX_PORTS))
		  return(true);
	  
	  return(false);
		
	}


	public void sendConnectionAnnouncement(int vport, int conno, int localport, String hostaddr)
	{
		vserialPorts[vport].sendConnectionAnnouncement(conno, localport, hostaddr);
	}


	public void setConn(int vport, int conno)
	{
		vserialPorts[vport].setConn(conno);
		
	}
	
	public int getConn(int vport)
	{
		return(vserialPorts[vport].getConn());
	}


	public String getHostIP(int vport)
	{
		if (vserialPorts[vport] != null)
		{
			return(DWVPortListenerPool.getConn(vserialPorts[vport].getConn()).getInetAddress().getCanonicalHostName());
		}
		return(null);
	}


	public int getHostPort(int vport)
	{
		if (vserialPorts[vport] != null)
		{
			return(DWVPortListenerPool.getConn(vserialPorts[vport].getConn()).getPort());
		}
		return(-1);
	}


	public void shutdown()
	{
		logger.debug("shutting down");
		
		for (int i = 0;i<MAX_PORTS;i++)
		{
			DWVPortListenerPool.closePortConnectionSockets(i);
			DWVPortListenerPool.closePortServerSockets(i);
			if (this.vserialPorts[i] != null)
			{
				this.vserialPorts[i].shutdown();
			}
		}
		
		
		
		
		
	}


	public synchronized int openUIPort() 
	{
		// find first available UI port
		
		int uiport = MAX_COCO_PORTS;
		
		while (uiport < MAX_PORTS)
		{
			if (this.vserialPorts[uiport] == null)
			{
				openPort(uiport);
				return(uiport);
			}
			
			if (this.vserialPorts[uiport].isOpen() == false)
			{
				openPort(uiport);
				return(uiport);
			}
			
			uiport++;
		}
		
		return(-1);
	}
	
}
