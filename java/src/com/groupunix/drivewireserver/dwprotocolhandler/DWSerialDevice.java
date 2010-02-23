package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class DWSerialDevice
{
	private static final Logger logger = Logger.getLogger("DWServer.DWSerialDevice");
	
	private SerialPort serialPort;

	
	DWSerialDevice()
	{
		logger.debug("init");
		
		if (DriveWireServer.config.containsKey("SerialDevice"))
		{
			setPort(DriveWireServer.config.getString("SerialDevice"));
		}
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

	
	public int setPort(String portname)
	{
		if (serialPort != null)
		{
			// close current port
			logger.info("closing port " + serialPort.getName());
			serialPort.close();
		}

		try 
		{
			connect(portname);
		} 
		//catch (IOException e1) 
		//{
		//	logger.warn(e1.getMessage());
		//	return(-1);
		//} 
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


	public void close()
	{
		logger.info("closing serial device");
		this.serialPort.close();
	}


	public void connect(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException
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
	
	
	
	public void comWrite(byte[] data, int len)
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
			
		}
	}	
	
	
	public void comWrite1(int data)
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
	
}
