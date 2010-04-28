package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.virtualprinter.DWVPrinter;
import com.groupunix.drivewireserver.virtualserial.DWVPortTermThread;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;



public class DWProtocolHandler implements Runnable
{

	private static final Logger logger = Logger.getLogger("DWServer.DWProtocolHandler");
	  

	
	// record keeping portion of dwTransferData
	private byte lastDrive = 0;
	private int readRetries = 0;
	private int writeRetries = 0;
	private int sectorsRead = 0;
	private int sectorsWritten = 0;
	private byte lastOpcode = DWDefs.OP_RESET1;
	private byte lastGetStat = (byte) 255;
	private byte lastSetStat = (byte) 255;
	private int lastChecksum = 0;
	private int lastError = 0;
	private byte[] lastLSN = new byte[3];
	
	private GregorianCalendar dwinitTime = new GregorianCalendar();
	
	// serial port instance
	
	private DWProtocolDevice protodev;
	
	// printer
	private DWVPrinter vprinter;
	
	// disk drives
	private DWDiskDrives diskDrives;
	
	private boolean wanttodie = false;
	// private static Thread readerthread;

	// RFM handler
	private DWRFMHandler rfmhandler;
	
	private int handlerno;
	public HierarchicalConfiguration config;
	private Thread termT;
	private DWVSerialPorts dwVSerialPorts;
	private DWVPortTermThread termHandler;
	
	// Event handler
	private DWProtocolEventHandler protocolEventHandler;
	
	
	public DWProtocolHandler(int handlerno, HierarchicalConfiguration hconf)
	{
		this.handlerno = handlerno;
		this.config = hconf;
		this.protocolEventHandler = new DWProtocolEventHandler(handlerno);
	}

	

	public void reset()
	{
		DoOP_RESET();
	}
	
	
	public boolean connected()
	{
		return(protodev.connected());
	}
	
	
	
