package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;



public class DWVModem {

	private static final Logger logger = Logger.getLogger("DWServer.DWVModem");
	
	private int vport;
	
	// modem state
	private boolean vmodem_echo = false;
	private boolean vmodem_verbose = true;
	private boolean vmodem_quiet = false;
	private int[] vmodem_registers = new int[256];
	private String vmodem_lastcommand = new String();
	private String vmodem_dialstring = new String();
	
	// modem constants
	private static final int RESP_OK = 0;
	private static final int RESP_CONNECT = 1;
	private static final int RESP_RING = 2;
	private static final int RESP_NOCARRIER = 3;
	private static final int RESP_ERROR = 4;
	private static final int RESP_NODIALTONE = 6;
	private static final int RESP_BUSY = 7;
	private static final int RESP_NOANSWER = 8;
	
	// registers we use
	private static final int REG_ANSWERONRING = 0;
	private static final int REG_RINGS = 1;
	private static final int REG_ESCCHAR = 2;
	private static final int REG_CR = 3;
	private static final int REG_LF = 4;
	private static final int REG_BS = 5;
	private static final int REG_GUARDTIME = 12;
	
	private Thread tcpthread;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	
	
	public DWVModem(DWProtocolHandler dwProto, int port) 
	{
		this.vport = port;
		this.dwVSerialPorts = dwProto.getVPorts();
		// logger.debug("new vmodem for port " + port);
		
		doCommandReset();
	}

	
	public void processCommand(String cmd) 
	{	
		
		int errors = 0;
		
		// hitting enter on a blank line is ok
		if (cmd.length() == 0)
		{
			return;
		}
		
		// A/ repeats last command
		if (cmd.equalsIgnoreCase("A/"))
		{
			cmd = this.vmodem_lastcommand;
		}
		else
		{
			this.vmodem_lastcommand = cmd;
		}
		
		// must start with AT
		if (cmd.toUpperCase().startsWith("AT"))
		{
			// AT by itself is OK
			if (cmd.length() == 2)
			{
				sendResponse(RESP_OK);
				return;
			}
			
			// process the string looking for commands
			
			
			boolean registers = false;
			boolean extended = false;
			boolean extendedpart = false;
			boolean dialing = false;
			
			String thiscmd = new String();
			String thisarg = new String();
			String thisreg = new String();
			
			for (int i = 2;i<cmd.length();i++)
			{
				extendedpart = false;
				
				if (dialing)
				{
					thisarg += cmd.substring(i, i+1);
				}
				else
				{
					switch(cmd.toUpperCase().charAt(i))
					{
						// commands
						case 'E':
						case '&':
						case 'Q':
						case 'Z':
						case 'I':
						case 'S':
						case 'B':
						case 'L':
						case 'M':
						case 'N':
						case 'X':
						case 'V':
						case 'F':
						case 'D':
							
							// handle extended mode
							if (extended)
							{
								switch(cmd.toUpperCase().charAt(i))
								{
									case 'V':
									case 'F':
										extendedpart = true;
										break;
								}
							}
							
							if (cmd.toUpperCase().charAt(i) == '&')
								extended = true;
							
							if (cmd.toUpperCase().charAt(i) == 'D')
								dialing = true;
													
							if (extendedpart)
							{
								thiscmd += cmd.substring(i, i+1);
							}
							else
							{
								// terminate existing command if any
								if (!(thiscmd.length() == 0))
								{
									errors += doCommand(thiscmd, thisreg, thisarg);
								}
							
								// set up for new command
								thiscmd = cmd.substring(i, i+1);
								thisarg = new String();
								thisreg = new String();
								
								
								// registers
								if (thiscmd.equalsIgnoreCase("S"))
									registers = true;
								else
									registers = false;
								
							}
							break;

						// assignment
						case '=':
							registers = false;
							break;
							
						// query
						case '?':
							thisarg = "?";
							break;
							
						// ignored
						case ' ':	
							break;
							
						// digits	
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							if (registers)
							{
								thisreg += cmd.substring(i, i+1);
							}
							else
							{
								thisarg += cmd.substring(i, i+1);
							}
							break;
						
						default:
							// unknown command/bad syntax
							errors++;
							break;
					}
							
				}
			}
			
			// last/only command in string
			if (!(thiscmd.length() == 0))
			{
				errors += doCommand(thiscmd, thisreg, thisarg);
			}
			
		}
		else
		{
			errors++;
		}
		
		// send response
		if (errors >= 0)
		{
			if (errors > 0)
			{
				sendResponse(RESP_ERROR);
			}
			else
			{
				sendResponse(RESP_OK);
			}
		}
		
	}

