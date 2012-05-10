package com.groupunix.drivewireui.library;

import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.eclipse.swt.graphics.Image;

import com.groupunix.drivewireui.DWLibrary;
import com.groupunix.drivewireui.MainWin;

public class FolderLibraryItem extends LibraryItem
{
	
	
	private Node node = null;

	public FolderLibraryItem(String title, Node item)
	{
		super(title);
		this.type = DWLibrary.TYPE_FOLDER;
		
		if (item != null)
			this.node = item;
		
	}

	public Node getNode()
	{
		return this.node;
	}
	
	
	public Image getIcon()
	{
		return org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/database-link.png");
	}
	

}
