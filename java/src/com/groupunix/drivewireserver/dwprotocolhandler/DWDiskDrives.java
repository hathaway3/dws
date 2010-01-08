package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWDriveAlreadyLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;



public class DWDiskDrives 
{
	public static final int MAX_DRIVES = 256;
		
	private static DWDisk[] diskDrives = new DWDisk[MAX_DRIVES];
	
	private static final Logger logger = Logger.getLogger("DWServer.DWDiskDrives");
	
	
	
	
	public static void LoadDiskSet(String filename)
	{
		if (filename == null)
		{
			return;
		}
		
		EjectAllDisks();
		
		logger.info("loading diskset '" + filename + "'");
		
		File f = new File(filename);
		
	    FileReader fr;
		try 
		{
			fr = new FileReader(f);
		} 
		catch (FileNotFoundException e) 
		{
			logger.warn("Diskset file '" + filename + "' not found.");
			return;
		}
		
	    BufferedReader br = new BufferedReader(fr);

	    String line;
	    
		try 
		{
			line = br.readLine();
		    
			while (line != null) 
		    {
		    	String[] parts = new String[3];
		    	
		    	parts = line.split(",", 3);
		    	DWDisk tmpdisk = new DWDisk();
		    	
		    	tmpdisk.setFilePath(parts[1]);
		    	
		    	if (parts[2].equals("1"))
		    		tmpdisk.setWriteProtect(true);
		    	
		    	LoadDisk(Integer.parseInt(parts[0]), tmpdisk);
		    	
		    	line = br.readLine();
			}
		} 
		catch (IOException e1) 
		{
			logger.error(e1.getMessage());
		} 
		catch (NumberFormatException e2) 
		{
			logger.error(e2.getMessage());
		} 
		catch (DWDriveNotValidException e3) 
		{
			logger.error(e3.getMessage());
		} 
		catch (DWDriveAlreadyLoadedException e4) 
		{
			logger.error(e4.getMessage());
		}
		finally
		{
			try 
			{
				br.close();
				fr.close();
			} 
			catch (IOException e) 
			{
				logger.warn(e.getMessage());
			}
		}
	}
	
	
	public static void saveDiskSet(String filename)
	{
		// save current disk set to file
		
		if (filename != null)
		{
			try 
			{
		     
				FileWriter fstream = new FileWriter(filename);
				BufferedWriter out = new BufferedWriter(fstream);

			
				for (int i = 0;i < MAX_DRIVES;i++)
				{
					if (diskLoaded(i))
					{
						String tstr = i + "," + getDiskFile(i) + ",";
					
						if (getWriteProtect(i))
						{
							tstr = tstr + "1";
						}
						else
						{
							tstr = tstr + "0";
						}
					
						tstr = tstr + "\n";
						
						out.write(tstr);
					}
				}
			
				out.close();
			
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage());
			}
		}
	}
	
	
	public static void LoadDiskFromFile(int driveno, String path) throws FileNotFoundException, DWDriveNotValidException, DWDriveAlreadyLoadedException
	{
		DWDisk tmpdisk = new DWDisk();
    	
    	tmpdisk.setFilePath(path);
    	
    	LoadDisk(driveno, tmpdisk);
	}
	
	
	public static void LoadDisk(int driveno, DWDisk disk) throws DWDriveNotValidException, DWDriveAlreadyLoadedException
	{
		validateDriveNo(driveno);
		
		if (diskDrives[driveno] != null)
		{
			throw new DWDriveAlreadyLoadedException("There is already a disk in drive " + driveno);
		}
		else
		{
			diskDrives[driveno] = disk;
			logger.info("loaded disk '" + disk.getFilePath() + "' in drive " + driveno);
			
			logger.debug(disk.diskInfo());
			
		}
	}
	
	
	public static void EjectDisk(int driveno) throws DWDriveNotValidException, DWDriveNotLoadedException
	{
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno] = null;
		logger.info("ejected disk from drive " + driveno);
	}
	
	public static void EjectAllDisks()
	{
		for (int i=0;i<MAX_DRIVES;i++)
		{
			if (diskDrives[i] != null)
			{
				try 
				{
					EjectDisk(i);
				} 
				catch (DWDriveNotValidException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (DWDriveNotLoadedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static void seekSector(int driveno, long lsn) throws DWDriveNotLoadedException, DWDriveNotValidException
	{
		checkLoadedDriveNo(driveno);

		diskDrives[driveno].seekSector(lsn);
	}
	
	public static void writeSector(int driveno, byte[] data) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveWriteProtectedException, IOException
	{
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno].writeSector(data);
	}
	
	public static byte[] readSector(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, IOException
	{
		byte[] data;
		
		checkLoadedDriveNo(driveno);
		
		data = diskDrives[driveno].readSector();
	
		return(data);
	}
	
	
	public static void validateDriveNo(int driveno) throws DWDriveNotValidException
	{
		if ((driveno < 0) || (driveno >= MAX_DRIVES))
		{
			throw new DWDriveNotValidException("There is no drive " + driveno + ". Valid drives numbers are 0 - "  + (MAX_DRIVES - 1));
		}
	}
	
	private static void checkLoadedDriveNo(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException
	{
		validateDriveNo(driveno);
		
		if (diskDrives[driveno] == null)
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
	}


	public static boolean diskLoaded(int i) 
	{
		if (diskDrives[i] == null)
		{
			return false;
		}
		return true;
	}


	public static String getDiskFile(int i) 
	{
		if (diskDrives[i] != null)
		{
			return(diskDrives[i].getFilePath());
		}
		return null;
	}


	public static boolean getWriteProtect(int i) {
		if (diskDrives[i] != null)
		{
			return(diskDrives[i].getWriteProtect());
		}
		return false;
	}


	public static byte[] nullSector() 
	{
		byte[] tmp = new byte[256];
		
		for (int i = 0;i<256;i++)
			tmp[i] = (byte) 0;
		
		return(tmp);
	}


	public static void setWriteProtect(int driveno, boolean onoff) 
	{
		diskDrives[driveno].setWriteProtect(onoff);
	}
	
	public static String getDiskName(int driveno)
	{
		return(diskDrives[driveno].getDiskName());
	}
	
	public static long getDiskSectors(int driveno)
	{
		return(diskDrives[driveno].getDiskSectors());
	}
	
}
