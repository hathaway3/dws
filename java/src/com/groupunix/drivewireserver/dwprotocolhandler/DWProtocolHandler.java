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
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwdisk.DWDiskDrives;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotOpenException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;
import com.groupunix.drivewireserver.dwhelp.DWHelp;
import com.groupunix.drivewireserver.virtualprinter.DWVPrinter;
import com.groupunix.drivewireserver.virtualserial.DWVPortTermThread;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;



public class DWProtocolHandler implements Runnable, DWProtocol
{

	private final Logger logger = Logger.getLogger("DWServer.DWProtocolHandler");

	
	// record keeping portion of dwTransferData
	private int lastDrive = 0;
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
	private long total_ops = 0;
	private long disk_ops = 0;
	private long vserial_ops = 0;
	
	private GregorianCalendar dwinitTime = new GregorianCalendar();
	
	// serial port instance
	
	private DWProtocolDevice protodev;
	
	// printer
	private DWVPrinter vprinter;
	
	// disk drives
	private DWDiskDrives diskDrives;
	
	private boolean wanttodie = false;

	// RFM handler
	private DWRFMHandler rfmhandler;
	
	private int handlerno;
	private HierarchicalConfiguration config;
	private Thread termT;
	private DWVSerialPorts dwVSerialPorts;
	private DWVPortTermThread termHandler;


	private DWHelp dwhelp;


	private boolean ready = false;


