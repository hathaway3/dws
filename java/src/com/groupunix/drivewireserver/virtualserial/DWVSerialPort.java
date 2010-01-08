package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class DWVSerialPort {

	private static final Logger logger = Logger.getLogger("DWServer.DWVSerialPort");
	
	private static final int BUFFER_SIZE = -1;  //infinite
	private int mode = 0;
	private int port = -1;
	private boolean connected = false;
	private boolean cocoinit = false;
	private String password = null;
	private String actionfile = null;
	private DWVModem vmodem = null;
	private DWUtilHandler utilhandler = null;
	
	private byte PD_INT = 0;
	private byte PD_QUT = 0;
	
	private	DWVSerialCircularBuffer inputBuffer = new DWVSerialCircularBuffer(BUFFER_SIZE, true);
	private	OutputStream output;
	
	private String hostIP = null;
	private int hostPort = -1;
	
	private boolean wanttodie = false;
	
	public DWVSerialPort(int port)
	{
		this.port = port;
		logger.debug("init port " + DWVSerialPorts.prettyPort(port));
	}
	
	
	
	public int bytesWaiting() 
	{
		int bytes = inputBuffer.getAvailable();
		
		if (bytes < 2)
		{
			// always ok to send 0 or 1
			return(bytes);
		}
		else
		{
			// never admit to having more than 255 bytes
			if (bytes < 256)
				return(bytes);
			else
				return(255);
		}
	}

	
	public void write(int databyte) 
	{
		if (this.connected)
		{
			try 
			{
				
				output.write((byte) databyte);
				// logger.debug("wrote byte to output buffer, size now " + output.getAvailable());
			} 
			catch (IOException e) 
			{
				logger.error("in write: " + e.getMessage());
			}
		}
		else
		{
			if (this.mode == DWVSerialPorts.MODE_VMODEM)
			{
				this.vmodem.takeInput(databyte);
			}
			else if (this.mode == DWVSerialPorts.MODE_UTIL)
			{
				
				this.utilhandler.takeInput(databyte);
			}
		}
		
	}


	public OutputStream getPortInput()
	{
		return(inputBuffer.getOutputStream());
	}
	
	public InputStream getPortOutput()
	{
		return(inputBuffer.getInputStream());
	}
	
	
	
	public byte read1() 
	{
	
		int databyte;
		
		try 
		{
			databyte = inputBuffer.getInputStream().read();
			return((byte) databyte);
		} 
		catch (IOException e) 
		{
			logger.error("in read1: " + e.getMessage());
		}
		
		return(-1);
		
	}


	public byte[] readM(int tmplen) 
	{
		byte[] buf = new byte[tmplen];
		
		try {
			inputBuffer.getInputStream().read(buf, 0, tmplen);
			return(buf);
			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			logger.error("Failed to read " + tmplen + " bytes in SERREADM... not good");
		}
		return null;
	}


	public void setMode(int mode) 
	{
		this.mode = mode;
		if (mode == DWVSerialPorts.MODE_VMODEM)
		{
			this.vmodem = new DWVModem(this.port, this.inputBuffer.getOutputStream());
		}
		else if (mode == DWVSerialPorts.MODE_UTIL)
		{
			this.utilhandler = new DWUtilHandler(this.port, this.inputBuffer.getOutputStream());
		}
	}



	public int getMode() 
	{
		return mode;
	}



	public void setConnected(boolean connected) 
	{
		this.connected = connected;
	}



	public boolean isConnected() 
	{
		return connected;
	}



	public void setCocoinit(boolean cocoinit) 
	{
		this.cocoinit = cocoinit;
	}



	public boolean isCocoinit() 
	{
		return cocoinit;
	}



	public void setPassword(String string) 
	{
		this.password = string;
		
	}
	
	public boolean checkPassword(String pw)
	{
		if (pw.equals(this.password))
		{
			return(true);
		}
		return(false);
	}



	public void setPortOutput(OutputStream output) 
	{
		this.output = output;
	}



	public String getHostIP() 
	{
		return(this.hostIP);
	}

	public void setHostIP(String ip)
	{
		this.hostIP = ip;
	}
	
	public int getHostPort()
	{
		return(this.hostPort);
	}
	
	public void setHostPort(int port)
	{
		this.hostPort = port;
	}

	
	public void setUtilMode(int mode)
	{
		this.utilhandler.setUtilmode(mode);
	}



	public void setActionFile(String fname) 
	{
		this.actionfile = fname;
	}



	public String getActionFile() 
	{
		return(this.actionfile);
	}



	public void setPD_INT(byte pD_INT) 
	{
		PD_INT = pD_INT;
		this.inputBuffer.setDW_PD_INT(PD_INT);
	}



	public byte getPD_INT() {
		return PD_INT;
	}



	public void setPD_QUT(byte pD_QUT) 
	{
		PD_QUT = pD_QUT;
		this.inputBuffer.setDW_PD_QUT(PD_QUT);
	}



	public byte getPD_QUT() 
	{
		return PD_QUT;
	}



	public void clearUtilityInputBuffer() 
	{
		this.inputBuffer.clear();
	}



	public void sendUtilityFailResponse(int code, String txt) 
	{
		this.utilhandler.respondFail(code, txt);
	}
	
	public void sendUtilityOKResponse(String txt) 
	{
		this.utilhandler.respondOk(txt);
	}



	public void term() 
	{
		// give ourselves the axe
		this.wanttodie = true;
		
	}
	
	public boolean isTerm()
	{
		return(this.wanttodie);
	}
	
}