	private int doCommand(String thiscmd, String thisreg, String thisarg) 
	{
		int errors = 0;
		int val = 0;
		int regval = 0;
		
		
		// convert arg and reg to int values
		try 
		{
			val = Integer.parseInt(thisarg);
			
		}
		catch (NumberFormatException e)
		{
			// we don't care
		}
		
		try 
		{
			regval = Integer.parseInt(thisreg);
		}
		catch (NumberFormatException e)
		{
			// we don't care
		}
			
		
		logger.debug("vmodem doCommand: " + thiscmd + "  reg: " + thisreg + " (" + regval +")  arg: " + thisarg + " (" + val +")");
		
		switch(thiscmd.toUpperCase().charAt(0))
		{
			// ignored
			case 'B':  // call negotiation, might implement if needed
			case 'L':
			case 'M':
			case 'N':
			case 'X':
				break;
				
			// reset
			case 'Z':
				doCommandReset();
				break;
				
			// toggles
			case 'E':
				if (val > 1)
					errors++;
				else
					if (val == 0)
						vmodem_echo = false;
					else
						vmodem_echo = true;
				break;
			case 'Q':
				if (val > 1)
					errors++;
				else
					if (val == 0)
						vmodem_quiet = false;
					else
						vmodem_quiet = true;
				break;
			case 'V':
				if (val > 1)
					errors++;
				else
					if (val == 0)
						vmodem_verbose = false;
					else
						vmodem_verbose = true;
				break;			
			
			// info
			case 'I':
				switch(val)
				{
					case 0:
						write("\n\rDWVM " +dwVSerialPorts.prettyPort(this.vport) + "\r\n");
						break;
					case 1:
					case 3:
						write("\n\rDriveWire " + DriveWireServer.DWServerVersion + " Virtual Modem on port " + dwVSerialPorts.prettyPort(this.vport) + "\r\n");
						break;
					case 2:
						try 
						{
							write("\n\rConnected to " + dwVSerialPorts.getHostIP(this.vport) + ":" + dwVSerialPorts.getHostPort(this.vport) + "\n\r");
						} 
						catch (DWPortNotValidException e) 
						{
							logger.error(e.getMessage());
							write(e.getMessage());
						} 
						catch (DWConnectionNotValidException e) 
						{
							logger.error(e.getMessage());
							write(e.getMessage());
						}
						break;
					case 4:
						doCommandShowProfile();
						break;
					default:
						errors++;
						break;
				}
				
				break;
				
			// extended commands
			case '&':
				if (thiscmd.length() > 1)
				{
					switch(thiscmd.toUpperCase().charAt(1))
					{
						case 'F':
							doCommandReset();
							break;
						case 'V':
							doCommandShowProfile();
							break;
						default:
							errors++;
							break;
					}
				}
				else
				{
					errors++;
				}
				
				break;
			
			// registers
			case 'S':
				// valid?
				if ((regval < 256) && (val < 256))
				{
					// display or set
					if (thisarg.equals("?"))
					{
						// display
						write("\n\r" + this.vmodem_registers[regval] + "\n\r");	
					}	
					else
					{
						// set
						this.vmodem_registers[regval] = val;
					}
				}
				else
					errors++;
				
			    break;
			    
			 // dial   
			 case 'D':
			 	 if (!(thisarg.length() == 0))
			 	 {
			 	 	 // if its ATDL, dont reset vs_dev so we redial last host
			 	 	 if (!thisarg.equalsIgnoreCase("L"))
			 	 	 {
			 	 	 	 this.vmodem_dialstring = thisarg;
			 	 	 }
			 	 	 if (doDial() == 0)
			 	 	 {
		 	 	     	sendResponse(RESP_NOANSWER);
			 	 	 }
			 	 	 //don't print another response
			 	 	 errors = -1;
			 	 	 return(errors);
			 	 }
			 	 else
			 	 {
			 	 	 // ATD without a number/host?
			 	 	 errors++;
			 	 }
			 	 break;
			 	 
			default:
				// error on unknown commands?  OK might be preferable
				errors++;
				break;
		}
		
		return(errors);
	}

