package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;

public class DWSerialDevice implements DWProtocolDevice
{
	private static final Logger logger = Logger.getLogger("DWServer.DWSerialDevice");
	
	private SerialPort serialPort = null;

	private boolean bytelog = false;
	private String device;
	private DWProtocol dwProto;
	private boolean DATurboMode = false; 
	private boolean xorinput = false;
	
	private byte[] prefix;
	private long readtime;

	private ArrayBlockingQueue<Byte> queue;

	private DWSerialReader evtlistener;

	
	public DWSerialDevice(DWProtocol dwProto) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException
	{
		
		this.dwProto = dwProto;
		
		this.device = dwProto.getConfig().getString("SerialDevice");
		
		prefix = new byte[1];
		//prefix[0] = (byte) 0xFF;
		prefix[0] = (byte) 0xC0;

		xorinput = dwProto.getConfig().getBoolean("ProtocolXORInputBits", false);
			
		bytelog = dwProto.getConfig().getBoolean("LogDeviceBytes", false);
		
		logger.debug("init " + device + " for handler #" + dwProto.getHandlerNo() + " (logging bytes: " + bytelog + "  xorinput: " + xorinput +")");
		
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
		
		if (this.serialPort != null)
		{
			logger.debug("closing serial device " +  device + " in handler #" + dwProto.getHandlerNo());
			
			serialPort.notifyOnDataAvailable(false);
			
			if (this.evtlistener != null)
			{
				this.evtlistener.shutdown();
				serialPort.removeEventListener();
			}
			
			serialPort.close();
			serialPort = null;
			
		}

		  
		  /*
		
		TimeLimiter service = new SimpleTimeLimiter();
		
	
		try
		{
			service.callWithTimeout(
			        new Callable<Boolean>() 
			        {
			          @Override
			          public Boolean call() throws InterruptedException, IOException 
			          {
			        	  if (serialPort != null)
			        	  {
			        		  logger.debug("closing serial device " +  device + " in handler #" + dwProto.getHandlerNo());
			        		  
			        		  //serialPort.getOutputStream().close();
			        		  serialPort.getInputStream().close();
			        		  
			        		  //serialPort.notifyOnDataAvailable(false);
			        		  //serialPort.removeEventListener();
			        		  
			        		  //evtlistener = null;
			        		  
			        		  serialPort.close();
			        		  
			        		  serialPort = null;
			        		  
			        		  return true;
			        	  }
			        	  return false;
			          }
			          
			        }, 4000, TimeUnit.MILLISECONDS, true);
			
		
		}
		
		catch (Exception e)
		{
			//System.out.println("Serial port trouble: " + e.getMessage());
			logger.warn("While closing serial port: " + e.getMessage());
		}
		
		
		*/
		  
	}

	
	public void shutdown()
	{
		this.close();
				
	}

	
	public void reconnect() throws UnsupportedCommOperationException, IOException, TooManyListenersException
	{
		if (this.serialPort != null)
		{
			setSerialParams(serialPort);               
        	
			if (this.evtlistener != null)
			{
				this.serialPort.removeEventListener();
			}
			
			this.queue = new ArrayBlockingQueue<Byte>(512);
			
			this.evtlistener = new DWSerialReader(serialPort.getInputStream(), queue);
			
			serialPort.addEventListener(this.evtlistener);
            serialPort.notifyOnDataAvailable(true);
		}
	}
	
