package com.groupunix.drivewireui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

public class DWBrowser extends Shell
{
	private Browser browser;
	private Composite header;
	private Composite footer;

	private ToolItem tltmBack;
	private ToolItem tltmForward;
	private ToolItem tltmReload;
	private Combo comboURL;
	private Spinner spinnerDrive;
	private Label lblWorkingDrive;
	private ToolItem tltmAppend;
	
	/**
	 * Create the shell.
	 * @param display
	 */
	public DWBrowser(Display display)
	{
		super(display, SWT.SHELL_TRIM);
		
		setText("DW Browser");
		setSize(800, 600);
		
		setLayout(new BorderLayout(0, 0));
		
		
		addShellListener(new ShellAdapter() {
			

			@Override
			public void shellClosed(ShellEvent e) 
			{
			
				MainWin.config.setProperty("Browser_Width", getSize().x);
				MainWin.config.setProperty("Browser_Height", getSize().y);
					
				MainWin.config.setProperty("Browser_x", getLocation().x);
				MainWin.config.setProperty("Browser_y", getLocation().y);
				
			}
		});
		
		
		if (MainWin.config.containsKey("Browser_Width") && MainWin.config.containsKey("Browser_Height"))
		{
			setSize(MainWin.config.getInt("Browser_Width"), MainWin.config.getInt("Browser_Height"));
		}
		
		if (MainWin.config.containsKey("Browser_x") && MainWin.config.containsKey("Browser_y"))
		{
			Point p = new Point(MainWin.config.getInt("Browser_x",0), MainWin.config.getInt("Browser_y",0));
			
			if (MainWin.isValidDisplayPos(p))
				setLocation(p);
		}
		
		
		browser = new Browser(this, SWT.BORDER);
		
		
		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changed(LocationEvent event) {
				comboURL.setText(event.location);
				
				
				
				if (browser.isBackEnabled())
					tltmBack.setEnabled(true);
				else
					tltmBack.setEnabled(false);
				
				if (browser.isForwardEnabled())
					tltmForward.setEnabled(true);
				else
					tltmForward.setEnabled(false);
				
				
				
			}
			
			@Override
			public void changing(LocationEvent event) 
			{
				if (isCocoLink(event.location))
				{
					event.doit = false;
					
					doCoCoLink(event.location);
				}
			}
		});
		browser.setLayoutData(BorderLayout.CENTER);
		
		browser.addTitleListener( new TitleListener() {
	         public void changed(TitleEvent event) {
	        	 setText("DW Browser - " + event.title);
	          }
	       });
		
		header = new Composite(this, SWT.NONE);
		header.setLayoutData(BorderLayout.NORTH);
		header.setLayout(new FormLayout());
		
		ToolBar toolBar = new ToolBar(header, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.top = new FormAttachment(0, 5);
		fd_toolBar.left = new FormAttachment(1, 5);
		toolBar.setLayoutData(fd_toolBar);
		
		tltmBack = new ToolItem(toolBar, SWT.NONE);
		tltmBack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.back();
			}
		});
		tltmBack.setWidth(30);
		tltmBack.setImage(SWTResourceManager.getImage(DWBrowser.class, "/toolbar/arrow-left-3.png"));
		
		tltmForward = new ToolItem(toolBar, SWT.NONE);
		tltmForward.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.forward();
			}
		});
		tltmForward.setWidth(30);
		tltmForward.setImage(SWTResourceManager.getImage(DWBrowser.class, "/toolbar/arrow-right-3.png"));
		
		tltmReload = new ToolItem(toolBar, SWT.NONE);
		tltmReload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.refresh();
			}
		});
		tltmReload.setWidth(30);
		tltmReload.setImage(SWTResourceManager.getImage(DWBrowser.class, "/toolbar/arrow-refresh.png"));
		
		comboURL = new Combo(header, SWT.NONE);
		
		comboURL.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 13)
				{
					browser.setUrl(comboURL.getText());
				}
				else if ((e.keyCode == 16777217) || (e.keyCode == 16777218))
				{
					e.doit = false;
				}
			}
		});
		comboURL.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				browser.setUrl(comboURL.getText());
			}
		});
		fd_toolBar.right = new FormAttachment(comboURL, -6);
		
		FormData fd_comboURL = new FormData();
		fd_comboURL.left = new FormAttachment(0, 90);
		fd_comboURL.right = new FormAttachment(100, -50);
		fd_comboURL.bottom = new FormAttachment(100, -5);
		fd_comboURL.top = new FormAttachment(2, 5);
		comboURL.setLayoutData(fd_comboURL);
		
		loadURLs();
		
		Label lblDW4 = new Label(header, SWT.NONE);
		lblDW4.setImage(SWTResourceManager.getImage(DWBrowser.class, "/dw/tiny4.png"));
		FormData fd_progressBar = new FormData();
		fd_progressBar.top = new FormAttachment(toolBar, 0, SWT.TOP);
		fd_progressBar.bottom = new FormAttachment(toolBar, 0, SWT.BOTTOM);
		fd_progressBar.height = 20;
		fd_progressBar.left = new FormAttachment(100, -40);
		fd_progressBar.right = new FormAttachment(100, -5);
		lblDW4.setLayoutData(fd_progressBar);
		
		footer = new Composite(this, SWT.NONE);
		footer.setLayoutData(BorderLayout.SOUTH);
		
		spinnerDrive = new Spinner(footer, SWT.BORDER);
		spinnerDrive.setMaximum(255);
		spinnerDrive.setBounds(10, 10, 47, 22);
		
		lblWorkingDrive = new Label(footer, SWT.NONE);
		lblWorkingDrive.setBounds(65, 12, 92, 15);
		lblWorkingDrive.setText("Working drive");
		
		ToolBar toolBar_1 = new ToolBar(footer, SWT.FLAT | SWT.RIGHT);
		toolBar_1.setBounds(175, 4, 169, 33);
		
		tltmAppend = new ToolItem(toolBar_1, SWT.CHECK);
		tltmAppend.setText("Append");
		
		ToolItem tltmDos = new ToolItem(toolBar_1, SWT.DROP_DOWN);
		tltmDos.setText("DOS");
		
		browser.setUrl("http://www.lcurtisboyle.com/nitros9/coco_game_list.html");
	}


	
	
	
	private void loadURLs()
	{
		int urls = MainWin.config.getMaxIndex("BrowserURL");
		
		for (int i = 0;i<=urls;i++)
		{
			this.comboURL.add(MainWin.config.getString("BrowserURL(" + i + ")"));
		}
	}





	protected void doCoCoLink(String url)
	{
		String filename = getFilename(url);
		
		if (filename != null)
		{
			String extension = getExtension(filename);
			
			if (extension != null)
			{
				// handle archives
				if (isExtension("Archive", extension))
				{
					FileObject fileobj;
					try
					{
						fileobj = VFS.getManager().resolveFile("zip:" + url + "!/");
						
						int disk = 0;
						int dos = 0;
						int os9 = 0;
						
						if (fileobj.exists() && fileobj.isReadable())
						{
							for (FileObject f : fileobj.getChildren())
							{
								String ext = this.getExtension(f.getName().getURI());
								
								if (this.isExtension("Disk", ext))
									disk++;
								
								if (this.isExtension("DOS", ext))
									dos++;
								
								//if (this.isExtension("0S9", ext))
								//	os9++;
								
								
							}

							if (disk+dos+os9 == 0)
							{
								showError("No usable files found", "The archive does not contain any files with known extensions.");
							}
							else if ((disk > 0) && (dos == 0) && (os9 == 0))
							{
								if (disk > 1)
								{
									// more than one dsk.. prompt
									MessageBox messageBox = new MessageBox(this, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
								    messageBox.setMessage("There are " + disk + " images in this archive.  Would you like to load them into drives " + this.spinnerDrive.getSelection() + " through " + (this.spinnerDrive.getSelection() + disk - 1) + "?");
								    messageBox.setText("Multiple disk images found");
								    int rc = messageBox.open();
								    
								    if (rc == SWT.YES)
								    {
								    	List<String> cmds = new ArrayList<String>();
										int off = 0;
										
								    	for (FileObject f : fileobj.getChildren())
										{
								    		
											String ext = this.getExtension(f.getName().getURI());
											
											if (this.isExtension("Disk", ext))
											{
												cmds.add("dw disk insert " + (this.spinnerDrive.getSelection()+off) + " " + f.getName().getURI());
												off++;
												
											}
											
										}
								    	
										sendCommandDialog(cmds, "Loading disk image..", "Please wait while the server loads the disk image.");
										
								    }
								}
								else
								{
									// just the one.. do it
									for (FileObject f : fileobj.getChildren())
									{
										String ext = this.getExtension(f.getName().getURI());
										
										if (this.isExtension("Disk", ext))
										{
											List<String> cmds = new ArrayList<String>();
											cmds.add("dw disk insert " + this.spinnerDrive.getSelection() + " " + f.getName().getURI());
											
											sendCommandDialog(cmds, "Loading disk image..", "Please wait while the server loads the disk image.");
										}
										
									}
								}
							}
							else if ((disk == 0) && (dos > 0) && (os9 == 0))
							{
								// all dos files
								
								String title = "Appending file(s) to image...";
								String msg = "Please wait while the server appends to the image in drive " + this.spinnerDrive.getSelection() + ".";
								List<String> cmds = new ArrayList<String>();
								
								if (!this.tltmAppend.getSelection())
								{
									title = "Creating disk image...";
									msg = "Please wait while the server creates a new DOS disk image and then adds the file(s).";
									cmds.add("dw disk create " + this.spinnerDrive.getSelection());
									cmds.add("dw disk dos format " + this.spinnerDrive.getSelection());
								}
								
								
								for (FileObject f : fileobj.getChildren())
								{
									String ext = this.getExtension(f.getName().getURI());
									
									if (this.isExtension("DOS", ext))
									{
										cmds.add("dw disk dos add " + this.spinnerDrive.getSelection() + " " + f.getName().getURI());
									}
									
								}
								
								sendCommandDialog(cmds, title, msg);
								
								
							}
							
							
						}
					} 
					catch (FileSystemException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				// handle disks
				else if (isExtension("Disk", extension))
				{
					List<String> cmds = new ArrayList<String>();
					cmds.add("dw disk insert " + this.spinnerDrive.getSelection() + " " + url);
					
					sendCommandDialog(cmds, "Loading disk image..", "Please wait while the server loads the disk image.");
					
				}
				// DOS
				else if (isExtension("DOS", extension))
				{
					String title = "Appending file to image...";
					String msg = "Please wait while the server appends the file to the image in drive " + this.spinnerDrive.getSelection() + ".";
					List<String> cmds = new ArrayList<String>();
					
					if (!this.tltmAppend.getSelection())
					{
						title = "Creating disk image...";
						msg = "Please wait while the server creates a new DOS disk image and then adds the file.";
						cmds.add("dw disk create " + this.spinnerDrive.getSelection());
						cmds.add("dw disk dos format " + this.spinnerDrive.getSelection());
					}
					
					cmds.add("dw disk dos add " + this.spinnerDrive.getSelection() + " " + url);
					
					sendCommandDialog(cmds, title, msg);
					
				}
				
				// OS9
				
			}
		}
	}


	private void showError(String title, String msg)
	{
		MessageBox messageBox = new MessageBox(this, SWT.ICON_ERROR | SWT.OK);
	    messageBox.setMessage(msg);
	    messageBox.setText(title);
	    messageBox.open();
	}





	protected boolean isCocoLink(String location)
	{
		String filename = getFilename(location);
		
		if (filename != null)
		{
			String extension = getExtension(filename);
		
			if ((extension != null) && isCoCoExtension(extension))
			{
				return(true);
			}
			
		}
		
		return false;
	}

	
	private String getExtension(String filename)
	{
		String ext = null;
	
		int lastdot = filename.lastIndexOf('.');
		
		// extension?
		if ((lastdot > 0) && (lastdot < filename.length()-2))
		{
			ext = filename.substring(lastdot+1);
		}
		
		return ext;	
	}

	private String getFilename(String location)
	{
		String filename = null;
	
		int lastslash = location.lastIndexOf('/');
		
		// parse up url
		if ((lastslash > 0) && (lastslash < location.length()-2))
		{
			filename = location.substring(lastslash+1);
		}
		
		return filename;
	}
	

	
	

	private boolean isCoCoExtension(String ext)
	{
		if (isExtension("Disk", ext))
			return true;
		
		if (isExtension("DOS", ext))
			return true;
		
		if (isExtension("OS9", ext))
			return true;
		
		if (isExtension("Archive",ext))
			return true;
		
		return false;
	}

	
	

	private boolean isExtension(String exttype, String ext)
	{
		if (MainWin.config.containsKey(exttype + "Extensions"))
		{
			@SuppressWarnings("unchecked")
			List<String> exts = MainWin.config.getList(exttype + "Extensions");
			
			if (exts.contains(ext.toLowerCase()))
				return(true);
		}
		return false;
	}


	@Override
	protected void checkSubclass()
	{
		// Disable the check that prevents subclassing of SWT components
	}
	
	
	protected void sendCommandDialog(final List<String> cmd, final String title, final String message) 
	{
		final Shell shell = this;
		
		this.getDisplay().asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  SendCommandWin win = new SendCommandWin(shell, SWT.DIALOG_TRIM, cmd,title, message);
						  win.open();
		
					  }
				  });
	}
	
	
	
}
