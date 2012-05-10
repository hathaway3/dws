package com.groupunix.drivewireui.library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.eclipse.swt.graphics.Image;

import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwdisk.DWSIDEImageDisk;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWRBFFileSystem;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWRBFFileSystemDirEntry;
import com.groupunix.drivewireserver.dwdisk.filesystem.RBFFileSystemIDSector;
import com.groupunix.drivewireui.DWLibrary;
import com.groupunix.drivewireui.MainWin;

public class DeviceLibraryItem extends LibraryItem
{

	private String path;
	private Node node;

	public DeviceLibraryItem(String title, String path, Node item)
	{
		super(title);
		
		System.out.println("device lib created?");
		
		this.setPath(path);
		if (item != null)
			this.setNode(item);
		this.type = DWLibrary.TYPE_DEVICE;
	}

	public Image getIcon()
	{
		return org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/filetypes/blockdevice-2.png");
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getPath()
	{
		return path;
	}

	public void setNode(Node node)
	{
		this.node = node;
	}

	public Node getNode()
	{
		return node;
	}

	/*
	public Vector<LibraryItem> getChildren()
	{
		Vector<LibraryItem> res = new Vector<LibraryItem>();
		
		
		
		File diskRoot = new File (this.path);
	    RandomAccessFile diskAccess;
		try
		{
			
			diskAccess = new RandomAccessFile (diskRoot, "r");
			
			// look for OS9 partition
			
			
		byte[] content = new byte[1024];
			
			diskAccess.seek(0);
			diskAccess.readFully (content);
			
			RBFFileSystemIDSector idsec = new RBFFileSystemIDSector(content);
				
			int ddmap = (Integer) idsec.getAttrib("DD.MAP"); 
			int ddbit = (Integer) idsec.getAttrib("DD.BIT");
				
			if  ((ddbit > 0) && (Math.abs(((Integer) idsec.getAttrib("DD.TOT")) - (ddmap / ddbit * 8)) < 8))
			{
				// looks like we have an os9 partition..
				
				System.out.println("OS9part found");
				
			
				DWSIDEImageDisk disk = new DWSIDEImageDisk(, curpos, curpos, false);
				
				
				DWRBFFileSystemDirEntry entry = new DWRBFFileSystemDirEntry();
				
				DWRBFFileSystem rbffs = new DWRBFFileSystem();
			
				
				
			}
			
			
			
			
			diskAccess.close();
			
			
		} 
		catch (FileNotFoundException e)
		{
			
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			
			e.printStackTrace();
		}
	    
		
		
		
		return res;
	}
	*/
}
