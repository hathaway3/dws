package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDisk;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWUtilDWThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilDWThread");
	
	private int vport = -1;
	private String strargs = null;
	private int handlerno;
	private DWVSerialPorts dwVSerialPorts;
	
	
	public DWUtilDWThread(int handlerno, int vport, String args)
	{
		this.vport = vport;
		this.strargs = args;
		this.handlerno = handlerno;
		this.dwVSerialPorts = DriveWireServer.getHandler(handlerno).getVPorts();
		
		logger.debug("init dw util thread");	
	}
	
	
	
	public void run() 
	{
		
		Thread.currentThread().setName("dwutil-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		logger.debug("run for handler #" + handlerno);
		
		
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
		else if (args[1].toLowerCase().startsWith("r"))
		{
			doReg(args);
		}
		
		else
		{
			// unknown command/syntax
			sendSyntaxError(args[1]);
		}
	
		// wait for output to flush
		// wait for output
		try {
			while ((dwVSerialPorts.bytesWaiting(this.vport) > 0) && (dwVSerialPorts.isOpen(this.vport)))
			{
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dwVSerialPorts.closePort(this.vport);
		logger.debug("exiting");
	}

	
	private void doReg(String[] args)
	{
		
		if (args.length == 3)
		{
			DriveWireServer.getHandler(this.handlerno).getEventHandler().registerEvent(args[2].toLowerCase(),this.vport);
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
			dwVSerialPorts.writeToCoco(this.vport, "registered for " + args[2]);
		}
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
					text += "Connection " + i + ": " + DWVPortListenerPool.getConn(i).getInetAddress().getHostName() + ":" + DWVPortListenerPool.getConn(i).getPort() + " (connected to port " + dwVSerialPorts.prettyPort(DWVPortListenerPool.getConnPort(i)) + ")\r\n";
				}
			}
			
			text += "\r\n";
			
			for (int i = 0; i<DWVPortListenerPool.MAX_LISTEN;i++)
			{
				if (DWVPortListenerPool.getListener(i) != null)
				{
					text += "Listener " + i + ": TCP port " + DWVPortListenerPool.getListener(i).getLocalPort() + " (control port " + dwVSerialPorts.prettyPort(DWVPortListenerPool.getListenerPort(i)) +")\r\n";
				}
			}
		}
		else
		{
			sendSyntaxError(args[2]);
			return;
		}
	
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, text);
	}

	
	
	private void sendSyntaxError(String txt)
	{
		dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + txt + "' is ambiguous or not a valid option.");
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
			sendSyntaxError(args[2]);
			return;
		}
	
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, text);
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
			text += "  dw server show handlers     - Show server handler instances\r\n";
			text += "  dw server show config       - Show server level configuration\r\n";
			text += "  dw server restart #         - Restart handler #\r\n";
			text += "  dw server dir [filepath]    - Show directory on server\r\n";
			text += "  dw server list [filepath]   - List file on server\r\n";
			text += "  dw server makepass [text]   - Return encrypted form of text (use with auth)\r\n";
		}
		else if (args[2].toLowerCase().startsWith("s"))
		{
			if (args.length == 3)
			{
				text += "\r\nDriveWire version " + DriveWireServer.DWServerVersion + " (" + DriveWireServer.DWServerVersionDate + ") status:\r\n\n";
			
				text += "Device:        " + DriveWireServer.getHandler(this.handlerno).config.getString("SerialDevice","unknown") + "\r\n";
				text += "CoCo Type:     " + DriveWireServer.getHandler(this.handlerno).config.getInt("CocoModel", 0) + "\r\n";
			
				text += "\r\n";
			
				text += "Last OpCode:   " + DWUtils.prettyOP(DriveWireServer.getHandler(handlerno).getLastOpcode()) + "\r\n";
				text += "Last GetStat:  " + DWUtils.prettySS(DriveWireServer.getHandler(handlerno).getLastGetStat()) + "\r\n";
				text += "Last SetStat:  " + DWUtils.prettySS(DriveWireServer.getHandler(handlerno).getLastSetStat()) + "\r\n";
				text += "Last Drive:    " + DriveWireServer.getHandler(handlerno).getLastDrive() + "\r\n";
				text += "Last LSN:      " + DWUtils.int3(DriveWireServer.getHandler(handlerno).getLastLSN()) + "\r\n";
				text += "Last Error:    " + (int) (DriveWireServer.getHandler(handlerno).getLastError() & 0xFF) + "\r\n";
			
				text += "\r\n";
			
				text += "Total Read Sectors:  " + String.format("%6d",DriveWireServer.getHandler(handlerno).getSectorsRead()) + "  (" + DriveWireServer.getHandler(handlerno).getReadRetries() + " retries)\r\n";
				text += "Total Write Sectors: " + String.format("%6d",DriveWireServer.getHandler(handlerno).getSectorsWritten()) + "  (" + DriveWireServer.getHandler(handlerno).getWriteRetries() + " retries)\r\n";
			
				text += "\r\n";
			
				text += "D#   Sectors  LSN   WP       Reads  Writes  Dirty \r\n";
			
				for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
				{
					if (DriveWireServer.getHandler(handlerno).getDiskDrives().diskLoaded(i))
					{
					 
						text += "X"+ String.format("%-3d ",i);
						text += String.format("%5d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getDiskSectors(i));
						text += String.format(" %5d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getLSN(i));
						text += String.format("  %5s  ", DriveWireServer.getHandler(handlerno).getDiskDrives().getWriteProtect(i));
						text += String.format(" %6d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getReads(i));
						text += String.format(" %6d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getWrites(i));
						text += String.format(" %5d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getDirtySectors(i));
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
					if (threads[i] != null)
					{
						text += String.format("%20s %3d %-8s %-14s %s",threads[i].getName(),threads[i].getPriority(),threads[i].getThreadGroup().getName(), threads[i].getState().toString(), threads[i].getClass().getCanonicalName()) + "\r\n";

					}
				}
			}
			else if (args[3].toLowerCase().startsWith("h"))
			{
				text += "\r\nDriveWire protocol handler instances:\r\n";
				
				for (int i = 0;i<DriveWireServer.getNumHandlers();i++)
				{
					if (DriveWireServer.getHandler(i) != null)
					{
						text += "\r\nHandler #" + i + ": Device " + DriveWireServer.getHandler(i).config.getString("SerialDevice") + " CocoModel " + DriveWireServer.getHandler(i).config.getString("CocoModel") + "\r\n"; 
					}
				}
				
			}
			else if (args[3].toLowerCase().startsWith("c"))
			{
				text += "\r\nDriveWire server configuration:\r\n\n";
				
				for (Iterator<String> i = DriveWireServer.serverconfig.getKeys(); i.hasNext();)
				{
					String key = i.next();
					String value = StringUtils.join(DriveWireServer.serverconfig.getStringArray(key), ", ");
				
					text += key + " = " + value + "\r\n";
				            
				}
				
			}
			else
			{
				sendSyntaxError(args[3]);
				return;
			}
			
		}
		else if (args[2].toLowerCase().startsWith("r"))
		{
			if (args.length == 4)
			{
				doRestart(args[3]);
				return;
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw server restart requires a handler # as an argument.");
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
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw server dir requires a filepath as an argument.");
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
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw server list requires a filepath as an argument.");
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
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw server makepass requires text as an argument.");
				return;
			}
		}
		else
		{
			sendSyntaxError(args[2]);
			return;
		}
	
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, text);
	}

	
	
	private void doRestart(String hno)
	{
		try
		{
			int handler = Integer.parseInt(hno);
			
			// validate
			if (DriveWireServer.isValidHandlerNo(handler))
			{
				
				dwVSerialPorts.sendUtilityOKResponse(this.vport, "restarting handler");
				dwVSerialPorts.writeToCoco(this.vport, "Restarting handler #" + handler + ".");
				
				// sync output
				try {
					while ((dwVSerialPorts.bytesWaiting(this.vport) > 0) && (dwVSerialPorts.isOpen(this.vport)))
					{
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				DriveWireServer.restartHandler(handler);
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 60,"Invalid handler #"); 
			}

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric handler #");
		} 
	
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
				text += dwVSerialPorts.prettyPort(i) + " ";
				
				if (i<10)
					text += " ";
				
				
				if (dwVSerialPorts.isOpen(i))
				{
					text += "open(" + dwVSerialPorts.getOpen(i) + ") ";
					
					text += " PD.INT=" + dwVSerialPorts.getPD_INT(i);
					text += " PD.QUT=" + dwVSerialPorts.getPD_QUT(i);
					
					
				}
				else
				{
					text += "closed   ";
				}
				
				
				
				if (dwVSerialPorts.isConnected(i))
				{
					text += " " + dwVSerialPorts.getHostIP(i) + ":" + dwVSerialPorts.getHostPort(i);
				}
				else
				{
					text += " not connected";
				}
				
				//text += " " + DWProtocolHandler.byteArrayToHexString(DWVSerialPorts.getDD(i));	
				
				
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
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw port close requires a port # as an argument.");
				return;
			}
		}
		else
		{
			sendSyntaxError(args[2]);
			return;
		}
	
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, text);	
	}

	
	
	
	private void doDisk(String[] args)
	{
		String text = new String();
		
		if (args.length == 2)
		{
			// help
			text += "Help for 'dw disk':\r\n\n";
			text += "  dw disk show                - Show current disks\r\n";
			text += "  dw disk show #              - Show details for disk in drive #\r\n";
			text += "  dw disk eject #             - Eject disk from drive #\r\n";
			text += "  dw disk insert # [filepath] - Load disk in drive #\r\n";
			text += "  dw disk reload #            - Reload disk in drive #\r\n";
			text += "  dw disk write #             - Write disk image in drive #\r\n";
			text += "  dw disk write # [filepath]  - Write disk image in drive # to path\r\n";
			text += "  dw disk create # [filepath] - Create new disk image\r\n";
			text += "  dw disk wp #                - Toggle write protect on drive #\r\n";
			text += "  dw disk set show            - Show available disk sets\r\n";
			text += "  dw disk set load [setname]  - Load disks in diskset file\r\n";
			text += "  dw disk dump disk# sector#  - Dump sector from disk\r\n";
			
		}
		else if (args[2].toLowerCase().startsWith("sh"))
		{
			if (args.length == 3)
			{
			
				text = "\r\nCurrent DriveWire Disks:\r\n\n";
			
				text += "D#   DSK File Name (* = write protected)       Disk Name\r\n";
				text += "---- ----------------------------------------- --------------------------------\r\n";
			
				for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
				{

					
					if (DriveWireServer.getHandler(handlerno).getDiskDrives().diskLoaded(i))
					{
						String df = DriveWireServer.getHandler(handlerno).getDiskDrives().getDiskFile(i);
					
						if (DriveWireServer.getHandler(handlerno).getDiskDrives().getWriteProtect(i))
						{
							df = df + "*";
						}
					 
						text += "X"+ String.format("%-3d ",i);
						text += String.format("%-42s",df);
						text += String.format("%-32s", DriveWireServer.getHandler(handlerno).getDiskDrives().getDiskName(i));
						text += "\r\n";
					}
			
				}
				
			}
			else
			{
				// detailed per disk
				doDiskDetail(args[3]);
				return;
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
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk eject requires a drive # as an argument.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("r"))
		{
			if (args.length == 4)
			{
				doDiskReload(args[3]);
				return;
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk reload requires a drive # as an argument.");
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
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk insert requires a drive # and a file path as arguments.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("wr"))
		{
			if (args.length == 4)
			{
				// write to current path
				doDiskWrite(args[3]);
				return;
			}
			else if (args.length == 5)
			{
				// write to alternate path
				doDiskWrite(args[3],args[4]);
				return;
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk write requires a drive # and an optional file path as arguments.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("c"))
		{
			if (args.length == 5)
			{
				// create disk
				doDiskCreate(args[3],args[4]);
				return;
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk create requires a drive # and file path as arguments.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("wp"))
		{
			if (args.length == 4)
			{
				doDiskWPToggle(args[3]);
				return;
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: dw disk wp requires a drive # as an argument.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("se"))
		{
			// disk sets
			if (args.length == 4)
			{
				if (args[3].toLowerCase().startsWith("sh"))
				{
					doDiskSetShow();
					return;
				}
			}
			else if (args.length == 5)
			{
				if (args[3].toLowerCase().startsWith("sh"))
				{
					doDiskSetShow(args[4]);
					return;
				}
				else if (args[3].toLowerCase().startsWith("sa"))
				{
					doDiskSetSave(args[4]);
					return;
				}
				else if (args[3].toLowerCase().startsWith("l"))
				{
					doDiskSetLoad(args[4]);
					return;
				}
				else
				{
					sendSyntaxError(args[3]);
					return;
				}
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error in dw disk set command.");
				return;
			}
		}
		else if (args[2].toLowerCase().startsWith("d"))
		{
			if (args.length == 5)
			{
				doDiskDump(args[3],args[4]);
				return;
			}
		}
		else
		{
			sendSyntaxError(args[2]);
			return;
		}
	
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, text);
	}

	
	private void doDiskReload(String drivestr)
	{

		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).reload();
	
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "disk reloaded");
			dwVSerialPorts.writeToCoco(this.vport, "Disk in drive #"+ driveno + " reloaded.");

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive #");
		} 
		catch (IOException e1)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 31,e1.getMessage());
		}
		
	}



	private void doDiskDetail(String drivestr)
	{
		String text;
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			text = "Details for disk in drive #" + driveno + ":\r\n\n";
			
			text += "Path: " + DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).getFilePath() + "\r\n";
			text += "Name: " + DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).getDiskName() + "\r\n\n";
			
			text += "Sectors  LSN   WP       Reads  Writes  Dirty \r\n";
			
			text += String.format("%5d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getDiskSectors(driveno));
			text += String.format(" %5d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getLSN(driveno));
			text += String.format("  %5s  ", DriveWireServer.getHandler(handlerno).getDiskDrives().getWriteProtect(driveno));
			text += String.format(" %6d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getReads(driveno));
			text += String.format(" %6d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getWrites(driveno));
			text += String.format(" %5d ", DriveWireServer.getHandler(handlerno).getDiskDrives().getDirtySectors(driveno));
			text += "\r\n\n";
			
			text += "Filesystem supports write: " + DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).isFSWriteable() + "\r\n";
		    text += " FS supports random write: " + DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).isRandomWriteable() + "\r\n";
			text += "      File can be written: " + DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).isWriteable() + "\r\n";     
			text += "  Disk is write protected: " + DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).getWriteProtect() + "\r\n";
			
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "disk detail");
			dwVSerialPorts.writeToCoco(this.vport, text);

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive #");
			
		} 
	}

	
	private void doDiskWrite(String drivestr)
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).writeDisk();
					
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "wrote disk");
			dwVSerialPorts.writeToCoco(this.vport, "Disk #" + driveno + " written.");

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive #");
			
		} 
		catch (IOException e1)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 30, e1.getMessage());
			
		}
		
		
	}
	
	private void doDiskCreate(String drivestr, String filepath)
	{
		FileSystemManager fsManager;
		FileObject fileobj;
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			// create file
			fsManager = VFS.getManager();
			fileobj = fsManager.resolveFile(filepath);
			
			if (fileobj.exists())
			{
				fileobj.close();
				throw new IOException("File already exists");
			}
		
			fileobj.createFile();
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().LoadDiskFromFile(driveno, filepath);
					
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "created disk");
			dwVSerialPorts.writeToCoco(this.vport, "Disk #" + driveno + " created.");

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive #");
			
		} 
		catch (IOException e1)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 30, e1.getMessage());
			
		} 
		catch (DWDriveNotValidException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 31, "Invalid drive number");
		} 
		catch (DWDriveAlreadyLoadedException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 32, "There is already a disk in drive " + drivestr);
		}
		
	}
	
	
	private void doDiskWrite(String drivestr, String path)
	{
		path = convertStarToBang(path);
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).writeDisk(path);
					
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "wrote disk");
			dwVSerialPorts.writeToCoco(this.vport, "Disk #" + driveno + " written.");

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive #");
			
		} 
		catch (IOException e1)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 30, e1.getMessage());
			
		}
		
		
	}
	
	
	
	
	
	private void doDiskSetLoad(String setname)
	{
		if (DriveWireServer.hasDiskset(setname))
		{
			DriveWireServer.getHandler(handlerno).getDiskDrives().LoadDiskSet(setname);
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
			dwVSerialPorts.writeToCoco(this.vport, "Loaded disk set '" + setname +"'.  Check log for errors.\r\n");
		}
		else
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 255, "Disk set not found.");
		}

	}

	private void doDiskSetSave(String filename)
	{
		/* TODO
		DriveWireServer.getHandler(handlerno).getDiskDrives().saveDiskSet(filename);
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, "Wrote current drive settings to disk set '" + filename +"'.  Check log for errors.");
		
		 */
		
		dwVSerialPorts.sendUtilityFailResponse(this.vport, 255, "Not implemented");
	}

	
	private void doDiskSetShow()
	{
		String text = new String();
		
		text = "Available disk sets:\r\n\n";
		
		List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
    	
		String[] setnames = new String[disksets.size()];
		int tmp = 0;
		
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration dset = (HierarchicalConfiguration) it.next();
		    
		    setnames[tmp]=dset.getString("Name","unnamed-" + tmp); 
		    tmp++;
		}
		
		int longest = 0;
    	
    	for (int i=0; i<setnames.length; i++) 
    	{
    		if (setnames[i].length() > longest)
    			longest = setnames[i].length();
    	}
    	
    	longest++;
    	longest++;
    	
    	int cols = (80 / longest);
    	
    	for (int i=0; i<setnames.length; i++) 
        {
        	text += String.format("%-" + longest + "s",setnames[i]);
        	if (((i+1) % cols) == 0)
        		text += "\r\n";
        }
		
		text += "\r\n";
    	
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, text);
	}
	
	private void doDiskSetShow(String setname)
	{
		String text = new String();
		
		if (DriveWireServer.hasDiskset(setname))
		{
			HierarchicalConfiguration theset = DriveWireServer.getDiskset(setname);
			
			text = "Details for disk set '" + setname + "':\r\n\n";
			
			text += "Description: " + theset.getString("Description","none") + "\r\n\n";
			
			// disks
			List<HierarchicalConfiguration> disks = theset.configurationsAt("disk");
	    	
			for(Iterator<HierarchicalConfiguration> it = disks.iterator(); it.hasNext();)
			{
			    HierarchicalConfiguration disk = (HierarchicalConfiguration) it.next();
			    text += "X" + disk.getInt("drive") + ": " + disk.getString("path");
			    if (disk.getBoolean("writeprotect",false))
			    {
			    	text += " (WP)";
			    }
			    
			    if (disk.getBoolean("bootable",false))
			    {
			    	text += " (boot)";
			    }
			    
			    
			    text +="\r\n";
			}
		
		
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
			dwVSerialPorts.writeToCoco(this.vport, text);
		}
		else
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 255, "No such disk set.");
		}
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
				
				text += "Current protocol handler configuration:\r\n\n";
				
				for (Iterator i = DriveWireServer.getHandler(this.handlerno).config.getKeys(); i.hasNext();)
				{
					String key = (String) i.next();
					String value = StringUtils.join(DriveWireServer.getHandler(this.handlerno).config.getStringArray(key), ", ");
				
					text += key + " = " + value + "\r\n";
				            
				}
			}
			else if (args.length == 4)
			{
				if (DriveWireServer.getHandler(this.handlerno).config.containsKey(args[3]))
				{
					String key = args[3];
					String value = StringUtils.join(DriveWireServer.getHandler(this.handlerno).config.getStringArray(key), ", ");
				
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
			//DriveWireServer.getHandler(handlerno).reloadConfig();
			text = "TBD! Loaded configuration from disk.";
		}
		else if (args[2].toLowerCase().startsWith("sa"))
		{
			// reload
			// DriveWireServer.getHandler(handlerno).saveConfig();
			text = "TBD! Saved configuration to disk.";
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
				
				if (DriveWireServer.getHandler(this.handlerno).config.containsKey(key))
				{
					DriveWireServer.getHandler(this.handlerno).config.clearProperty(key);
				}
				
				DriveWireServer.getHandler(this.handlerno).config.addProperty(key, value);
				
				text += "Set key '" + key + "'\r\n";
			}
			else
			{
				dwVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error, use: dw config set KEY VALUE [VALUE2] [VALUE3 etc]");
		    	return;
			}
		}
			
		else
		{
			sendSyntaxError(args[2]);
			return;
		}
	
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "data follows");
		dwVSerialPorts.writeToCoco(this.vport, text);
	}
	
	
	
	

	private void doMakePass(String pw)
	{
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "encrypted pw follows");
		
		BasicPasswordEncryptor bpe = new BasicPasswordEncryptor();
				
		dwVSerialPorts.writeToCoco(this.vport, "Encypted form of '" + pw + "' is: " + bpe.encryptPassword(pw));

	}


	private void doDiskInsert(String drivestr, String path) 
	{
		
		path = convertStarToBang(path);
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
		
			DriveWireServer.getHandler(handlerno).getDiskDrives().validateDriveNo(driveno);
			
			// eject any current disk
			if (DriveWireServer.getHandler(handlerno).getDiskDrives().diskLoaded(driveno))
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().EjectDisk(driveno);
			}
			
			// load new disk
			
			DriveWireServer.getHandler(handlerno).getDiskDrives().LoadDiskFromFile(driveno, path);
			
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "Disk inserted in drive " + driveno);
			dwVSerialPorts.writeToCoco(this.vport, "Disk loaded in drive " + driveno);

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive # '" + drivestr + "'");
		}
		catch (DWDriveNotValidException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 20,"Drive '" + drivestr + "' is not valid.");
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 20,e.getMessage());
				
		} 
		catch (FileSystemException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 21,e.getMessage());
		} 
		catch (DWDriveAlreadyLoadedException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 22,e.getMessage());		
			
		} 
		catch (FileNotFoundException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 23,e.getMessage());	
		} 
		catch (IOException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 24,e.getMessage());	
		}
		
		
	}

	
	private void doDiskDump(String drivestr, String sectorstr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
			int sectorno = Integer.parseInt(sectorstr);
			
			
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "sector data");
			dwVSerialPorts.writeToCoco(this.vport, new String(DriveWireServer.getHandler(handlerno).getDiskDrives().getDisk(driveno).getSector(sectorno).getData()));

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive or sector #");
			
		} 
		
	}
	
	
	
	private void doDiskEject(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			DriveWireServer.getHandler(handlerno).getDiskDrives().EjectDisk(driveno);
		
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "Disk ejected from drive " + driveno);
			dwVSerialPorts.writeToCoco(this.vport, "Disk ejected from drive " + driveno);

		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive # '" + drivestr + "'");
			
		} 
		catch (DWDriveNotValidException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 20,e.getMessage());
			
		} 
		catch (DWDriveNotLoadedException e) 
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 20,e.getMessage());
			
		}
		
		return;
		
	}
		
		
	private void doDiskWPToggle(String drivestr) 
	{
		
		try
		{
			int driveno = Integer.parseInt(drivestr);
	
			if (DriveWireServer.getHandler(handlerno).getDiskDrives().getWriteProtect(driveno))
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().setWriteProtect(driveno, false);
				dwVSerialPorts.sendUtilityOKResponse(this.vport, "write protect toggle succeeded");
			    dwVSerialPorts.writeToCoco(this.vport, "Disk in drive " + driveno + " is now writeable.");
			}
			else
			{
				DriveWireServer.getHandler(handlerno).getDiskDrives().setWriteProtect(driveno, true);
				dwVSerialPorts.sendUtilityOKResponse(this.vport, "write protect toggle succeeded");
			    dwVSerialPorts.writeToCoco(this.vport, "Disk in drive " + driveno + " is now write protected.");
			}
			
		}
		catch (NumberFormatException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: non numeric drive # '" + drivestr + "'");
			
		} 
		
		return;
	}
		
	


	private void doList(String path) 
	{
		String text = new String();
		
		FileSystemManager fsManager;
		InputStream ins = null;
		FileObject fileobj = null;
		FileContent fc = null;
		
		try
		{
			fsManager = VFS.getManager();
		
			path = convertStarToBang(path);
			
			fileobj = fsManager.resolveFile(path);
		
			fc = fileobj.getContent();
			
			ins = fc.getInputStream();
			
			dwVSerialPorts.sendUtilityOKResponse(this.vport, "file data follows");
			
			int data = ins.read();
			
			while (data != -1)
			{
				dwVSerialPorts.writeToCoco(this.vport, (byte)data);
				
				text += Character.toString((char) data);
				data = ins.read();
						
			}
			
			logger.debug("text size: " + text.length());
		} 
		catch (FileSystemException e)
		{
			
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 8, e.getMessage());
	    	
		} catch (IOException e)
		{
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 9, e.getMessage());
	    }	
		finally
		{
			try
			{
				if (ins != null)
					ins.close();
				
				if (fc != null)
					fc.close();
				
				if (fileobj != null)
					fileobj.close();
				
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	private String convertStarToBang(String txt)
	{
	
		txt = txt.replaceAll("\\*", "!");
		
		return txt;
	}



	private void doDir(String path) 
	{
		FileSystemManager fsManager;
		
		String text = new String();
		
		path = convertStarToBang(path);
				
		try
		{
			fsManager = VFS.getManager();
		
			FileObject dirobj = fsManager.resolveFile(path);
			
			FileObject[] children = dirobj.getChildren();

			text += "Directory of " + dirobj.getName().getURI() + "\r\n\n";
		
			
			int longest = 0;
	    	
	    	for (int i=0; i<children.length; i++) 
	    	{
	    		if (children[i].getName().getBaseName().length() > longest)
	    			longest = children[i].getName().getBaseName().length();
	    	}
	    	
	    	longest++;
	    	longest++;
	    	
	    	int cols = (80 / longest);
	    	
	    	for (int i=0; i<children.length; i++) 
	        {
	        	text += String.format("%-" + longest + "s",children[i].getName().getBaseName());
	        	if (((i+1) % cols) == 0)
	        		text += "\r\n";
	        }
			
			
			
		} catch (FileSystemException e)
		{
			
			dwVSerialPorts.sendUtilityFailResponse(this.vport, 10, e.getMessage());
	    	return;
			
		}
		
	    dwVSerialPorts.sendUtilityOKResponse(this.vport, "directory follows");
	    dwVSerialPorts.writeToCoco(this.vport, text);
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
		
		dwVSerialPorts.sendUtilityOKResponse(this.vport, "help text follows");
	    dwVSerialPorts.writeToCoco(this.vport, text);
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
            Thread[] copy = new Thread[threads.length];
            System.arraycopy(threads, 0, copy, 0, threads.length);
	    return copy;
	}
	
}
