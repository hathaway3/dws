package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWVSerialPort {

	private static final Logger logger = Logger.getLogger("DWServer.DWVSerialPort");
	
	private static final int BUFFER_SIZE = -1;  //infinite

	private int port = -1;
	private int handlerno;
	
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
	
	private int conno = -1;
	
	// midi message stuff
	private ShortMessage mmsg;
	private int mmsg_pos = 0;
	private int mmsg_data1;
	private int mmsg_status;
	private int last_mmsg_status;
	
	private boolean midi_seen = false;
	private boolean log_midi_bytes = false;
	
	public DWVSerialPort(int handlerno, int port)
	{
		logger.debug("New DWVSerialPort for port " + port + " in handler #" + handlerno);
		this.port = port;
		this.handlerno = handlerno;
		
		if (port != DWVSerialPorts.TERM_PORT)
		{
			this.porthandler = new DWVPortHandler(handlerno, port);
		}
		
		if (DriveWireServer.getHandler(handlerno).config.getBoolean("LogMIDIBytes", false))
		{
			this.log_midi_bytes = true;
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
		
		if (this.port == DWVSerialPorts.MIDI_PORT)
		{
			if (!midi_seen)
			{
				logger.debug("MIDI data on port " + this.port);
				midi_seen = true;
			}
			
			
			// incomplete, but enough to make most things work for now
			
			
			databyte = (int)(databyte & 0xFF);
			
			if (databyte == 248)
			{
				// timing tick
				try 
				{
					
					mmsg.setMessage(ShortMessage.TIMING_CLOCK);
					DriveWireServer.getHandler(handlerno).getVPorts().sendMIDIMsg(mmsg, -1);
				} 
				catch (InvalidMidiDataException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (log_midi_bytes)
				{
					logger.debug("midimsg: timing tick");
				}
			}
			else if (databyte > 127)
			{
				// status byte
				
				last_mmsg_status = mmsg_status;
				mmsg_status = databyte;
				// logger.debug("MIDI status byte: " + (databyte & 0xFF));

				mmsg_pos = 0;
				
			}
			else
			{
				if (mmsg_pos == 0)
				{
					//data1
					
					mmsg_data1 = databyte;
					mmsg_pos = 1;
				}
				else
				{
					//data2
					
					mmsg = new ShortMessage();
										
					try 
					{
						mmsg.setMessage(mmsg_status, mmsg_data1, databyte);
						DriveWireServer.getHandler(handlerno).getVPorts().sendMIDIMsg(mmsg, -1);
					} 
					catch (InvalidMidiDataException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (log_midi_bytes)
					{
						byte[] tmpb = {(byte) mmsg_status, (byte) mmsg_data1, (byte) databyte};
						logger.debug("midimsg: " + DWUtils.byteArrayToHexString( tmpb ));
					}
					
					mmsg_pos = 0;
				}
			}
			
		}
		else
		{	
			// if we are connected, pass the data
			if ((this.connected) || (this.port == DWVSerialPorts.TERM_PORT))
			{
				if (output == null)
				{
					// logger.debug("write to null stream on port " + this.port);
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
			logger.debug("close port " + this.port + ", total opens: " + this.opens + " data in buffer: " + this.inputBuffer.getAvailable());
			
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
			// this actually happens in normal operation, when both sides have code to
			// close port on exit.. probably not worth an error message
			
			// logger.error("close port " + this.port + " with no opens?");
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

	public void sendConnectionAnnouncement(int conno, int localport, String hostaddr)
	{
		this.porthandler.announceConnection(conno, localport, hostaddr);
		
	}

	public void setConn(int conno)
	{
		this.conno = conno;
	}
	
	public int getConn()
	{
		return(this.conno);
	}

	public void shutdown()
	{
		// close this port
		this.connected = false;
		this.opens = 0;
		this.output = null;
		this.porthandler = null;
		this.wanttodie = true;
		
		
	}
	
	
	
}