	public void shutdown()
	{
		logger.info("handler #" + handlerno + ": shutdown requested");
		
		this.wanttodie = true;
		this.protodev.shutdown();
	}
	
	
	public void run()
	{
		int opcodeint = -1;
		
		Thread.currentThread().setName("dwproto-" + handlerno + "-" +  Thread.currentThread().getId());
		
		// this thread has got to run a LOT or we might lose bytes on the serial port
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		logger.info("handler #" + handlerno + ": starting...");

		
		// setup protocol device
		
		if (config.getString("DeviceType","serial").equalsIgnoreCase("serial") )
		{
		
			// create serial device
			if ((config.containsKey("SerialDevice") && config.containsKey("CocoModel")))		
			{
				try 
				{
					protodev = new DWSerialDevice(this.handlerno, config.getString("SerialDevice"), config.getInt("CocoModel"));
				}
				catch (NoSuchPortException e1)
				{
					wanttodie = true;
					logger.error("handler #"+handlerno+": Serial device '" + config.getString("SerialDevice") + "' not found");
				} 
				catch (PortInUseException e2)
				{
					wanttodie = true;
					logger.error("handler #"+handlerno+": Serial device '" + config.getString("SerialDevice") + "' in use");
				}
				catch (UnsupportedCommOperationException e3)
				{
					wanttodie = true;
					logger.error("handler #"+handlerno+": Unsupported comm operation while opening serial port '"+config.getString("SerialDevice")+"'");
				}
			}	
			else
			{
				logger.error("Serial mode requires both SerialDevice and CocoModel to be set");
				wanttodie = true;
			}
		}
		else if (config.getString("DeviceType").equalsIgnoreCase("tcp"))
		{
			// create TCP device
			if (config.containsKey("TCPDevicePort"))		
			{
				try 
				{
					protodev = new DWTCPDevice(this.handlerno, config.getInt("TCPDevicePort"));
				} 
				catch (IOException e) 
				{
					wanttodie = true;
					logger.error("handler #"+handlerno+": " + e.getMessage());
				}
			}	
			else
			{
				logger.error("TCP mode requires TCPDevicePort to be set");
				wanttodie = true;
			}
			
		}
		
		// if we've got a device, setup environment
		if (!wanttodie)
		{
			// setup drives
			diskDrives = new DWDiskDrives(this.handlerno);
				
			if (config.containsKey("DefaultDiskSet"))
			{
				diskDrives.LoadDiskSet(config.getString("DefaultDiskSet"));
			}
				
			// setup virtual ports
			this.dwVSerialPorts = new DWVSerialPorts(this.handlerno);	
			dwVSerialPorts.resetAllPorts();

			// setup printer
			vprinter = new DWVPrinter(handlerno);
				
			// setup RFM handler
			rfmhandler = new DWRFMHandler(handlerno);

			// setup term device
			if (config.containsKey("TermPort"))
			{
				logger.info("handler #" + handlerno + ": starting term device listener thread");
				this.termHandler = new DWVPortTermThread(this.handlerno, config.getInt("TermPort"));
				this.termT = new Thread(termHandler);
				this.termT.start();
			}
		}			

		// protocol loop
		while(!wanttodie)
		{ 
						
			// try to get an opcode
			try 
			{
					opcodeint = protodev.comRead1(false);
			} 
			catch (DWCommTimeOutException e) 
			{
				// this should not actually ever get thrown, since we call comRead1 with timeout = false..
				logger.error(e.getMessage());
				opcodeint = -1;
			}
						
					
			if (opcodeint > -1)
			{
				lastOpcode = (byte) opcodeint;
							
				switch(lastOpcode)
				{
					case DWDefs.OP_RESET1:
					case DWDefs.OP_RESET2:
					case DWDefs.OP_RESET3:
						DoOP_RESET();
						break;

					case DWDefs.OP_DWINIT:
						DoOP_DWINIT();
						break;
									
					case DWDefs.OP_INIT:
						DoOP_INIT();
						break;

					case DWDefs.OP_TERM:
						DoOP_TERM();	
						break;

					case DWDefs.OP_REREAD:
					case DWDefs.OP_READ:
						DoOP_READ(lastOpcode);
						break;

					case DWDefs.OP_REREADEX:
					case DWDefs.OP_READEX:
						DoOP_READEX(lastOpcode);
						break;

					case DWDefs.OP_WRITE:
					case DWDefs.OP_REWRITE:
						DoOP_WRITE(lastOpcode);
						break;


					case DWDefs.OP_GETSTAT:
					case DWDefs.OP_SETSTAT:
						DoOP_STAT(lastOpcode);
						break;

					case DWDefs.OP_TIME:
						DoOP_TIME();
						break;

					case DWDefs.OP_PRINT:
						DoOP_PRINT();
						break;

					case DWDefs.OP_PRINTFLUSH:
						DoOP_PRINTFLUSH();
						break;
							
					case DWDefs.OP_SERREADM:
						DoOP_SERREADM();
						break;

					case DWDefs.OP_SERREAD:
						DoOP_SERREAD();
						break;

					case DWDefs.OP_SERWRITE:
						DoOP_SERWRITE();
						break;

					case DWDefs.OP_SERSETSTAT:
						DoOP_SERSETSTAT();
						break;
							      
					case DWDefs.OP_SERGETSTAT:
						DoOP_SERGETSTAT();
						break;
			    
					case DWDefs.OP_SERINIT:
						DoOP_SERINIT();
						break;
							      
					case DWDefs.OP_SERTERM:
						DoOP_SERTERM();
						break;	
									
					case DWDefs.OP_NOP:
						DoOP_NOP();
						break;
								
					case DWDefs.OP_RFM:
						DoOP_RFM();
						break;
									
					default:
						logger.info("UNKNOWN OPCODE: " + opcodeint);
						break;
					
				}
				
			}
			else
			{
				logger.debug("neg opcode");
			}
			
		}

					
		logger.info("handler #"+ handlerno+ ": exiting");
		
		
		if (this.dwVSerialPorts != null)
		{
			this.dwVSerialPorts.shutdown();
		}
		
		if (this.diskDrives != null)
		{
			this.diskDrives.shutdown();
		}
		
		if (this.termT != null)
		{
			termHandler.shutdown();
			termT.interrupt();
		}
		
		if (protodev != null)
		{
			protodev.shutdown();
		}
			
	}

	
	
	
	
