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
				DoOP_RFM_CREATE();
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
				DoOP_RFM_READ();
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
				DoOP_RFM_GETSTT();
				break;
			case RFM_OP_SETSTT:
				DoOP_RFM_SETSTT();
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
			
			this.paths[pathno].close();
			this.paths[pathno] = null;
			
		} 
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	private void DoOP_RFM_SETSTT()
	{
		logger.debug("SETSTT");
	}


	private void DoOP_RFM_GETSTT()
	{
		logger.debug("GETSTT");		
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
			byte[] maxbytes = new byte[2];
			
			maxbytes = serdev.comRead(2);
			
			// send result
			/*
			serdev.comWrite1(5);
			byte[] tmp = new byte[5];
			tmp[0] = (byte) 'T';
			tmp[1] = (byte) 'E';
			tmp[2] = (byte) 'S';
			tmp[3] = (byte) 'T';
			tmp[4] = (byte) 13;
			serdev.comWrite(tmp,5);
			*/
			serdev.comWrite1(0);
			logger.debug("readln on path " + pathno );
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


	private void DoOP_RFM_READ()
	{
		logger.debug("READ");
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


	private void DoOP_RFM_CREATE()
	{
		logger.debug("CREATE");	
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
			
			serdev.comWrite1(216);
			
			logger.debug("opened path " + pathno + " to " + pathstr);
		} 
		catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
}
