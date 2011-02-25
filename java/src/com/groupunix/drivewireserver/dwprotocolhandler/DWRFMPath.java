package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.OS9Defs;

public class DWRFMPath
{
	private static final Logger logger = Logger.getLogger("DWServer.DWRFMPath");
		
	private int pathno;
	private String pathstr;
	private String localroot;
	private int seekpos;
	private int handlerno;
	
	private boolean dirmode = false;
	private byte[] dirbuffer;
	
	private FileSystemManager fsManager;
	private FileObject fileobj;
	
	public DWRFMPath(int handlerno, int pathno) throws FileSystemException
	{
		this.setPathno(pathno);
		this.setSeekpos(0);
		logger.debug("new path " + pathno);
		
		this.fsManager = VFS.getManager();
		this.setLocalroot(DriveWireServer.getHandler(this.handlerno).getConfig().getString("RFMRoot","/"));
		
	}

	public void setPathno(int pathno)
	{
		logger.debug("set path to " + pathno);
		this.pathno = pathno;
	}

	public int getPathno()
	{
		return pathno;
	}

	public void setPathstr(String pathstr)
	{
		this.pathstr = "/";
		logger.debug("set pathstr to " + this.pathstr);
		
	}

	public String getPathstr()
	{
		return pathstr;
	}

	public void close()
	{
		logger.debug("closing path " + this.pathno + " to " + this.pathstr);
		try
		{
			fileobj.close();
		} catch (FileSystemException e)
		{
			logger.warn("error closing file: " + e.getMessage());
		}
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

	public int openFile(int modebyte)
	{
		// attempt to open local file
		
		try
		{
			fileobj = fsManager.resolveFile(this.localroot + this.pathstr);
			
			if (((byte)modebyte & OS9Defs.MODE_DIR) == OS9Defs.MODE_DIR)
			{
				// Directory
				if (fileobj.isReadable())		
				 {
					if (fileobj.getType() == FileType.FOLDER)
					{
						this.dirmode = true;
						genDirBuffer();
						
						logger.debug("directory open: modebyte " + modebyte);
						
						return(0);
						
					}
					else
					{
						 fileobj.close();
						 return(214);
					 }	
				 }
				 else
				 {
					 fileobj.close();
					 return(216);
				 }
			}
			else
			{
				// File
			
				if (fileobj.isReadable())		
				{
					return(0);
				}
				else
				{
					fileobj.close();
					return(216);
				}
			}
		} 
		catch (FileSystemException e)
		{
			logger.warn("open failed: " + e.getMessage());
			return(216);
		}
	}

	private void genDirBuffer() throws FileSystemException 
	{
		FileObject[] childs = this.fileobj.getChildren();
		
		if (childs.length > 0)
		{
			this.dirbuffer = new byte[(childs.length * 32) + 64];
			
			// . and ..
			this.dirbuffer[0] = (byte) '.';
			this.dirbuffer[1] = (byte) ('.' + 128);
			this.dirbuffer[32] = (byte) ('.' + 128);
			
			// file entries
			for (int i = 2;i<(childs.length+2);i++)
			{
				 String fname = childs[i-2].getName().getBaseName();
				 if (fname.length() <= 29)
				 {
					 System.arraycopy(fname.getBytes(), 0, this.dirbuffer, (i*32), fname.length());
					 
					 // set high bit in last char of filename
					 this.dirbuffer[(i*32)+fname.length()-1] = (byte) (this.dirbuffer[(i*32)+fname.length()-1] + 128);
					 
					 // need to set last 3 bytes to something...
					 
				 }
				 else
				 {
					 logger.debug("cannot add long named file '" +  childs[i-2].getName() + "'");
				 }
			}
			
		}
		else
		{
			logger.debug("empty directory");
			this.dirbuffer = null;
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
		
		try
		{
			// attempt to open local file
			fileobj = fsManager.resolveFile(this.localroot + this.pathstr);
			
			
			if (fileobj.exists())
			 {
				 // file already exists
				fileobj.close();
				return(218);
				 
			 }
			 else
			 {
				 fileobj.createFile();
				 return(0);
			 }
		} catch (FileSystemException e)
		{
			logger.warn("create failed: " + e.getMessage());
			return(245);
		}
	}

	public int getBytesAvail(int maxbytes)
	{
		
		if (this.dirmode)
		{
			// Dir mode
			// return # bytes left in the dirbuffer
			
			return(this.dirbuffer.length - this.seekpos);
			
		}
		else
		{
			// File mode
			// return # bytes left in file from current seek pos, up to maxbytes
	
			File f = new File(this.localroot + this.pathstr);
			if (f.exists())
			{
				// we only handle int sized files..
				int tmpsize = (int)f.length() - this.seekpos;
			
				// only 256 per call
				if (tmpsize > 127)
				{
					tmpsize = 127;
				}
			
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
	}

	public byte[] getBytes(int availbytes)
	{
		// TODO very crappy !
		// return byte array of next availbytes bytes from file, move seekpos
		// TODO structure blindly assumes this will work.
		// like above need to implement exceptions/error handling passed up to caller
		
		byte[] buf = new byte[availbytes];
		
		if (this.dirmode)
		{
			System.arraycopy(this.dirbuffer, this.seekpos, buf, 0, availbytes);
			// this.seekpos += availbytes;
		}
		else
		{
			RandomAccessFile inFile = null;
		
			File f = new File(this.localroot + this.pathstr);
			if (f.exists())
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

	public void setFd(byte[] buf) throws FileSystemException
	{
		DWRFMFD fd = new DWRFMFD(DriveWireServer.getHandler(this.handlerno).getConfig().getString("RFMRoot","/") + this.pathstr);
		
		fd.readFD();
		
		byte[] fdtmp = fd.getFD();
		
		System.arraycopy(buf, 0, fdtmp, 0, buf.length);
		
		fd.setFD(fdtmp);
		
		fd.writeFD();
				
	}

	public byte[] getFd(int size) throws FileSystemException
	{
		byte[] b = new byte[size];
		
		DWRFMFD fd = new DWRFMFD(DriveWireServer.getHandler(this.handlerno).getConfig().getString("RFMRoot","/") + this.pathstr);
		
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

