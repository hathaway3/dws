package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWUtilDWThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilDWThread");
	
	private int vport = -1;
	private String strargs = null;
	
	public DWUtilDWThread(int vport, String args)
	{
		this.vport = vport;
		this.strargs = args;

		logger.debug("init dw util thread");	
	}
	
	public void run() 
	{
		
		Thread.currentThread().setName("dwutil-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		
		String[] args = this.strargs.split(" ");
		
		// disk port server config log 
		
		if (((args.length >= 2) && (args[1].toLowerCase().startsWith("h"))) || (args.length == 1))
		{
			doHelp();
		}
		else if (args[1].toLowerCase().startsWith("d"))
		{
			doDisk(args);
		}
		else if (args[1].toLowerCase().startsWith("p"))
		{
			doPort(args);
		}
		else if (args[1].toLowerCase().startsWith("s"))
		{
			doServer(args);
		}
		else if (args[1].toLowerCase().startsWith("c"))
		{
			doConfig(args);
		}
		else if (args[1].toLowerCase().startsWith("l"))
		{
			doLog(args);
		}
		else if (args[1].toLowerCase().startsWith("n"))
		{
			doNet(args);
		}
		
		
		else
		{
			// unknown command/syntax
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: Unknown command '" + args[1] + "'");
		}
	
		// wait for output to flush
		// wait for output
		try {
			while ((DWVSerialPorts.bytesWaiting(this.vport) > 0) && (DWVSerialPorts.isOpen(this.vport)))
			{
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DWVSerialPorts.closePort(this.vport);
		logger.debug("exiting");
	}

	
	
	private void doNet(String[] args)
	{
		String text = new String();
		
		if (args.length == 2)
		{
			// help
			text += "Help for 'dw net':\r\n\n";
			text += "  dw net show                 - Show networking status\r\n";
			
		}
		else if (args[2].toLowerCase().startsWith("s"))
		{
			text += "\r\nDriveWire Network Connections:\r\n\n";

			for (int i = 0; i<DWVPortListenerPool.MAX_CONN;i++)
			{
				if (DWVPortListenerPool.getConn(i) != null)
				{
					text += "Connection " + i + ": " + DWVPortListenerPool.getConn(i).getInetAddress().getHostName() + ":" + DWVPortListenerPool.getConn(i).getPort() + " (connected to port " + DWVSerialPorts.prettyPort(DWVPortListenerPool.getConnPort(i)) + ")\r\n";
				}
			}
			
			text += "\r\n";
			
			for (int i = 0; i<DWVPortListenerPool.MAX_LISTEN;i++)
			{
				if (DWVPortListenerPool.getListener(i) != null)
				{
					text += "Listener " + i + ": TCP port " + DWVPortListenerPool.getListener(i).getLocalPort() + " (control port " + DWVSerialPorts.prettyPort(DWVPortListenerPool.getListenerPort(i)) +")\r\n";
				}
			}
		}
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + args[2] + "' is not a valid option.");
			return;
		}
	
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		DWVSerialPorts.writeToCoco(this.vport, text);
	}

	
	
	private void doLog(String[] args)
	{
		String text = new String();
		
		if (args.length == 2)
		{
			// help
			text += "Help for 'dw log':\r\n\n";
			text += "  dw log show                 - Show last 20 log lines\r\n";
			text += "  dw log show #               - Show last # log lines\r\n";
			
		}
		else if (args[2].toLowerCase().startsWith("s"))
		{
		int lines = 20;
			
			if (args.length == 4)
			{
				try
				{
					lines = Integer.parseInt(args[3]);
				}
				catch (NumberFormatException e)
				{
					// don't care
				}
			}
			
			text += "\r\nDriveWire Server Log (" + DriveWireServer.getLogEventsSize() + " events in buffer):\r\n\n";
			
			ArrayList<String> loglines = DriveWireServer.getLogEvents(lines);
			
			for (int i = 0;i<loglines.size();i++)
			{
				text += loglines.get(i);
				
			}
	
		}
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + args[2] + "' is not a valid option.");
			return;
		}
	
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		DWVSerialPorts.writeToCoco(this.vport, text);
	}

	
	
	private void doServer(String[] args)
	{
		String text = new String();
		
		if (args.length == 2)
		{
			// help
			text += "Help for 'dw server':\r\n\n";
			text += "  dw server show              - Show server status\r\n";
			text += "  dw server show threads      - Show server threads\r\n";
			text += "  dw server dir [filepath]    - Show directory on server\r\n";
			text += "  dw server list [filepath]   - List file on server\r\n";
			text += "  dw server makepass [text]   - Return encrypted form of text (use with auth)\r\n";
		}
		else if (args[2].toLowerCase().startsWith("s"))
		{
			if (args.length == 3)
			{
				text += "\r\nDriveWire version " + DriveWireServer.DWServerVersion + " (" + DriveWireServer.DWServerVersionDate + ") status:\r\n\n";
			
				text += "Device:        " + DriveWireServer.config.getString("SerialDevice","unknown") + "\r\n";
				text += "CoCo Type:     " + DriveWireServer.config.getInt("CocoModel", 0) + "\r\n";
			
				text += "\r\n";
			
				text += "Last OpCode:   " + DWProtocolHandler.prettyOP(DWProtocolHandler.getLastOpcode()) + "\r\n";
				text += "Last GetStat:  " + DWProtocolHandler.prettySS(DWProtocolHandler.getLastGetStat()) + "\r\n";
				text += "Last SetStat:  " + DWProtocolHandler.prettySS(DWProtocolHandler.getLastSetStat()) + "\r\n";
				text += "Last Drive:    " + DWProtocolHandler.getLastDrive() + "\r\n";
				text += "Last LSN:      " + DWProtocolHandler.int3(DWProtocolHandler.getLastLSN()) + "\r\n";
				text += "Last Error:    " + (int) (DWProtocolHandler.getLastError() & 0xFF) + "\r\n";
			
				text += "\r\n";
			
				text += "Total Read Sectors:  " + String.format("%6d",DWProtocolHandler.getSectorsRead()) + "  (" + DWProtocolHandler.getReadRetries() + " retries)\r\n";
				text += "Total Write Sectors: " + String.format("%6d",DWProtocolHandler.getSectorsWritten()) + "  (" + DWProtocolHandler.getWriteRetries() + " retries)\r\n";
			
				text += "\r\n";
			
				text += "D#   Sectors  LSN   WP       Reads  Writes  Dirty \r\n";
			
				for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
				{
					if (DWProtocolHandler.getDiskDrives().diskLoaded(i))
					{
					 
						text += "X"+ String.format("%-3d ",i);
						text += String.format("%5d ", DWProtocolHandler.getDiskDrives().getDiskSectors(i));
						text += String.format(" %5d ", DWProtocolHandler.getDiskDrives().getLSN(i));
						text += String.format("  %5s  ", DWProtocolHandler.getDiskDrives().getWriteProtect(i));
						text += String.format(" %6d ", DWProtocolHandler.getDiskDrives().getReads(i));
						text += String.format(" %6d ", DWProtocolHandler.getDiskDrives().getWrites(i));
						text += String.format(" %5d ", DWProtocolHandler.getDiskDrives().getDirtySectors(i));
						text += "\r\n";
					}
				}
			}
			else if (args[3].toLowerCase().startsWith("t"))
			{
				text += "\r\nDriveWire Server Threads:\r\n\n";

				Thread[] threads = getAllThreads();
				
				for (int i = 0;i<threads.length;i++)
				{
					text += String.format("%20s %3d %-8s %-14s %s",threads[i].getName(),threads[i].getPriority(),threads[i].getThreadGroup().getName(), threads[i].getState().toString(), threads[i].getClass().getCanonicalName()) + "\r\n";
				}
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + args[3] + "' is not a valid option to dw server show.");
				return;
			}
			
		}
		else if (args[2].toLowerCase().startsWith("d"))
		{
			if (args.length == 4)
			{
				doDir(args[3]);
				return;
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw server dir requires a filepath as an argument.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("l"))
		{
			if (args.length == 4)
			{
				doList(args[3]);
				return;
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw server list requires a filepath as an argument.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("m"))
		{
			if (args.length == 4)
			{
				doMakePass(args[3]);
				return;
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw server mmakepass requires text as an argument.");
				return;
			}
		}
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + args[2] + "' is not a valid option.");
			return;
		}
	
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		DWVSerialPorts.writeToCoco(this.vport, text);
	}

	private void doPort(String[] args)
	{
		String text = new String();
		
		if (args.length == 2)
		{
			// help
			text += "Help for 'dw port':\r\n\n";
			text += "  dw port show                - Show current port status\r\n";
			text += "  dw port close #             - Force port # to close\r\n";
			
			
		}
		else if (args[2].toLowerCase().startsWith("s"))
		{
			text += "\r\nCurrent port status:\r\n\n";
			
			for (int i = 0;i<DWVSerialPorts.MAX_PORTS;i++)
			{
				text += DWVSerialPorts.prettyPort(i) + " ";
				
				if (i<10)
					text += " ";
				
				
				if (DWVSerialPorts.isOpen(i))
				{
					text += "open (" + DWVSerialPorts.getOpen(i) + ") ";
				}
				else
				{
					text += "closed   ";
				}
				
				
				if (DWVSerialPorts.isConnected(i))
				{
							
					text += DWVSerialPorts.getHostIP(i);
					text += ":" + DWVSerialPorts.getHostPort(i);
				}
				else
				{
					text += "not connected";
				}
				
				if (DriveWireServer.config.getBoolean("TelnetUseAuth", false))
				{
					text += "  " + DWVSerialPorts.getUserName(i);
					text += " (" + DWVSerialPorts.getUserGroup(i) + ")";
					
				}
				
				text += "\r\n";
			}
			
		}
		else if (args[2].toLowerCase().startsWith("c"))
		{
			if (args.length == 4)
			{
				//TODO port close
				text += "not implemented yet\r\n";
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw port close requires a port # as an argument.");
				return;
			}
		}
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + args[2] + "' is not a valid option.");
			return;
		}
	
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		DWVSerialPorts.writeToCoco(this.vport, text);	
	}

	
	
	
	private void doDisk(String[] args)
	{
		String text = new String();
		
		if (args.length == 2)
		{
			// help
			text += "Help for 'dw disk':\r\n\n";
			text += "  dw disk show                - Show current disks\r\n";
			text += "  dw disk eject #             - Eject disk in drive #\r\n";
			text += "  dw disk insert # [filepath] - Load disk in drive #\r\n";
			text += "  dw disk wp #                - Toggle write protect on drive #\r\n";
			text += "  dw disk set show [filepath] - Show disks in diskset file\r\n";
			text += "  dw disk set load [filepath] - Load disks in diskset file\r\n";
			text += "  dw disk set save [filepath] - Save current disks to diskset file\r\n";
			
			
		}
		else if (args[2].toLowerCase().startsWith("sh"))
		{
			text = "\r\nCurrent DriveWire Disks:\r\n\n";
			
			text += "D#   DSK File Name (* = write protected)       Disk Name\r\n";
			text += "---- ----------------------------------------- --------------------------------\r\n";
			
			for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
			{
				if (DWProtocolHandler.getDiskDrives().diskLoaded(i))
				{
					String df = DWProtocolHandler.getDiskDrives().getDiskFile(i);
					
					if (DWProtocolHandler.getDiskDrives().getWriteProtect(i))
					{
						df = df + "*";
					}
					 
					text += "X"+ String.format("%-3d ",i);
					text += String.format("%-42s",df);
					text += String.format("%-32s", DWProtocolHandler.getDiskDrives().getDiskName(i));
					text += "\r\n";
				}
				
				
			}
			
		}
		else if (args[2].toLowerCase().startsWith("e"))
		{
			if (args.length == 4)
			{
				doDiskEject(args[3]);
				return;
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk eject requires a drive # as an argument.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("i"))
		{
			if (args.length == 5)
			{
				doDiskInsert(args[3], args[4]);
				return;
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk insert requires a drive # and a file path as arguments.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("w"))
		{
			if (args.length == 4)
			{
				doDiskWPToggle(args[3]);
				return;
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk wp requires a drive # as an argument.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("se"))
		{
			text += "not implemented";
		}
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + args[2] + "' is not a valid option.");
			return;
		}
	
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		DWVSerialPorts.writeToCoco(this.vport, text);
	}

	
	@SuppressWarnings("unchecked")
	private void doConfig(String[] args)
	{
		String text = new String();
		
		if (args.length == 2)
		{
			// help
			text += "Help for 'dw config':\r\n\n";
			text += "  dw config show              - Show current configuration\r\n";
			text += "  dw config show [key]        - Show current value for key\r\n";
			text += "  dw config set [key] [value] - Set config item key = value\r\n";
			text += "  dw config save              - Save current configuration to disk\r\n";
			text += "  dw config load              - Load configuration from disk\r\n";
			
		}
		else if (args[2].toLowerCase().startsWith("sh"))
		{
			if (args.length == 3)
			{
				// show config
				
				text += "Current DriveWire configuration:\r\n\n";
				
				for (Iterator i = DriveWireServer.config.getKeys(); i.hasNext();)
				{
					String key = (String) i.next();
					String value = StringUtils.join(DriveWireServer.config.getStringArray(key), ", ");
				
					text += key + " = " + value + "\r\n";
				            
				}
			}
			else if (args.length == 4)
			{
				if (DriveWireServer.config.containsKey(args[3]))
				{
					String key = args[3];
					String value = StringUtils.join(DriveWireServer.config.getStringArray(key), ", ");
				
					text += key + " = " + value;
				}
				else
				{
					text += "Key '" + args[3] + "' is not set.";
				}
			}
			
		}
		else if (args[2].toLowerCase().startsWith("l"))
		{
			// reload
			DriveWireServer.loadConfig();
			text = "Loaded configuration from disk.";
		}
		else if (args[2].toLowerCase().startsWith("sa"))
		{
			// reload
			DriveWireServer.saveConfig();
			text = "Saved configuration to disk.";
		}
		else if (args[2].toLowerCase().startsWith("se"))
		{
			// set value
			if (args.length >= 5)
			{
				String key = args[3];
				String value = args[4];
				
				for (int i = 5;i<args.length;i++)
				{
					value += " " + args[i];
				}
				
				if (DriveWireServer.config.containsKey(key))
				{
					DriveWireServer.config.clearProperty(key);
				}
				
				DriveWireServer.config.addProperty(key, value);
				
				text += "Set key '" + key + "'\r\n";
			}
			else
			{
				DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error, use: dw config set KEY VALUE [VALUE2] [VALUE3 etc]");
		    	return;
			}
		}
			
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + args[2] + "' is not a valid option.");
			return;
		}
	
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		DWVSerialPorts.writeToCoco(this.vport, text);
	}
	
	
	
	

	private void doMakePass(String pw)
	{
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "encrypted pw follows");
		
		BasicPasswordEncryptor bpe = new BasicPasswordEncryptor();
				
		DWVSerialPorts.writeToCoco(this.vport, "Encypted form of '" + pw + "' is: " + bpe.encryptPassword(pw));

	}


	private void doDiskInsert(String drivestr, String path) 
	{
		try
		{
			int driveno = Integer.parseInt(drivestr);
		
			DWProtocolHandler.getDiskDrives().validateDriveNo(driveno);
			
			// eject any current disk
			if (DWProtocolHandler.getDiskDrives().diskLoaded(driveno))
			{
				DWProtocolHandler.getDiskDrives().EjectDisk(driveno);
			}
			
			// load new disk
			
			DWProtocolHandler.getDiskDrives().LoadDiskFromFile(driveno, path);
		
			DWVSerialPorts.sendUtilityOKResponse(this.vport, "Disk inserted in drive " + driveno);
			DWVSerialPorts.writeToCoco(this.vport, "Disk loaded in drive " + driveno);

		}
		catch (NumberFormatException e)
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive # '" + drivestr + "'");
		}
		catch (DWDriveNotValidException e) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 20,"Drive '" + drivestr + "' is not valid.");
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 20,e.getMessage());
				
		} 
		catch (FileNotFoundException e) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 21,"File not found on server: '" + path + "'");
		} 
		catch (DWDriveAlreadyLoadedException e) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 22,e.getMessage());		
			
		}
		
		
	}

	private void doDiskEject(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			DWProtocolHandler.getDiskDrives().EjectDisk(driveno);
		
			DWVSerialPorts.sendUtilityOKResponse(this.vport, "Disk ejected from drive " + driveno);
			DWVSerialPorts.writeToCoco(this.vport, "Disk ejected from drive " + driveno);

		}
		catch (NumberFormatException e)
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive # '" + drivestr + "'");
			
		} 
		catch (DWDriveNotValidException e) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 20,e.getMessage());
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 20,e.getMessage());
			
		}
		
		return;
	}
		
		
	private void doDiskWPToggle(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DWProtocolHandler.getDiskDrives().getWriteProtect(driveno))
			{
				DWProtocolHandler.getDiskDrives().setWriteProtect(driveno, false);
				DWVSerialPorts.sendUtilityOKResponse(this.vport, "write protect toggle succeeded");
			    DWVSerialPorts.writeToCoco(this.vport, "Disk in drive " + driveno + " is now writeable.");
			}
			else
			{
				DWProtocolHandler.getDiskDrives().setWriteProtect(driveno, true);
				DWVSerialPorts.sendUtilityOKResponse(this.vport, "write protect toggle succeeded");
			    DWVSerialPorts.writeToCoco(this.vport, "Disk in drive " + driveno + " is now write protected.");
			}
			
		}
		catch (NumberFormatException e)
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive # '" + drivestr + "'");
			
		} 
		
		return;
	}
		
	


	private void doList(String path) 
	{
		String text = new String();
	    
		try 
		{
		    StringBuffer fileData = new StringBuffer(1000);
	        BufferedReader reader;
			
				reader = new BufferedReader(
				        new FileReader(path));
		    char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1){
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	            buf = new char[1024];
	        }
	        reader.close();
	        text = fileData.toString();
		} 
		catch (FileNotFoundException e) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 8,"File not found on server.");
			return;
		} 
		catch (IOException e1) 
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 9,"IO Error on server: " + e1.getMessage());
			return;
		}
    
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "file data follows");
	    DWVSerialPorts.writeToCoco(this.vport, text);
	}
	
	
	private void doDir(String path) 
	{
		String text = new String();
	
		File dir = new File(path);
	    
	    String[] children = dir.list();
	    if (children == null) 
	    {
	    	DWVSerialPorts.sendUtilityFailResponse(this.vport, 10, "Either '" + path + "' does not exist or it is not a directory");
	    	return;
	    }   
	    else 
	    {
	    	int longest = 0;
	    	
	    	for (int i=0; i<children.length; i++) 
	    	{
	    		if (children[i].length() > longest)
	    			longest = children[i].length();
	    	}
	    	
	    	longest++;
	    	longest++;
	    	
	    	int cols = (80 / longest);
	    	
	    	for (int i=0; i<children.length; i++) 
	        {
	        	text += String.format("%-" + longest + "s",children[i]);
	        	if (((i+1) % cols) == 0)
	        		text += "\r\n";
	        }
	    }
		
	    DWVSerialPorts.sendUtilityOKResponse(this.vport, "directory follows");
	    DWVSerialPorts.writeToCoco(this.vport, text);
	}
	
	

	
	private void doHelp()
	{
		String text = new String();
		
		text = "\r\nHelp for DriveWire " + DriveWireServer.DWServerVersion + " commands:";
		
		text += "\r\n\n";
		
		text += "  Type dw followed by any subcommand without options for specific help.\r\n\n";
		
		text += "  dw disk <options>      - Disk commands\r\n";
		text += "  dw port <options>      - Port commands\r\n";
		text += "  dw net <options>       - Networking commands\r\n";
		text += "  dw server <options>    - Server commands\r\n";
		text += "  dw config <options>    - Configuration commands\r\n";
		text += "  dw log <options>       - Logging commands\r\n";
		
		
		text += "\n";
		text += "  (all commands may be abbreviated to their shortest unique form)\r\n";
		
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "help text follows");
	    DWVSerialPorts.writeToCoco(this.vport, text);
	}

	private ThreadGroup getRootThreadGroup( ) {

	    ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
	    ThreadGroup ptg;
	    while ( (ptg = tg.getParent( )) != null )
	        tg = ptg;
	    return tg;
	}
	
	private Thread[] getAllThreads( ) {
	    final ThreadGroup root = getRootThreadGroup( );
	    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
	    int nAlloc = thbean.getThreadCount( );
	    int n = 0;
	    Thread[] threads;
	    do {
	        nAlloc *= 2;
	        threads = new Thread[ nAlloc ];
	        n = root.enumerate( threads, true );
	    } while ( n == nAlloc );
	    return java.util.Arrays.copyOf( threads, n );
	}
	
}
