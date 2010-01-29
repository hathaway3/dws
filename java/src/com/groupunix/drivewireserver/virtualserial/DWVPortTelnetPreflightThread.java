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
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.regionName;

public class DWVPortTelnetPreflightThread implements Runnable
{

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortTelnetPreflightThread");
	
	
	private Socket skt;
	private int vport;

	// telnet login prompt timeout, 50ms * X
	private static int MAX_TIMEOUT = 900;
	
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

			// GeoIP
			
			if (DriveWireServer.config.getBoolean("GeoIPLookup",false))
			{
				if (geoIPBanned(skt.getInetAddress().getHostAddress()) == true)
				{
					doBanned();
				}
			}
			
			if (skt.isClosed())
			{
				// bail out
				logger.debug("thread exiting after geoip ban check");
				return;
			}
			
			
			// check banned
			if ((DriveWireServer.config.containsKey("TelnetBanned")) && (this.protect == true))
			{
				String[] thebanned = DriveWireServer.config.getStringArray("TelnetBanned");
			
				for (int i = 0;i<thebanned.length ;i++)
				{
					if (this.skt.getInetAddress().getHostAddress().startsWith(thebanned[i]))
					{
						logger.info("Connection from banned IP " + thebanned[i]);
					
						doBanned();

					}
				}
			
			}
		
		
			if (skt.isClosed())
			{
				// bail out
				logger.debug("thread exiting after IP ban check");
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
								logger.info("AUTH: login from " + username + " (group " + DWVSerialPorts.getUserGroup(this.vport) + ")");
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
			
			
			if ((DriveWireServer.config.containsKey("TelnetBannerFile")) && (banner == true))
			{
				displayFile(skt.getOutputStream(), DriveWireServer.config.getString("TelnetBannerFile"));
			}
			
		} 
		catch (IOException e)
		{
			logger.warn("IOException: " + e.getMessage());
			
			if (skt.isConnected())
			{
				logger.debug("closing socket");
				try
				{
					skt.close();
				} catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}
			
		}
			
		
		if (skt.isClosed() == false)
		{
			
			logger.debug("Preflight success for " + skt.getInetAddress().getHostName());
			
			//add connection to pool
			int conno = DWVPortListenerPool.addConn(this.vport, skt, 1);

			
			// announce new connection to listener
			DWVSerialPorts.sendConnectionAnnouncement(this.vport, conno, skt.getLocalPort(), skt.getInetAddress().getHostAddress());
					
		}
		
