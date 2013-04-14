package com.groupunix.drivewireserver.dwprotocolhandler.vmodem;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TooManyListenersException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwhelp.DWHelp;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolDevice;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolTimers;
import com.groupunix.drivewireserver.dwprotocolhandler.DWSerialDevice;
import com.groupunix.drivewireserver.dwprotocolhandler.DWVSerialProtocol;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

public class VModemProtocolHandler implements Runnable, DWVSerialProtocol
{

	private final Logger logger = Logger.getLogger("DWServer.VModemProtocolHandler");
	
	private DWProtocolDevice protodev = null;
	
	private boolean started = false;
	private boolean ready = false;
	private boolean connected = false;
	private GregorianCalendar inittime = new GregorianCalendar();


	private int handlerno;
	private HierarchicalConfiguration config;


	private boolean wanttodie = false;

	private DWProtocolTimers timers = new DWProtocolTimers();

	private DWHelp dwhelp = new DWHelp(this);
	private DWVSerialPorts vSerialPorts;
	

	
	public VModemProtocolHandler(int handlerno, HierarchicalConfiguration hconf )
	{
		this.handlerno = handlerno;
		this.config = hconf;
		this.vSerialPorts = new DWVSerialPorts(this);
		this.vSerialPorts.resetAllPorts();
		
		if (config.containsKey("HelpFile"))
    	{
    		this.dwhelp = new DWHelp(config.getString("HelpFile"));
    	}
    	
		
	}
	

	
	@Override
	public void run() 
	{
		int readbyte = -1;
		
		logger.info("VModemHandler #" + this.handlerno + " starting");
		this.started = true;
		
		this.timers.resetTimer(DWDefs.TIMER_START);
		
		if (this.protodev == null)
			setupProtocolDevice();
		
		
		this.ready = true;
		
		logger.debug("handler #" + handlerno + " is ready");
		
		
		
		while (!wanttodie)
		{
	
			if (protodev != null)
			{
				try 
				{
					readbyte = protodev.comRead1(false);
				}				
				catch (IOException e) 
				{
					logger.error("Strange result in proto read loop: "  + e.getMessage());
				} 
				catch (DWCommTimeOutException e)
				{
					logger.error("Timeout in proto read loop: "  + e.getMessage());
				}
			}
				
			
			if ((readbyte > -1) && (this.protodev != null))
			{
				// take input byte
				try 
				{
					this.vSerialPorts.getPortInput(0).write(readbyte);
				} 
				catch (IOException e) 
				{
					logger.error(e.getMessage());
				} 
				catch (DWPortNotValidException e) 
				{
					logger.error(e.getMessage());
				}
			
			}
			
		}
		
		
		
		logger.debug("handler #" + handlerno + " is exiting");
		this.vSerialPorts.shutdown();
	}
	
	
	
	
	private void setupProtocolDevice()
	{
		
		if ((protodev != null))
			protodev.shutdown();
		
		// create serial device
		if ((config.containsKey("SerialDevice") && config.containsKey("SerialRate")))		
		{
			try 
			{
				protodev = new DWSerialDevice(this);
			}
			catch (NoSuchPortException e1)
			{
				logger.error("handler #"+handlerno+": Serial device '" + config.getString("SerialDevice") + "' not found");
			} 
			catch (PortInUseException e2)
			{
				logger.error("handler #"+handlerno+": Serial device '" + config.getString("SerialDevice") + "' in use");
			}
			catch (UnsupportedCommOperationException e3)
			{
				logger.error("handler #"+handlerno+": Unsupported comm operation while opening serial port '"+config.getString("SerialDevice")+"'");
			} 
			catch (IOException e)
			{
				logger.error("handler #"+handlerno+": IO exception while opening serial port '"+config.getString("SerialDevice")+"'");
			} 
			catch (TooManyListenersException e)
			{
				logger.error("handler #"+handlerno+": Too many listeneres while opening serial port '"+config.getString("SerialDevice")+"'");
			}
			
		}	
		else
		{
			logger.error("VModem requires both SerialDevice and SerialRate to be set, please configure this instance.");
		}
			
	}



	@Override
	public void shutdown() 
	{
		logger.debug("vmodem handler #" + handlerno + ": shutdown requested");
		
		this.wanttodie  = true;
		
		if (this.protodev != null)
			this.protodev.shutdown();
	}

	@Override
	public boolean isDying() 
	{
		return wanttodie;
	}

	@Override
	public boolean isStarted() 
	{
		return this.started ;
	}

	@Override
	public boolean isReady() 
	{
		return this.ready ;
	}

	@Override
	public HierarchicalConfiguration getConfig() 
	{
		return this.config;
	}

	@Override
	public DWProtocolDevice getProtoDev() 
	{
		return this.protodev;
	}

	@Override
	public GregorianCalendar getInitTime() 
	{
		return this.inittime;
	}

	@Override
	public String getStatusText() 
	{
		return "VModem status TODO";
	}

	@Override
	public void resetProtocolDevice() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncStorage() 
	{
		// noop
		
	}

	@Override
	public int getHandlerNo() 
	{
		return this.handlerno;
	}

	@Override
	public Logger getLogger() 
	{
		return this.logger;
	}

	@Override
	public int getCMDCols() 
	{
		return 0;
	}

	@Override
	public DWHelp getHelp() 
	{
		return this.dwhelp ;
	}

	@Override
	public void submitConfigEvent(String propertyName, String string) 
	{
		// noop
	}

	@Override
	public long getNumOps() 
	{
		return 0;
	}

	@Override
	public long getNumDiskOps() 
	{
		return 0;
	}

	@Override
	public long getNumVSerialOps() 
	{
		return 0;
	}

	@Override
	public DWProtocolTimers getTimers() 
	{
		return this.timers;
	}



	@Override
	public boolean isConnected() 
	{
		return this.connected ;
	}


	@Override
	public boolean hasPrinters() 
	{
		
		return false;
	}
	
	@Override
	public boolean hasDisks() {
		
		return false;
	}

	@Override
	public boolean hasMIDI() {
		
		return false;
	}

	@Override
	public boolean hasVSerial() {
		
		return true;
	}



	@Override
	public DWVSerialPorts getVPorts() 
	{
		return this.vSerialPorts;
	}
}
