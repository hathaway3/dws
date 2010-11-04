package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

public class DWSerialDevice implements DWProtocolDevice
{
	private static final Logger logger = Logger.getLogger("DWServer.DWSerialDevice");
	
	private SerialPort serialPort;
	private boolean wanttodie = false;
	private int handlerno;
	private boolean bytelog = false;
	private String device;
	
	public DWSerialDevice(int handlerno, String device, int cocomodel) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException
	{
		this.device = device;
		this.handlerno = handlerno;
		
		bytelog = DriveWireServer.getHandler(this.handlerno).config.getBoolean("LogDeviceBytes", false);
		
		logger.debug("init " + device + " for handler #" + handlerno + " (logging bytes: " + bytelog + ")");
		
		connect(device, cocomodel);
				
	}
	
	
	public boolean connected()
	{
		if (this.serialPort != null)
		{
			return(true);
		}
		else
		{
			return(false);
		}
	}

	

	public void close()
	{
		logger.info("closing serial device " +  this.device + " in handler #" + this.handlerno);
		this.serialPort.close();
	}

	
	public void shutdown()
	{
		logger.debug("shutting down");
		this.wanttodie = true;
		
		try
		{
			this.serialPort.getInputStream().close();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.serialPort.close();
		
	}

	private void connect(String portName, int cocomodel) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException
	{
		logger.info("attempting to open device '" + portName + "'");
		
		logger.info("Note: RXTX Version mismatch here is not a problem...");
		
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
                
                setSerialParams(serialPort, cocomodel);               
                  
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
	
	
	private void setSerialParams(SerialPort sport, int cocomodel) throws UnsupportedCommOperationException 
	{
		int rate;
		
		if (DriveWireServer.getHandler(this.handlerno).config.containsKey("RateOverride"))
		{
			rate = DriveWireServer.getHandler(this.handlerno).config.getInt("RateOverride");
		}
		else
		{
			switch(cocomodel)
			{
				case 1:
					rate = 38400;
					break;
				case 2:
					rate = 57600;
					break;
				default:
					rate = 115200;
			}
		}
		
		logger.debug("setting port params to " + rate +" 8N1" );
		sport.setSerialPortParams(rate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		
		
	}
	
	
	public int getRate()
	{
		return(this.serialPort.getBaudRate());
	}
	

	
	public void comWrite(byte[] data, int len)
	{	
		try 
		{
			serialPort.getOutputStream().write(data, 0, len);
			
			// extreme cases only
			
			if (bytelog)
			{
				String tmps = new String();
				
				for (int i = 0;i< data.length;i++)
				{
					tmps += " " + (int)(data[i] & 0xFF);
				}
				
				logger.debug("WRITE " + data.length + ":" + tmps);
				
			}
			
		} 
		catch (IOException e) 
		{
			// problem with comm port, bail out
			logger.error(e.getMessage());
			
		}
	}	
	
	
	public void comWrite1(int data)
	{
		try 
		{
			serialPort.getOutputStream().write((byte) data);
			
			if (bytelog)
				logger.debug("WRITE1: " + data);
			
		} 
		catch (IOException e) 
		{
			// problem with comm port, bail out
			logger.error(e.getMessage());
			
		}
	}
	
	
	
	public byte[] comRead(int len) throws DWCommTimeOutException 
	{
	
		byte[] buf = new byte[len];
		
		for (int i = 0;i<len;i++)
		{
			buf[i] = (byte) comRead1(true);
		}
		
		return(buf);
	}
	
	
	
	public int comRead1(boolean timeout) throws DWCommTimeOutException 
	{
		int retdata = -1;
		
		try {

			while ((retdata == -1) && (!this.wanttodie))
			{
				retdata = serialPort.getInputStream().read();
			}
			
			if (bytelog)
				logger.debug("READ1: " + retdata);
			
			if (wanttodie)
			{
				logger.debug("died while in read1");
				return(-1);
			}
			
			return(retdata);
		} 
		catch (IOException e) 
		{
			logger.error("error reading byte, I want to die: " + e.getMessage());
			this.wanttodie = true;
			return(-1);
		}
		

	}
	
}
