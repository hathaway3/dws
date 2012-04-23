package com.groupunix.drivewireserver.dwdisk.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwdisk.DWDiskSector;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWDriveWriteProtectedException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFileNotFoundException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemFullException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidDirectoryException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFATException;
import com.groupunix.drivewireserver.dwexceptions.DWFileSystemInvalidFilenameException;
import com.groupunix.drivewireserver.dwexceptions.DWInvalidSectorException;
import com.groupunix.drivewireserver.dwexceptions.DWSeekPastEndOfDeviceException;

public class DWRBFFileSystem extends DWFileSystem
{

	public DWRBFFileSystem(DWDisk disk)
	{
		super(disk);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<DWDECBFileSystemDirEntry> getDirectory() throws IOException,
			DWFileSystemInvalidDirectoryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasFile(String filename) throws IOException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<DWDiskSector> getFileSectors(String filename)
			throws DWFileSystemFileNotFoundException,
			DWFileSystemInvalidFATException, IOException,
			DWDiskInvalidSectorNumber, DWFileSystemInvalidDirectoryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DWDECBFileSystemDirEntry getDirEntry(String filename)
			throws DWFileSystemFileNotFoundException, IOException,
			DWFileSystemInvalidDirectoryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getFileContents(String filename)
			throws DWFileSystemFileNotFoundException,
			DWFileSystemInvalidFATException, IOException,
			DWDiskInvalidSectorNumber, DWFileSystemInvalidDirectoryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addFile(String filename, byte[] filecontents)
			throws DWFileSystemFullException,
			DWFileSystemInvalidFilenameException,
			DWFileSystemFileNotFoundException, DWFileSystemInvalidFATException,
			IOException, DWDiskInvalidSectorNumber,
			DWFileSystemInvalidDirectoryException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void format() throws DWInvalidSectorException,
			DWSeekPastEndOfDeviceException, DWDriveWriteProtectedException,
			IOException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFSName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValidFS()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getFSErrors()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
