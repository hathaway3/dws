package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWRFMPath
{
	private static final Logger logger = Logger.getLogger("DWServer.DWRFMPath");
		
	private int pathno;
	private String pathstr;
	private String localroot;
	private int seekpos;
		
	
	public DWRFMPath(int pathno)
	{
		this.setPathno(pathno);
		this.setSeekpos(0);
		logger.debug("new path " + pathno);
		this.setLocalroot(DriveWireServer.config.getString("RFMRoot","/"));
		
	}

	public void setPathno(int pathno)
	{
		this.pathno = pathno;
	}

	public int getPathno()
	{
		return pathno;
	}

	public void setPathstr(String pathstr)
	{
		this.pathstr = pathstr;
	}

	public String getPathstr()
	{
		return pathstr;
	}

	public void close()
	{
		logger.debug("closing path " + this.pathno + " to " + this.pathstr);
	}

	public void setSeekpos(int seekpos)
	{
		this.seekpos = seekpos;
		logger.debug("seek to " + seekpos + " on path " + this.pathno);
	}

	public int getSeekpos()
	{
		return seekpos;
	}

	public int openFile()
	{
		// attempt to open local file
		 File f = new File(this.localroot + this.pathstr);
		 if (f.exists())
		 {
			 return(0);
		 }
		 else
		 {
			 return(216);
		 }
	}

	public void setLocalroot(String localroot)
	{
		this.localroot = localroot;
	}

	public String getLocalroot()
	{
		return localroot;
	}

	public int createFile()
	{
		// attempt to open local file
		 File f = new File(this.localroot + this.pathstr);
		 if (f.exists())
		 {
			 // file already exists
			 return(218);
		 }
		 else
		 {
			 try
			{
				if (f.createNewFile())
				{
					return(0);
				}
				else
				{
					// write error?
					return(245);
				}
			} 
			catch (IOException e)
			{
				logger.error("IOException creating file: " + e.getMessage());
				// write error
				return(245);
				
			}
		 }
	}

	public int getBytesAvail(int maxbytes)
	{
		// return # bytes left in file from current seek pos, up to maxbytes

		File f = new File(this.localroot + this.pathstr);
		if (f.exists())
		{
			// we only handle int sized files..
			int tmpsize = (int)f.length() - this.seekpos;
			
			if (tmpsize > maxbytes)
			{
				return(maxbytes);
			}
			return(tmpsize);
			
		}
		else
		{
			 //TODO wrong!
			 return(0);
		}

	}

	public byte[] getBytes(int availbytes)
	{
		// TODO very crappy !
		// return byte array of next availbytes bytes from file, move seekpos
		// TODO structure blindly assumes this will work.
		// like above need to implement exceptions/error handling passed up to caller
		
		
		byte[] buf = new byte[availbytes];
		RandomAccessFile inFile = null;
		
		File f = new File(this.localroot + this.pathstr);
		if (f.exists())
		{
			if (f.isDirectory())
			{
				// total hack
				logger.debug("DIR: asked for "+ availbytes);
				buf = "01234567890123456789012345670000".getBytes();
			}
			else
			{
				logger.debug("FILE: asked for "+ availbytes);
				
				try
				{
					inFile = new RandomAccessFile(f, "r");
				
					inFile.seek(seekpos);
				
					//TODO what if we don't get buf.length??
					//this.seekpos += 
					inFile.read(buf);
				
				
				
				} 
				catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally
				{
					try
					{
						inFile.close();
					} 
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}	
			
			}
		}
		return(buf);
	}

	public void incSeekpos(int bytes)
	{
		this.seekpos += bytes;
		logger.debug("incSeekpos to " + this.seekpos);
	}

	public void setFd(byte[] buf)
	{
		DWRFMFD fd = new DWRFMFD(DriveWireServer.config.getString("RFMRoot","/") + this.pathstr);
		
		fd.readFD();
		
		byte[] fdtmp = fd.getFD();
		
		System.arraycopy(buf, 0, fdtmp, 0, buf.length);
		
		fd.setFD(fdtmp);
		
		fd.writeFD();
				
	}

	public byte[] getFd(int size)
	{
		byte[] b = new byte[size];
		
		DWRFMFD fd = new DWRFMFD(DriveWireServer.config.getString("RFMRoot","/") + this.pathstr);
		
		fd.readFD();
		
		System.arraycopy(fd.getFD(), 0, b, 0, size);
		return(b);
	}

	public void writeBytes(byte[] buf, int maxbytes)
	{
		// write to file
		RandomAccessFile inFile = null;
		
		File f = new File(this.localroot + this.pathstr);
		if (f.exists())
		{
			try
			{
				inFile = new RandomAccessFile(f, "rw");
			
				inFile.seek(this.seekpos);
				
				//TODO what if we don't get buf.length??
				//this.seekpos += 
				inFile.write(buf);		
			
			} 
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		else
		{
			logger.error("write to non existent file");
		}
	}
	
	
	
}

