package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

public class DWUtilWgetThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilWgetThread");
	
	private int vport = -1;
	private String strurl = null;
	private static final int CHUNK_SIZE = 128;
	
	public DWUtilWgetThread(int vport, String url)
	{
		this.vport = vport;
		this.strurl = url;
		logger.debug("init wget thread");	
	}
	
	public void run() 
	{
		URL url = null;
		
		Thread.currentThread().setName("wget-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		// try to get URL
		try { 
		    url = new URL(this.strurl);
		} catch (MalformedURLException e) {
		    // sorry
			writeSections(e.getMessage());
		}
		
		if (url != null)
		{
			String content = "Uknown error fetching url";
			
			URLConnection urlConnection;
			try {
				urlConnection = url.openConnection();
			
			urlConnection.setAllowUserInteraction(false);

			InputStream urlStream = url.openStream();
			// String type = URLConnection.guessContentTypeFromStream(urlStream);
			// if (type != null)
			// {
		
				// first, read in the entire URL
				byte b[] = new byte[1000];
				int numRead = urlStream.read(b);
				content = new String(b, 0, numRead);
				while (numRead != -1) {
					numRead = urlStream.read(b);
					if (numRead != -1) {
						String newContent = new String(b, 0, numRead);
						content += newContent;
					}
				}
			// }
			urlStream.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			writeSections(content);
			
		}
		
	}

	
	private void writeSections(String data) 
	{
		String dataleft = data;
		
		// send data in (numbytes)(bytes) format
		
		while (dataleft.length() > CHUNK_SIZE)
		{
			// send a 255 char chunk
			
			write1((byte) CHUNK_SIZE);
			write(dataleft.substring(0, CHUNK_SIZE));
			dataleft = dataleft.substring(CHUNK_SIZE);
			logger.debug("sent chunk, left: " + dataleft.length());
		/*	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
		// send last chunk
		int bytes = dataleft.length();
		
		write1((byte) bytes);
		
		write(dataleft);
		
		// send termination
		write1((byte) 0);
		
	}

	
	private void write1(byte data)
	{
		try 
		{
			DWVSerialPorts.getPortInput(this.vport).write(data);
		} 
		catch (IOException e) 
		{
			logger.error("IO error writing to port T" + this.vport);
		}
	}

	private void write(String str)
	{
		
		try 
		{
			DWVSerialPorts.getPortInput(this.vport).write(str.getBytes());
		} 
		catch (IOException e) 
		{
			logger.error("IO error writing to port T" + this.vport);
		}
	}
	
}
