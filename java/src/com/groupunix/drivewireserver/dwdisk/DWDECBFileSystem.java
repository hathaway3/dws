package com.groupunix.drivewireserver.dwdisk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.groupunix.drivewireserver.DECBDefs;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemBadFilenameException;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemFullException;
import com.groupunix.drivewireserver.dwexceptions.DWDECBFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;

public class DWDECBFileSystem
{
	private Vector<DWDiskSector> sectors;
	
	
	public DWDECBFileSystem(DWDisk disk)
	{
		this.sectors = disk.sectors;

	}
	
	
	public DWDECBFileSystem(Vector<DWDiskSector> sectors)
	{
		this.sectors = sectors;
	}


	public List<DWDECBFileSystemDirEntry> getDirectory()
	{
		List<DWDECBFileSystemDirEntry> dir = new ArrayList<DWDECBFileSystemDirEntry>();
		
		for (int i = 0;i<9;i++)
		{
			for (int j = 0;j<8;j++)
			{
				byte[] buf = new byte[32];
				System.arraycopy(sectors.get(i + DECBDefs.DIRECTORY_OFFSET).getData(), 32*j , buf, 0, 32);
				DWDECBFileSystemDirEntry entry = new DWDECBFileSystemDirEntry(buf);
				dir.add(entry);
			}
		}
		
		return dir;
	}


