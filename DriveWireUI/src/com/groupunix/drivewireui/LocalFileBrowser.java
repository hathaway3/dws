package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class LocalFileBrowser implements Runnable
{
	String startpath;
	boolean dir;
	boolean save;
	String title;
	String buttontext;
	
	String selected = null;
	
	public LocalFileBrowser(final boolean save, final boolean dir, final String startpath, final String title, final String buttontext)
	{
		this.startpath = startpath;
		this.dir = dir;
		this.save = save;
		this.title = title;
		this.buttontext = buttontext;
	}
	
	
	@Override
	public void run() 
	{
		if (!dir)
		{
			FileDialog fd;
			
			if (save)
				fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			else
				fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
			
	        fd.setText(title);
	        fd.setFilterPath(startpath);
	        //String[] filterExt = { "*.txt", "*.doc", ".rtf", "*.*" };
	        //fd.setFilterExtensions(filterExt);
	        selected = fd.open();
		}
		else
		{
			DirectoryDialog dd = new DirectoryDialog(Display.getCurrent().getActiveShell());
			
			dd.setFilterPath(startpath);
			dd.setText(title);
			dd.setMessage(buttontext);
			
			selected = dd.open();
			
		}
	}

	public String getSelected()
	{
		return selected;
	}

}
