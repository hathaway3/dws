package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class DWUtilWeatherThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilWeatherThread");
	
	private int vport = -1;
	private String strurl = null;
	@SuppressWarnings("unused")
	private static final int CHUNK_SIZE = 128;
	
	public DWUtilWeatherThread(int vport, String url)
	{
		this.vport = vport;
		this.strurl = url;
		logger.debug("init wget thread");	
	}
	
	public void run() 
	{
		URL url = null;
	
		String report = new String();
		
		Thread.currentThread().setName("weather-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		if (strurl.length() == 0)
		{
			DWVSerialPorts.writeSections(this.vport,"Usage:  weather [zipcode or City,ST]\r\n");
		}
		else
		{
			
			// replace spaces in arg with %20
			Pattern p = Pattern.compile("\\s");
			Matcher m = p.matcher(strurl);
			strurl = m.replaceAll("%20");
				
			// try to get URL
			try 
			{ 
				url = new URL("http://www.srh.noaa.gov/port/port_zc.php?inputstring=" + this.strurl);
			} 
			catch (MalformedURLException e) 
			{
				// sorry
				DWVSerialPorts.writeSections(this.vport,e.getMessage());
			}
		
			if (url != null)
			{
				String content = "Uknown error fetching weather data";
			
				URLConnection urlConnection;
				try 
				{
					urlConnection = url.openConnection();
			
					urlConnection.setAllowUserInteraction(false);

					InputStream urlStream = url.openStream();
				
					// first, read in the entire URL
					byte b[] = new byte[1000];
					int numRead = urlStream.read(b);
					content = new String(b, 0, numRead);
					while (numRead != -1) 
					{
						numRead = urlStream.read(b);
						if (numRead != -1) 
						{
						String newContent = new String(b, 0, numRead);
						content += newContent;
						}
					}
				
					urlStream.close();
				
					// now strip out the weather info
					
					
					
					p = Pattern.compile(".*\\<hr\\>");
					
					m = p.matcher(content);
					
					if (m.find())
					{
						content = content.substring(0, m.end(0));
						
						// remove any remaining tags
						p = Pattern.compile("\\<.+\\>");
						m = p.matcher(content);
						content = m.replaceAll("");
						
						// turn &deg; into nothing
						p = Pattern.compile("&deg;");
						m = p.matcher(content);
						content = m.replaceAll("");
						
						report = content;
					}
					else
					{
						report = "Sorry, I was not able to parse the weather data.\r\n";
					}
					
					
					
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					DWVSerialPorts.writeSections(this.vport,e.getMessage());
					e.printStackTrace();
				}
			
				DWVSerialPorts.writeSections(this.vport,report);
			
			}
		}
		
	}

	
}
