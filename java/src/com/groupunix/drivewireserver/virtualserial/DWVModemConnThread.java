package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWVModemConnThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVModemConnThread");
	
	private Socket skt; 
	private String clientHost = "none";
	private int clientPort = -1;
	private int vport = -1;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	
	 /**
	    * The Telnet command code IAC (255)
	    */
	   
	public static final int IAC = 255;

	   /**
	    * The Telnet command code SE (240)
	    */
	   
	public static final int SE = 240;

	   /**
	    * The Telnet command code NOP (241)
	    */
	public static final int NOP = 241;

	   /**
	    * The Telnet command code DM (242)
	    */
	   
	public static final int DM = 242;

	   /**
	    * The Telnet command code BREAK (243)
	    */
	   
	public static final int BREAK = 243;

	   /**
	    * The Telnet command code IP (244)
	    */
	   
	public static final int IP = 244;

	   /**
	    * The Telnet command code AO (245)
	    */
	   
	public static final int AO = 245;

	   /**
	    * The Telnet command code IAC (246)
	    */
	   
	public static final int AYT = 246;

	   /**
	    * The Telnet command code EC (247)
	    */
	   
	public static final int EC = 247;

	   /**
	    * The Telnet command code EL (248)
	    */
	   
	public static final int EL = 248;

	   /**
	    * The Telnet command code GA (249)
	    */
	   
	public static final int GA = 249;

	   /**
	    * The Telnet command code SB (250)
	    */
	   
	public static final int SB = 250;

	   /**
	    * The Telnet command code WILL (251)
	    */
	   
	public static final int WILL = 251;

	   /**
	    * The Telnet command code WONT (252)
	    */
	   
	public static final int WONT = 252;

	   /**
	    * The Telnet command code DO (253)
	    */
	   
	public static final int DO = 253;

	   /**
	    * The Telnet command code DONT (254)
	    */
	   
	public static final int DONT = 254;
	
	
	
	
	public DWVModemConnThread(int handlerno, int vport, String host, int tcpport) 
	{
		this.vport = vport;
 		this.clientHost = host;
		this.clientPort = tcpport;
		this.handlerno = handlerno;
		this.dwVSerialPorts = ((DWProtocolHandler) DriveWireServer.getHandler(this.handlerno)).getVPorts();
		
	}

	public void run() 
	{
		Thread.currentThread().setName("mdmconn-" + Thread.currentThread().getId());
		logger.debug("thread run for connection to " + this.clientHost + ":" + clientPort);
		
		try 
		{
			
			skt = new Socket(clientHost,clientPort);
			
			dwVSerialPorts.markConnected(vport);
			dwVSerialPorts.setUtilMode(vport, DWDefs.UTILMODE_VMODEMOUT);
			dwVSerialPorts.setPortOutput(vport, skt.getOutputStream());
				
			//int lastbyte = 0;
				
			int telmode = 0;
			
			dwVSerialPorts.getPortInput(vport).write("CONNECT\r\n".getBytes());
			
			while (skt.isConnected())
			{
				int data = skt.getInputStream().read();
				
				if (DriveWireServer.getHandler(this.handlerno).getConfig().getBoolean("LogVPortBytes"))
					logger.debug("VMODEM to CoCo: " + data + "  (" + (char)data + ")");
				
				if (data >= 0)
				{
					// telnet stuff
					if (telmode == 1)
					{
						switch(data)
						{
						  	case SE:
						  	case NOP:
						  	case DM:
						  	case BREAK:
						  	case IP:
						  	case AO:
						  	case AYT:
						  	case EC:
						  	case EL:
						  	case GA:
						  	case SB:
						  		
						  		break;
						  		
						  	case WILL:
						  		data = skt.getInputStream().read();
					        	skt.getOutputStream().write(255);
					        	skt.getOutputStream().write(DONT);
					        	skt.getOutputStream().write(data);
					        	break;
					        	
					        case WONT:
					        case DONT:
					        	data = skt.getInputStream().read();
					        	break;
					        	
					        case DO:
					        	data = skt.getInputStream().read();
					        	skt.getOutputStream().write(255);
					        	skt.getOutputStream().write(WONT);
					        	skt.getOutputStream().write(data);
					        	break;
						
					        	
						}
						telmode  = 0;
					}
					switch(data)
					{
						case IAC:
							telmode = 1;
							break;
						
						default:
							// write it to the serial port
							dwVSerialPorts.getPortInput(vport).write((byte) data);
			         
			        	   
					}
					
					
				
				}
				else
				{
					logger.info("end of stream from TCP client at " + this.clientHost + ":" + this.clientPort);
					if (skt.isConnected())
					{
						logger.debug("closing socket");
						skt.close();
					}
						
				}
			}
			
		} 
		catch (IOException e) 
		{
			logger.warn("IO error in connection to " + this.clientHost + ":" + this.clientPort + " = " + e.getMessage());
		}
		catch (DWPortNotValidException e)
		{
			logger.warn(e.getMessage());
		} 
		finally
		{
			if (this.vport > -1)
			{
				dwVSerialPorts.markDisconnected(this.vport);
				// TODO: this is all wrong
				try 
				{
					dwVSerialPorts.getPortInput(vport).write("\r\n\r\nNO CARRIER\r\n".getBytes());
				} 
				catch (IOException e) 
				{
					logger.warn(e.getMessage());
				} 
				catch (DWPortNotValidException e)
				{
					logger.warn(e.getMessage());
				}
			}
			logger.debug("thread exiting");
		}
	}

}
