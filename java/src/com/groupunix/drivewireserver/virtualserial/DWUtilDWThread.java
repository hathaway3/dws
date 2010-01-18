package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

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
		
		
		if (((args.length >= 2) && (args[1].toLowerCase().startsWith("h"))) || (args.length == 1))
		{
			doHelp();
		}
		else if ((args.length == 3) && (args[1].toLowerCase().startsWith("s")))
		{
			doShow(args[2]);
		}
		else if ((args.length >= 2) && (args[1].toLowerCase().startsWith("d")))
		{
			doDir(args);
		}
		else if ((args.length >= 3) && (args[1].toLowerCase().startsWith("li")))
		{
			doList(strargs.substring(8));
		}
		else if ((args.length == 4) && (args[1].toLowerCase().startsWith("lo")))
		{
		    doDiskInsert(args[2],args[3]);
		}
		else if ((args.length == 3) && (args[1].toLowerCase().startsWith("e")))
		{
			doDiskEject(args[2]);
		}
		else if ((args.length == 3) && (args[1].toLowerCase().startsWith("w")))
		{
			doDiskWPToggle(args[2]);
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
	
	
	private void doDir(String[] args) 
	{
		String text = new String();
		String path = ".";
		
		if (args.length > 2)
		{
			path = args[2];
		}
	
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
	
	

	private void doShow(String arg) 
	{
		String text = new String();
		
		if (arg.toLowerCase().startsWith("d"))
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
		else if (arg.toLowerCase().startsWith("pd"))
		{
			text += "\r\nCurrent port device descriptors:\r\n\n";
			
			for (int i = 0;i<DWVSerialPorts.MAX_PORTS;i++)
			{
				text += DWVSerialPorts.prettyPort(i) + " ";
				
				if (i<10)
					text += " ";
			
				text += DWProtocolHandler.byteArrayToHexString(DWVSerialPorts.getDD(i));
				
				text += "\r\n";
			}
			
		}
		else if (arg.toLowerCase().startsWith("l"))
		{
			text += "\r\nDriveWire Server Log:\r\n\n";
			
			ArrayList<String> loglines = DriveWireServer.getLogEvents(10);
			
			for (int i = 0;i<loglines.size();i++)
			{
				text += loglines.get(i);
				text += "\r\n";
			}
			
			
		}
		else if (arg.toLowerCase().startsWith("po"))
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
		else if (arg.toLowerCase().startsWith("s"))
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
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + arg + "' is not a valid option to show.");
	    	return;
		}
		
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "command response follows");
	    DWVSerialPorts.writeToCoco(this.vport, text);
	}

	private void doHelp()
	{
		String text = new String();
		
		text = "\r\nHelp for DriveWire " + DriveWireServer.DWServerVersion + " commands:";
		
		text += "\r\n\n";
		
		text += "  dw show [disks|ports|pdesc|status] - Show various system information.";
				
		text += "\r\n\n";
		
		text += "  dw load [drive number] [filename] - Load disk image into drive.";
		
		text += "\r\n";
		
		text += "  dw eject [drive number] - Eject disk.";
		
		text += "\r\n";
		
		text += "  dw wp [drive number] - Toggle write protect on disk.";
		
		text += "\r\n\n";
		
		text += "  dw dir [server path] - Show directory in server's filesystem.";
		
		text += "\r\n";
		
		text += "  dw list [server path] - List contents of file in server's filesystem.";
		
		text += "\r\n";
		
		text += "  dw copy [local path] [to|from] [server path] - Copy file from/to server.";
		
		
		text += "\r\n\n";
		
		text += "  (all commands may be abbreviated to their shortest unique form)\r\n";
		
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "help text follows");
	    DWVSerialPorts.writeToCoco(this.vport, text);
	}
	
}
