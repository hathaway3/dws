package com.groupunix.drivewireui.library;

import org.eclipse.swt.graphics.Image;

import com.groupunix.drivewireui.DWLibrary;
import com.groupunix.drivewireui.MainWin;

public class MountedLibraryItem extends LibraryItem
{

	public MountedLibraryItem(String title)
	{
		super(title);
		this.type = DWLibrary.TYPE_FOLDER_MOUNTED;
	}

	
	public Image getIcon()
	{
		return org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/disk-insert.png");
	}
}
