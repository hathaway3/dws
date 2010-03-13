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
	public static final int MAX_PORTS = 15;
	
	
	
	private static DWVSerialPort[] vserialPorts = new DWVSerialPort[MAX_PORTS];
	
	private static int[] dataWait = new int[MAX_PORTS];
	
	
	public static void openPort(int port)
	{
		if (vserialPorts[port] == null)
		{
			logger.error("WHY IS THIS PORT NULL? RESETING IT.");
			resetPort(port);
		}
		
 		vserialPorts[port].open();
	}


	public static String prettyPort(int port) 
	{
		if (port == TERM_PORT)
		{
			return("Term");
		}
		return("/N" + port);
	}


	public static void closePort(int port)
	{
		vserialPorts[port].close();	
	}
	

	public static byte[] serRead() 
	{
		byte[] response = new byte[2];
		
		// redesigned to avoid bandwidth hogging
		
		// first look for termed ports
		for (int i = 0;i<MAX_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if (vserialPorts[i].isTerm())
				{
					response[0] = (byte) 16;  // port status
					response[1] = (byte) i;   // 000 portnumber
					
					logger.info("sending terminated status to coco for port " + i);
					
					vserialPorts[i] = new DWVSerialPort(i);
					
					return(response);
				}
			}
		}
		
		
		
		// first data pass, increment data waiters
		
		for (int i = 0;i<MAX_PORTS;i++)
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
		
		for (int i = 0;i<MAX_PORTS;i++)
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


	public static void serWriteM(int port, String str)
	{
		for (int i = 0;i<str.length();i++)
		{
			serWrite(port, str.charAt(i));
		}
	}
	

	public static void serWrite(int port, int databyte) 
	{
		// logger.debug("write to port " + port + ": " + databyte);
		
		if (port < MAX_PORTS)
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



	public static byte[] serReadM(int tmpport, int tmplen) 
	{
		byte[] data = new byte[tmplen];
		
		data = vserialPorts[tmpport].readM(tmplen);
		
		return(data);
	}

	
	
	
	public static OutputStream getPortInput(int vport) 
	{
		return (vserialPorts[vport].getPortInput());
	}

	public static InputStream getPortOutput(int vport) 
	{
		return (vserialPorts[vport].getPortOutput());
	}
	
	public static void setPortOutput(int vport, OutputStream output)
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


	public static void markConnected(int vport) 
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


	public static void markDisconnected(int vport) 
	{
		vserialPorts[vport].setConnected(false);
	}


	public static boolean isConnected(int port)
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].isConnected());
		}
		return(false);
	}

	

	public static void setUtilMode(int port, int mode)
	{
		vserialPorts[port].setUtilMode(mode);
	}
	
		
	
	
	public static void write1(int port, byte data)
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

	public static void write(int port, String str)
	{
		
			vserialPorts[port].writeM(str);
		
	}

	
	
	public static void setPD_INT(int port, byte pD_INT) 
	{
		vserialPorts[port].setPD_INT(pD_INT);
	}



	public static byte getPD_INT(int port) 
	{
		return(vserialPorts[port].getPD_INT());
	}



	public static void setPD_QUT(int port, byte pD_QUT) 
	{
		vserialPorts[port].setPD_QUT(pD_QUT);
	}



	public static byte getPD_QUT(int port) 
	{
		return(vserialPorts[port].getPD_QUT());
	}






	public static void sendUtilityFailResponse(int vport, int code, String txt) 
	{
		logger.debug("API FAIL: port " + vport + " code " + code + ": " + txt);
		vserialPorts[vport].sendUtilityFailResponse(code, txt);
	}


	public static void sendUtilityOKResponse(int vport, String txt) 
	{
		logger.debug("API OK: port " + vport + ": " + txt);
		vserialPorts[vport].sendUtilityOKResponse(txt);
	}


	public static int bytesWaiting(int vport) 
	{
		return(vserialPorts[vport].bytesWaiting());
	}

	

	public static void setDD(byte vport, byte[] devdescr)
	{
		vserialPorts[vport].setDD(devdescr);
	}


	public static void resetAllPorts() 
	{
		logger.debug("Resetting all virtual serial ports - part 1, close all sockets");
		
		
		for (int i = 0;i<MAX_PORTS;i++)
		{
			DWVPortListenerPool.closePortConnectionSockets(i);
			DWVPortListenerPool.closePortServerSockets(i);
		}
		
		logger.debug("Resetting all virtual serial ports - part 2, init all ports");
		
		//vserialPorts = new DWVSerialPort[MAX_PORTS];
		for (int i = 0;i<MAX_PORTS;i++)
		{
			// dont reset term
			if (i != TERM_PORT)
				resetPort(i);
		}
	}

	public static void resetPort(int i)
	{
		vserialPorts[i] = new DWVSerialPort(i);
	}
	
	public static boolean isOpen(int vport) 
	{
		if (vserialPorts[vport] != null)
			return(vserialPorts[vport].isOpen());
		
		return(false);
	}


	public static int getOpen(int i) 
	{
		return(vserialPorts[i].getOpen());
	}


	public static byte[] getDD(int i)
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


	public static void writeToCoco(int vport, byte databyte) 
	{
		vserialPorts[vport].writeToCoco(databyte);
	}
	
	public static void writeToCoco(int vport, String str) 
	{
		vserialPorts[vport].writeToCoco(str);
	}



	public static boolean hasOutput(int vport)
	{
		if (vserialPorts[vport] != null)
		{
			return(vserialPorts[vport].hasOutput());
		}

		return false;
	}
	
	public static boolean isNull(int vport)
	{
		if (vserialPorts[vport] == null)
			return(true);
		
		return(false);
	}


	public static boolean isValid(byte b)
	{
	  if ((b >= 0) && (b < MAX_PORTS))
		  return(true);
	  
	  return(false);
		
	}


	public static void sendConnectionAnnouncement(int vport, int conno, int localport, String hostaddr)
	{
		vserialPorts[vport].sendConnectionAnnouncement(conno, localport, hostaddr);
	}


	public static void setConn(int vport, int conno)
	{
		vserialPorts[vport].setConn(conno);
		
	}
	
	public static int getConn(int vport)
	{
		return(vserialPorts[vport].getConn());
	}


	public static String getHostIP(int vport)
	{
		if (vserialPorts[vport] != null)
		{
			return(DWVPortListenerPool.getConn(vserialPorts[vport].getConn()).getInetAddress().getCanonicalHostName());
		}
		return(null);
	}


	public static int getHostPort(int vport)
	{
		if (vserialPorts[vport] != null)
		{
			return(DWVPortListenerPool.getConn(vserialPorts[vport].getConn()).getPort());
		}
		return(-1);
	}
	
}
