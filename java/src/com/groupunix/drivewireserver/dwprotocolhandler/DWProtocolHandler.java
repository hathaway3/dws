package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.virtualprinter.DWVPrinter;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;



public class DWProtocolHandler implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWProtocolHandler");
	  
	// DW protocol contants
	public static final byte DW_PROTOCOL_VERSION = 4;
	
	// DW protocol op codes
	
	public static final byte OP_NOP = 0;
	public static final byte OP_GETSTAT = 'G';
	public static final byte OP_SETSTAT = 'S';
	public static final byte OP_READ = 'R';
	public static final byte OP_READEX = (byte) ('R'+128);
	public static final byte OP_WRITE = 'W';
	public static final byte OP_REREAD = 'r';
	public static final byte OP_REREADEX = (byte) ('r'+128);
	public static final byte OP_REWRITE = 'w';
	public static final byte OP_INIT = 'I';
	public static final byte OP_TERM = 'T';
	public static final byte OP_TIME = '#';
	public static final byte OP_RESET3 = (byte) 248;  // my coco gives 248 on reset..?
	public static final byte OP_RESET2 = (byte) 254;
	public static final byte OP_RESET1 = (byte) 255;
	public static final byte OP_DWINIT = (byte) 'Z';
	public static final byte OP_PRINT = 'P';
	public static final byte OP_PRINTFLUSH = 'F';
	public static final byte OP_SERREAD = 'C';
	public static final byte OP_SERREADM = 'c';
	public static final byte OP_SERWRITE = (byte) ('C'+128);
	public static final byte OP_SERSETSTAT = (byte) ('D'+128);
	public static final byte OP_SERGETSTAT = 'D';
	public static final byte OP_SERINIT = 'E';
	public static final byte OP_SERTERM = (byte) ('E'+128);
	public static final byte OP_RFM = (byte) ('V'+128);
	
	// response codes
	public static final byte DWERROR_WP = (byte) 0xF2;
	public static final byte DWERROR_CRC = (byte) 0xF3;
	public static final byte DWERROR_READ = (byte) 0xF4;
	public static final byte DWERROR_WRITE = (byte) 0xF5;
	public static final byte DWERROR_NOTREADY = (byte) 0xF6;
	public static final byte DWOK = (byte) 0;
	

	
	// input buffer
	public static final int INPUT_WAIT = 250;
	
	
	
	// record keeping portion of dwTransferData
	private static byte lastDrive = 0;
	private static int readRetries = 0;
	private static int writeRetries = 0;
	private static int sectorsRead = 0;
	private static int sectorsWritten = 0;
	private static byte lastOpcode = OP_RESET1;
	private static byte lastGetStat = (byte) 255;
	private static byte lastSetStat = (byte) 255;
	private static int lastChecksum = 0;
	private static int lastError = 0;
	private static byte[] lastLSN = new byte[3];
	
	private static GregorianCalendar dwinitTime = new GregorianCalendar();
	
	// serial port instance
	
	private static DWSerialDevice serdev;
	
	// printer
	private static DWVPrinter vprinter;
	
	// disk drives
	private static DWDiskDrives diskDrives;
	
	//internal
	private static int wanttodie = 0;
	// private static Thread readerthread;

	// RFM handler
	private static DWRFMHandler rfmhandler;
	
	
	
	
	
	
	public static void reset()
	{
		DoOP_RESET();
	}
	
	
	public static boolean connected()
	{
		return(serdev.connected());
	}
	
	
	public static int setPort( String portname ) 
	{
	
		return(serdev.setPort(portname));
	}
	
	public static void shutdown()
	{
		logger.info("protocol handler has been asked to shutdown");
		
		serdev.close();
		wanttodie = 1;
		
	}
	
	
	
	
	public void run()
	{
		int opcodeint = -1;
		wanttodie = 0;

		Thread.currentThread().setName("dwproto-" + Thread.currentThread().getId());
		
		// this thread has got to run a LOT or we might lose bytes on the serial port
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		logger.info("DWProtocolHandler started");

		// serial device
		serdev = new DWSerialDevice();
		
		// setup drives
		diskDrives = new DWDiskDrives();
		
		if (DriveWireServer.config.containsKey("DefaultDiskSet"))
    	{
    		diskDrives.LoadDiskSet(DriveWireServer.config.getString("DefaultDiskSet"));
    	}
		
		// setup ports
		DWVSerialPorts.resetAllPorts();

		// setup printer
		vprinter = new DWVPrinter();
		
		// setup RFM handler
		rfmhandler = new DWRFMHandler();
		
		
		while(wanttodie == 0)
		{ 
			//if (DWVSerialPorts.isNull(0) == true)
			//	logger.debug("NULL TERM OUT");
			
			// try to get an opcode, use int to avoid signed bytes
			try 
			{
				opcodeint = serdev.comRead1(false);
			} 
			catch (DWCommTimeOutException e) 
			{
				// this should not actually ever get thrown, since we call comRead1 with timeout = false..
				logger.error(e.getMessage());
			}
			
		
			if (opcodeint > -1)
			{
				
				lastOpcode = (byte) opcodeint;
				
				switch(lastOpcode)
				{
					case OP_RESET1:
					case OP_RESET2:
					case OP_RESET3:
						DoOP_RESET();
						break;

					case OP_DWINIT:
						DoOP_DWINIT();
						break;
						
					case OP_INIT:
						DoOP_INIT();
						break;

					case OP_TERM:
						DoOP_TERM();	
						break;

					case OP_REREAD:
					case OP_READ:
						DoOP_READ(lastOpcode);
						break;

					case OP_REREADEX:
					case OP_READEX:
						DoOP_READEX(lastOpcode);
						break;

					case OP_WRITE:
					case OP_REWRITE:
						DoOP_WRITE(lastOpcode);
						break;


					case OP_GETSTAT:
					case OP_SETSTAT:
						DoOP_STAT(lastOpcode);
						break;

					case OP_TIME:
						DoOP_TIME();
						break;

					case OP_PRINT:
						DoOP_PRINT();
						break;

					case OP_PRINTFLUSH:
						DoOP_PRINTFLUSH();
						break;
				
					case OP_SERREADM:
						DoOP_SERREADM();
						break;

					case OP_SERREAD:
						DoOP_SERREAD();
						break;

					case OP_SERWRITE:
						DoOP_SERWRITE();
						break;

					case OP_SERSETSTAT:
						DoOP_SERSETSTAT();
						break;
				      
					case OP_SERGETSTAT:
						DoOP_SERGETSTAT();
						break;
    
					case OP_SERINIT:
						DoOP_SERINIT();
						break;
				      
					case OP_SERTERM:
						DoOP_SERTERM();
						break;	
						
					case OP_NOP:
						DoOP_NOP();
						break;
					
					case OP_RFM:
						DoOP_RFM();
						break;
						
					default:
						logger.info("UNKNOWN OPCODE: " + opcodeint);
						break;
				}
				

			}
		
		}
			
		logger.info("DWProtocolHandler exiting");
		
		serdev.close();
			
	}

	
	
	
	// DW OP methods

	
	private void DoOP_DWINIT() 
	{
		int drv_version;
	
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_DWINIT");
		}
		
		try
		{
			drv_version = serdev.comRead1(true);
			
			// are we limited to dw3?
			if (!DriveWireServer.config.getBoolean("DW3Only", false))
			{
				// send response
				serdev.comWrite1(DW_PROTOCOL_VERSION);
			
				logger.debug("DWINIT sent proto ver " + DW_PROTOCOL_VERSION + ", got driver version " + drv_version);
			
				// driver version is not used for anything yet...
			
				// coco has just booted nos9
				dwinitTime = new GregorianCalendar();
			
				// reset all ports
				DWVSerialPorts.resetAllPorts();
			}
			else
			{
				logger.debug("DWINIT recieved, ignoring due to DW3Only setting");
			}
		} 
		catch (DWCommTimeOutException e)
		{
			logger.error("Timed out reading DWINIT data byte");
		}
			

	}
	
	
	private void DoOP_NOP() 
	{
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_NOP");
		}
	}
	
	
	private void DoOP_RFM() 
	{
		int rfm_op;
		
		
		try
		{
			rfm_op = serdev.comRead1(true);
			logger.info("DoOP_RFM call " + rfm_op );
			
			rfmhandler.DoRFMOP(serdev,rfm_op);
			
		} catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	



	private void DoOP_TERM() 
	{
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_TERM");
		}
	}
	
	private void DoOP_INIT() 
	{
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_INIT");
		}
	}
	
	private static void DoOP_RESET() 
	{
		// coco has been reset/turned on
		
		// reset stats
		
		lastDrive = 0;
		readRetries = 0;
		writeRetries = 0;
		sectorsRead = 0;
		sectorsWritten = 0;
		lastOpcode = OP_RESET1;
		lastGetStat = (byte) 255;
		lastSetStat = (byte) 255;
		lastChecksum = 0;
		lastError = 0;
		
		lastLSN = new byte[3];
		
		// Sync disks??
		
		// reset all ports?
		DWVSerialPorts.resetAllPorts();
	
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_RESET");
		}
		
	}
	

	
	private void DoOP_WRITE(byte opcode)
	{
		byte[] cocosum = new byte[2];
		byte[] responsebuf = new byte[262];
		byte response = 0;
		byte[] sector = new byte[256];
		
		try 
		{
			// read rest of packet
			responsebuf = serdev.comRead(262);

			lastDrive = responsebuf[0];
			System.arraycopy( responsebuf, 1, lastLSN, 0, 3 );
			System.arraycopy( responsebuf, 4, sector, 0, 256 );
			System.arraycopy( responsebuf, 260, cocosum, 0, 2 );

			
			// Compute Checksum on sector received - NOTE: no V1 version checksum
			lastChecksum = computeChecksum(sector, 256);
			
			
			
		} 
		catch (DWCommTimeOutException e1) 
		{
			// Timed out reading data from Coco
			logger.error("DoOP_WRITE: " + e1.getMessage());
			
			// reset, abort
			return;
		} 
		
		
		
		// Compare checksums 
		if (lastChecksum != int2(cocosum))
		{
			// checksums do not match, tell Coco
			serdev.comWrite1(DWERROR_CRC);
			
			logger.warn("DoOP_WRITE: Bad checksum, drive: " + lastDrive + " LSN: " + int3(lastLSN) + " CocoSum: " + int2(cocosum) + " ServerSum: " + lastChecksum);
			
			return;
		}

				
		// do the write
		response = DWOK;
		
		try 
		{
			// Seek to LSN in DSK image 
			diskDrives.seekSector(lastDrive,int3(lastLSN));
			// Write sector to DSK image 
			diskDrives.writeSector(lastDrive,sector);
		} 
		catch (DWDriveNotLoadedException e1) 
		{
			// send drive not ready response
			response = DWERROR_NOTREADY;
			logger.warn(e1.getMessage());
		} 
		catch (DWDriveNotValidException e2) 
		{
			// basically the same as not ready
			response = DWERROR_NOTREADY;
			logger.warn(e2.getMessage());
		} 
		catch (DWDriveWriteProtectedException e3) 
		{
			// hopefully this is appropriate
			response = DWERROR_WP;
			logger.debug("DWWP EX");
			logger.warn(e3.getMessage());
		} 
		catch (IOException e4) 
		{
			// error on our end doing the write
			response = DWERROR_WRITE;
			logger.error(e4.getMessage());
		} 
		
		// record error
		if (response != DWOK)
			lastError = response;
		
		// send response
		serdev.comWrite1(response);
		
		// Increment sectorsWritten count
		if (response == DWOK)
			sectorsWritten++;
		
		if (opcode == OP_REWRITE)
		{
			writeRetries++;
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_REWRITE lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
			}
		}
		else
		{
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_WRITE lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
			}
		}
		
		return;
	}
	
	
	
	
	private void DoOP_READ(int opcode)
	{
		// READ / REREAD is not defined in current spec?
		// yet it exists in the linux source of drivewire 3 server.. 
		// leaving it out for now
		
		logger.error("got OP_READ??");
		try {
			Thread.sleep(275);
		} 
		catch (InterruptedException e) 
		{
			logger.info(e.getMessage());
		}
		
		return;
	}

	
	
	private void DoOP_READEX(int opcode)
	{
		byte[] cocosum = new byte[2];
		byte[] mysum = new byte[2];
		byte[] responsebuf = new byte[4];
		byte[] sector = new byte[256];
		
		try 
		{
			// read rest of packet
			
			responsebuf = serdev.comRead(4);
			
			lastDrive = responsebuf[0];
			System.arraycopy( responsebuf, 1, lastLSN, 0, 3 );
					
			// seek to requested LSN
			diskDrives.seekSector(lastDrive, int3(lastLSN));
			
			// load lastSector with bytes from file
			sector = diskDrives.readSector(lastDrive);
				
		} 
		catch (DWDriveNotLoadedException e1) 
		{
			// zero sector
			sector = diskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e1.getMessage());	
		} 
		catch (DWDriveNotValidException e2) 
		{
			// zero sector
			sector = diskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e2.getMessage());
		} 
		catch (IOException e3) 
		{
			// zero sector
			sector = diskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e3.getMessage());
		} 
		catch (DWCommTimeOutException e4) 
		{
			// timeout.. abort
			logger.error("DoOP_READEX, during read packet: " + e4.getMessage());
			return;
		}
				
		// write out response sector
		serdev.comWrite(sector, 256);
		
		try 
		{
			// get cocosum
			cocosum  = serdev.comRead(2);
		} 
		catch (DWCommTimeOutException e) 
		{
			// timeout.. abort
			logger.error("DoOP_READEX, during read cocosum: " + e.getMessage());
			
			return;
		}

		lastChecksum = computeChecksum(sector, 256);

		mysum[0] = (byte) ((lastChecksum >> 8) & 0xFF);
		mysum[1] = (byte) ((lastChecksum << 0) & 0xFF);

		if ((mysum[0] == cocosum[0]) && (mysum[1] == cocosum[1]))
		{
			// Good checksum, all is well
			sectorsRead++;
			serdev.comWrite1(DWOK);
		
			if (opcode == OP_REREADEX)
			{
				readRetries++;
				logger.warn("DoOP_REREADEX lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
			}
			else
			{
				if (DriveWireServer.config.getBoolean("LogOpCode", false))
				{
					logger.info("DoOP_READEX lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
				}
			}
		}
		else
		{
			// checksum mismatch
			// sectorsRead++;  should we increment this?
			serdev.comWrite1(DWERROR_CRC);
			
			if (opcode == OP_REREADEX)
			{
				readRetries++;
				logger.warn("DoOP_REREADEX CRC check failed, lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
			}
			else
			{
				logger.warn("DoOP_READEX CRC check failed, lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
			}
			
		}
		
	}

	
	private void DoOP_STAT(byte opcode)
	{
		byte[] responsebuf = new byte[2];
		
		try 
		{
			// get packet args
			// drive # and stat
			responsebuf = serdev.comRead(2);
			
			lastDrive = responsebuf[0];
			
		} 
		catch (DWCommTimeOutException e1) 
		{
			// timeout.. abort
			logger.error("DoOP_GET/SETSTAT, during read packet: " + e1.getMessage());
			return;
		} 

		if (opcode == OP_GETSTAT)
		{
			lastGetStat = responsebuf[1];
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_GETSTAT: " + prettySS(responsebuf[1]) + " lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
			}
		}
		else
		{
			lastSetStat = responsebuf[1];
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SETSTAT " + prettySS(responsebuf[1]) + " lastDrive: " + (int) lastDrive + " LSN: " + int3(lastLSN));
			}
		}
	}
	
	
	private void DoOP_TIME()
	{
		GregorianCalendar c = (GregorianCalendar) Calendar.getInstance();
		
		serdev.comWrite1(c.get(Calendar.YEAR)-108);
		serdev.comWrite1(c.get(Calendar.MONTH)+1);
		serdev.comWrite1(c.get(Calendar.DAY_OF_MONTH));
		serdev.comWrite1(c.get(Calendar.HOUR_OF_DAY));
		serdev.comWrite1(c.get(Calendar.MINUTE));
		serdev.comWrite1(c.get(Calendar.SECOND));
		// comWrite1(c.get(Calendar.DAY_OF_WEEK));
		
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_TIME");
		}
			
		return;
	}
	
	
	
	// serial ports
	
	private void DoOP_SERGETSTAT() 
	{
		byte[] responsebuf = new byte[2];
		
		
		try 
		{
			// get packet args
			// port # and stat
			responsebuf = serdev.comRead(2);
			if (responsebuf[1] != 1)
			{
				if (DriveWireServer.config.getBoolean("LogOpCode", false))
				{
					logger.info("DoOP_SERGETSTAT: " + prettySS(responsebuf[1]) + " port: " + responsebuf[0] + "(" + DWVSerialPorts.prettyPort(responsebuf[0]) + ")");
				}
			}
			
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading packet byte in SERGETSTAT");
		}
	}
	
	private void DoOP_SERSETSTAT() 
	{
		byte[] responsebuf = new byte[2];
		
		
		try 
		{
			// get packet args
			// port # and stat
			responsebuf = serdev.comRead(2);
			
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SERSETSTAT: " + prettySS(responsebuf[1]) + " port: " + responsebuf[0] + "(" + DWVSerialPorts.prettyPort(responsebuf[0]) + ")");
			}
			
			if (!DWVSerialPorts.isValid(responsebuf[0]))
			{
				logger.debug("Invalid port '" + responsebuf[0] + "' in sersetstat, ignored.");
				return;
			}
			
			switch(responsebuf[1])
			{
				// SS.ComSt
				case 0x28:
					byte[] devdescr = new byte[26];
					devdescr = serdev.comRead(26);
					
					// logger.debug("COMST: " + byteArrayToHexString(devdescr));
					
					// should move into DWVSerialPorts
					
					// store it
					DWVSerialPorts.setDD(responsebuf[0],devdescr);
					
					// set PD.INT offset 16 and PD.QUT offset 17
					if (DWVSerialPorts.getPD_INT(responsebuf[0]) != devdescr[16])
					{
						DWVSerialPorts.setPD_INT(responsebuf[0], devdescr[16]);
						// logger.debug("Changed PD.INT to " + devdescr[16] + " on port " + DWVSerialPorts.prettyPort(responsebuf[0]));	
					}
					
					if (DWVSerialPorts.getPD_QUT(responsebuf[0]) != devdescr[17])
					{
						DWVSerialPorts.setPD_QUT(responsebuf[0], devdescr[17]);
						// logger.debug("Changed PD.QUT to " + devdescr[17] + " on port " + DWVSerialPorts.prettyPort(responsebuf[0]));	
					}
					
					break;
					
				// SS.Open	
				case 0x29:
					DWVSerialPorts.openPort(responsebuf[0]);
					break;
					
				//SS.Close
				case 0x2A:
					DWVSerialPorts.closePort(responsebuf[0]);
					break;
					
			}
			
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading packet byte in SERSETSTAT");
		}
		
	}
	
	private void DoOP_SERINIT() 
	{
		byte[] responsebuf = new byte[2];
		
		try 
		{
			// get packet args
			// port # (mode no longer sent)
			responsebuf = serdev.comRead(1);
			
			int portnum = responsebuf[0];
			// int portmode = responsebuf[1];
			
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SERINIT for port " + DWVSerialPorts.prettyPort(portnum));
			}
			
			
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading packet byte in SERINIT");
		}
		
	}
	
	
	private void DoOP_SERTERM() 
	{
		int portnum;
		
		try 
		{
			// get packet args
			// just port # 
			portnum = serdev.comRead1(true);
		
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SERTERM for port " + portnum);
			}
			
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading packet byte in SERTERM");
		}
		
	}
	
	
	
	private void DoOP_SERREAD() 
	{
		byte[] result = new byte[2];
		
		result = DWVSerialPorts.serRead();
		
		serdev.comWrite(result, 2);
		
		//if (result[0] != 0)
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_SERREAD response " + (int) (result[0] & 0xFF) + ":" + (int) (result[1] & 0xFF));
		}
	}
	
	private void DoOP_SERWRITE() 
	{
		byte[] cmdpacket = new byte[2];
				
		try {
			cmdpacket = serdev.comRead(2);
			
			DWVSerialPorts.serWrite(cmdpacket[0],cmdpacket[1]);
			
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.debug("DoOP_SERWRITE to port " + cmdpacket[0]);
			}
			
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading SERWRITE packet: " + e.getMessage());
		}
		
	}
	
	private void DoOP_SERREADM() 
	{
		byte[] cmdpacket = new byte[2];
		byte[] data = new byte[256];
		
		try {
			cmdpacket = serdev.comRead(2);
			
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				try {
				logger.debug("DoOP_SERREADM for " +  (cmdpacket[1] & 0xFF) + " bytes on port " + cmdpacket[0] + " (" + DWVSerialPorts.getPortOutput(cmdpacket[0]).available() + " bytes in buffer)");
				} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}
			
			data = DWVSerialPorts.serReadM((int) cmdpacket[0],(cmdpacket[1] & 0xFF));
			
			// logger.debug(new String(data));
			
			serdev.comWrite(data, (int) (cmdpacket[1] & 0xFF));
			
			
			
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading SERREADM packet: " + e.getMessage());
		}
		
		
	}
	
	
	
	// printing
	
	private void DoOP_PRINT() 
	{
		int tmpint;

		try 
		{
			tmpint = serdev.comRead1(true);
			
			if (DriveWireServer.config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_PRINT: byte "+ tmpint);
			}
			
			vprinter.addByte((byte) tmpint);
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading print byte");
		}
		
		
	}
	
	private void DoOP_PRINTFLUSH() 
	{
		if (DriveWireServer.config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_PRINTFLUSH");
		}
		
		vprinter.flush();
	}
	
	
	
	
	// utilities methods from original dw server
	
	
	private int computeChecksum(byte[] data, int numbytes) 
	{
		int lastChecksum = 0;

		/* Check to see if numbytes is odd or even */
		while (numbytes > 0)
		{
			numbytes--;
			lastChecksum += (int) (data[numbytes] & 0xFF) ;
		}

		return(lastChecksum);
		
	}


	public static int int4(byte[] data) 
	{
		 return( (data[0] & 0xFF) << 32) + ((data[0] & 0xFF) << 16) + ((data[1] & 0xFF) << 8) + (data[2] & 0xFF);
	}
	
	public static int int3(byte[] data) 
	{
		 return((data[0] & 0xFF) << 16) + ((data[1] & 0xFF) << 8) + (data[2] & 0xFF);
	}

	
	public static int int2(byte[] data) 
	{
		 return((data[0] & 0xFF) << 8) + (data[1] & 0xFF);
	}


	public static byte getLastDrive() {
		return lastDrive;
	}


	public static int getReadRetries() {
		return readRetries;
	}


	public static int getWriteRetries() {
		return writeRetries;
	}


	public static int getSectorsRead() {
		return sectorsRead;
	}


	public static int getSectorsWritten() {
		return sectorsWritten;
	}


	public static byte getLastOpcode() {
		return lastOpcode;
	}


	public static byte getLastGetStat() {
		return lastGetStat;
	}


	public static byte getLastSetStat() {
		return lastSetStat;
	}


	public static int getLastChecksum() {
		return lastChecksum;
	}


	public static int getLastError() {
		return lastError;
	}


	public static byte[] getLastLSN() {
		return lastLSN;
	}


