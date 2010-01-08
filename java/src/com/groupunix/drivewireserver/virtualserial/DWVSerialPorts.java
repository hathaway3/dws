package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class DWVSerialPorts {

	private static final Logger logger = Logger.getLogger("DWServer.DWVSerialPorts");
	
	public static final int MULTIREAD_LIMIT = 4;
	public static final int MAX_PORTS = 15;
	public static final int MODE_TELNET = 0;
	public static final int MODE_VMODEM = 1;
	public static final int MODE_PTY = 2;
	public static final int MODE_UTIL = 3;
	public static final int MODE_DEFAULT = MODE_TELNET;
	
	public static final int CHUNK_SIZE = 240;
	
	private static DWVSerialPort[] vserialPorts = new DWVSerialPort[MAX_PORTS];
	
	private static int[] dataWait = new int[MAX_PORTS];
	
	public static void initPort(int port, int mode)
	{
		if (vserialPorts[port] != null)
		{
			// need to do clean shutdown
			
		}
		
		logger.info("init virtual serial port " + prettyPort(port) + " mode " + mode);
		vserialPorts[port] = new DWVSerialPort(port);
		
		vserialPorts[port].setMode(mode);
		
	}


	public static String prettyPort(int port) 
	{
		if (port < 8)
		{
			return("/T" + port);
		}
		else
		{
			return("/U" + (port - 8));
		}
	}


	public static void closePort(int port)
	{
		logger.info("closing virtual serial port " + prettyPort(port));
		vserialPorts[port] = null;
	}
	

	public static byte[] serRead() 
	{
		byte[] response = new byte[2];
		
		// redesigned to avoid bandwidth hogging
		
		// first pass, increment data waiters
		
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
			response[0] = (byte) (oldestMport + 129);     // add one and 128 for serreadm
			
			
			
			response[1] = (byte) vserialPorts[oldestMport].bytesWaiting(); //send data size
		}
		else
		{
			// no waiting ports
			
			response[0] = (byte) 0;
			response[1] = (byte) 0;
		}
		
		
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
			if (vserialPorts[port].isCocoinit())
			{
				// normal write
				vserialPorts[port].write(databyte);
			}
			else
			{
				// write to port we don't have open.. possibly due to server restart without coco restart.
				// make the best of it by initializing the port and trying a write.. 
				logger.info("write to non init port " + prettyPort(port) +", doing init then write");
				initPort(port, vserialPorts[port].getMode());
				Cocoinit(port);
				vserialPorts[port].write(databyte);  
			}
		}
		else
		{
			logger.error("asked to write to nonexistant port " + prettyPort(port));
		}
		
	}



	public static byte[] serReadM(int tmpport, int tmplen) 
	{
		byte[] data = new byte[tmplen];
		
		data = vserialPorts[tmpport].readM(tmplen);
		
		return(data);
	}


	public static int nextFreePort(int mode) 
	{
		for (int i = 0;i<MAX_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if ((vserialPorts[i].getMode() == mode) && (vserialPorts[i].isCocoinit()) && (vserialPorts[i].isConnected() == false))
				{
					return(i);
				}
			}
		}
		
		return(-1);
	}

	public static int numPortsMode(int mode) 
	{
		int tot = 0;
		
		for (int i = 0;i<MAX_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if ((vserialPorts[i].getMode() == mode))
						
				{
					tot++;
				}
			}
		}
		
		return(tot);
	}
	
	
	public static int numConnectedPortsMode(int mode) 
	{
		int tot = 0;
		
		for (int i = 0;i<MAX_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if ((vserialPorts[i].getMode() == mode) && (vserialPorts[i].isConnected() == true))
				{
					tot++;
				}
			}
		}
		
		return(tot);
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
		vserialPorts[vport].setPortOutput(output);
	}


	public static void markConnected(int vport) 
	{
		vserialPorts[vport].setConnected(true);
	}


	public static void markDisconnected(int vport) 
	{
		vserialPorts[vport].setConnected(false);
	}

	public static void Cocoinit(int vport) 
	{
		// start up handlers if needed
		vserialPorts[vport].setCocoinit(true);
	}


	public static void Cocoterm(int vport) 
	{
		vserialPorts[vport].setCocoinit(false);
	}

	

	public static void LoadPortSet(String filename) 
	{
		// load port defaults
	
		if (filename == null)
		{
			return;
		}
			
		logger.info("loading portset '" + filename + "'");
			
		File f = new File(filename);
			
	    FileReader fr;
		try 
		{
			fr = new FileReader(f);
		} 
		catch (FileNotFoundException e) 
		{
			logger.warn("Portset file '" + filename + "' not found.");
			return;
		}
			
		BufferedReader br = new BufferedReader(fr);

		String line;
		    
		try 
		{
			line = br.readLine();
			    
			while (line != null) 
		    {
				String[] parts = new String[3];
		    	
		    	parts = line.split(",", 4);
		    	
		    	int portnum = Integer.parseInt(parts[0]);
		    	
		    	if ((portnum >= 0) && (portnum < MAX_PORTS))
		    	{
		    		// port # is valid
		    		
		    		int mode = Integer.parseInt(parts[1]);
		    		
		    		if ((mode == MODE_TELNET) || (mode == MODE_VMODEM) || (mode == MODE_PTY) || (mode == MODE_UTIL))
		    		{
		    			// mode is valid
		    			
		    			initPort(portnum,mode);
		    			
		    			// if we have a third option
		    			
		    			if (parts.length > 2)
		    			{
		    				if (!parts[2].equals(""))
		    				{
		    					// password is set
		    					vserialPorts[portnum].setPassword(parts[2]);
		    				
		    					logger.info("Port " + portnum +" is password protected");
		    				}
		    			
		    				// a fourth option
		    				if (parts.length > 3)
		    				{
		    					if (!parts[3].equals(""))
			    				{
			    					// password is set
			    					vserialPorts[portnum].setActionFile(parts[3]);
			    				
			    					logger.info("Port " + portnum +" has action file: " + parts[3]);
			    				}
		    				}
		    			}
		    			
		    			
		    		}
		    		else
		    		{
		    			logger.warn("invalid mode '" + parts[1] + "' for port " + portnum + " in set file");
		    		}
		    		
		    	}
		    	else
		    	{
		    		logger.warn("invalid port number '" + parts[0] +"' in set file");
		    	}
		    	
		    	// get next line
		    	line = br.readLine();
			}
		}
		catch (IOException e1) 
		{
			logger.error(e1.getMessage());
		} 
		catch (NumberFormatException e2) 
		{
			logger.error(e2.getMessage());
		} 
		catch (ArrayIndexOutOfBoundsException e3)
		{
			logger.error(e3.getMessage());
		}
		finally
		{
			try 
			{
				br.close();
				fr.close();
			} 
			catch (IOException e) 
			{
				logger.warn(e.getMessage());
			}
		}
		
	}


	public static boolean isEnabled(int port)
	{
		if (vserialPorts[port] != null)
		{
			return(true);
		}
		return(false);
	}

	public static boolean isConnected(int port)
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].isConnected());
		}
		return(false);
	}

	public static int getMode(int port)
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].getMode());
		}
		return(-1);
	}
	
	
	public static String getHostIP(int port) 
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].getHostIP());
		}
		return("unknown");
	}

	public static int getHostPort(int port) 
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].getHostPort());
		}
		return(-1);
	}


	public static void setHostIP(int vport, String clientIP) 
	{
		vserialPorts[vport].setHostIP(clientIP);
	}


	public static void setHostPort(int vport, int clientPort) 
	{
		vserialPorts[vport].setHostPort(clientPort);
	}


	public static boolean isCocoInit(int port) 
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].isCocoinit());
		}
		return(false);
	}

	public static void setUtilMode(int port, int mode)
	{
		if (vserialPorts[port] != null)
		{
			vserialPorts[port].setUtilMode(mode);
		}
	}
	
	public static String getPrettyMode(int mode)
	{
		switch(mode)
		{
			case 0:
				return("telnet");
			case 1:
				return("vmodem");
			case 2:
				return("psuedo tty");
			case 3:
				return("utility");
		}
		
		return("unknown");
	}
	
	
	public static void writeSections(int port, String data) 
	{
		String dataleft = data;
		
		// send data in (numbytes)(bytes) format
		
		while (dataleft.length() > CHUNK_SIZE)
		{
			// send a chunk
			
			write1(port, (byte) CHUNK_SIZE);
			write(port, dataleft.substring(0, CHUNK_SIZE));
			dataleft = dataleft.substring(CHUNK_SIZE);
			logger.debug("sent chunk for " + prettyPort(port) + ", left: " + dataleft.length());
		}
		
		// send last chunk
		int bytes = dataleft.length();
		
		write1(port, (byte) bytes);
		
		write(port, dataleft);
		
		// send termination
		write1(port, (byte) 0);
		
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
		
		try 
		{
			getPortInput(port).write(str.getBytes());
		} 
		catch (IOException e) 
		{
			logger.error("IO error writing to port " + prettyPort(port));
		}
	}


	public static String getActionFile(int port) 
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].getActionFile());
		}
		
		return(null);
	}
	
	
	public static void setPD_INT(int port, byte pD_INT) 
	{
		if (vserialPorts[port] != null)
		{
			vserialPorts[port].setPD_INT(pD_INT);
		}
		
	}



	public static byte getPD_INT(int port) 
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].getPD_INT());
		}
		return(0);
	}



	public static void setPD_QUT(int port, byte pD_QUT) 
	{
		if (vserialPorts[port] != null)
		{
			vserialPorts[port].setPD_QUT(pD_QUT);
		}
	}



	public static byte getPD_QUT(int port) 
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].getPD_QUT());
		}
		return(0);
	}
	
	
}
