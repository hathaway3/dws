package com.groupunix.drivewireserver.dwprotocolhandler;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;




public class DWDisk {

	private static final Logger logger = Logger.getLogger("DWServer.DWDisk");
	
	public static final int MAX_SECTORS = 32768; // what is coco's max?
	private int LSN;
	private String filePath;
	private boolean	wrProt = false;
	private DWDiskSector[] sectors = new DWDiskSector[MAX_SECTORS];
	
	private int reads = 0;
	private int writes = 0;
	
	
	public int DD_TOT()
	{
		byte[] dd_tot = new byte[3];
		System.arraycopy( sectors[0].getData(), 0, dd_tot, 0, 3 ); 
		return(DWProtocolHandler.int3(dd_tot));
	}
	
	public int DD_TKS()
	{
		return((int) sectors[0].getData()[3]);
	}
	
	public int DD_MAP()
	{
		byte[] dd_map = new byte[2];
		System.arraycopy( sectors[0].getData(), 4, dd_map, 0, 2 ); 
		return(DWProtocolHandler.int2(dd_map));
	}
	
	public int DD_BIT()
	{
		byte[] dd_bit = new byte[2];
		System.arraycopy( sectors[0].getData(), 6, dd_bit, 0, 2 ); 
		return(DWProtocolHandler.int2(dd_bit));
	}
	
	public int DD_DIR()
	{
		byte[] dd_dir = new byte[3];
		System.arraycopy( sectors[0].getData(), 8, dd_dir, 0, 3 ); 
		return(DWProtocolHandler.int3(dd_dir));
	}
	
	public int DD_OWN()
	{
		byte[] dd_own = new byte[2];
		System.arraycopy( sectors[0].getData(), 11, dd_own, 0, 2 ); 
		return(DWProtocolHandler.int2(dd_own));
	}
	
	public byte DD_ATT()
	{
		return(sectors[0].getData()[13]);
	}
	
	public long DD_DSK()
	{
		byte[] dd_dsk = new byte[2];
		System.arraycopy( sectors[0].getData(), 14, dd_dsk, 0, 2 ); 
		return(DWProtocolHandler.int2(dd_dsk));
	}
	
	public byte DD_FMT()
	{
		return(sectors[0].getData()[16]);
	}
	
	public long DD_SPT()
	{
		byte[] dd_spt = new byte[2];
		System.arraycopy( sectors[0].getData(), 17, dd_spt, 0, 2 ); 
		return(DWProtocolHandler.int2(dd_spt));
	}
	
	public long DD_BT()
	{
		byte[] dd_bt = new byte[3];
		System.arraycopy( sectors[0].getData(), 21, dd_bt, 0, 3 ); 
		return(DWProtocolHandler.int3(dd_bt));
	}
	
	public long DD_BSZ()
	{
		byte[] dd_bsz = new byte[2];
		System.arraycopy( sectors[0].getData(), 24, dd_bsz, 0, 2 ); 
		return(DWProtocolHandler.int2(dd_bsz));
	}
	
	public byte[] DD_DAT()
	{
		byte[] dd_dat = new byte[5];
		System.arraycopy( sectors[0].getData(), 26, dd_dat, 0, 5 ); 
		return(dd_dat);
	}
	
	public byte[] DD_NAM()
	{
		byte[] dd_nam = new byte[32];
		System.arraycopy( sectors[0].getData(), 31, dd_nam, 0, 32 ); 
		return(dd_nam);
	}
	
	public byte[] DD_OPT()
	{
		byte[] dd_opt = new byte[32];
		System.arraycopy( sectors[0].getData(), 63, dd_opt, 0, 32 ); 
		return(dd_opt);
	}
	
	
	public String diskInfo()
	{
		String ret = new String();
		ret = "Disk name: '" + cocoString(DD_NAM()) + "'";
		
		ret += ", Total sectors: " + DD_TOT();
		
		ret += ", Root dir @ " + DD_DIR();
		return(ret);
	}
	
	public synchronized String getDiskName()
	{
		return(cocoString(DD_NAM()));
	}
	
