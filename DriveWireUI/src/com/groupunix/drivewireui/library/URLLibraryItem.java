package com.groupunix.drivewireui.library;

import org.eclipse.swt.graphics.Image;

import com.groupunix.drivewireui.DWLibrary;
import com.groupunix.drivewireui.MainWin;

public class URLLibraryItem extends LibraryItem
{
	private String url;
	private String iconpath;
	
	public URLLibraryItem(String title, String url)
	{
		super(title);
		
		this.setUrl(url);
		
		this.type = DWLibrary.TYPE_URL;
		this.iconpath = "/menu/world-link.png";
		
		
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}
	
	public Image getIcon()
	{
		return org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, this.iconpath);
	}
	
	
	
}