//	public static SerialPort getSerialPort() {
//		return serialPort;
//	}
	
		
	
	public static String byteArrayToHexString(byte in[]) {

	    byte ch = 0x00;

	    int i = 0; 

	    if (in == null || in.length <= 0)

	        return null;

	        

	    String pseudo[] = {"0", "1", "2",
	"3", "4", "5", "6", "7", "8",
	"9", "A", "B", "C", "D", "E",
	"F"};

	    StringBuffer out = new StringBuffer(in.length * 2);

	    

	    while (i < in.length) {

	        ch = (byte) (in[i] & 0xF0);

	        ch = (byte) (ch >>> 4);
	     // shift the bits down

	        ch = (byte) (ch & 0x0F);    
	// must do this is high order bit is on!

	        out.append(pseudo[ (int) ch]); // convert the

	        ch = (byte) (in[i] & 0x0F); // Strip off

	        out.append(pseudo[ (int) ch]); // convert the

	        i++;

	    }

	    String rslt = new String(out);

	    return rslt;

	}    

	
	public static GregorianCalendar getDWInitTime()
	{
		return(dwinitTime);
	}

	
	public static String prettySS(byte statcode)
	{
		String result = "unknown";
		
		switch (statcode)
		{
			case 0x00:
				result = "SS.Opt";
				break;

			case 0x02:
				result = "SS.Size";
				break;

			case 0x03:
				result = "SS.Reset";
				break;

			case 0x04:
				result = "SS.WTrk";
				break;

			case 0x05:
				result = "SS.Pos";
				break;

			case 0x06:
				result = "SS.EOF";
				break;

			case 0x0A:
				result = "SS.Frz";
				break;

			case 0x0B:
				result = "SS.SPT";
				break;

			case 0x0C:
				result = "SS.SQD";
				break;

			case 0x0D:
				result = "SS.DCmd";
				break;

			case 0x0E:
				result = "SS.DevNm";
				break;

			case 0x0F:
				result = "SS.FD";
				break;

			case 0x10:
				result = "SS.Ticks";
				break;

			case 0x11:
				result = "SS.Lock";
				break;

			case 0x12:
				result = "SS.VarSect";
				break;

			case 0x14:
				result = "SS.BlkRd";
				break;

			case 0x15:
				result = "SS.BlkWr";
				break;

			case 0x16:
				result = "SS.Reten";
				break;

			case 0x17:
				result = "SS.WFM";
				break;

			case 0x18: 
				result = "SS.RFM";
				break;
			
			case 0x1A:
				result = "SS.SSig";
				break;
				
			case 0x1B:
				result = "SS.Relea";
				break;

			case 0x1C:
				result = "SS.Attr";
				break;

			case 0x1E:
				result = "SS.RsBit";
				break;

			case 0x20:
				result = "SS.FDInf";
				break;

			case 0x26:
				result = "SS.DSize";
				break;

			case 0x27:
				result = "SS.KySns";
				break;

			// added for SCF/Ns	
			case 0x28:
				result = "SS.ComSt";
				break;
				
			case 0x29:
				result = "SS.Open";
				break;	
				
			case 0x2A:
				result = "SS.Close";
				break;
				
			case 0x30:
				result = "SS.HngUp";
				break;
				
			case (byte) 255:
				result =  "None";
				break;
			
			default:
				result = "Unknown: " + statcode;
		}
		
		return(result);
	}


	public static String prettyOP(byte opcode) 
	{
		String res = "Unknown";

		switch(opcode)
		{
		case OP_NOP:
			res = "OP_NOP";
			break;

		case OP_INIT:
			res = "OP_INIT";
			break;

		case OP_READ:
			res = "OP_READ";
			break;

		case OP_READEX:
			res = "OP_READEX";
			break;

		case OP_WRITE:
			res = "OP_WRITE";
			break;

		case OP_REREAD:
			res = "OP_REREAD";
			break;

		case OP_REREADEX:
			res = "OP_REREADEX";
			break;

		case OP_REWRITE:
			res = "OP_REWRITE";
			break;

		case OP_TERM:
			res = "OP_TERM";
			break;

		case OP_RESET1:
		case OP_RESET2:
		case OP_RESET3:
			res =  "OP_RESET";
			break;

		case OP_GETSTAT:
			res = "OP_GETSTAT";
			break;

		case OP_SETSTAT:
			res = "OP_SETSTAT";
			break;

		case OP_TIME:
			res = "OP_TIME";
			break;

		case OP_PRINT:
			res = "OP_PRINT";
			break;

		case OP_PRINTFLUSH:
			res = "OP_PRINTFLUSH";
			break;

		case OP_SERREADM:
			res = "OP_SERREADM";
			break;

		case OP_SERREAD:
			res = "OP_SERREAD";      
			break;

		case OP_SERWRITE:
			res = "OP_SERWRITE";
			break;

		case OP_SERSETSTAT:
			res = "OP_SERSETSTAT";
            break;
            
		case OP_SERGETSTAT:
			res =  "OP_SERGETSTAT";
            break;
		
		case OP_SERINIT:
			res =  "OP_SERINIT";
            break;
            
		case OP_SERTERM:
			res =  "OP_SERTERM";
            break;
            
		case OP_DWINIT:
			res =  "OP_DWINIT";
            break;
            
		default:
				res = "Unknown: " + opcode;
		}
		
		return(res);
	}


	public static DWDiskDrives getDiskDrives() {
		return diskDrives;
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<String> getPortNames()
	{
		ArrayList<String> ports = new ArrayList();
		
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		while ( portEnum.hasMoreElements() ) 
		{
			CommPortIdentifier portIdentifier = portEnum.nextElement();
		    if (portIdentifier.getPortType() == 1)
		    {
		    	ports.add(portIdentifier.getName());
		    }
		        
		}        
				
		return(ports);
	}


	//public static synchronized void resetCocoModel() throws UnsupportedCommOperationException
	//{
	//	logger.debug("resetting serial port parameters to model " + DriveWireServer.config.getInt("CocoModel"));
	//	setSerialParams(serialPort);
	//}

	
}
	