	// DW OP methods

	
	private void DoOP_DWINIT() 
	{
		int drv_version;
	
		if (config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_DWINIT");
		}
		
		try
		{
			drv_version = protodev.comRead1(true);
			
			// are we limited to dw3?
			if (!config.getBoolean("DW3Only", false))
			{
				// send response
				protodev.comWrite1(DWDefs.DW_PROTOCOL_VERSION);
			
				logger.debug("DWINIT sent proto ver " + DWDefs.DW_PROTOCOL_VERSION + ", got driver version " + drv_version);
			
				// driver version is not used for anything yet...
			
				// coco has just booted nos9
				dwinitTime = new GregorianCalendar();
			
				// reset all ports
				dwVSerialPorts.resetAllPorts();
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
		if (config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_NOP");
		}
	}
	
	
	private void DoOP_RFM() 
	{
		int rfm_op;
		
		
		try
		{
			rfm_op = protodev.comRead1(true);
			logger.info("DoOP_RFM call " + rfm_op );
			
			rfmhandler.DoRFMOP(protodev,rfm_op);
			
		} catch (DWCommTimeOutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	



	private void DoOP_TERM() 
	{
		if (config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_TERM");
		}
	}
	
	private void DoOP_INIT() 
	{
		if (config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_INIT");
		}
	}
	
	private void DoOP_RESET() 
	{
		// coco has been reset/turned on
		
		// reset stats
		
		lastDrive = 0;
		readRetries = 0;
		writeRetries = 0;
		sectorsRead = 0;
		sectorsWritten = 0;
		lastOpcode = DWDefs.OP_RESET1;
		lastGetStat = (byte) 255;
		lastSetStat = (byte) 255;
		lastChecksum = 0;
		lastError = 0;
		
		lastLSN = new byte[3];
		
		// Sync disks??
		
		// reset all ports?
		dwVSerialPorts.resetAllPorts();
	
		if (config.getBoolean("LogOpCode", false))
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
			responsebuf = protodev.comRead(262);

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
		if (lastChecksum != DWUtils.int2(cocosum))
		{
			// checksums do not match, tell Coco
			protodev.comWrite1(DWDefs.DWERROR_CRC);
			
			logger.warn("DoOP_WRITE: Bad checksum, drive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN) + " CocoSum: " + DWUtils.int2(cocosum) + " ServerSum: " + lastChecksum);
			
			return;
		}

				
		// do the write
		response = DWDefs.DWOK;
		
		try 
		{
			// Seek to LSN in DSK image 
			diskDrives.seekSector(lastDrive,DWUtils.int3(lastLSN));
			// Write sector to DSK image 
			diskDrives.writeSector(lastDrive,sector);
		} 
		catch (DWDriveNotLoadedException e1) 
		{
			// send drive not ready response
			response = DWDefs.DWERROR_NOTREADY;
			logger.warn(e1.getMessage());
		} 
		catch (DWDriveNotValidException e2) 
		{
			// basically the same as not ready
			response = DWDefs.DWERROR_NOTREADY;
			logger.warn(e2.getMessage());
		} 
		catch (DWDriveWriteProtectedException e3) 
		{
			// hopefully this is appropriate
			response = DWDefs.DWERROR_WP;
			logger.debug("DWWP EX");
			logger.warn(e3.getMessage());
		} 
		catch (IOException e4) 
		{
			// error on our end doing the write
			response = DWDefs.DWERROR_WRITE;
			logger.error(e4.getMessage());
		} 
		
		// record error
		if (response != DWDefs.DWOK)
			lastError = response;
		
		// send response
		protodev.comWrite1(response);
		
		// Increment sectorsWritten count
		if (response == DWDefs.DWOK)
			sectorsWritten++;
		
		if (opcode == DWDefs.OP_REWRITE)
		{
			writeRetries++;
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_REWRITE lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
		}
		else
		{
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_WRITE lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
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
			
			responsebuf = protodev.comRead(4);
			
			lastDrive = responsebuf[0];
			System.arraycopy( responsebuf, 1, lastLSN, 0, 3 );
					
			// seek to requested LSN
			diskDrives.seekSector(lastDrive, DWUtils.int3(lastLSN));
			
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
				
		// artificial delay test
		if (config.containsKey("ReadDelay"))
		{
			try 
			{
				logger.debug("read delay " + config.getLong("ReadDelay") + " ms...");
				Thread.sleep(config.getLong("ReadDelay"));
			} 
			catch (InterruptedException e) 
			{
				logger.warn("Interrupted during read delay");	
			}
		}
		
		// write out response sector
		protodev.comWrite(sector, 256);
		
		// calc checksum
		lastChecksum = computeChecksum(sector, 256);

		mysum[0] = (byte) ((lastChecksum >> 8) & 0xFF);
		mysum[1] = (byte) ((lastChecksum << 0) & 0xFF);
		
		// logger.debug("looking for checksum " + mysum[0] + ":" + mysum[1]);
		
		try 
		{
			// get cocosum
			cocosum  = protodev.comRead(2);
		} 
		catch (DWCommTimeOutException e) 
		{
			// timeout.. abort
			logger.error("DoOP_READEX, during read cocosum: " + e.getMessage());
			
			return;
		}

		

		if ((mysum[0] == cocosum[0]) && (mysum[1] == cocosum[1]))
		{
			// Good checksum, all is well
			sectorsRead++;
			protodev.comWrite1(DWDefs.DWOK);
		
			if (opcode == DWDefs.OP_REREADEX)
			{
				readRetries++;
				logger.warn("DoOP_REREADEX lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
			else
			{
				if (config.getBoolean("LogOpCode", false))
				{
					logger.info("DoOP_READEX lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
				}
			}
		}
		else
		{
			// checksum mismatch
			// sectorsRead++;  should we increment this?
			protodev.comWrite1(DWDefs.DWERROR_CRC);
			
			if (opcode == DWDefs.OP_REREADEX)
			{
				readRetries++;
				logger.warn("DoOP_REREADEX CRC check failed, lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
			else
			{
				logger.warn("DoOP_READEX CRC check failed, lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
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
			responsebuf = protodev.comRead(2);
			
			lastDrive = responsebuf[0];
			
		} 
		catch (DWCommTimeOutException e1) 
		{
			// timeout.. abort
			logger.error("DoOP_GET/SETSTAT, during read packet: " + e1.getMessage());
			return;
		} 

		if (opcode == DWDefs.OP_GETSTAT)
		{
			lastGetStat = responsebuf[1];
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_GETSTAT: " + DWUtils.prettySS(responsebuf[1]) + " lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
		}
		else
		{
			lastSetStat = responsebuf[1];
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SETSTAT " + DWUtils.prettySS(responsebuf[1]) + " lastDrive: " + (int) lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
		}
	}
	
	
	private void DoOP_TIME()
	{
		GregorianCalendar c = (GregorianCalendar) Calendar.getInstance();
		
		protodev.comWrite1(c.get(Calendar.YEAR)-108);
		protodev.comWrite1(c.get(Calendar.MONTH)+1);
		protodev.comWrite1(c.get(Calendar.DAY_OF_MONTH));
		protodev.comWrite1(c.get(Calendar.HOUR_OF_DAY));
		protodev.comWrite1(c.get(Calendar.MINUTE));
		protodev.comWrite1(c.get(Calendar.SECOND));
		
		if (config.getBoolean("OpTimeSendsDOW", false))
		{
			protodev.comWrite1(c.get(Calendar.DAY_OF_WEEK));
		}
		
		
		if (config.getBoolean("LogOpCode", false))
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
			responsebuf = protodev.comRead(2);
			if (responsebuf[1] != 1)
			{
				if (config.getBoolean("LogOpCode", false))
				{
					logger.info("DoOP_SERGETSTAT: " + DWUtils.prettySS(responsebuf[1]) + " port: " + responsebuf[0] + "(" + dwVSerialPorts.prettyPort(responsebuf[0]) + ")");
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
			responsebuf = protodev.comRead(2);
			
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SERSETSTAT: " + DWUtils.prettySS(responsebuf[1]) + " port: " + responsebuf[0] + "(" + dwVSerialPorts.prettyPort(responsebuf[0]) + ")");
			}
			
			if (!dwVSerialPorts.isValid(responsebuf[0]))
			{
				logger.debug("Invalid port '" + responsebuf[0] + "' in sersetstat, ignored.");
				return;
			}
			
			switch(responsebuf[1])
			{
				// SS.ComSt
				case 0x28:
					byte[] devdescr = new byte[26];
					devdescr = protodev.comRead(26);
					
					// logger.debug("COMST: " + byteArrayToHexString(devdescr));
					
					// should move into DWVSerialPorts
					
					// store it
					dwVSerialPorts.setDD(responsebuf[0],devdescr);
					
					// set PD.INT offset 16 and PD.QUT offset 17
					if (dwVSerialPorts.getPD_INT(responsebuf[0]) != devdescr[16])
					{
						dwVSerialPorts.setPD_INT(responsebuf[0], devdescr[16]);
						// logger.debug("Changed PD.INT to " + devdescr[16] + " on port " + DWVSerialPorts.prettyPort(responsebuf[0]));	
					}
					
					if (dwVSerialPorts.getPD_QUT(responsebuf[0]) != devdescr[17])
					{
						dwVSerialPorts.setPD_QUT(responsebuf[0], devdescr[17]);
						// logger.debug("Changed PD.QUT to " + devdescr[17] + " on port " + DWVSerialPorts.prettyPort(responsebuf[0]));	
					}
					
					break;
					
				// SS.Open	
				case 0x29:
					dwVSerialPorts.openPort(responsebuf[0]);
					break;
					
				//SS.Close
				case 0x2A:
					dwVSerialPorts.closePort(responsebuf[0]);
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
			responsebuf = protodev.comRead(1);
			
			int portnum = responsebuf[0];
			// int portmode = responsebuf[1];
			
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SERINIT for port " + dwVSerialPorts.prettyPort(portnum));
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
			portnum = protodev.comRead1(true);
		
			if (config.getBoolean("LogOpCode", false))
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
		
		result = dwVSerialPorts.serRead();
		
		protodev.comWrite(result, 2);
		
		//if (result[0] != 0)
		if (config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_SERREAD response " + (int) (result[0] & 0xFF) + ":" + (int) (result[1] & 0xFF));
		}
	}
	
	private void DoOP_SERWRITE() 
	{
		byte[] cmdpacket = new byte[2];
				
		try {
			cmdpacket = protodev.comRead(2);
			
			dwVSerialPorts.serWrite(cmdpacket[0],cmdpacket[1]);
			
			if (config.getBoolean("LogOpCode", false))
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
			cmdpacket = protodev.comRead(2);
			
			if (config.getBoolean("LogOpCode", false))
			{
				try {
				logger.debug("DoOP_SERREADM for " +  (cmdpacket[1] & 0xFF) + " bytes on port " + cmdpacket[0] + " (" + dwVSerialPorts.getPortOutput(cmdpacket[0]).available() + " bytes in buffer)");
				} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}
			
			data = dwVSerialPorts.serReadM((int) cmdpacket[0],(cmdpacket[1] & 0xFF));
			
			// logger.debug(new String(data));
			
			protodev.comWrite(data, (int) (cmdpacket[1] & 0xFF));
			
			
			
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
			tmpint = protodev.comRead1(true);
			
			if (config.getBoolean("LogOpCode", false))
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
		if (config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_PRINTFLUSH");
		}
		
		vprinter.flush();
	}
	
	
	
	
		
	
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



	public byte getLastDrive() {
		return lastDrive;
	}


	public int getReadRetries() {
		return readRetries;
	}


	public int getWriteRetries() {
		return writeRetries;
	}


	public int getSectorsRead() {
		return sectorsRead;
	}


	public int getSectorsWritten() {
		return sectorsWritten;
	}


	public byte getLastOpcode() {
		return lastOpcode;
	}


	public byte getLastGetStat() {
		return lastGetStat;
	}


	public byte getLastSetStat() {
		return lastSetStat;
	}


	public int getLastChecksum() {
		return lastChecksum;
	}


	public int getLastError() {
		return lastError;
	}


	public byte[] getLastLSN() {
		return lastLSN;
	}
		

	
	public GregorianCalendar getDWInitTime()
	{
		return(dwinitTime);
	}

	
	

	public DWDiskDrives getDiskDrives() {
		return this.diskDrives;
	}
	
	public DWVSerialPorts getVPorts() {
		return this.dwVSerialPorts;
	}




	public boolean isDying()
	{
		return this.wanttodie;
	}



	public DWProtocolEventHandler getEventHandler() {
		// TODO Auto-generated method stub
		return this.protocolEventHandler;
	}
	
	
}
	

