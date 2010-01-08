package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	private DWVSerialCircularBuffer input;
	
	public DWUtilDWThread(int vport, DWVSerialCircularBuffer utilstream, String args)
	{
		this.vport = vport;
		this.strargs = args;
		this.input = utilstream;
		logger.debug("init dw util thread");	
	}
	
	public void run() 
	{
		
		Thread.currentThread().setName("dwutil-" + Thread.currentThread().getId());
		
		logger.debug("run");
		
		String[] args = this.strargs.split(" ");
		
		if (((args.length >= 2) && (args[1].equalsIgnoreCase("help"))) || (args.length == 1))
		{
			doHelp();
		}
		else if ((args.length == 3) && (args[1].equalsIgnoreCase("show")))
		{
			doShow(args[2]);
		}
		else if ((args.length >= 2) && (args[1].equalsIgnoreCase("dir")))
		{
			doDir(args);
		}
		else if ((args.length >= 3) && (args[1].equalsIgnoreCase("list")))
		{
			doList(strargs.substring(8));
		}
		else if ((args.length >= 3) && (args[1].equalsIgnoreCase("wall")))
		{
			doWall(strargs.substring(8));
		}
		else if ((args.length == 4) && (args[1].equalsIgnoreCase("load")))
		{
		    doDiskInsert(args[2],args[3]);
		}
		else if ((args.length == 3) && (args[1].equalsIgnoreCase("eject")))
		{
			doDiskEject(args[2]);
		}
		else
		{
			// unknown command/syntax
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2,"Syntax error: Unknown command '" + args[1] + "'");
		}
	
		// wait for output to flush
		// wait for output
		try {
			while ((DWVSerialPorts.bytesWaiting(this.vport) > 0) && (DWVSerialPorts.isCocoInit(this.vport)))
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
		
			DWDiskDrives.validateDriveNo(driveno);
			
			// eject any current disk
			if (DWDiskDrives.diskLoaded(driveno))
			{
				DWDiskDrives.EjectDisk(driveno);
			}
			
			// load new disk
			
			DWDiskDrives.LoadDiskFromFile(driveno, path);
		
			DWVSerialPorts.sendUtilityOKResponse(this.vport, "Disk inserted in drive " + driveno);
			
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
	
			DWDiskDrives.EjectDisk(driveno);
		
			DWVSerialPorts.sendUtilityOKResponse(this.vport, "Disk ejected from drive " + driveno);
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
		
		
		
	
	private void doWall(String msg) 
	{
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "Sending announcement");
		DWVSerialPorts.write(this.vport, "Sending your announcement to all active telnet ports\r\n");
		
		// send message
		for (int i = 0;i<DWVSerialPorts.MAX_PORTS;i++)
		{
			if ((DWVSerialPorts.getMode(i) == DWVSerialPorts.MODE_TELNET) && DWVSerialPorts.isConnected(i))
			{
				DWVSerialPorts.serWriteM(i, "\r\n\n***** " + msg + " *****\r\n\n");
			}
		}
		
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
	    DWVSerialPorts.write(this.vport, text);
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
	    DWVSerialPorts.write(this.vport, text);
	}
	
	

	private void doShow(String arg) 
	{
		String text = new String();
		
		if (arg.equalsIgnoreCase("disks"))
		{
			text = "\r\nCurrent DriveWire Disks:\r\n\n";
			
			for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
			{
				if (DWDiskDrives.diskLoaded(i))
				{
					text += "X"+i;
					text += ": " + String.format("%-32s",DWDiskDrives.getDiskFile(i));
					text += String.format("%-34s", DWDiskDrives.getDiskName(i));
					text += " " + DWDiskDrives.getDiskSectors(i);
					text += "\r\n";
				}
			}
			
		}
		else if (arg.equalsIgnoreCase("ports"))
		{
			text += "\r\nCurrent port status:\r\n\n";
			
			for (int i = 0;i<DWVSerialPorts.MAX_PORTS;i++)
			{
				text += DWVSerialPorts.prettyPort(i) + " ";
				
				if (DWVSerialPorts.isEnabled(i))
				{
					text += String.format("enabled, %-10s", DWVSerialPorts.prettyMode(DWVSerialPorts.getMode(i)));
					
					if (DWVSerialPorts.isCocoInit(i))
					{
						text += "inized      ";
					}
					else
					{
						text += "not inized  ";
					}
					
					text += String.format("%-15s", DWVSerialPorts.getActionFile(i));
					
					if ((DWVSerialPorts.getMode(i) == 0) || (DWVSerialPorts.getMode(i) == 1))
					{
						if (DWVSerialPorts.isConnected(i))
						{
							
							text += DWVSerialPorts.getHostIP(i);
							text += ":" + DWVSerialPorts.getHostPort(i);
						}
						else
						{
							text += "not connected";
						}
					}
				}
				else
				{
					text += "disabled";
				}
				
				text += "\r\n";
			}
			
		}
		else if (arg.equalsIgnoreCase("status"))
		{
			text += "\r\nDriveWire Status:\r\n\n";
			
			text += "Device:        " + DriveWireServer.config.getString("SerialDevice","unknown") + "\r\n";
			text += "CoCo Type:     " + DriveWireServer.config.getInt("CocoModel", 0) + "\r\n";
			
			text += "\r\n";
			
			text += "Last OpCode:   " + (int) (DWProtocolHandler.getLastOpcode() & 0xFF) + "\r\n";
			text += "Last GetStat:  " + (int) (DWProtocolHandler.getLastGetStat() & 0xFF) + "\r\n";
			text += "Last SetStat:  " + (int) (DWProtocolHandler.getLastSetStat() & 0xFF) + "\r\n";
			text += "Last Drive:    " + DWProtocolHandler.getLastDrive() + "\r\n";
			text += "Last LSN:      " + DWProtocolHandler.long3(DWProtocolHandler.getLastLSN()) + "\r\n";
			text += "Last Error:    " + (int) (DWProtocolHandler.getLastError() & 0xFF) + "\r\n";
			
			text += "\r\n";
			
			text += "Read Sectors:  " + DWProtocolHandler.getSectorsRead() + "\r\n";
			text += "Read Retries:  " + DWProtocolHandler.getReadRetries() + "\r\n";
			text += "Write Sectors: " + DWProtocolHandler.getSectorsWritten() + "\r\n";
			text += "Write Retries: " + DWProtocolHandler.getWriteRetries() + "\r\n";
			
			text += "\r\n";
			
		}
		else
		{
			DWVSerialPorts.sendUtilityFailResponse(this.vport, 2, "Syntax error: '" + arg + "' is not a valid option to show.");
	    	return;
		}
		
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "command response follows");
	    DWVSerialPorts.write(this.vport, text);
	}

	private void doHelp()
	{
		String text = new String();
		
		text = "\r\nHelp for DriveWire server commands:";
		
		text += "\r\n\n";
		
		text += "  dw show [disks|ports|status] - Show various system information.";
				
		text += "\r\n\n";
		
		text += "  dw load [drive number] [filename] - load disk image into drive.";
		
		text += "\r\n";
		
		text += "  dw eject [drive number] - eject disk.";
		
		text += "\r\n\n";
		
		text += "  dw dir [path] - show directory in server's filesystem.";
		
		text += "\r\n";
		
		text += "  dw list [path] - list contents of file in server's filesystem.";
		
		text += "\r\n";
		
		DWVSerialPorts.sendUtilityOKResponse(this.vport, "help text follows");
	    DWVSerialPorts.write(this.vport, text);
	}
	
}
