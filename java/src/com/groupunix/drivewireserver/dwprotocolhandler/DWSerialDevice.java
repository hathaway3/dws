package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

import org.apache.log4j.Logger;

public class DWSerialDevice implements DWProtocolDevice
{
	private static final Logger logger = Logger.getLogger("DWServer.DWSerialDevice");
	
	private SerialPort serialPort;
	private boolean wanttodie = false;
	private boolean bytelog = false;
	private String device;
	private DWProtocol dwProto;
	private boolean DATurboMode = false; 
	private byte[] prefix;
	
	
	public DWSerialDevice(DWProtocol dwProto) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException
	{
		
		this.dwProto = dwProto;
		
		this.device = dwProto.getConfig().getString("SerialDevice");
		
		prefix = new byte[1];
		//prefix[0] = (byte) 0xFF;
		prefix[0] = (byte) 0xC0;

		
		
		bytelog = dwProto.getConfig().getBoolean("LogDeviceBytes", false);
		
		logger.debug("init " + device + " for handler #" + dwProto.getHandlerNo() + " (logging bytes: " + bytelog + ")");
		
		connect(device);
				
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
		logger.debug("closing serial device " +  this.device + " in handler #" + dwProto.getHandlerNo());
		this.serialPort.close();
	
	}

	
	public void shutdown()
	{
		
		this.wanttodie = true;
		
		try
		{
			logger.debug("close serial input stream");
			this.serialPort.getInputStream().close();
		} 
		catch (IOException e)
		{
			logger.warn(e.getMessage());
		}
		
		logger.debug("close serial port");
		this.serialPort.close();
		
	}

	private void connect(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException
	{
		logger.debug("attempting to open device '" + portName + "'");
		

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        
        
		if ( portIdentifier.isCurrentlyOwned() )
		{
			throw new PortInUseException();
		}
		else
		{
			CommPort commPort = portIdentifier.open("DriveWire",2000);
            
				if ( commPort instanceof SerialPort )
				{
            	
					serialPort = (SerialPort) commPort;

					// these settings seem to solve the lost bytes problems on my usb adapter
					// dedicating a thread to busy wait on the port works better than using the
					// event driven model... ok i guess
					serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
					serialPort.enableReceiveThreshold(1);
					serialPort.enableReceiveTimeout(3000);
                	
					
					setSerialParams(serialPort);               
                
					logger.info("opened serial device " + portName);
				}
				else
				{
					logger.error("The operating system says '" + portName +"' is not a serial port!");
					throw new NoSuchPortException();
				}
			}
    

	}
	
	
	private void setSerialParams(SerialPort sport) throws UnsupportedCommOperationException 
	{
		int rate;
		int parity = 0;
		int stopbits = 1;
		int databits = 8;
		
		rate = dwProto.getConfig().getInt("SerialRate", 115200);
		
		
		if (dwProto.getConfig().containsKey("SerialStopbits"))
		{
			String sb =  dwProto.getConfig().getString("SerialStopbits");
			
			if (sb.equals("1"))
				stopbits = SerialPort.STOPBITS_1;
			else if (sb.equals("1.5"))
				stopbits = SerialPort.STOPBITS_1_5;
			else if (sb.equals("2"))
				stopbits = SerialPort.STOPBITS_2;
			
		}
		
		if (dwProto.getConfig().containsKey("SerialParity"))
		{
			String p = dwProto.getConfig().getString("SerialParity");
			
			if (p.equals("none"))
				parity = SerialPort.PARITY_NONE;
			else if (p.equals("even"))
				parity = SerialPort.PARITY_EVEN;
			else if (p.equals("odd"))
				parity = SerialPort.PARITY_ODD;
			else if (p.equals("mark"))
				parity = SerialPort.PARITY_MARK;
			else if (p.equals("space"))
				parity = SerialPort.PARITY_SPACE;
			
					
		}
		
		logger.debug("setting port params to " + rate + " " + databits + ":" + parity + ":" + stopbits );
		sport.setSerialPortParams(rate, databits, stopbits, parity);
		
	}
	
	
	public int getRate()
	{
		return(this.serialPort.getBaudRate());
	}
	

	
	
	public void comWrite(byte[] data, int len, boolean pfix)
	{	
		try 
		{
			if (dwProto.getConfig().getBoolean("ProtocolFlipOutputBits", false)  || this.DATurboMode) 
				data = DWUtils.reverseByteArray(data);
				
			
			if (pfix && (dwProto.getConfig().getBoolean("ProtocolResponsePrefix", false)  || this.DATurboMode))
			{
				byte[] out = new byte[this.prefix.length + len];
				System.arraycopy(this.prefix, 0, out, 0, this.prefix.length);
				System.arraycopy(data, 0, out, this.prefix.length, len);
				
				serialPort.getOutputStream().write(out);
			}
			else
			{
				serialPort.getOutputStream().write(data, 0, len);
			}
			
			
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
	
	


	public void comWrite1(int data, boolean pfix)
	{
		
		
		try 
		{
			if (dwProto.getConfig().getBoolean("ProtocolFlipOutputBits", false) || this.DATurboMode) 
				data = DWUtils.reverseByte(data);
				
			
			if (pfix && (dwProto.getConfig().getBoolean("ProtocolResponsePrefix", false)  || this.DATurboMode))
			{
				byte[] out = new byte[this.prefix.length + 1];
				out[out.length - 1] = (byte)data;
				System.arraycopy(this.prefix, 0, out, 0, this.prefix.length);
				
				serialPort.getOutputStream().write(out);
			}
			else
			{
				serialPort.getOutputStream().write((byte) data);
			}
			
			if (bytelog)
				logger.debug("WRITE1: " + data);
			
		} 
		catch (IOException e) 
		{
			// problem with comm port, bail out
			logger.error(e.getMessage());
			
		}
	}
	
	
	
	public byte[] comRead(int len) throws IOException 
	{
	
		byte[] buf = new byte[len];
		
		//int sofar = 0;

		
		// we never got more than 1 byte at a time even at 230k, so
		// went back to 1 by one method
		
		/*
		while (sofar < len)
		{
			sofar += serialPort.getInputStream().read(buf, sofar, (len-sofar));
			
			if (bytelog)
				logger.debug("READ " + len + ": " + " got " + sofar);
		
			
		}
		*/
		
		
		for (int i = 0;i<len;i++)
		{
			buf[i] = (byte) comRead1(true);
		}
		
		
		return(buf);
	}
	
	
	
	public int comRead1(boolean timeout) throws IOException 
	{
		int retdata = -1;
		
	

		while ((retdata == -1) && (!this.wanttodie) && (serialPort != null))
		{
			retdata = serialPort.getInputStream().read();
		}
		
		if (bytelog)
			logger.debug("READ1: " + retdata);
		
		if (wanttodie)
		{
			//logger.debug("died while in read1");
			return(-1);
		}
		
		return(retdata);
	

	}


	@Override
	public String getDeviceName() 
	{
		return(this.serialPort.getName());
	}


	@Override
	public String getDeviceType() 
	{
		return("serial");
	}


	public void enableDATurbo() throws UnsupportedCommOperationException
	{
		this.serialPort.setSerialPortParams(230400, 8, 2, 0);
		this.DATurboMode = true;
	}
	
}
