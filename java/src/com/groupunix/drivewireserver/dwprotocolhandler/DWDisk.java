package com.groupunix.drivewireserver.dwprotocolhandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;




public class DWDisk {

	private static final Logger logger = Logger.getLogger("DWServer.DWDisk");
	
	private long LSN;
	private String filePath;
	private File fh;
	private MappedByteBuffer diskBuf;
	private FileChannel rwChannel;
	
	private boolean	wrProt = false;
	
	public long DD_TOT()
	{
		byte[] dd_tot = new byte[3];
		diskBuf.position(0);
		diskBuf.get(dd_tot, 0, 3); 
		return(long3(dd_tot));
	}
	
	public int DD_TKS()
	{
		byte[] dd_tks = new byte[1];
		diskBuf.position(3);
		diskBuf.get(dd_tks, 0, 1); 
		return((int) dd_tks[0]);
	}
	
	public long DD_MAP()
	{
		byte[] dd_map = new byte[2];
		diskBuf.position(4);
		diskBuf.get(dd_map, 0, 2); 
		return(long2(dd_map));
	}
	
	public long DD_BIT()
	{
		byte[] dd_bit = new byte[2];
		diskBuf.position(6);
		diskBuf.get(dd_bit, 0, 2); 
		return(long2(dd_bit));
	}
	
	public long DD_DIR()
	{
		byte[] dd_dir = new byte[3];
		diskBuf.position(8);
		diskBuf.get(dd_dir, 0, 3); 
		return(long3(dd_dir));
	}
	
	public long DD_OWN()
	{
		byte[] dd_own = new byte[2];
		diskBuf.position(11);
		diskBuf.get(dd_own, 0, 2); 
		return(long2(dd_own));
	}
	
	public byte DD_ATT()
	{
		byte[] dd_att = new byte[1];
		diskBuf.position(13);
		diskBuf.get(dd_att, 0, 1); 
		return(dd_att[0]);
	}
	
	public long DD_DSK()
	{
		byte[] dd_dsk = new byte[2];
		diskBuf.position(14);
		diskBuf.get(dd_dsk, 0, 2); 
		return(long2(dd_dsk));
	}
	
	public byte DD_FMT()
	{
		byte[] dd_fmt = new byte[1];
		diskBuf.position(16);
		diskBuf.get(dd_fmt, 0, 1); 
		return(dd_fmt[0]);
	}
	
	public long DD_SPT()
	{
		byte[] dd_spt = new byte[2];
		diskBuf.position(17);
		diskBuf.get(dd_spt, 0, 2); 
		return(long2(dd_spt));
	}
	
	public long DD_BT()
	{
		byte[] dd_bt = new byte[3];
		diskBuf.position(21);
		diskBuf.get(dd_bt, 0, 3); 
		return(long3(dd_bt));
	}
	
	public long DD_BSZ()
	{
		byte[] dd_bsz = new byte[2];
		diskBuf.position(24);
		diskBuf.get(dd_bsz, 0, 2); 
		return(long2(dd_bsz));
	}
	
	public byte[] DD_DAT()
	{
		byte[] dd_dat = new byte[5];
		diskBuf.position(26);
		diskBuf.get(dd_dat, 0, 5); 
		return(dd_dat);
	}
	
	public byte[] DD_NAM()
	{
		byte[] dd_nam = new byte[32];
		diskBuf.position(31);
		diskBuf.get(dd_nam, 0, 32); 
		return(dd_nam);
	}
	
	public byte[] DD_OPT()
	{
		byte[] dd_opt = new byte[32];
		diskBuf.position(63);
		diskBuf.get(dd_opt, 0, 32); 
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
	
	public String getDiskName()
	{
		return(cocoString(DD_NAM()));
	}
	
	public long getDiskSectors()
	{
		return(DD_TOT());
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

	public void seekSector(long newLSN)
	{
		// TODO should check that the sector exists..
		this.LSN = newLSN;
		// logger.debug("seek to sector " + newLSN + " for '" + this.filePath + "'");
	}

	public long getLSN()
	{
		return(this.LSN);
	}
	
	public void setWriteProtect(boolean wp)
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
	
	public boolean getWriteProtect()
	{
		return(this.wrProt);
	}
	
	public void setFilePath(String fp) throws FileNotFoundException
	{
		// check file exists, maybe should check read/write access too
		File tmp = new File(fp);
		if (tmp.exists())
		{
			this.filePath = fp;
			this.fh = new File(fp);
			
			this.rwChannel = new RandomAccessFile(fh, "rw").getChannel();
	        
			try 
			{
				this.diskBuf = this.rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4718592);
				logger.debug("created memmap (using " + rwChannel.size() + " bytes)");
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			logger.debug("set filepath to '" + fp + "'");
		}
		else
		{
			throw new FileNotFoundException();
		}	
	}
	
	
	public String getFilePath()
	{
		return(this.filePath);
	}
	
	
	public byte[] readSector() throws IOException
	{
	// read 256 bytes from pos lastLSN of file into lastSector
		
		int pos = (int) (this.LSN * 256);
		
		// logger.debug("reading sector " + this.LSN + " (" + pos + ") from '" + this.filePath + "'");
		
		byte[] buf = new byte[256];
		    
		try
		{
			diskBuf.position(pos);
			diskBuf.get(buf, 0, 256);
		}
		catch (Exception e)
		{
			logger.error("DISK ERROR: " + e.getMessage());
		}
		
		
		return(buf);	
	}
	
	public void writeSector(byte[] data) throws DWDriveWriteProtectedException, IOException
	{
		// write 256 bytes from lastSector at pos lastLSN of file
	
		if (this.wrProt)
		{
			throw new DWDriveWriteProtectedException("Disk is write protected");
		}
		else
		{
			int pos =  (int) (this.LSN * 256);
		    
			// logger.debug("writing to sector " + this.LSN + " (" + pos + ") in '" + this.filePath + "'");
			
			diskBuf.position(pos);
			diskBuf.put(data, 0, 256);
		}
	}

	public static long long4(byte[] data) 
	{
		 return(((data[0] & 0xFF) << 32) + ((data[1] & 0xFF) << 16) + ((data[2] & 0xFF) << 8) + (data[3] & 0xFF));
	}

	
	public static long long3(byte[] data) 
	{
		 return((data[0] & 0xFF) << 16) + ((data[1] & 0xFF) << 8) + (data[2] & 0xFF);
	}

	
	public static long long2(byte[] data) 
	{
		 return((data[0] & 0xFF) << 8) + (data[1] & 0xFF);
	}

	
	
}