	private boolean resetPending = false;
	
	

	
	public DWProtocolHandler(int handlerno, HierarchicalConfiguration hconf)
	{
		this.handlerno = handlerno;
		this.config = hconf;
		
		config.addConfigurationListener(new DWProtocolConfigListener(this));   
		
	}

	
	public HierarchicalConfiguration getConfig()
	{
		return(this.config);
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
		logger.debug("handler #" + handlerno + ": shutdown requested");
		
		this.wanttodie = true;
		
		if (this.protodev != null)
			this.protodev.shutdown();
	}
	
	
	public void run()
	{
		int opcodeint = -1;
		
		Thread.currentThread().setName("dwproto-" + handlerno + "-" +  Thread.currentThread().getId());
		
		// this thread should run a LOT or we might lose bytes on the serial port on slow computers
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		setupProtocolDevice();
		
		// setup environment and get started
		if (!wanttodie)
		{
			logger.debug("handler #" + handlerno + ": setting up...");

			// setup drives
			diskDrives = new DWDiskDrives(this);
			
			// setup virtual ports
			this.dwVSerialPorts = new DWVSerialPorts(this);	
			dwVSerialPorts.resetAllPorts();

			// setup printer
			vprinter = new DWVPrinter(this);
				
			// setup RFM handler
			rfmhandler = new DWRFMHandler(handlerno);

			// setup term device
			if (config.containsKey("TermPort"))
			{
				logger.debug("handler #" + handlerno + ": starting term device listener thread");
				this.termHandler = new DWVPortTermThread(this, config.getInt("TermPort"));
				this.termT = new Thread(termHandler);
				this.termT.setDaemon(true);
				this.termT.start();
			}
			
			// load help
	    	if (config.containsKey("HelpFile"))
	    	{
	    		this.dwhelp = new DWHelp(config.getString("HelpFile"));
	    	}
	    	else
	    	{
	    		this.dwhelp = new DWHelp(this);
	    	}
			
		}			

		this.ready = true;
		logger.info("handler #" + handlerno + " is ready");
		
		// protocol loop
		while(!wanttodie)
		{ 
			opcodeint = -1;
			
			// try to get an opcode
			if (!(protodev == null) && !resetPending)
			{
				try 
				{
					opcodeint = protodev.comRead1(false);
				} 
				catch (IOException e) 
				{
					// this should not actually ever get thrown, since we call comRead1 with timeout = false..
					logger.error("Strange result in proto read loop: "  + e.getMessage());
				}
			}
				
			if (opcodeint > -1)
			{
				lastOpcode = (byte) opcodeint;
				total_ops++;
				
				// fast writes
				if ((lastOpcode >= DWDefs.OP_FASTWRITE_BASE) && (lastOpcode <= (DWDefs.OP_FASTWRITE_BASE + DWVSerialPorts.MAX_COCO_PORTS - 1)))
				{
					DoOP_FASTSERWRITE(lastOpcode);
					vserial_ops++;
				}
				else
				{
					// regular OP decode

					
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
							disk_ops++;
							break;

						case DWDefs.OP_REREADEX:
						case DWDefs.OP_READEX:
							DoOP_READEX(lastOpcode);
							disk_ops++;
							break;

						case DWDefs.OP_WRITE:
						case DWDefs.OP_REWRITE:
							DoOP_WRITE(lastOpcode);
							disk_ops++;
							break;

						case DWDefs.OP_GETSTAT:
						case DWDefs.OP_SETSTAT:
							DoOP_STAT(lastOpcode);
							disk_ops++;
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
							vserial_ops++;
							break;

						case DWDefs.OP_SERREAD:
							DoOP_SERREAD();
							vserial_ops++;
							break;

						case DWDefs.OP_SERWRITE:
							DoOP_SERWRITE();
							vserial_ops++;
							break;

						case DWDefs.OP_SERSETSTAT:
							DoOP_SERSETSTAT();
							vserial_ops++;
							break;
							      
						case DWDefs.OP_SERGETSTAT:
							DoOP_SERGETSTAT();
							vserial_ops++;
							break;
			    
						case DWDefs.OP_SERINIT:
							DoOP_SERINIT();
							vserial_ops++;
							break;
							      
						case DWDefs.OP_SERTERM:
							DoOP_SERTERM();
							vserial_ops++;
							break;	
									
						case DWDefs.OP_NOP:
						case DWDefs.OP_230K230K:
							DoOP_NOP();
							break;
								
						case DWDefs.OP_RFM:
							DoOP_RFM();
							break;
								
						case DWDefs.OP_230K115K:
							DoOP_230K115K();
							break;
						
						case DWDefs.OP_NAMEOBJ_MOUNT:
							DoOP_NAMEOBJ_MOUNT();
							break;
							
							
						default:
							logger.warn("UNKNOWN OPCODE: " + opcodeint);
							break;
					}	
				}
	
				
			}
			else
			{
				if (!this.wanttodie)
				{
					if (this.resetPending)
						logger.debug("device is resetting...");
					else if (!config.getString("DeviceType","").equals("dummy"))
						logger.debug("device unavailable, will retry in " + config.getInt("DeviceFailRetryTime",6000) + "ms");
				
					// take a break, reset, hope things work themselves out
					try 
					{
						
						Thread.sleep(config.getInt("DeviceFailRetryTime",6000));
						
						setupProtocolDevice();
						
					} 
					catch (InterruptedException e) 
					{	
						logger.error("Interrupted during failed port delay.. giving up on this crazy situation");
						wanttodie = true;
					}
				}
			}

		}
 
			
		logger.debug("handler #"+ handlerno+ ": exiting");
		
		
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

	
	private void DoOP_NAMEOBJ_MOUNT()
	{
		long starttime = System.currentTimeMillis();
		
		try
		{
			int namesize = protodev.comRead1(true);
			byte[] namebuf = new byte[namesize];
			namebuf = protodev.comRead(namesize);
			String objname = new String(namebuf);
			
			
			//int result = diskDrives.nameObjMount(objname);
			int result = -1;
			
			// artificial delay test
			if (config.containsKey("NameObjMountDelay"))
			{
				try 
				{
					logger.debug("named object mount delay " + config.getLong("NameObjMountDelay") + " ms...");
					Thread.sleep(config.getLong("NameObjMountDelay"));
				} 
				catch (InterruptedException e) 
				{
					logger.warn("Interrupted during mount delay");	
				}
			}
			
			protodev.comWrite1(result, false);
			
			if (config.getBoolean("LogOpCode", false))
			{
				long delay = System.currentTimeMillis() - starttime;
				logger.info("DoOP_NAMEOBJ_MOUNT for '" + objname  + "' result: " + result + ", call took " + delay + "ms");
			}
			
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
	}
	
	
	private void DoOP_230K115K() 
	{
		if (config.getBoolean("DetectDATurbo", false))
		{
			try 
			{
				((DWSerialDevice) protodev).enableDATurbo();
				logger.info("Detected switch to 230k mode");
			} 
			catch (UnsupportedCommOperationException e) 
			{
				logger.error("comm port did not make the switch to 230k mode: " + e.getMessage());
				logger.error("bail out!");
				this.wanttodie = true;
			}
		}
	}


	private void DoOP_FASTSERWRITE(byte opcode) 
	{
		int databyte;
		int port = opcode - DWDefs.OP_FASTWRITE_BASE;
		
		try {
			databyte = protodev.comRead1(false);
			
			dwVSerialPorts.serWrite(port,databyte);
			
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_FASTSERWRITE to port " + port + ": " + databyte);
			}
			
		} 
		catch (IOException e) 
		{
			logger.error("Timeout reading FASTSERWRITE data byte: " + e.getMessage());
		} 
		catch (DWPortNotOpenException e1) 
		{
			logger.error(e1.getMessage());
		} 
		catch (DWPortNotValidException e2)
		{
			logger.error(e2.getMessage());
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
				protodev.comWrite1(DWDefs.DW_PROTOCOL_VERSION, true);
			
				logger.debug("DWINIT sent proto ver " + DWDefs.DW_PROTOCOL_VERSION + ", got driver version " + drv_version);
			
				// driver version is not used for anything yet...
			
				// coco has just booted nos9
				dwinitTime = new GregorianCalendar();
			
				// reset all ports
				dwVSerialPorts.resetAllPorts();
			}
			else
			{
				logger.debug("DWINIT received, ignoring due to DW3Only setting");
			}
		} 
		catch (IOException e)
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
			
		} catch (IOException e)
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
		
		// reset all ports
		dwVSerialPorts.resetAllPorts();
	
		if (config.getBoolean("LogOpCode", false))
		{
			logger.info("DoOP_RESET");
		}
		
	}
	

	
	private void DoOP_WRITE(byte opcode)
	{
		byte[] cocosum = new byte[2];
		byte[] responsebuf = new byte[getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE) + 6];
		byte response = 0;
		byte[] sector = new byte[getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)];
		
		try 
		{
			// read rest of packet
			responsebuf = protodev.comRead(getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE) + 6);

			lastDrive = responsebuf[0] & 0xff;
			System.arraycopy( responsebuf, 1, lastLSN, 0, 3 );
			System.arraycopy( responsebuf, 4, sector, 0, getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE) );
			System.arraycopy( responsebuf, getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE) + 4, cocosum, 0, 2 );

			
			// Compute Checksum on sector received - NOTE: no V1 version checksum
			lastChecksum = computeChecksum(sector, getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE));
			
			
			
		} 
		catch (IOException e1) 
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
			protodev.comWrite1(DWDefs.DWERROR_CRC, true);
			
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
			logger.warn(e3.getMessage());
		} 
		catch (IOException e4) 
		{
			// error on our end doing the write
			response = DWDefs.DWERROR_WRITE;
			logger.error(e4.getMessage());
		} 
		catch (DWInvalidSectorException e5) 
		{
			response = DWDefs.DWERROR_WRITE;
			logger.warn(e5.getMessage());
		} 
		catch (DWSeekPastEndOfDeviceException e6) 
		{
			response = DWDefs.DWERROR_WRITE;
			logger.warn(e6.getMessage());
		} 
		
		// record error
		if (response != DWDefs.DWOK)
			lastError = response;
		
		// send response
		protodev.comWrite1(response, true);
		
		// Increment sectorsWritten count
		if (response == DWDefs.DWOK)
			sectorsWritten++;
		
		if (opcode == DWDefs.OP_REWRITE)
		{
			writeRetries++;
			if (config.getBoolean("LogOpCode", false))
			{
				logger.warn("DoOP_REWRITE lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
		}
		else
		{
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_WRITE lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
		}
		
		return;
	}
	
	
	
	
	private void DoOP_READ(int opcode)
	{
		byte[] mysum = new byte[2];
		byte[] responsebuf = new byte[4];
		byte[] sector = new byte[getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)];
		byte result = DWDefs.DWOK; 
		
		try 
		{
			// read rest of packet - drive # and 3 byte LSN
			responsebuf = protodev.comRead(4);
			
			// store that..
			lastDrive = responsebuf[0] & 0xff;
			System.arraycopy( responsebuf, 1, lastLSN, 0, 3 );
					
			// attempt seek to requested LSN (will throw one of the many exceptions caught below if it cannot)
			diskDrives.seekSector(lastDrive, DWUtils.int3(lastLSN));
			
			// we didn't throw an exception in our seek, so load a buffer with the sector's data
			sector = diskDrives.readSector(lastDrive);
		
			
		// deal with all kinds of things that could have gone wrong as we did the seek and read above...	
		} 
		catch (DWDriveNotLoadedException e1) 
		{
			logger.warn("DoOP_READ: " + e1.getMessage());
			result = DWDefs.DWERROR_NOTREADY;
		} 
		catch (DWDriveNotValidException e2) 
		{
			logger.warn("DoOP_READ: " + e2.getMessage());
			result = DWDefs.DWERROR_NOTREADY;
		} 
		catch (IOException e3) 
		{
			logger.warn("DoOP_READ: " + e3.getMessage());
			result = DWDefs.DWERROR_READ;
		} 
		catch (DWInvalidSectorException e5) 
		{
			logger.error("DoOP_READ: " + e5.getMessage());
			result = DWDefs.DWERROR_READ;
		} 
		catch (DWSeekPastEndOfDeviceException e6) 
		{
			logger.error("DoOP_READ: " + e6.getMessage());
			result = DWDefs.DWERROR_READ;
		} 
		catch (DWImageFormatException e7)
		{
			logger.error("DoOP_READ: " + e7.getMessage());
			result = DWDefs.DWERROR_READ;
		} 

		// send ultimate result to coco in a response byte
		
		protodev.comWrite1(result, true);
		
		
		if (result == DWDefs.DWOK)
		{
			// if our response was OK, next we send the sector data
			
			// write out response sector
			protodev.comWrite(sector, getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE), true);
			
			// calc a checksum
			lastChecksum = computeChecksum(sector, getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE));

			mysum[0] = (byte) ((lastChecksum >> 8) & 0xFF);
			mysum[1] = (byte) ((lastChecksum << 0) & 0xFF);
			
			// send checksum to coco
			protodev.comWrite(mysum, 2, true);
			
			// we're done.. do housekeeping stuff
			sectorsRead++;
	
			if (opcode == DWDefs.OP_REREAD)
			{
				readRetries++;
				logger.warn("DoOP_REREAD lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
			else
			{
				if (config.getBoolean("LogOpCode", false))
				{
					logger.info("DoOP_READ lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
				}
			}
			
		}
		
	}

	
	
	private void DoOP_READEX(int opcode)
	{
		byte[] cocosum = new byte[2];
		byte[] mysum = new byte[2];
		byte[] responsebuf = new byte[4];
		byte[] sector = new byte[getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE)];
		byte result = DWDefs.DWOK; 
		
		try 
		{
			// read rest of packet
			
			responsebuf = protodev.comRead(4);
			
			lastDrive = responsebuf[0] & 0xff;
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
			result = DWDefs.DWERROR_NOTREADY;
		} 
		catch (DWDriveNotValidException e2) 
		{
			// zero sector
			sector = diskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e2.getMessage());
			result = DWDefs.DWERROR_NOTREADY;
		} 
		catch (IOException e3) 
		{
			// zero sector
			sector = diskDrives.nullSector();
			logger.warn("DoOP_READEX: " + e3.getMessage());
			result = DWDefs.DWERROR_READ;
		} 
		catch (DWInvalidSectorException e5) 
		{
			sector = diskDrives.nullSector();
			logger.error("DoOP_READEX: " + e5.getMessage());
			result = DWDefs.DWERROR_READ;
		} 
		catch (DWSeekPastEndOfDeviceException e6) 
		{
			sector = diskDrives.nullSector();
			logger.error("DoOP_READEX: " + e6.getMessage());
			result = DWDefs.DWERROR_READ;
		} 
		catch (DWImageFormatException e7)
		{
			sector = diskDrives.nullSector();
			logger.error("DoOP_READEX: " + e7.getMessage());
			result = DWDefs.DWERROR_READ;
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
		protodev.comWrite(sector, getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE), true);
		
		if (!config.getBoolean("ProtocolDisableReadChecksum",false))
		{
			// calc checksum
			lastChecksum = computeChecksum(sector, getConfig().getInt("DiskSectorSize", DWDefs.DISK_SECTORSIZE));

			mysum[0] = (byte) ((lastChecksum >> 8) & 0xFF);
			mysum[1] = (byte) ((lastChecksum << 0) & 0xFF);
		
			// 	logger.debug("looking for checksum " + mysum[0] + ":" + mysum[1]);
		
			try 
			{
				// get cocosum
				cocosum  = protodev.comRead(2);
			} 
			catch (IOException e) 
			{
				// timeout.. abort
				logger.error("DoOP_READEX, during read cocosum: " + e.getMessage());
			
				return;
			}

			if (((mysum[0] == cocosum[0]) && (mysum[1] == cocosum[1])) || config.getBoolean("ProtocolLieAboutCRC",false))
			{
				// Good checksum, all is well
				sectorsRead++;
		
				if (opcode == DWDefs.OP_REREADEX)
				{
					readRetries++;
					logger.warn("DoOP_REREADEX lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
				}
				else
				{
					if (config.getBoolean("LogOpCode", false))
					{
						logger.info("DoOP_READEX lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
					}
				}
			}
			else
			{
				// checksum mismatch
				// 	sectorsRead++;  should we increment this?

				result = DWDefs.DWERROR_CRC;
			
				if (opcode == DWDefs.OP_REREADEX)
				{
					readRetries++;
					logger.warn("DoOP_REREADEX CRC check failed, lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
				}
				else
				{
					logger.warn("DoOP_READEX CRC check failed, lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
					
					try 
					{
						this.diskDrives.getDisk(lastDrive).incParam("_read_errors");
					} 
					catch (DWDriveNotLoadedException e) 
					{
						logger.warn(e.getMessage());
					} 
					catch (DWDriveNotValidException e) 
					{
						logger.warn(e.getMessage());
					}
					
					
				}
			
			}
			
		}
		// send result byte
		protodev.comWrite1(result, true);

	}

	
	private void DoOP_STAT(byte opcode)
	{
		byte[] responsebuf = new byte[2];
		
		try 
		{
			// get packet args
			// drive # and stat
			responsebuf = protodev.comRead(2);
			
			lastDrive = responsebuf[0] & 0xff;
			
		} 
		catch (IOException e1) 
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
				logger.info("DoOP_GETSTAT: " + DWUtils.prettySS(responsebuf[1]) + " lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
		}
		else
		{
			lastSetStat = responsebuf[1];
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SETSTAT " + DWUtils.prettySS(responsebuf[1]) + " lastDrive: " + lastDrive + " LSN: " + DWUtils.int3(lastLSN));
			}
		}
	}
	
	
	private void DoOP_TIME()
	{
		GregorianCalendar c = (GregorianCalendar) Calendar.getInstance();
		byte[] buf = new byte[7];
		
		buf[0] = (byte) (c.get(Calendar.YEAR)-108);
		buf[1] = (byte) (c.get(Calendar.MONTH)+1);
		buf[2] = (byte) c.get(Calendar.DAY_OF_MONTH);
		buf[3] = (byte) c.get(Calendar.HOUR_OF_DAY);
		buf[4] = (byte) c.get(Calendar.MINUTE);
		buf[5] = (byte) c.get(Calendar.SECOND);
		buf[6] = (byte) c.get(Calendar.DAY_OF_WEEK);
		
		
		if (config.getBoolean("OpTimeSendsDOW", false))
		{
			protodev.comWrite(buf, 7, true);
		}
		else
		{
			protodev.comWrite(buf, 6, true);
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
		catch (IOException e) 
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
		catch (IOException e) 
		{
			logger.error("Timeout reading packet byte in SERSETSTAT");
		} 
		catch (DWPortNotValidException e) 
		{
			logger.error(e.getMessage());
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
			
			
			if (config.getBoolean("LogOpCode", false))
			{
				logger.info("DoOP_SERINIT for port " + dwVSerialPorts.prettyPort(portnum));
			}
	
			
			
		} 
		catch (IOException e) 
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
		catch (IOException e) 
		{
			logger.error("Timeout reading packet byte in SERTERM");
		}
		
	}
	
	
	
	private void DoOP_SERREAD() 
	{
		byte[] result = new byte[2];
		
		result = dwVSerialPorts.serRead();
		
		protodev.comWrite(result, 2, true);
		
		//if (result[0] != 0)
		if (config.getBoolean("LogOpCodePolls", false))
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
		catch (IOException e) 
		{
			logger.error("Timeout reading SERWRITE packet: " + e.getMessage());
		} 
		catch (DWPortNotOpenException e1) 
		{
			logger.error(e1.getMessage());
		} 
		catch (DWPortNotValidException e2) 
		{
			logger.error(e2.getMessage());
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
				try 
				{
					logger.info("DoOP_SERREADM for " +  (cmdpacket[1] & 0xFF) + " bytes on port " + cmdpacket[0] + " (" + dwVSerialPorts.getPortOutput(cmdpacket[0]).available() + " bytes in buffer)");
				} 
				catch (IOException e) 
				{
					logger.warn("While logging: " + e.getMessage());
				}
			}
			
			data = dwVSerialPorts.serReadM((int) cmdpacket[0],(cmdpacket[1] & 0xFF));
			
			protodev.comWrite(data, (int) (cmdpacket[1] & 0xFF), true);
	
		} 
		catch (IOException e) 
		{
			logger.error("Timeout reading SERREADM packet: " + e.getMessage());
		} 
		catch (DWPortNotOpenException e1) 
		{
			logger.error(e1.getMessage());
		} 
		catch (DWPortNotValidException e2) 
		{
			logger.error(e2.getMessage());
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
		catch (IOException e) 
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



	public int getLastDrive() {
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
		

	
	public GregorianCalendar getInitTime()
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


	
	public DWProtocolDevice getProtoDev()
	{
		return(this.protodev);
	}




	
	public void resetProtocolDevice()
	{
		if (!this.wanttodie)
		{
			logger.info("requesting protocol device reset");
			
			// flag that we want a reset
			this.resetPending = true;
			
			// kill device
			if (protodev != null)
				this.protodev.shutdown();
			
		}
	}
	
	
	private void setupProtocolDevice()
	{
		
		if ((protodev != null) && (!resetPending))
			protodev.shutdown();
		
		
		
		
		if (config.getString("DeviceType","dummy").equalsIgnoreCase("dummy") )
		{
			this.resetPending = false;
		}
		else if (config.getString("DeviceType").equalsIgnoreCase("serial") )
		{
		
			// create serial device
			if ((config.containsKey("SerialDevice") && config.containsKey("SerialRate")))		
			{
				try 
				{
					protodev = new DWSerialDevice(this);
					this.resetPending = false;
				}
				catch (NoSuchPortException e1)
				{
					//wanttodie = true; lets keep on living and see how that goes
					logger.error("handler #"+handlerno+": Serial device '" + config.getString("SerialDevice") + "' not found");
				} 
				catch (PortInUseException e2)
				{
					//wanttodie = true;
					logger.error("handler #"+handlerno+": Serial device '" + config.getString("SerialDevice") + "' in use");
				}
				catch (UnsupportedCommOperationException e3)
				{
					//wanttodie = true;
					logger.error("handler #"+handlerno+": Unsupported comm operation while opening serial port '"+config.getString("SerialDevice")+"'");
				}
			}	
			else
			{
				logger.error("Serial mode requires both SerialDevice and SerialRate to be set, please configure this instance.");
				//wanttodie = true;
			}
		}
		else if (config.getString("DeviceType").equalsIgnoreCase("tcp") || config.getString("DeviceType").equalsIgnoreCase("tcp-server"))
		{
			// create TCP device
			if (config.containsKey("TCPServerPort"))		
			{
				try 
				{
					protodev = new DWTCPDevice(this.handlerno, config.getInt("TCPServerPort"));
				} 
				catch (IOException e) 
				{
					//wanttodie = true;
					logger.error("handler #"+handlerno+": " + e.getMessage());
				}
			}	
			else
			{
				logger.error("TCP server mode requires TCPServerPort to be set, cannot use this configuration");
				//wanttodie = true;
			}
			
		}
		else if (config.getString("DeviceType").equalsIgnoreCase("tcp-client"))
		{
			// create TCP device
			if (config.containsKey("TCPClientPort") && config.containsKey("TCPClientHost"))		
			{
				try 
				{
					protodev = new DWTCPClientDevice(this.handlerno, config.getString("TCPClientHost"), config.getInt("TCPClientPort"));
				} 
				catch (IOException e) 
				{
					//wanttodie = true;
					logger.error("handler #"+handlerno+": " + e.getMessage());
				}
			}	
			else
			{
				logger.error("TCP client mode requires TCPClientPort and TCPClientHost to be set, cannot use this configuration");
				//wanttodie = true;
			}
			
		}	
	}


	@Override
	public String getStatusText() 
	{
		String text = new String();
		
		text += "Last OpCode:   " + DWUtils.prettyOP(getLastOpcode()) + "\r\n";
		text += "Last GetStat:  " + DWUtils.prettySS(getLastGetStat()) + "\r\n";
		text += "Last SetStat:  " + DWUtils.prettySS(getLastSetStat()) + "\r\n";
		text += "Last Drive:    " + getLastDrive() + "\r\n";
		text += "Last LSN:      " + DWUtils.int3(getLastLSN()) + "\r\n";
		text += "Last Error:    " + ((int) getLastError() & 0xFF) + "\r\n";
	
		text += "\r\n";
		
		// TODO:  include read and write retries per disk
		// text += "Total Read Sectors:  " + String.format("%6d",DriveWireServer.getHandler(handlerno).getSectorsRead()) + "  (" + DriveWireServer.getHandler(handlerno).getReadRetries() + " retries)\r\n";
		//text += "Total Write Sectors: " + String.format("%6d",DriveWireServer.getHandler(handlerno).getSectorsWritten()) + "  (" + DriveWireServer.getHandler(handlerno).getWriteRetries() + " retries)\r\n";
	
		
		
		return(text);
	}




	public String getName()
	{
		return (this.config.getString("[@name]","Unnamed #" + this.handlerno));
			
	}
	
	public int getHandlerNo()
	{
		return this.handlerno;
	}


	@Override
	public void syncStorage() 
	{
		if (this.diskDrives != null)
		{
			this.diskDrives.sync();
		}
		else
		{
			logger.debug("handler is alive, but disk drive object is null, probably startup taking a while.. skipping");
		}
		
	}
	
	public DWVPrinter getVPrinter()
	{
		return(this.vprinter);
	}
	
	public Logger getLogger()
	{
		return this.logger;
	}


	public int getCMDCols() 
	{
		return getConfig().getInt("DWCommandOutputWidth",DWDefs.DWCMD_DEFAULT_COLS);
	}


	public DWHelp getHelp() 
	{
		return dwhelp;
	}


	@Override
	public boolean isReady() 
	{
		return this.ready ;
	}


	@Override
	public void submitConfigEvent(String key, String val)
	{
		DriveWireServer.submitInstanceConfigEvent(this.handlerno, key, val);
	}


	@Override
	public long getNumOps()
	{
		// TODO Auto-generated method stub
		return this.total_ops;
	}


	@Override
	public long getNumDiskOps()
	{
		// TODO Auto-generated method stub
		return this.disk_ops;
	}


	@Override
	public long getNumVSerialOps()
	{
		// TODO Auto-generated method stub
		return this.vserial_ops;
	}


	
	
}
	

