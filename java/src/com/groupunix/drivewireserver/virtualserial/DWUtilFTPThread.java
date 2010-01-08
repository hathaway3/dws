package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class DWUtilFTPThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilFTPThread");
	
	private int vport;
	private String host;
	private static final int CHUNK_SIZE = 128;
	private static final byte MODE_INPUT = (byte) 240;
	private static final byte MODE_WRITEFILE = (byte) 241;
	private static final byte MODE_FILECLOSE = (byte) 242;
	private static final byte MODE_STDOUT = (byte) 243;
	private static final byte MODE_FILENAME = (byte) 244;
	private static final byte MODE_CREATEFILE = (byte) 245;
	
	public DWUtilFTPThread(int port, String arg)
	{
		this.vport = port;
		this.host = arg;
	}
	
//	@Override
	public void run() 
	{
		@SuppressWarnings("unused")
		String input;
		
		logger.debug("run");
		
		// connect to host
		FTPClient ftp = new FTPClient();
	    try 
	    {
			ftp.connect( this.host);

	    
			logger.debug("connected to " + this.host);
	    	
	    	writeSections("Connected to " + this.host + "\r\n");
	    	
	    	writeSections(ftp.getReplyString());
	    	
	    	int reply = ftp.getReplyCode();

	        if(!FTPReply.isPositiveCompletion(reply)) 
	        {
	        	// negative response to connect
	        	writeSections("FTP server refused connection.");
	        }
	        else
	        {
	        	// lets log in
	        	writeSections("Name: ");
	    	
	        	String tmpstr = readInput();
	    	
	        	ftp.user(tmpstr);
	    	
	        	writeSections(ftp.getReplyString());
	    	
	          	writeSections("Password: ");
	    	
		      	tmpstr = readInput();
	    	
		      	ftp.pass(tmpstr);
	    	
		       	writeSections(ftp.getReplyString());
		        	
		       	reply = ftp.getReplyCode();

			    if(FTPReply.isPositiveCompletion(reply)) 
			    {
			       	// finally we are logged in..
			        
			    	boolean loggedin = true;
			    	
			    	while (ftp.isConnected() && loggedin)
			    	{
			    		writeSections("ftp> ");
			        	
			    		tmpstr = readInput();
			        	
			    		// very, very simple just to play for now
			    		if (tmpstr.equals("help"))
			    		{
			    			writeSections("Supported commands (not much, I know):\r\n\n");
			    			writeSections("help    dir     cd      pwd     quit\r\n");
			    			writeSections("bin     ascii   get     put     stat\r\n\n");
			    		}
			    		else if (tmpstr.startsWith("cd "))
			    		{
			    			if (tmpstr.length() > 3)
			    			{
			    				reply = ftp.cwd(tmpstr.substring(3));
			    				writeSections(ftp.getReplyString());
			    			}
			    		} 
			    		else if (tmpstr.equals("quit"))
			    		{
			    			ftp.logout();
			    			writeSections(ftp.getReplyString());
			    			loggedin = false;
			    		} 
			    		else if (tmpstr.equals("bin"))
			    		{
			    			reply = ftp.type(FTP.BINARY_FILE_TYPE);
			    			writeSections(ftp.getReplyString());
			    			
			    		}
			    		else if (tmpstr.equals("ascii"))
			    		{
			    			reply = ftp.type(FTP.ASCII_FILE_TYPE);
			    			writeSections(ftp.getReplyString());
			    		}
			    		else if (tmpstr.equals("stat"))
			    		{
			    			reply = ftp.stat();
			    			writeSections(ftp.getReplyString());
			    		}
			    		else if (tmpstr.equals("noop"))
			    		{
			    			reply = ftp.noop();
			    			writeSections(ftp.getReplyString());
			    		}
			    		else if (tmpstr.equals("pwd"))
			    		{
			    			ftp.sendCommand("PWD");
			    			writeSections(ftp.getReplyString());
			    		}
			    		else if (tmpstr.startsWith("dir"))
			    		{
			    			FTPFile[] files;
			    			
			    			if (tmpstr.length() > 4)
			    			{
			    				files = ftp.listFiles(tmpstr.substring(4));
			    			}
			    			else
			    			{
			    				files = ftp.listFiles();
			    			}
			    			
			    			writeSections(ftp.getReplyString());
			    			
			    			// file listing
			    			for (int i = 0;i<files.length;i++)
			    			{
			    				writeSections(files[i].getRawListing() + "\r\n");
			    				// writeSections(files[i].getName() + "\r\n");
			    			}
			    			
			    		}
			    		else if (tmpstr.startsWith("get "))
			    		{
			    			if (tmpstr.length() > 4)
			    			{
			    				InputStream filein = ftp.retrieveFileStream(tmpstr.substring(4));
			    				
			    				writeSections(ftp.getReplyString());
			    				
			    				//if (FTPReply.isPositiveIntermediate(ftp.getReplyCode()))
			    				if (filein != null)
			    				{
			    					byte[] buf = new byte[128];
			    					int bytesread = filein.read(buf);
			    					
			    					// send filename command
		    						write1(MODE_FILENAME);
		    						
		    						// send file name length
		    						write1((byte) ((tmpstr.substring(4).length())+1));
		    						
		    						// send file name
		    						write(tmpstr.substring(4));
		    						write1((byte) 13);
		    						
		    						// send create file command
		    						write1(MODE_CREATEFILE);
		    						
		    						// send file mode
		    						write1(MODE_WRITEFILE);
		    								
			    					// send the file
		    						
		    						
			    					while (bytesread > 0)
			    					{
			    						
			    						
			    						//send file
			    						
			    						// cr/lf filter for ascii mode?
			    						/*
			    						if (ftp.getDataConnectionMode() == FTP.ASCII_FILE_TYPE)
			    						 
			    						{
			    							String text = new String();
			    							// ascii mode
			    							for (int i = 0;i<bytesread;i++)
			    							{
			    								if (buf[i] == 10)
			    								{
			    									text += Character.toString((char) 13);
			    									text += Character.toString((char) 10);
			    								}
			    								else
			    								{
			    									text += Character.toString((char) buf[i]);
			    								}
			    							}
			    							
			    							writeSections(text);
			    						}
			    						else
			    						{  
			    						*/
			    							// binary mode
			    							writeSections(new String(buf).substring(0, bytesread));
			    						// }
			    						bytesread = filein.read(buf);
			    					}
			    				
			    					// send file close
			    					write1(MODE_FILECLOSE);
			    					// send write to stdout
			    					write1(MODE_STDOUT);
			    								    					
			    					
			    					if(!ftp.completePendingCommand()) 
			    					{
			    						// transfer failed
			    					}
			    					
			    					writeSections(ftp.getReplyString());
			    					
			    				}
			    				else
			    				{
			    					filein.close();
			    				}
			    				
			    			}
			    			
			    		}
			    		else
				    	{
				    		writeSections("Sorry, either that is not a command or I haven't implemented it yet.\r\n");
				    	}
			    					        	
			    		
			    	}
			    	
			        	
			        	
			    }
	        }
	    	
	    	ftp.disconnect();
	    }
	    catch (SocketException e) 
	    {
	    	writeSections("Socket error: " + e.getMessage());
	    } 
	    catch (IOException e1) 
	    {
	    	writeSections("IO error: " + e1.getMessage());
	    }
	    finally 
	    {
	        if(ftp.isConnected()) 
	        {
	          try 
	          {
	        	  ftp.disconnect();
	          } 
	          catch(IOException ioe) 
	          {
	            // do nothing
	          }
	        }
	    }
	    
		write1((byte) 0);
		logger.debug("exit");
	}

	
	private String readInput()
	{
		String input = new String();
		
		// send input command
		write1(MODE_INPUT);
		
		synchronized(DWUtilHandler.ftpSync)
		{
			DWUtilHandler.ftpInput = null;
		
			while (DWUtilHandler.ftpInput == null)
			{
				try 
				{
					DWUtilHandler.ftpSync.wait();
				}
				catch (InterruptedException ex) 
				{
					System.err.println( ex );
				}

			}
			input = DWUtilHandler.ftpInput;
			logger.debug("got input: " + input);
		}
		
		return(input);
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
		// write1((byte) 0);
		
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
