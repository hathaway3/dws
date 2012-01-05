package com.groupunix.drivewireserver.dwdisk;

public class DWDECBFileSystemDirEntry
{
	/*
	Byte Description
	0—7 Filename, which is left justified and blank, filled. If byte0 is 0,
	then the file has been ‘KILL’ed and the directory entry is available
	for use. If byte0 is $FF, then the entry and all following entries
	have never been used.
	8—10 Filename extension
	11 File type: 0=BASIC, 1=BASIC data, 2=Machine language, 3= Text editor
	source
	12 ASCII flag: 0=binary or crunched BASIC, $FF=ASCII
	13 Number of the first granule in the file
	14—15 Number of bytes used in the last sector of the file
	16—31 Unused (future use)
	*/
	
	
	byte[] data = new byte[32];
	
	public DWDECBFileSystemDirEntry(byte[] buf)
	{
		System.arraycopy(buf, 0, this.data, 0, 32);
	}

	public String getFileName()
	{
		return(new String(data).substring(0, 8));
	}
	
	public String getFileExt()
	{
		return(new String(data).substring(8,11));
	}

	public boolean isUsed()
	{
		if (data[0] == (byte) 255)
			return false;
		
		if (data[0] == (byte) 0)
			return false;
		
		return true;
	}
	
	public boolean isKilled()
	{
		if (data[0] == (byte) 0)
			return true;
		
		return false;
	}
	
	
	
	public byte getFileType()
	{
		return(this.data[11]);
	}
	
	public byte getFileFlag()
	{
		return(this.data[12]);
	}
	
	public byte getFirstGranule()
	{
		return(this.data[13]);
	}
	
	public int getBytesInLastSector()
	{
		return( (0xFF & this.data[14])*256 + (0xFF & this.data[15]) );
	}
	
	
}
