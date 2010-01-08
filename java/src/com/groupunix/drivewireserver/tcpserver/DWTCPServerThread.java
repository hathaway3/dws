package com.groupunix.drivewireserver.tcpserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.python.util.PythonInterpreter;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;



public class DWTCPServerThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWTCPServerThread");
	
	private Socket skt; 
	private String clientIP = "none";
	private int clientPort = -1;
	private int vport = -1;
	
	public DWTCPServerThread(Socket skt) 
	{
		this.skt = skt;
		this.clientIP = skt.getInetAddress().getHostName();
		this.clientPort = skt.getPort();
	
	}

	public void run() 
	{
		Thread.currentThread().setName("tcpconn-" + Thread.currentThread().getId());
		logger.debug("thread run for " + this.clientIP + ":" + clientPort);
		
		try 
		{
			skt.getOutputStream().write(("DriveWire TCP Server " + DriveWireServer.DWServerVersion + "\r\n\n").getBytes());

			// check banned
			if (DriveWireServer.config.containsKey("TelnetBanned"))
			{
				String[] thebanned = DriveWireServer.config.getStringArray("TelnetBanned");
				
				for (int i = 0;i<thebanned.length ;i++)
				{
					if (this.skt.getInetAddress().getHostAddress().equals(thebanned[i]))
					{
						logger.info("Connection from banned IP " + thebanned[i]);
						
						// IP is banned
						if (DriveWireServer.config.containsKey("TelnetBannedFile"))
						{
							displayFile(skt.getOutputStream(), DriveWireServer.config.getString("TelnetBannedFile"));
						}
						else
						{
							skt.getOutputStream().write("No ports available.\r\n".getBytes());
						}
						
						if (skt.isConnected())
						{
							logger.debug("closing socket");
							skt.close();
						}
						
					}
				}
				
			}
			
			if (skt.isClosed() == false)
			{
				// look for available port
				vport = DWVSerialPorts.nextFreePort(DWVSerialPorts.MODE_TELNET);
			
				if (vport > -1)
				{
					logger.info("connected new client from " + this.clientIP + ":" + this.clientPort + " to virtual port T" + vport);
					// annouce
					skt.getOutputStream().write(("Connected to port T" + vport + "\r\n\n").getBytes());
				
					if (DriveWireServer.config.containsKey("TelnetBannerFile"))
					{
						displayFile(skt.getOutputStream(), DriveWireServer.config.getString("TelnetBannerFile"));
					}
			
					// connect socket to port
					// chanconnector.connect(skt.getInputStream(), skt.getOutputStream(), DWChannelConnector.CHANMODE_TCPIN,  DWVSerialPorts.getPortOutput(vport), DWVSerialPorts.getPortInput(vport), DWChannelConnector.CHANMODE_TCPOUT);
				
					DWVSerialPorts.markConnected(vport);
					DWVSerialPorts.setPortOutput(vport, skt.getOutputStream());
				
					DWVSerialPorts.setHostIP(vport, this.clientIP);
					DWVSerialPorts.setHostPort(vport, this.clientPort);
				
					// session automation stuff
				
					if (DWVSerialPorts.getActionFile(vport) != null)
					{
						logger.debug("processing action file for port " + vport + ", '" + DWVSerialPorts.getActionFile(vport) +"'" );
						processActionFile(DWVSerialPorts.getActionFile(vport), skt, vport);
					}
				
					// ask telnet to turn off echo, should probably be a setting or left to the client, or done in automation script
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
				
					skt.getOutputStream().write(buf, 0, 9);
				
					// read back the echoed controls.. sometimes doesn't work out so well. really need a better telnet implementation!
				
					for (int i = 0; i<9; i++)
					{
						skt.getInputStream().read();
					}
				
				
					int lastbyte = 0;
				
					while (skt.isConnected())
					{
						// process input from the tcp socket right here, better than adding yet another thread
						int data = skt.getInputStream().read();
						if (data >= 0)
						{
							// filter CR/LF.. should really do this in the client or at least make it a setting
							if (!((lastbyte == 13) && ((data == 10) || (data == 0))))
							{
								// write it to the serial port
								DWVSerialPorts.getPortInput(vport).write((byte) data);
								lastbyte = data;
							}
						
						}
						else
						{
							logger.info("end of stream from TCP client at " + this.clientIP + ":" + this.clientPort);
							if (skt.isConnected())
							{
								logger.debug("closing socket");
								skt.close();
							}
						
						}
					
					}
				
				}
				else
				{
					// no ports..
					if (DriveWireServer.config.containsKey("TelnetNoPortsBannerFile"))
					{
						displayFile(skt.getOutputStream(), DriveWireServer.config.getString("TelnetNoPortsBannerFile"));
					}
					else
					{
						skt.getOutputStream().write("No ports available.\r\n".getBytes());
					}
				
					logger.debug("no ports available for new client at " + this.clientIP + ":" + this.clientPort);
				
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
			logger.warn("IO error in connection to " + this.clientIP + ":" + this.clientPort + " = " + e.getMessage());
		} 
		finally
		{
			if (this.vport > -1)
			DWVSerialPorts.markDisconnected(this.vport);
			logger.debug("thread exiting");
		}
	}

	private void processActionFile(String actionFile, Socket skt, int vport) 
	{
		// process actions in action file
		
		FileInputStream fstream;
		
			
		
		
		try 
		{
			fstream = new FileInputStream(actionFile);
		
			// OutputStream telnetOut = skt.getOutputStream();
		  			
			DataInputStream in = new DataInputStream(fstream);
				
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
			// only in jre 1.6 :(
			// ScriptEngineManager mgr = new ScriptEngineManager();
			// ScriptEngine eng = mgr.getEngineByName("python");
		       
			PythonInterpreter eng = new PythonInterpreter();

			// for 1.5?
			eng.set("port", new Integer(this.vport));
			eng.set("socket", skt);
			
			// for 1.6
			// eng.put("port", new Integer(this.vport));
			
			// PyObject localvars = interp.getLocals();
			// interp.set("localvars", localvars);
		    			
			String strLine;
			   
			while ((strLine = br.readLine()) != null)
			{
				// eval python (hopefully) in file
				
				// for 1.6, eval
				eng.exec(strLine);
			
			}
			
						
		} 
		catch (FileNotFoundException e) 
		{
			logger.warn("File not found: " + actionFile);
		} 
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		// for 1.6 style
		/* catch (ScriptException e2) 
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		*/
		
	}


	private void displayFile(OutputStream outputStream, String fname) 
	{
		FileInputStream fstream;
		
		try 
		{
			fstream = new FileInputStream(fname);
		
			DataInputStream in = new DataInputStream(fstream);
				
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
			String strLine;
			   
			logger.debug("sending file '" + fname + "' to telnet client");
			
			while ((strLine = br.readLine()) != null)
			{
				  outputStream.write(strLine.getBytes());
				  outputStream.write("\r\n".getBytes());
			}
		} 
		catch (FileNotFoundException e) 
		{
			logger.warn("File not found: " + fname);
		} 
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		 
	}

	

	
	
}