	public synchronized int getDiskSectors()
	{
		int num = 0;
		
		for (int i = 0;i<MAX_SECTORS;i++)
		{
			if (this.sectors[i] != null)
			{
				num++;
			}
		}
		
		return(num);
	}
	
	
	private String cocoString(byte[] bytes) 
	{
		// return string from 6809 style string
		String ret = new String();
		
		int i = 0;
		
		// thanks Christopher Hawks
		while ((i < bytes.length - 1) && (bytes[i] > 0))
		{
			ret += Character.toString((char) bytes[i]);
			i++;
		}
		
		ret += Character.toString((char) (bytes[i] + 128));
		
		return(ret);
	}

	public synchronized void seekSector(int newLSN)
	{
		// TODO should check that the sector exists..
		this.LSN = newLSN;
		// logger.debug("seek to sector " + newLSN + " for '" + this.filePath + "'");
	}

	public int getLSN()
	{
		return(this.LSN);
	}
	
	public synchronized void setWriteProtect(boolean wp)
	{
		if (wp)
		{
			logger.debug("write protecting '" + this.filePath + "'");
		}
		else
		{
			logger.debug("write enabling '" + this.filePath + "'");
		}
		
		this.wrProt = wp;
	}
	
	public synchronized boolean getWriteProtect()
	{
		return(this.wrProt);
	}
	
	public synchronized void setFilePath(String fp) throws FileNotFoundException
	{
		// check file exists, maybe should check read/write access too
		File tmp = new File(fp);
		if (tmp.exists())
		{
			this.filePath = fp;
			logger.debug("set filepath to '" + fp + "'");
			
			loadSectors(tmp);
			
		}
		else
		{
			throw new FileNotFoundException();
		}	
	}
	
	
	private void loadSectors(File file) 
	{
		// load file into sector array
	    FileInputStream fis;
		try 
		{
			fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
	        DataInputStream dis = new DataInputStream(bis);

	        int sector = 0;
	        	        
		    while (dis.available() != 0) 
		    {
		    	byte[] buffer = new byte[256];
		    	int bytesRead = dis.read(buffer, 0, 256);
		    	
		    	this.sectors[sector] = new DWDiskSector(sector);
		    	this.sectors[sector].setData(buffer, false);
		    	
		    	if (bytesRead != 256)
		    	{
		    		logger.error("did not get 256 bytes for sector " + sector + ", got " + bytesRead);
		    	}
		    	
		    	sector++;
		    }
		     
		    // dispose all the resources after using them.
		    fis.close();
		    bis.close();
		    dis.close();
		    

		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        	
	}

	
	
	public String getFilePath()
	{
		return(this.filePath);
	}
	
	
	public synchronized byte[] readSector() throws IOException
	{
		// logger.debug("Read sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));
		this.reads++;
		
		if (this.sectors[this.LSN] == null)
		{
			logger.debug("request for undefined sector " + this.LSN);
			this.sectors[this.LSN] = new DWDiskSector(this.LSN);
		}
		
		return(this.sectors[this.LSN].getData());	
	}
	
	public synchronized void writeSector(byte[] data) throws DWDriveWriteProtectedException, IOException
	{
		
		if (this.wrProt)
		{
			throw new DWDriveWriteProtectedException("Disk is write protected");
		}
		else
		{
			if (sectors[this.LSN] == null)
			{
				// expand disk / add sector
				this.sectors[this.LSN] = new DWDiskSector(this.LSN);
				logger.debug("new sector " + this.LSN);
			}
			sectors[this.LSN].setData(data);
			
			this.writes++;
			
			// logger.debug("write sector " + this.LSN + "\r" + DWProtocolHandler.byteArrayToHexString(this.sectors[this.LSN].getData()));

			
		}
	}

	public int getReads() {
		return reads;
	}


	public int getWrites() {
		return writes;
	}

	public synchronized int getDirtySectors() 
	{
		int drt = 0;
		
		for (int i=0;i<MAX_SECTORS;i++)
		{
			if (this.sectors[i] != null)
			{
				if (this.sectors[i].isDirty())
				{
					drt++;
				}
			}
		}
		
		return(drt);
	}
	
	public synchronized DWDiskSector getSector(int no)
	{
		return(this.sectors[no]);
	}
}