	private void connect(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException
	{
		logger.debug("attempting to open device '" + portName + "'");
		

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        
        
		//if ( portIdentifier.isCurrentlyOwned() )
	//	{
	//		throw new PortInUseException();
	//	}
	//	else
		{
			CommPort commPort = portIdentifier.open("DriveWire",2000);
            
				if ( commPort instanceof SerialPort )
				{
            	
					serialPort = (SerialPort) commPort;

					reconnect();
					
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
		
		serialPort.enableReceiveThreshold(1);
		
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
		
		int flow = SerialPort.FLOWCONTROL_NONE;
		
		if (dwProto.getConfig().getBoolean("SerialFlowControl_RTSCTS_IN", false))
			flow = flow | SerialPort.FLOWCONTROL_RTSCTS_IN;
		
		if (dwProto.getConfig().getBoolean("SerialFlowControl_RTSCTS_OUT", false))
			flow = flow | SerialPort.FLOWCONTROL_RTSCTS_OUT;
		
		if (dwProto.getConfig().getBoolean("SerialFlowControl_XONXOFF_IN", false))
			flow = flow | SerialPort.FLOWCONTROL_XONXOFF_IN;
		
		if (dwProto.getConfig().getBoolean("SerialFlowControl_XONXOFF_OUT", false))
			flow = flow | SerialPort.FLOWCONTROL_XONXOFF_OUT;
		
		serialPort.setFlowControlMode(flow);
		
		logger.debug("setting port params to " + rate + " " + databits + ":" + parity + ":" + stopbits );
		sport.setSerialPortParams(rate, databits, stopbits, parity);
		
		if (dwProto.getConfig().containsKey("SerialDTR"))
		{
			sport.setDTR(dwProto.getConfig().getBoolean("SerialDTR", false));
			logger.debug("setting port DTR to " + dwProto.getConfig().getBoolean("SerialDTR", false));
		}
		
		if (dwProto.getConfig().containsKey("SerialRTS"))
		{
			sport.setRTS(dwProto.getConfig().getBoolean("SerialRTS", false));
			logger.debug("setting port RTS to " + dwProto.getConfig().getBoolean("SerialRTS", false));
		}
		
		
	}
	
	
	public int getRate()
	{
		if (this.serialPort != null)
			return(this.serialPort.getBaudRate());
		return -1;
	}
	

	
	
	public void comWrite(byte[] data, int len, boolean pfix)
	{	
		try 
		{
			if (dwProto.getConfig().getBoolean("ProtocolFlipOutputBits", false)  || this.DATurboMode) 
				data = DWUtils.reverseByteArray(data);
				
			
			if (this.dwProto.getConfig().containsKey("WriteByteDelay"))
			{
				for (int i = 0;i< len;i++)
				{
					comWrite1(data[i],pfix);
				}
			}
			else
			{
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
					
					for (int i = 0;i< len;i++)
					{
						tmps += " " + (int)(data[i] & 0xFF);
					}
					
					logger.debug("WRITE " + len + ":" + tmps);
					
				}
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
				
			if (this.dwProto.getConfig().containsKey("WriteByteDelay"))
			{
				try
				{
					Thread.sleep(this.dwProto.getConfig().getLong("WriteByteDelay"));
				} 
				catch (InterruptedException e)
				{
					logger.warn("interrupted during writebytedelay");
				}
			}
			
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
				logger.debug("WRITE1: " + (0xFF & data));
			
		} 
		catch (IOException e) 
		{
			// problem with comm port, bail out
			logger.error(e.getMessage());
			
		}
	}
	
	
	
	public byte[] comRead(int len) throws IOException, DWCommTimeOutException 
	{
		byte[] buf = new byte[len];
		
		for (int i = 0;i<len;i++)
		{
			buf[i] = (byte) comRead1(true, false);
		}
		
		if (this.bytelog)
		{
			String tmps = new String();
			
			for (int i = 0;i< buf.length;i++)
			{
				tmps += " " + (int)(buf[i] & 0xFF);
			}
		
			logger.debug("READ " + len + ": " + tmps);
		}
		
		return(buf);
	}
	
	
	public int comRead1(boolean timeout) throws IOException, DWCommTimeOutException 
	{
		return comRead1(timeout, true);
	}
	
	
	public int comRead1(boolean timeout, boolean blog) throws IOException, DWCommTimeOutException 
	{
		
		int res = -1;
		
		try
		{
			while ((res == -1) && (this.serialPort != null)) 
			{
				long starttime = System.currentTimeMillis();
				Byte read = queue.poll(200, TimeUnit.MILLISECONDS);
				this.readtime += System.currentTimeMillis() - starttime;
				
				if (read != null)
					res = 0xFF & read;
				else if (timeout)
				{
					throw (new DWCommTimeOutException("No data in 200 ms"));
				}
				
			}
		} 
		catch (InterruptedException e)
		{
			logger.debug("interrupted in serial read");
		}
		
		if (this.xorinput )
			res = res ^ 0xFF;
		
		if (blog && this.bytelog)
			logger.debug("READ1: " + res);
		
		return res;
	}


	@Override
	public String getDeviceName() 
	{
		if (this.serialPort != null)
			return(this.serialPort.getName());
		
		return null;
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
	
	public long getReadtime()
	{
		return this.readtime;
	}
	
	public void resetReadtime()
	{
		this.readtime = 0;
	}


	public SerialPort getSerialPort()
	{
		return this.serialPort;
	}


	@Override
	public String getClient() 
	{
		return null;
	}
}
