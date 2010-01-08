package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwprotocolhandler.DWDiskDrives;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWUtilDWThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilWgetThread");
	
	private int vport = -1;
	private String strargs = null;
	private static final int CHUNK_SIZE = 240;
	
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
		String response = new String();
		
		response = "'" + this.strargs + "' " + args.length + " ?";
		
		if ((args.length == 1) && ((args[0].equalsIgnoreCase("help")) || (args[0].equals(""))))
		{
			response = helpText();
		}
		else if ((args.length == 2) && (args[0].equalsIgnoreCase("show")))
		{
			response = showText(args[1]);
		}
		else if ((args.length >= 1) && (args[0].equalsIgnoreCase("dir")))
		{
			response = dirText(args);
		}
		else if ((args.length >= 2) && (args[0].equalsIgnoreCase("list")))
		{
			response = listText(strargs.substring(5));
		}
		else if ((args.length >= 2) && (args[0].equalsIgnoreCase("wall")))
		{
			response = wallText(strargs.substring(5));
		}
		
		writeSections(response);
	}

	
	private String wallText(String msg) 
	{
		String text = "Sending your announcement to all active telnet ports";
		
		// send message
		for (int i = 0;i<DWVSerialPorts.MAX_PORTS;i++)
		{
			if ((DWVSerialPorts.getMode(i) == DWVSerialPorts.MODE_TELNET) && DWVSerialPorts.isConnected(i))
			{
				DWVSerialPorts.serWriteM(i, "\r\n\n\n***** " + msg + " *****\r\n\n");
			}
		}
		
		return(text);
	}

	private String listText(String path) 
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
			text = "File not found.";
		} 
		catch (IOException e1) 
		{
			text = "IO Error: " + e1.getMessage();
		}
    
	    	
		return(text);
	}
	
	
	private String dirText(String[] args) 
	{
		String text = new String();
		String path = ".";
		
		if (args.length > 1)
		{
			path = args[1];
		}
	
		File dir = new File(path);
	    
	    String[] children = dir.list();
	    if (children == null) 
	    {
	        text = "Either '" + path + "' does not exist or it is not a directory";
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
		
		return(text);
	}
	
	

	private String showText(String arg) 
	{
		String text = new String();
		
		if (arg.equalsIgnoreCase("disks"))
		{
			text = "\r\nDriveWire disks:\r\n\n";
			
			for (int i = 0;i<DWDiskDrives.MAX_DRIVES;i++)
			{
				if (DWDiskDrives.diskLoaded(i))
				{
					text += "Drive X"+i;
					text += ": " + DWDiskDrives.getDiskFile(i);
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
					text += String.format("enabled, %-10s", DWVSerialPorts.getPrettyMode(DWVSerialPorts.getMode(i)));
					
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
		else if (arg.equalsIgnoreCase("stats"))
		{
			text += "\r\nDriveWire status:\r\n\n";
			
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
			text = "No info for '" + arg + "'";
		}
		
		return(text);
	}

	private String helpText()
	{
		String text = new String();
		
		text = "\r\nHelp for DriveWire server commands:";
		
		text += "\r\n\n";
		
		text += "  dw show [disks|ports|stats] - Show various status information.";
				
		text += "\r\n\n";
		
		text += "  dw load [drive number] [filename] - load disk image into drive.";
		
		text += "\r\n";
		
		text += "  dw eject [drive number] - eject disk.";
		
		text += "\r\n\n";
		
		text += "  dw dir [path] - show directory in server's filesystem.";
		
		text += "\r\n";
		
		text += "  dw list [path] - list contents of file in server's filesystem.";
		
		text += "\r\n";
		
		return(text);
	}
	
	
	private void writeSections(String data) 
	{
		String dataleft = data;
		
		// send data in (numbytes)(bytes) format
		
		while (dataleft.length() > CHUNK_SIZE)
		{
			// send a chunk
			
			write1((byte) CHUNK_SIZE);
			write(dataleft.substring(0, CHUNK_SIZE));
			dataleft = dataleft.substring(CHUNK_SIZE);
			logger.debug("sent chunk, left: " + dataleft.length());
		}
		
		// send last chunk
		int bytes = dataleft.length();
		
		write1((byte) bytes);
		
		write(dataleft);
		
		// send termination
		write1((byte) 0);
		
	}

	
	private void write1(byte data)
	{
		try 
		{
			DWVSerialPorts.getPortInput(this.vport).write(data);
		} 
		catch (IOException e) 
		{
			logger.error("IO error writing to port T" + this.vport);
		}
	}

	private void write(String str)
	{
		
		try 
		{
			DWVSerialPorts.getPortInput(this.vport).write(str.getBytes());
		} 
		catch (IOException e) 
		{
			logger.error("IO error writing to port T" + this.vport);
		}
	}
	
}
