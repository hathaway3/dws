package com.groupunix.drivewireserver.dwprotocolhandler;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

public class DWRFMHandler
{
	private static final Logger logger = Logger.getLogger("DWServer.DWRFMHandler");
	
	// RFM opcodes
	public static final byte RFM_OP_CREATE = (byte) 1;
	public static final byte RFM_OP_OPEN = (byte) 2;
	public static final byte RFM_OP_MAKDIR = (byte) 3;
	public static final byte RFM_OP_CHGDIR = (byte) 4;
	public static final byte RFM_OP_DELETE = (byte) 5;
	public static final byte RFM_OP_SEEK = (byte) 6;
	public static final byte RFM_OP_READ = (byte) 7;
	public static final byte RFM_OP_WRITE = (byte) 8;
	public static final byte RFM_OP_READLN = (byte) 9;
	public static final byte RFM_OP_WRITLN = (byte) 10;
	public static final byte RFM_OP_GETSTT = (byte) 11;
	public static final byte RFM_OP_SETSTT = (byte) 12;
	public static final byte RFM_OP_CLOSE = (byte) 13;
	
	
	private DWRFMPath[] paths = new DWRFMPath[256];
	
	public DWRFMHandler()
	{
		logger.debug("init");
		
	}
	
	public void DoRFMOP(DWSerialDevice serdev, int rfm_op)
	{
		switch (rfm_op)
		{
			case RFM_OP_CREATE:
				DoOP_RFM_CREATE(serdev);
				break;
			case RFM_OP_OPEN:
				DoOP_RFM_OPEN(serdev);
				break;
			case RFM_OP_MAKDIR:
				DoOP_RFM_MAKDIR();
				break;
			case RFM_OP_CHGDIR:
				DoOP_RFM_CHGDIR();
				break;
			case RFM_OP_DELETE:
				DoOP_RFM_DELETE();
				break;
			case RFM_OP_SEEK:
				DoOP_RFM_SEEK(serdev);
				break;
			case RFM_OP_READ:
				DoOP_RFM_READ(serdev);
				break;
			case RFM_OP_WRITE:
				DoOP_RFM_WRITE();
				break;
			case RFM_OP_READLN:
				DoOP_RFM_READLN(serdev);
				break;
			case RFM_OP_WRITLN:
				DoOP_RFM_WRITLN();
				break;
			case RFM_OP_GETSTT:
				DoOP_RFM_GETSTT(serdev);
				break;
			case RFM_OP_SETSTT:
				DoOP_RFM_SETSTT(serdev);
				break;
			case RFM_OP_CLOSE:
				DoOP_RFM_CLOSE(serdev);
				break;
			
		}
	}
	
	
	private void DoOP_RFM_CLOSE(DWSerialDevice serdev)
	{
		logger.debug("CLOSE");
		
		// read path #
		try
		{
			int pathno = serdev.comRead1(true);
			
			if (this.paths[pathno] == null)
			{
				logger.error("close on null path: " + pathno);
			}
			else
			{
				this.paths[pathno].close();
				this.paths[pathno] = null;
			}
			
			// send response
			serdev.comWrite1(0);
			
		} 
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	private void DoOP_RFM_SETSTT(DWSerialDevice serdev)
	{
		logger.debug("SETSTT");
		
		// read path #
		try
		{
			int pathno = serdev.comRead1(true);
			
			// read call
			int call = serdev.comRead1(true);
			
			logger.debug("SETSTT path " + pathno + " call " + call);
			
		}
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}


	private void DoOP_RFM_GETSTT(DWSerialDevice serdev)
	{
		logger.debug("GETSTT");
		
		// read path #
		try
		{
			int pathno = serdev.comRead1(true);
			
			// read call
			int call = serdev.comRead1(true);
			
			logger.debug("GETSTT path " + pathno + " call " + call);
			
		}
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}


	private void DoOP_RFM_WRITLN()
	{
		logger.debug("WRITLN");	
	}


	private void DoOP_RFM_READLN(DWSerialDevice serdev)
	{
		logger.debug("READLN");
		
		// read path #
		try
		{
			int pathno = serdev.comRead1(true);
			
			// read max bytes
			byte[] maxbytesb = new byte[2];
			
			maxbytesb= serdev.comRead(2);
			
			int maxbytes = DWProtocolHandler.int2(maxbytesb);
			
			int availbytes = this.paths[pathno].getBytesAvail(maxbytes);

			logger.debug("initial AB: " + availbytes);
			
			byte[] buf = new byte[availbytes];
			
			System.arraycopy(this.paths[pathno].getBytes(availbytes),0, buf, 0, availbytes);
			
			// find $0D or end
			int x = 0;
			while (x < availbytes)
			{
				if (buf[x] == (byte)13)
				{
					availbytes = x+1;
				}
				x++;
			}
			
			logger.debug("adjusted AB: " + availbytes);
			
			serdev.comWrite1(availbytes);
			
			if (availbytes > 0)
			{
				serdev.comWrite(buf, availbytes);
				this.paths[pathno].incSeekpos(availbytes);
			}
			
			logger.debug("readln on path " + pathno + " maxbytes: " + maxbytes + " availbytes: " + availbytes );
		} 
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}


	private void DoOP_RFM_WRITE()
	{
		logger.debug("WRITE");		
	}


	private void DoOP_RFM_READ(DWSerialDevice serdev)
	{
		logger.debug("READ");
		
		// read path #
		try
		{
			int pathno = serdev.comRead1(true);
			
			// read max bytes
			byte[] maxbytesb = new byte[2];
			
			maxbytesb= serdev.comRead(2);
			
			int maxbytes = DWProtocolHandler.int2(maxbytesb);
			
			int availbytes = this.paths[pathno].getBytesAvail(maxbytes);
			
			byte[] buf = new byte[availbytes];
			
			System.arraycopy(this.paths[pathno].getBytes(availbytes),0, buf, 0, availbytes);
			
			serdev.comWrite1(availbytes);
			
			if (availbytes > 0)
			{
				serdev.comWrite(buf, availbytes);
				this.paths[pathno].incSeekpos(availbytes);
			}
			
			logger.debug("read on path " + pathno + " maxbytes: " + maxbytes + " availbytes: " + availbytes );
		} 
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
	}


	private void DoOP_RFM_SEEK(DWSerialDevice serdev)
	{
		logger.debug("SEEK");
		try
		{
			int pathno = serdev.comRead1(true);
			
			// read seek pos
			byte[] seekpos = new byte[4];
			
			seekpos = serdev.comRead(4);
			
			this.paths[pathno].setSeekpos(DWProtocolHandler.int4(seekpos));
			
			// assume it worked, for now
			serdev.comWrite1(0);
			
		}
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			
	}


	private void DoOP_RFM_DELETE()
	{
		logger.debug("DELETE");		
	}


	private void DoOP_RFM_CHGDIR()
	{
		logger.debug("CHGDIR");
	}


	private void DoOP_RFM_MAKDIR()
	{
		logger.debug("MAKDIR");
	}


	private void DoOP_RFM_CREATE(DWSerialDevice serdev)
	{
		logger.debug("CREATE");
		
		// read path #
		try
		{
			int pathno = serdev.comRead1(true);
			
			// read path str
			String pathstr = new String();
		
			int nchar = serdev.comRead1(true);
			while (nchar != 13)
			{
				pathstr += Character.toString((char) nchar);
				nchar = serdev.comRead1(true);
			}

			// send result
			
	
			this.paths[pathno] = new DWRFMPath(pathno);
			this.paths[pathno].setPathstr(pathstr);
			
			int result = this.paths[pathno].createFile();
			
			serdev.comWrite1(result);
			
			logger.debug("create path " + pathno + " to " + pathstr + ": result " + result);
		} 
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}


	private void DoOP_RFM_OPEN(DWSerialDevice serdev)
	{
		logger.debug("OPEN");
		
		// read path #
		try
		{
			int pathno = serdev.comRead1(true);
			
			// read path str
			String pathstr = new String();
		
			int nchar = serdev.comRead1(true);
			while (nchar != 13)
			{
				pathstr += Character.toString((char) nchar);
				nchar = serdev.comRead1(true);
			}

			// send result
			
			// anything needed for dealing with multiple opens..
			
			this.paths[pathno] = new DWRFMPath(pathno);
			this.paths[pathno].setPathstr(pathstr);
			
			int result = this.paths[pathno].openFile();
			
			serdev.comWrite1(result);
			
			logger.debug("open path " + pathno + " to " + pathstr + ": result " + result);
		} 
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
}