	private int doDial() 
	{
		String tcphost;
		int tcpport;
		
		// parse dialstring
		String[] dparts = this.vmodem_dialstring.split(":");

		if (dparts.length == 1)
		{
			tcphost = dparts[0];
			tcpport = 23;
		}
		else if (dparts.length == 2)
		{
			tcphost = dparts[0];
			tcpport = Integer.parseInt(dparts[1]);
		}
		else
		{
			return 0;
		}
		
		// start TCP thread
		this.tcpthread = new Thread(new DWVModemConnThread(this.handlerno, this.vport, tcphost, tcpport));
		//this.tcpthread = new Thread(new DWVPortTCPConnectionThread(this.handlerno, this.vport, tcphost, tcpport));
		
		this.tcpthread.start();

		return 1;
	}

	private void doCommandShowProfile() 
	{
		// display current modem settings
		write("Active profile:" + getCRLF() + getCRLF());
		
		write("E" + onoff(this.vmodem_echo) + " ");
		write("Q" + onoff(this.vmodem_quiet) + " ");
		write("V" + onoff(this.vmodem_verbose) + " ");
		
		write(getCRLF() + getCRLF());
		
		// show S0-S37, only 0-13 and 36-37 are well documented
		for (int i = 0;i<38;i++)
		{
			write(String.format("S%03d=%03d  ", i, this.vmodem_registers[i]));
			Thread.yield();
		}
		
		write(getCRLF() + getCRLF());
	}

	private String onoff(boolean val) 
	{
		if (val)
			return("1");
		else
			return("0");
	}

	private void doCommandReset() 
	{
		// state
		this.vmodem_echo = false;
		this.vmodem_lastcommand = new String();
		this.vmodem_quiet = false;
		this.vmodem_verbose = true;
		
		// registers
		this.vmodem_registers[REG_ANSWERONRING] = 0;
		this.vmodem_registers[REG_RINGS] = 0;
		this.vmodem_registers[REG_ESCCHAR] = 43;
		this.vmodem_registers[REG_CR] = 13;
		this.vmodem_registers[REG_LF] = 10;
		this.vmodem_registers[REG_BS] = 8;
		this.vmodem_registers[REG_GUARDTIME] = 50;
		
	}

	
	private String getCRLF()
	{
		return(Character.toString((char)this.vmodem_registers[REG_CR]) + Character.toString((char)this.vmodem_registers[REG_LF]));
	}
	
	
	private void sendResponse(int resp) 
	{
		
		// quiet mode
		if (!vmodem_quiet)
		{
			// verbose mode
			if (vmodem_verbose)
			{
				write(getVerboseResponse(resp) + getCRLF());
			}
			else
			{
				write(resp + getCRLF());
			}
		}
	}

	private String getVerboseResponse(int resp) 
	{
		String msg;
		
		switch(resp)
		{
			case RESP_OK:
				msg = "OK";
				break;
			case RESP_CONNECT:
				msg = "CONNECT";
				break;
			case RESP_RING:
				msg = "RING";
				break;
			case RESP_NOCARRIER:
				msg = "NO CARRIER";
				break;
			case RESP_ERROR:
				msg = "ERROR";
				break;
			case RESP_NODIALTONE:
				msg = "NO DIAL TONE";
				break;
			case RESP_BUSY:
				msg = "BUSY";
				break;
			case RESP_NOANSWER:
				msg = "NO ANSWER";
				break;
			default:
				msg = "UKNOWN";
				break;
		}
		return(msg);
	}
	
	private void write(String str)
	{
		try 
		{
			dwVSerialPorts.writeToCoco(this.vport, str);
		} 
		catch (DWPortNotValidException e) 
		{
			logger.error(e.getMessage());
		}
	}


	public boolean isEcho() 
	{
		return(this.vmodem_echo);
	}


	public int getCR() 
	{
		return(this.vmodem_registers[REG_CR]);
	}

	public int getLF() 
	{
		return(this.vmodem_registers[REG_LF]);
	}
	
	public int getBS() 
	{
		return(this.vmodem_registers[REG_BS]);
	}
	
	
}
