package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class DWVSerialPort {

	private static final Logger logger = Logger.getLogger("DWServer.DWVSerialPort");
	
	private static final int BUFFER_SIZE = -1;  //infinite

	private int port = -1;
	private boolean connected = false;
	private int opens = 0;

	private DWVPortHandler porthandler = null;

	
	private byte PD_INT = 0;
	private byte PD_QUT = 0;
	private byte[] DD = new byte[26];
	
	private	DWVSerialCircularBuffer inputBuffer = new DWVSerialCircularBuffer(BUFFER_SIZE, true);
	private	OutputStream output;
	
	private String hostIP = null;
	private int hostPort = -1;
	
	private int userGroup = -1;
	private String userName = "unknown";
	
	private boolean wanttodie = false;
	private Socket socket = null;
	private ServerSocket serversocket = null;
	
	
	public DWVSerialPort(int port)
	{
		logger.debug("New DWVSerialPort for port " + port);
		this.port = port;
		if (port != DWVSerialPorts.TERM_PORT)
		{
			this.porthandler = new DWVPortHandler(port);
		}
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
		// if we are connected, pass the data
		if ((this.connected) || (this.port == DWVSerialPorts.TERM_PORT))
		{
			if (output == null)
			{
				logger.debug("write to null stream on port " + this.port);
			}
			else
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
		}
		// otherwise process as command
		else
		{
			this.porthandler.takeInput(databyte);
		}
		
	}

	public void writeM(String str)
	{
		for (int i = 0;i<str.length();i++)
		{
			write(str.charAt(i));
		}
	}
	
	
	public void writeToCoco(String str)
	{
		try {
			inputBuffer.getOutputStream().write(str.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeToCoco(byte databyte)
	{
		try {
			inputBuffer.getOutputStream().write(databyte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


	public void setConnected(boolean connected) 
	{
		this.connected = connected;
	}



	public boolean isConnected() 
	{
		return connected;
	}

	
	public boolean isOpen()
	{
		if (this.opens > 0)
		{
			return(true);
		}
		else
		{
			return(false);
		}
	}


	public void open()
	{
		this.opens++;
		logger.debug("open port " + this.port + ", total opens: " + this.opens);
	}
	
	public void close()
	{
		if (this.opens > 0)
		{
			this.opens--;
			logger.debug("close port " + this.port + ", total opens: " + this.opens);
			
			// send term if last open
			if (this.opens == 0)
			{
				logger.debug("setting term on port " + this.port);
				this.wanttodie = true;
				
				if (this.output != null)
				{
					logger.debug("closing output on port " + this.port);
					try {
						output.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (this.serversocket != null)
				{
					logger.debug("closing server socket on port " + this.port);
					
					try {
						this.serversocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					this.serversocket = null;
				}
				
				if (this.socket != null)
				{
					logger.debug("closing socket on port " + this.port);
					
					try {
						this.socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					this.socket = null;
				}
				
			}
		}
		else
		{
			logger.error("close port " + this.port + " with no opens?");
		}
		
	}
	
	public boolean isTerm()
	{
		return(wanttodie);
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
		//this.utilhandler.setUtilmode(mode);
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




	public void sendUtilityFailResponse(int errno, String txt) 
	{
		String perrno = String.format("%03d", errno);
		logger.debug("command failed: " + perrno + " " + txt);
		try {
			inputBuffer.getOutputStream().write(("FAIL " + perrno + " " + txt + (char) 13).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.utilhandler.respondFail(code, txt);
	}
	
	public void sendUtilityOKResponse(String txt) 
	{
		try {
			inputBuffer.getOutputStream().write(("OK " + txt + (char) 13).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.utilhandler.respondOk(txt);
	}



	public void setDD(byte[] devdescr) 
	{
		this.DD = devdescr;
	}

	public byte[] getDD()
	{
		return(this.DD);
	}

	public int getOpen() 
	{
		return(this.opens);
	}

	public void setUserGroup(int userGroup) {
		this.userGroup = userGroup;
	}

	public int getUserGroup() {
		return userGroup;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setSocket(ServerSocket skt) 
	{
		this.serversocket = skt;
	}
	
	public void setSocket(Socket skt) 
	{
		this.socket = skt;
	}

	public boolean hasOutput()
	{
		if (output == null)
		{
			return false;
		}
		else
		{
			return true;
		}
		
	}
	

	

	
}