		logger.debug("exiting");
	}

	

	
	

	private void doBanned()
	{
		// IP is banned
		
		try
		{
				
			if (DriveWireServer.config.containsKey("TelnetBannedFile"))
			{
				displayFile(skt.getOutputStream(), DriveWireServer.config.getString("TelnetBannedFile"));
			}
			else
			{
				skt.getOutputStream().write("No ports available.\r\n".getBytes());
			}
	
		}
		catch (IOException e1)
		{
			logger.warn("IOException: " + e1.getMessage());
		}
		
			
		if (skt.isConnected())
		{
			logger.debug("closing socket");
			try
			{
				skt.close();
			} catch (IOException e)
			{
				logger.warn("IOException closing socket: " + e.getMessage());
			}
			
		}	
	}

	
	
	private boolean geoIPBanned(String hostAddress)
	{
		Location loc = lookupGeoIP(hostAddress);

		
		if (loc != null)
		{
			// log details
			logger.info("GeoIP: cc='" + loc.countryCode + "' country='" + loc.countryName + "' region='" + regionName.regionNameByCode(loc.countryCode, loc.region) + "' city='" + loc.city + "'  lat: " + loc.latitude + " lng: " + loc.longitude );
			
			// do country, regionName, city
			if (DriveWireServer.config.containsKey("GeoIPBannedCountries"))
			{
				String[] thebanned = DriveWireServer.config.getStringArray("GeoIPBannedCountries");
				
				for (int i = 0;i<thebanned.length ;i++)
				{
					try 
					{
						
					
						if ((loc.countryName.equalsIgnoreCase(thebanned[i])) || (loc.countryCode.equalsIgnoreCase(thebanned[i]))) 
						{
							logger.info("Connection from banned country: " + thebanned[i]);
							return true;
						}
					}
					catch (NullPointerException e) 
					{
						// don't care
					}
				}	
			}
			
			if (DriveWireServer.config.containsKey("GeoIPBannedRegions"))
			{
				String[] thebanned = DriveWireServer.config.getStringArray("GeoIPBannedRegions");
				
				for (int i = 0;i<thebanned.length ;i++)
				{
					try
					{
						if ((loc.region.equalsIgnoreCase(thebanned[i])) || (regionName.regionNameByCode(loc.countryCode, loc.region).equalsIgnoreCase(thebanned[i]))) 
						{
							logger.info("Connection from banned region: " + thebanned[i]);
							return true;
						}
					}
					catch (NullPointerException e) 
					{
						// don't care
					}
				}	
			}
			
			if (DriveWireServer.config.containsKey("GeoIPBannedCities"))
			{
				String[] thebanned = DriveWireServer.config.getStringArray("GeoIPBannedCities");
				
				for (int i = 0;i<thebanned.length ;i++)
				{
					try
					{
						if (loc.city.equalsIgnoreCase(thebanned[i]))
						{
							logger.info("Connection from banned city: " + thebanned[i]);
							return true;
						}
					}
					catch (NullPointerException e) 
					{
						// don't care
					}
				}	
			}
			
		}
		
		return false;
	}

	
	private boolean checkAuth(String username, String password) 
	{
		boolean result = false;
		
		if (DriveWireServer.config.containsKey("TelnetPasswdFile"))
		{
			// look for username in passwd file
			FileInputStream fstream;
			
			BasicPasswordEncryptor bpe = new BasicPasswordEncryptor();
			
			try 
			{
				fstream = new FileInputStream(DriveWireServer.config.getString("TelnetPasswdFile"));
			
				DataInputStream in = new DataInputStream(fstream);
					
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
					
				String strLine;
				String founduser = null;
				String foundpass = null;
				int foundgroup = -1;
				
				
				while (((strLine = br.readLine()) != null) && (founduser == null))
				{
					if (!strLine.startsWith("#"))
					{
						String[] parts = new String[3];
				    	parts = strLine.split(",", 3);

				    	if (parts.length == 3)
				    	{
				    		if (parts[0].equals(username))
				    		{
				    			founduser = parts[0];
				    			foundpass = parts[1];
				    			foundgroup = Integer.parseInt(parts[2]);
				    			 
				    			logger.info("AUTH: login from '" + founduser + "' group " + foundgroup);
				    		}
				    	}
					}
				}
				
				fstream.close();
				
				if (founduser == null)
				{
					logger.debug("AUTH: User not found: '" + username + "'");
				}
				else
				{
					// found user
					
			    	if (bpe.checkPassword(password, foundpass))
			    	{
			    		// match
			    		result = true;
			    		DWVSerialPorts.setUserName(this.vport, founduser);
			    		DWVSerialPorts.setUserGroup(this.vport, foundgroup);
			    		
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
	
	
	private Location lookupGeoIP(String ip)
	{
		try {
		    LookupService cl = new LookupService(DriveWireServer.config.getString("GeoIPDatabaseFile"), LookupService.GEOIP_MEMORY_CACHE );
	      
		    
		    Location l2 = cl.getLocation(ip);
		    
	          
		    if (l2 == null)
		    {
		    	logger.debug("no results from GeoIP lookup");
		    }
		  
		    cl.close();
		    
		    return(l2);
		}
		catch (IOException e) {
		    System.out.println("IO Exception");
		}
		
		return(null);
	}
	
	
}
