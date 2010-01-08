package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;



public class DWProtocolHandler implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWProtocolHandler");
	  
	
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
	private static String lastMessage = "DriveWire Server " + DriveWireServer.DWServerVersion;

	public static byte[] lastSector = new byte[256];
	private static GregorianCalendar dwinitTime = new GregorianCalendar();
	
	// serial port instance
	private static SerialPort serialPort;
	
	//internal
	private static int wanttodie = 0;
	// private static CircularByteBuffer serialInputBuf = new CircularByteBuffer();
	// private static Thread readerthread;

	
	public static void reset()
	{
		DoOP_RESET();
	}
	
	
	public static boolean connected()
	{
		// could check DTR/DCD/DSR something, but none of this seems to exist on my DW cable or the USB adapter doesn't pass it, something.. oh well this eems to work
		if (serialPort != null)
		{
			return(true);
		}
		else
		{
			return(false);
		}
	}
	
	public static int setPort( String portname ) 
	{
	
		if (serialPort != null)
		{
			// close current port
			logger.info("closing port " + serialPort.getName());
			serialPort.close();
		}

		try 
		{
			connectSerial(portname);
		} 
		catch (IOException e1) 
		{
			logger.warn(e1.getMessage());
			return(-1);
		} 
		catch (NoSuchPortException e2) 
		{
			logger.warn(e2.getMessage());
			return(-1);
		} 
		catch (PortInUseException e3) 
		{
			logger.warn(e3.getMessage());
			return(-1);
		} 
		catch (UnsupportedCommOperationException e4) 
		{
			logger.warn(e4.getMessage());
			return(-1);
		}
		
		return(1);

	}
	
	public static void shutdown()
	{
		logger.info("protocol handler has been asked to shutdown");
		
		serialPort.close();
		wanttodie = 1;
		
	}
	
	private static void connectSerial ( String portName ) throws IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException
    {
		logger.info("attempting to open device '" + portName + "'");
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            logger.error("Port is already in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open("DWProtocolHandler",2000);
            
            if ( commPort instanceof SerialPort )
            {
            	
                serialPort = (SerialPort) commPort;

                // these settings seem to solve the lost bytes problems on my usb adapter
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                serialPort.enableReceiveThreshold(1);
                serialPort.enableReceiveTimeout(3000);
                
                setSerialParams(serialPort);               
                  
                // try this..
                
               /* readerthread = new Thread(new DWProtoReader(serialPort.getInputStream(), serialInputBuf.getOutputStream() ));
                readerthread.start();
                */
                
                logger.info("succesfully opened " + portName);
                
            }
            else
            {
                logger.error("Only serial devices are allowed.");
            }
        }     
    }
	

	private static void setSerialParams(SerialPort sport) throws UnsupportedCommOperationException 
	{
		switch(DriveWireServer.config.getInt("CocoModel", 3))
		{
			case 1:
				sport.setSerialPortParams(38400,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
				break;
			case 2:
				sport.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
				break;
			default:
				sport.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
				break;
			
		}
	}

	
	private void comWrite(byte[] data, int len)
	{	
		try 
		{
			serialPort.getOutputStream().write(data, 0, len);
			
			// extreme cases only
			
			/*
			for (int i = 0;i< data.length;i++)
			{
				logger.debug("WRITE: " + (int)(data[i] & 0xFF));
			}
			*/
			
		} 
		catch (IOException e) 
		{
			// problem with comm port, bail out
			logger.error(e.getMessage());
			wanttodie = 1;
		}
	}	
	
	private void comWrite1(int data)
	{
		try 
		{
			serialPort.getOutputStream().write((byte) data);
			
			// extreme cases only
			// logger.debug("WRITE1: " + data);
			
		} 
		catch (IOException e) 
		{
			// problem with comm port, bail out
			logger.error(e.getMessage());
			wanttodie = 1;
		}
	}
	
	
	
	private byte[] comRead(int len) throws DWCommTimeOutException 
	{
	
		byte[] buf = new byte[len];
		
		for (int i = 0;i<len;i++)
		{
			buf[i] = (byte) comRead1(true);
		}
		
		return(buf);
	}
	
	
	
	private int comRead1(boolean timeout) throws DWCommTimeOutException 
	{
		int retdata = -1;
		
		try {

			while (retdata == -1)
			{
				retdata = serialPort.getInputStream().read();
			}
			
			// extreme cases only
			// logger.debug("READ1: " + retdata);
			
			return(retdata);
		} 
		catch (IOException e) 
		{
			logger.error("error reading byte: " + e.getMessage());
			return(-1);
		}
		
		
		/* try {
			retdata = serialInputBuf.getInputStream().read();
			// logger.debug("Read byte: " + retdata);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return(retdata);
		*/
	}


	
	public void run()
	{
		int opcodeint = -1;
		wanttodie = 0;

		Thread.currentThread().setName("dwproto-" + Thread.currentThread().getId());
		
		// this thread has got to run a LOT or we lose bytes on the serial port
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		logger.info("DWProtocolHandler started");
		
		while(wanttodie == 0)
		{ 
			// try to get an opcode, use int to avoid signed bytes
			try 
			{
				opcodeint = comRead1(false);
			} 
			catch (DWCommTimeOutException e) 
			{
				// this should not actually ever get thrown, since we call comRead1 with timeout = false..
				logger.error(e.getMessage());
			}
			
			
			// logger.debug("main sees byte: " + opcodeint);
		
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
						
					default:
						logger.info("UNKNOWN OPCODE: " + opcodeint);
						break;
				}
				
				
				
				
			}
		
		}
			
		logger.info("DWProtocolHandler exiting");
		
		if (serialPort != null)
		{
			serialPort.close();
		}
		
			
	}

	
	
	
	// DW OP methods

	
	private void DoOP_DWINIT() 
	{
		logger.info("DoOP_DWINIT");
		
		// coco has just booted nos9
		this.dwinitTime = new GregorianCalendar();
		
		// reset all ports
		DWVSerialPorts.resetAllPorts();
	}
	
	private void DoOP_NOP() 
	{
		logger.info("DoOP_NOP");
	}
	
	
	
	private void DoOP_TERM() 
	{
		logger.info("DoOP_TERM");
	}
	
	private void DoOP_INIT() 
	{
		logger.info("DoOP_INIT");
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
		lastSector = new byte[256];
		
		// Sync disks??
		
		// reset all ports
		DWVSerialPorts.resetAllPorts();
		
		logger.info("DoOP_RESET");
		
	}
	

	
	private void DoOP_WRITE(byte opcode)
	{
		byte[] cocosum = new byte[2];
		byte[] responsebuf = new byte[262];
		byte response = 0;
		
		try 
		{
			// read rest of packet
			responsebuf = comRead(262);

			lastDrive = responsebuf[0];
			System.arraycopy( responsebuf, 1, lastLSN, 0, 3 );
			System.arraycopy( responsebuf, 4, lastSector, 0, 256 );
			System.arraycopy( responsebuf, 260, cocosum, 0, 2 );

			
			// Compute Checksum on sector received - NOTE: no V1 version checksum
			lastChecksum = computeChecksum(lastSector, 256);
			
			
			
		} 
		catch (DWCommTimeOutException e1) 
		{
			// Timed out reading data from Coco
			logger.error("DoOP_WRITE: " + e1.getMessage());
			
			// reset, abort
			return;
		} 
		
		
		
		// Compare checksums 
		if (lastChecksum != long2(cocosum))
		{
			// checksums do not match, tell Coco
			comWrite1(DWERROR_CRC);
			
			logger.warn("DoOP_WRITE: Bad checksum, drive: " + lastDrive + " LSN: " + long3(lastLSN) + " CocoSum: " + long2(cocosum) + " ServerSum: " + lastChecksum);
			
			return;
		}

		// checksums match
		// comWrite1(DWOK);
		
		
		// do the write
		response = DWOK;
		
		try 
		{
			// Seek to LSN in DSK image 
			DWDiskDrives.seekSector(lastDrive,long3(lastLSN));
			// Write lastSector to DSK image 
			DWDiskDrives.writeSector(lastDrive,lastSector);
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
		comWrite1(response);
		
		// Increment sectorsWritten count
		if (response == DWOK)
			sectorsWritten++;
		
		if (opcode == OP_REWRITE)
		{
			writeRetries++;
			logger.info("DoOP_REWRITE lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
		}
		else
		{
			logger.info("DoOP_WRITE lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
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
		
		try 
		{
			// read rest of packet
			
			responsebuf = comRead(4);
			
			lastDrive = responsebuf[0];
			System.arraycopy( responsebuf, 1, lastLSN, 0, 3 );
					
			// seek to requested LSN
			DWDiskDrives.seekSector(lastDrive,long3(lastLSN));
			
			// load lastSector with bytes from file
			lastSector = DWDiskDrives.readSector(lastDrive);
				
		} 
		catch (DWDriveNotLoadedException e1) 
		{
			// zero sector
			lastSector = DWDiskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e1.getMessage());	
		} 
		catch (DWDriveNotValidException e2) 
		{
			// zero sector
			lastSector = DWDiskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e2.getMessage());
		} 
		catch (IOException e3) 
		{
			// zero sector
			lastSector = DWDiskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e3.getMessage());
		} 
		catch (DWCommTimeOutException e4) 
		{
			// timeout.. abort
			logger.error("DoOP_READEX, during read packet: " + e4.getMessage());
			return;
		}
				
		// write out response sector
		comWrite(lastSector, 256);
		
		try 
		{
			// get cocosum
			cocosum  = comRead(2);
		} 
		catch (DWCommTimeOutException e) 
		{
			// timeout.. abort
			logger.error("DoOP_READEX, during read cocosum: " + e.getMessage());
			
			return;
		}

		lastChecksum = computeChecksum(lastSector, 256);

		mysum[0] = (byte) ((lastChecksum >> 8) & 0xFF);
		mysum[1] = (byte) ((lastChecksum << 0) & 0xFF);

		if ((mysum[0] == cocosum[0]) && (mysum[1] == cocosum[1]))
		{
			// Good checksum, all is well
			sectorsRead++;
			comWrite1(DWOK);
		
			if (opcode == OP_REREADEX)
			{
				readRetries++;
				logger.info("DoOP_REREADEX lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
			}
			else
			{
				// logger.info("DoOP_READEX lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
			}
		}
		else
		{
			// checksum mismatch
			// sectorsRead++;  should we increment this?
			comWrite1(DWERROR_CRC);
			
			if (opcode == OP_REREADEX)
			{
				readRetries++;
				logger.info("DoOP_REREADEX CRC check failed, lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
			}
			else
			{
				logger.info("DoOP_READEX CRC check failed, lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
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
			responsebuf = comRead(2);
			
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
			logger.info("DoOP_GETSTAT lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
		}
		else
		{
			lastSetStat = responsebuf[1];
			logger.info("DoOP_SETSTAT lastDrive: " + (int) lastDrive + " LSN: " + long3(lastLSN));
		}
	}
	
	
	private void DoOP_TIME()
	{
		GregorianCalendar c = (GregorianCalendar) Calendar.getInstance();
		
		comWrite1(c.get(Calendar.YEAR)-108);
		comWrite1(c.get(Calendar.MONTH)+1);
		comWrite1(c.get(Calendar.DAY_OF_MONTH));
		comWrite1(c.get(Calendar.HOUR_OF_DAY));
		comWrite1(c.get(Calendar.MINUTE));
		comWrite1(c.get(Calendar.SECOND));
		// comWrite1(c.get(Calendar.DAY_OF_WEEK));
		
		logger.info("DoOP_TIME");
			
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
			responsebuf = comRead(2);
			if (responsebuf[1] != 1)
			{
				logger.info("DoOP_SER G ETSTAT for port " + responsebuf[0] + "(" + DWVSerialPorts.prettyPort(responsebuf[0]) + ") stat: " + responsebuf[1]);
			}
			
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading packet byte in SER G ETSTAT");
		}
	}
	
	private void DoOP_SERSETSTAT() 
	{
		byte[] responsebuf = new byte[2];
		
		
		try 
		{
			// get packet args
			// port # and stat
			responsebuf = comRead(2);
			logger.info("DoOP_SERSETSTAT for port " + DWVSerialPorts.prettyPort(responsebuf[0]) + " stat: " + responsebuf[1]);
			
			// if stat is $28, 26 bytes coming
			if (responsebuf[1] == 0x28)
			{
				byte[] devdescr = new byte[26];
				devdescr = comRead(26);
				
				// display it for now
				String tmpstr = byteArrayToHexString(devdescr);
				logger.debug("DoOP_SERSETSTAT DD: " + tmpstr);
				
				// set PD.INT offset 16 and PD.QUT offset 17
				if (DWVSerialPorts.getPD_INT(responsebuf[0]) != devdescr[16])
				{
					DWVSerialPorts.setPD_INT(responsebuf[0], devdescr[16]);
					logger.debug("Changed PD.INT to " + devdescr[16] + " on port " + DWVSerialPorts.prettyPort(responsebuf[0]));	
				}
				
				if (DWVSerialPorts.getPD_QUT(responsebuf[0]) != devdescr[17])
				{
					DWVSerialPorts.setPD_QUT(responsebuf[0], devdescr[17]);
					logger.debug("Changed PD.QUT to " + devdescr[17] + " on port " + DWVSerialPorts.prettyPort(responsebuf[0]));	
				}
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
			// port # and mode
			responsebuf = comRead(2);
			
			int portnum = responsebuf[0];
			int portmode = responsebuf[1];
			
			logger.info("DoOP_SERINIT for port " + DWVSerialPorts.prettyPort(portnum) + " mode " + portmode);
			
			if ((portnum >= 0) && (portnum <= DWVSerialPorts.MAX_PORTS))
			{
				// server side init if port isn't defined
				if (!DWVSerialPorts.isEnabled(portnum))
				{
					logger.debug("coco sent init for port we don't have defined.. no big deal");
					DWVSerialPorts.initPort(portnum, portmode);
				}
			

				if (!DWVSerialPorts.isCocoInit(portnum))
				{
					// record coco init, do any per instance setup					
					DWVSerialPorts.Cocoinit(portnum);
				}
				else
				{
					// check mode
					if (DWVSerialPorts.getMode(portnum) != portmode)
					{
						logger.warn("Coco sent init for port " + DWVSerialPorts.prettyPort(portnum) +" in different mode than we have it in.. reinitializing");
						
						DWVSerialPorts.Cocoterm(portnum);
						DWVSerialPorts.closePort(portnum);
						DWVSerialPorts.initPort(portnum, portmode);
						DWVSerialPorts.Cocoinit(portnum);
						
					}
					
				}
			}
			else
			{
				logger.warn("got init for invalid port " + portnum);
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
			portnum = comRead1(true);
			
			logger.info("DoOP_SERTERM for port " + portnum);
			
			if ((portnum >= 0) && (portnum <= DWVSerialPorts.MAX_PORTS))
			{
				// if port isn't defined
				if (!DWVSerialPorts.isEnabled(portnum))
				{
					logger.debug("coco sent TERM for port we don't have defined.. ignoring");
				}
				else if (!DWVSerialPorts.isCocoInit(portnum))
				{
					logger.debug("coco sent TERM for port we don't have inized.. ignoring");
				}
				else
				{
					// cocoterm
					DWVSerialPorts.Cocoterm(portnum);
				}
			}
			else
			{
				logger.warn("got term for invalid port " + portnum);
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
		
		comWrite(result, 2);
		
		//if (result[0] != 0)
		// logger.debug("DoOP_SERREAD response " + (int) (result[0] & 0xFF) + ":" + (int) (result[1] & 0xFF));
	}
	
	private void DoOP_SERWRITE() 
	{
		byte[] cmdpacket = new byte[2];
				
		try {
			cmdpacket = comRead(2);
			
			DWVSerialPorts.serWrite(cmdpacket[0],cmdpacket[1]);
				
			// logger.debug("DoOP_SERWRITE to port " + cmdpacket[0]);
			
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
			cmdpacket = comRead(2);
			
			try {
				logger.debug("DoOP_SERREADM for " +  (cmdpacket[1] & 0xFF) + " bytes on port " + cmdpacket[0] + " (" + DWVSerialPorts.getPortOutput(cmdpacket[0]).available() + " bytes remain)");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			data = DWVSerialPorts.serReadM((int) cmdpacket[0],(cmdpacket[1] & 0xFF));
			
			// logger.debug(new String(data));
			
			comWrite(data, (int) (cmdpacket[1] & 0xFF));
			
			
			
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
		
		// just throws the data away for now
		
		try 
		{
			tmpint = comRead1(true);
			logger.info("DoOP_PRINT: byte "+ tmpint);
		} 
		catch (DWCommTimeOutException e) 
		{
			logger.error("Timeout reading print byte");
		}
		
		
	}
	
	private void DoOP_PRINTFLUSH() 
	{
		logger.info("DoOP_PRINTFLUSH");
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


	
	public static long long3(byte[] data) 
	{
		 return((data[0] & 0xFF) << 16) + ((data[1] & 0xFF) << 8) + (data[2] & 0xFF);
	}

	
	public static long long2(byte[] data) 
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


	public static String getLastMessage() {
		return lastMessage;
	}


	public static byte[] getLastSector() {
		return lastSector;
	}


	public static SerialPort getSerialPort() {
		return serialPort;
	}
	
		
	
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

	
}
	

