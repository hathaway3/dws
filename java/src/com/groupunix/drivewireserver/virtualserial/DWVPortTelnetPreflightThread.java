package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWVPortTelnetPreflightThread implements Runnable
{

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTelnetPreflightThread");
	
	
	private Socket skt;
	private int vport;

	// telnet login prompt timeout, 50ms * X
	private static int MAX_TIMEOUT = 900;
	
	private int usergroup = -1;
	private String username = "unknown";
	private boolean loginOK = true;
	private boolean auth = false;
	private boolean protect = false;
	private boolean banner = false;
	private boolean telnet = false;
	
	
	public DWVPortTelnetPreflightThread(int vport, Socket skt, boolean doTelnet, boolean doAuth, boolean doProtect, boolean doBanner)
	{
		this.vport = vport;
		this.skt = skt;
		this.auth = doAuth;
		this.protect = doProtect;
		this.banner = doBanner;
		this.telnet = doTelnet;
	}

	public void run()
	{
		logger.info("preflight checks for new connection from " + skt.getInetAddress().getHostName());
		
		try
		{
			// hello
			skt.getOutputStream().write(("DriveWire Telnet Server " + DriveWireServer.DWServerVersion + "\r\n\n").getBytes());

			// check banned
			if ((DriveWireServer.config.containsKey("TelnetBanned")) && (this.protect == true))
			{
				String[] thebanned = DriveWireServer.config.getStringArray("TelnetBanned");
			
				for (int i = 0;i<thebanned.length ;i++)
				{
					if (this.skt.getInetAddress().getHostAddress().startsWith(thebanned[i]))
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
		
		
			if (skt.isClosed())
			{
				// bail out
				logger.debug("thread exiting after ban check");
				return;
			}
		

			if (telnet == true)
			{
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
		
		
				skt.getOutputStream().write(buf, 0, 9);
		
				// 	read back the echoed controls - TODO has issues
		
				for (int i = 0; i<9; i++)
				{
					skt.getInputStream().read();
				}
			}
				
			// do auth
			if (auth == true)
			{
				this.loginOK = false;
				
				// display preauth
				if (DriveWireServer.config.containsKey("TelnetPreAuthFile"))
				{
					displayFile(skt.getOutputStream(), DriveWireServer.config.getString("TelnetPreAuthFile"));
				}
			
				// username
				if (skt.isClosed() == false)
				{
					skt.getOutputStream().write("Username: ".getBytes());
					String username = readLine(true);
					
					if ((skt.isClosed() == false) && (username.length() > 0))
					{
						skt.getOutputStream().write("\r\nPassword: ".getBytes());
						String password = readLine(false);
						
						if ((skt.isClosed() == false) && (password.length() > 0))
						{
							if (checkAuth(username,password))
							{
								this.loginOK = true;
								logger.info("AUTH: login from " + username);
								skt.getOutputStream().write(("\r\n\nAuthorized for group " + this.usergroup + "\r\n\n").getBytes());
							}
							else
							{
								logger.warn("AUTH: bad login from " + username);
								skt.getOutputStream().write("\r\nBad login.\r\n".getBytes());
							}
						}
					}
					
				}
			}
			
			if (!this.loginOK)
			{
				skt.close();
			}
			
			if (skt.isClosed())
			{
				// bail out
				logger.debug("thread exiting after auth");
				return;
			}
			
			if (auth == true)
			{
				DWVSerialPorts.setUserName(this.vport,this.username);
				DWVSerialPorts.setUserGroup(this.vport, this.usergroup);
			}
			
			if ((DriveWireServer.config.containsKey("TelnetBannerFile")) && (banner == true))
			{
				displayFile(skt.getOutputStream(), DriveWireServer.config.getString("TelnetBannerFile"));
			}
			
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		if (skt.isClosed() == false)
		{
			
			logger.debug("Preflight success for " + skt.getInetAddress().getHostName());
			
			//add connection to pool
			int conno = DWVPortListenerPool.addConn(skt, 1);

			logger.debug("announcing connection #" + conno);
			// announce new connection to listener
			DWVSerialPorts.writeToCoco(this.vport, conno + " " + skt.getLocalPort() + " " +  skt.getInetAddress().getHostAddress() + (char) 13);		
		}
		
		logger.debug("exiting");
	}

	

	
	private boolean checkAuth(String username, String password) 
	{
		boolean result = false;
		
		if (DriveWireServer.config.containsKey("TelnetPasswdFile"))
		{
			// look for username in passwd file
			FileInputStream fstream;
			
			try 
			{
				fstream = new FileInputStream(DriveWireServer.config.getString("TelnetPasswdFile"));
			
				DataInputStream in = new DataInputStream(fstream);
					
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
					
				String strLine;
				String userData = null;   
				
				while (((strLine = br.readLine()) != null) && (userData == null))
				{
					if (strLine.startsWith(username))
					{
						userData = strLine;
					}
				}
				
				fstream.close();
				
				if (userData == null)
				{
					logger.debug("AUTH: User not found: '" + username + "'");
				}
				else
				{
					String[] parts = new String[3];
			    	
			    	parts = userData.split(",", 3);
			    	
			    	BasicPasswordEncryptor bpe = new BasicPasswordEncryptor();
			    	
			    	// logger.debug(bpe.encryptPassword("test"));
			    	
			    	if (bpe.checkPassword(password, parts[1]))
			    	{
			    		// match
			    		result = true;
			    		this.username = username;
			    		this.usergroup = Integer.parseInt(parts[2]);
			    		
			    	}
			    	else
			    	{
			    		logger.debug("AUTH: bad password from '" + username + "'");
			    	}
				}
				
				
			} 
			catch (FileNotFoundException e) 
			{
				logger.warn("TelnetPasswdFile not found");
			} 
			catch (IOException e1) 
			{
				logger.warn("IO Error reading passwd file: " + e1.getMessage());
			}
			catch (NumberFormatException e2) 
			{
				logger.error(e2.getMessage());
			} 
			
			
		}
		
		return(result);
	}

	private String readLine(boolean echo) 
	{
		String input = "";
		
		int lastchar = 0;
		
		try 
		{
		
			while ((lastchar != 13) && (skt.isClosed() == false))
			{
				int timeout = 0;
				
				while ((skt.getInputStream().available() == 0) && (timeout < MAX_TIMEOUT))
				{
					Thread.sleep(50);
					timeout++;
				}
				
				if (timeout >= MAX_TIMEOUT)
				{
					logger.info("AUTH: timed out");
					skt.close();
				}
				
				lastchar = skt.getInputStream().read();
				
				if (echo)
				{
					skt.getOutputStream().write(lastchar);
				}
				else
				{
					if ((lastchar > 31) && (lastchar < 127))
					{
						skt.getOutputStream().write(42);
					}
				}
				
				if ((lastchar > 31) && (lastchar < 127))
				{
					input += Character.toString((char) lastchar);
				}
				else if (((lastchar == 8) || (lastchar == 127)) && (input.length() > 0))
				{
					input = input.substring(0, input.length() - 1);
				}
			}
	
		} 
		catch (IOException e) 
		{
			logger.debug("IO error in readline: " + e.getMessage());
			input = "";
		}
		catch (InterruptedException e1) 
		{
			logger.debug("Interrupted in readline sleep");
			input = "";
		}
		
		// logger.debug("Readline: '" + input + "'");
			
		return(input);
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
			
			fstream.close();
			
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
