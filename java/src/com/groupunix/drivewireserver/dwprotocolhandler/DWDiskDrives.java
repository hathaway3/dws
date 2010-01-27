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
	
	private DWDisk[] diskDrives = new DWDisk[MAX_DRIVES];
	private static final Logger logger = Logger.getLogger("DWServer.DWDiskDrives");
	private Thread lazyWriterThread;
	
	public DWDiskDrives()
	{
		logger.debug("disk drives init");
		
		// start lazy writer
		lazyWriterThread = new Thread(new DWDiskLazyWriter());
		lazyWriterThread.start();
		
	}
	
	
	public void LoadDiskSet(String filename)
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
		    
			while ((line = br.readLine()) != null)
		    {
				
		    	String[] parts = new String[3];
		    	parts = line.split(",", 3);
		    	
		    	if (parts != null)
		    	{
		    		if ((parts.length == 3) && (!line.startsWith("#")))
		    		{
		    			
		    			DWDisk tmpdisk = new DWDisk();
		    			
		    			try 
		    			{
							tmpdisk.setFilePath(parts[1]);
							if (parts[2].equals("1"))
			    				tmpdisk.setWriteProtect(true);
			    			LoadDisk(Integer.parseInt(parts[0]), tmpdisk);
						} 
		    			catch (FileNotFoundException e) 
		    			{
		    				logger.warn("File not found attempting to load drive " + parts[0]);
		    			}
		    			
		    		}
		    	}	
			}
		} 
		catch (NumberFormatException e2) 
		{
			logger.error("NumberFormat: " + e2.getMessage());
		} 
		catch (DWDriveNotValidException e3) 
		{
			logger.error("DriveNotValid: " + e3.getMessage());
		} 
		catch (DWDriveAlreadyLoadedException e4) 
		{
			logger.error("DriveAlreadyLoaded: " + e4.getMessage());
		} 
		catch (IOException e) 
		{
			logger.error("IO error: " + e.getMessage());
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
	
	
	public void saveDiskSet(String filename)
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
	
	
	public  void LoadDiskFromFile(int driveno, String path) throws FileNotFoundException, DWDriveNotValidException, DWDriveAlreadyLoadedException
	{
		DWDisk tmpdisk = new DWDisk();
    	
    	tmpdisk.setFilePath(path);
    	
    	LoadDisk(driveno, tmpdisk);
	}
	
	
	public void LoadDisk(int driveno, DWDisk disk) throws DWDriveNotValidException, DWDriveAlreadyLoadedException
	{
		validateDriveNo(driveno);
		
		if (diskDrives[driveno] != null)
		{
			throw new DWDriveAlreadyLoadedException("There is already a disk in drive " + driveno);
		}
		else
		{
			diskDrives[driveno] = disk;
			logger.info("loaded disk '" + disk.getFilePath() + "' in drive " + driveno + " with checksum " + disk.getChecksum());
			
			logger.debug(disk.diskInfo());
			
		}
	}
	
	public void ReLoadDisk(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, FileNotFoundException, DWDriveAlreadyLoadedException
	{
		if (diskDrives[driveno] == null)
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
		
		String filename = diskDrives[driveno].getFilePath();
		
		EjectDisk(driveno);
		LoadDiskFromFile(driveno,filename);
		
	}
	
	
	public void EjectDisk(int driveno) throws DWDriveNotValidException, DWDriveNotLoadedException
	{
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno] = null;
		logger.info("ejected disk from drive " + driveno);
	}
	
	public void EjectAllDisks()
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
	
	
	public void seekSector(int driveno, int lsn) throws DWDriveNotLoadedException, DWDriveNotValidException
	{
		checkLoadedDriveNo(driveno);

		diskDrives[driveno].seekSector(lsn);
	}
	
	public void writeSector(int driveno, byte[] data) throws DWDriveNotLoadedException, DWDriveNotValidException, DWDriveWriteProtectedException, IOException
	{
		checkLoadedDriveNo(driveno);
		
		diskDrives[driveno].writeSector(data);
	}
	
	public byte[] readSector(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException, IOException
	{
		checkLoadedDriveNo(driveno);
		
		return(diskDrives[driveno].readSector());
	
	}
	
	
	public void validateDriveNo(int driveno) throws DWDriveNotValidException
	{
		if ((driveno < 0) || (driveno >= MAX_DRIVES))
		{
			throw new DWDriveNotValidException("There is no drive " + driveno + ". Valid drives numbers are 0 - "  + (MAX_DRIVES - 1));
		}
	}
	
	private void checkLoadedDriveNo(int driveno) throws DWDriveNotLoadedException, DWDriveNotValidException
	{
		validateDriveNo(driveno);
		
		if (diskDrives[driveno] == null)
		{
			throw new DWDriveNotLoadedException("There is no disk in drive " + driveno);
		}
	}


	public boolean diskLoaded(int i) 
	{
		if (diskDrives[i] == null)
		{
			return false;
		}
		return true;
	}


	public String getDiskFile(int i) 
	{
		if (diskDrives[i] != null)
		{
			return(diskDrives[i].getFilePath());
		}
		return null;
	}


	public boolean getWriteProtect(int i) {
		if (diskDrives[i] != null)
		{
			return(diskDrives[i].getWriteProtect());
		}
		return false;
	}


	public byte[] nullSector() 
	{
		byte[] tmp = new byte[256];
		
		for (int i = 0;i<256;i++)
			tmp[i] = (byte) 0;
		
		return(tmp);
	}


	public void setWriteProtect(int driveno, boolean onoff) 
	{
		diskDrives[driveno].setWriteProtect(onoff);
	}
	
	public String getDiskName(int driveno)
	{
		return(diskDrives[driveno].getDiskName());
	}
	
	public int getDiskSectors(int driveno)
	{
		return(diskDrives[driveno].getDiskSectors());
	}


	public int getLSN(int i) 
	{
		return(diskDrives[i].getLSN());
	}


	public int getReads(int i) 
	{
		return(diskDrives[i].getReads());
	}


	public int getWrites(int i) 
	{
		return(diskDrives[i].getWrites());
	}


	public int getDirtySectors(int i) 
	{
		return(diskDrives[i].getDirtySectors());
	}


	public DWDisk getDisk(int driveno) 
	{
		return(diskDrives[driveno]);
	}


	public String getChecksum(int driveno)
	{
		return(diskDrives[driveno].getChecksum());
	}
	
	public String getDiskChecksum(int driveno)
	{
		return(diskDrives[driveno].getMD5Checksum(diskDrives[driveno].getFilePath()));
	}
	
	public void syncDisk(int driveno)
	{
		diskDrives[driveno].syncDisk();
	}


	public void mergeMemWithDisk(int driveno)
	{
		diskDrives[driveno].mergeMemWithDisk();		
	}
	
}