	public boolean hasFile(String filename)
	{
		
		for (DWDECBFileSystemDirEntry e : this.getDirectory())
		{
			if ((e.getFileName().trim() + "." + e.getFileExt()).equalsIgnoreCase(filename))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	public ArrayList<DWDiskSector> getFileSectors(String filename) throws DWDECBFileSystemFileNotFoundException, DWDECBFileSystemInvalidFATException
	{
		return(new DWDECBFileSystemFAT(sectors.get(DECBDefs.FAT_OFFSET)).getFileSectors(this.sectors, getDirEntry(filename).getFirstGranule()));
	}


	public DWDECBFileSystemDirEntry getDirEntry(String filename) throws DWDECBFileSystemFileNotFoundException
	{
		
		for (DWDECBFileSystemDirEntry e : this.getDirectory())
		{
			if ((e.getFileName().trim() + "." + e.getFileExt()).equalsIgnoreCase(filename))
			{
				return e;
			}
		}
		
		throw (new DWDECBFileSystemFileNotFoundException("File '" + filename + "' not found in DOS directory."));
	}


	public String getFileContents(String filename) throws DWDECBFileSystemFileNotFoundException, DWDECBFileSystemInvalidFATException
	{
		String res = "";
		
		ArrayList<DWDiskSector> sectors = this.getFileSectors(filename);
		
		if ((sectors != null) && (sectors.size() > 0))
		{
			for (int i = 0; i<sectors.size() - 1;i++)
			{
				res += new String(sectors.get(i).getData());
			}
		}
		
		// last sector is partial bytes
		
		byte[] buf = new byte[getDirEntry(filename).getBytesInLastSector()];
		
		System.arraycopy(sectors.get(sectors.size()-1).getData(), 0, buf, 0, getDirEntry(filename).getBytesInLastSector());
		
		res += new String(buf);
		
		return res;
	}
	
	
	public void addFile(String filename, byte[] filecontents) throws DWDECBFileSystemFullException, DWDECBFileSystemBadFilenameException, DWDECBFileSystemFileNotFoundException, DWDECBFileSystemInvalidFATException
	{
		DWDECBFileSystemFAT fat = new DWDECBFileSystemFAT(sectors.get(DECBDefs.FAT_OFFSET));
		// make fat entries
		byte firstgran = fat.allocate(filecontents.length);
		
		// dir entry
		this.addDirectoryEntry(filename, firstgran, (byte) (filecontents.length % 256));
		
		// put content into sectors
		ArrayList<DWDiskSector> sectors = this.getFileSectors(filename);
		
		int byteswritten = 0;
		byte[] buf = new byte[256];
		
		for (DWDiskSector sector : sectors)
		{
			if (filecontents.length - byteswritten >= 256)
			{
				System.arraycopy(filecontents, byteswritten, buf, 0, 256);
				byteswritten += 256;
			}
			else
			{
				System.arraycopy(filecontents, byteswritten, buf, 0, (filecontents.length - byteswritten));
				// zero pad partial sectors?
				for (int i = (filecontents.length - byteswritten); i < 256; i++)
					buf[i] = 0;
				byteswritten += (filecontents.length - byteswritten);
			}
			
			sector.setData(buf);
			
		
		}
		
		
	}


	private void addDirectoryEntry(String filename, byte firstgran, byte leftovers) throws DWDECBFileSystemFullException, DWDECBFileSystemBadFilenameException
	{
		List<DWDECBFileSystemDirEntry> dr = this.getDirectory();
		
		int dirsize = 0;
		
		for (DWDECBFileSystemDirEntry d : dr)
		{
			if (d.isUsed())
				dirsize++;
		}
		
		if (dirsize > 67)
			throw (new DWDECBFileSystemFullException("No free directory entries"));
			
		byte[] buf = new byte[32];
		byte[] secdata = new byte[256];
		
		DWDiskSector sec = sectors.get((dirsize / 8) + DECBDefs.DIRECTORY_OFFSET);

		secdata = sec.getData();
		
		String[] fileparts = filename.split("\\.");

		if (fileparts.length != 2)
			throw (new DWDECBFileSystemBadFilenameException("Invalid filename (parts) '" + filename + "' " + fileparts.length));
		
		String name = fileparts[0];
		String ext = fileparts[1];
		
		if ((name.length()<1) || (name.length()>8))
			throw (new DWDECBFileSystemBadFilenameException("Invalid filename (name) '" + filename + "'"));
		
		if (ext.length() != 3)
			throw (new DWDECBFileSystemBadFilenameException("Invalid filename (ext) '" + filename + "'"));
		
		while (name.length()<8)
			name += " ";
		
		System.arraycopy(name.getBytes(), 0 , buf, 0, 8);
		System.arraycopy(ext.getBytes(), 0, buf, 8, 3);
		
		
		// try to recognize filetype.. assume binary?
		DWDECBFileSystemDirExtensionMapping mapping = new DWDECBFileSystemDirExtensionMapping(ext, DECBDefs.FLAG_BIN, DECBDefs.FILETYPE_ML);
		
		if (DriveWireServer.serverconfig.getMaxIndex("DECBExtensionMapping") > -1)
		{
			for (int i = 0;i<=DriveWireServer.serverconfig.getMaxIndex("DECBExtensionMapping");i++)
			{
				String kp = "DECBExtensionMapping(" + i + ")";
				// validate entry first
				if (DriveWireServer.serverconfig.containsKey(kp + "[@extension]") && DriveWireServer.serverconfig.containsKey(kp + "[@ascii]") && DriveWireServer.serverconfig.containsKey(kp + "[@type]"))
				{
					if (DriveWireServer.serverconfig.getString(kp + "[@extension]").equalsIgnoreCase(ext))
					{
						// we have a winner
						
						mapping.setType(DriveWireServer.serverconfig.getByte(kp + "[@type]"));
						
						if (DriveWireServer.serverconfig.getBoolean(kp + "[@ascii]"))
							mapping.setFlag(DECBDefs.FLAG_ASCII);
						else
							mapping.setFlag(DECBDefs.FLAG_BIN);
						
					}
					
				}
				
				
			}
			
		}	

		// set our dirinfos
		buf[11] = mapping.getType();
		buf[12] = mapping.getFlag();
		
	
		
		buf[13] = firstgran;
		
		buf[14] = 0;
		buf[15] = leftovers;
				
		System.arraycopy(buf, 0 , secdata, (dirsize % 8) * 32, 32);
		
		sec.setData(secdata);
	}


	public static void format(DWDisk disk) throws DWInvalidSectorException, DWSeekPastEndOfDeviceException, DWDriveWriteProtectedException, IOException
	{
		// just init to all FF (mess does this?)
		
		byte[] buf = new byte[256];
		
		for (int i = 0;i<256;i++)
			buf[i] = (byte) 0xFF;
		
		for (int i =0; i< 630;i++)
		{
			disk.seekSector(i);
			disk.writeSector(buf);
		}
		
		
	}


	public String dumpFat()
	{
		DWDECBFileSystemFAT fat = new DWDECBFileSystemFAT(sectors.get(DECBDefs.FAT_OFFSET));
		
		return(fat.dumpFat());
	}
	
}
