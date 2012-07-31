package com.groupunix.drivewireui.plugins;

import java.lang.Thread.UncaughtExceptionHandler;
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
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

import com.groupunix.drivewireui.ErrorWin;
import com.groupunix.drivewireui.GradientHelper;
import com.groupunix.drivewireui.MainWin;
import com.groupunix.drivewireui.SendCommandWin;
import com.groupunix.drivewireui.UIUtils;

public class DWBrowser extends Composite
{
	public final static int LTYPE_LOCAL_ROOT = 0;
	public final static int LTYPE_LOCAL_FOLDER = 1;
	public final static int LTYPE_LOCAL_ENTRY = 2;
	public final static int LTYPE_NET_ROOT = 10;
	public final static int LTYPE_NET_FOLDER = 11;
	public final static int LTYPE_NET_ENTRY = 12;
	public final static int LTYPE_CLOUD_ROOT = 20;
	public final static int LTYPE_CLOUD_FOLDER = 21;
	public final static int LTYPE_CLOUD_ENTRY = 22;
	
	private Browser browser;
	private Composite header;


	private ToolItem tltmBack;
	private ToolItem tltmForward;
	private ToolItem tltmReload;
	private Combo comboURL;
	private Spinner spinnerDrive;
	
	private Canvas canvas;

	private CTabItem ourtab;
	
	
	
	public DWBrowser(final Composite parent, String url, final CTabItem ourtab)
	{
		super(parent, SWT.BORDER);
		this.ourtab = ourtab;
		
		setLayout(new BorderLayout(0, 0));
		
		//setBounds(comp.getBounds());
		
		header = new Composite(this, SWT.NONE);
		header.setLayoutData(BorderLayout.NORTH);
		header.setLayout(new FormLayout());
		
		GradientHelper.applyVerticalGradientBG(header, MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		
		header.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event)
			{
				GradientHelper.applyVerticalGradientBG(header, MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
				
				
			} } );
		
		header.setBackgroundMode(SWT.INHERIT_FORCE);
		
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
		tltmReload.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/view-refresh-7.png"));
		
		comboURL = new Combo(header, SWT.NONE);
		comboURL.setBackground(new Color(MainWin.getDisplay(), 255,255,255));
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
		fd_comboURL.right = new FormAttachment(100, -90);
		fd_comboURL.bottom = new FormAttachment(100, -5);
		fd_comboURL.top = new FormAttachment(2, 5);
		comboURL.setLayoutData(fd_comboURL);
		
		canvas = new Canvas(header, SWT.NONE);
		FormData fd_canvas = new FormData();
		fd_canvas.left = new FormAttachment(100, -75);
		fd_canvas.right = new FormAttachment(100, -55);
		fd_canvas.top = new FormAttachment(2, 6);
		fd_canvas.bottom = new FormAttachment(2, 28);
		canvas.setLayoutData(fd_canvas);
		
		spinnerDrive = new Spinner(header, SWT.BORDER);
		spinnerDrive.setBackground(new Color(MainWin.getDisplay(), 255,255,255));
		spinnerDrive.setToolTipText("Working drive");
		FormData fd_spinnerDrive = new FormData();
		fd_spinnerDrive.bottom = new FormAttachment(100, -5);
		fd_spinnerDrive.right = new FormAttachment(100, -5);
		fd_spinnerDrive.left = new FormAttachment(100, -50);
		spinnerDrive.setLayoutData(fd_spinnerDrive);
		spinnerDrive.setMaximum(255);
		
		//DWBrowserUtils.GenerateHTMLDir("E:/cocodisks");
		
		browser = null;
		
		UncaughtExceptionHandler uncex =  Thread.currentThread().getUncaughtExceptionHandler();
		
		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, final Throwable e)
			{
				
				System.out.println("It seems we cannot open a browser window on this system.");
				System.out.println();
				System.out.println("The error message is: " + e.getMessage());
				System.out.println();
				
				MainWin.config.setProperty("NoBrowsers", true);
				
				System.out.println("I've disabled opening browsers in the configuration.  You will have to restart DriveWire.");
				System.exit(1);
			}
		
			});
		
		
			
		
			browser = new Browser(this, SWT.NONE);
			
			Thread.currentThread().setUncaughtExceptionHandler(uncex);
			
			
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
			
			browser.addTitleListener( new TitleListener() {
		         public void changed(TitleEvent event) {
		        	 ourtab.setText(event.title);
		        	 ourtab.setImage(SWTResourceManager.getImage(MainWin.class, "/menu/www.png"));
		          }
		       });
			
			
			
			if (url != null)
			{
				browser.setUrl(url);
			}
			else
			{
				// browser.setUrl(MainWin.config.getString("Browser_homepage", "http://cococoding.com/cloud") );
			}
			
		
		
		
		
	}

	
	


	public void openURL(String url)
	{
		browser.setUrl(url);
		
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
									MessageBox messageBox = new MessageBox(MainWin.getDisplay().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO );
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
								
								/*
								if (!this.tltmAppend.getSelection())
								{
									title = "Creating disk image...";
									msg = "Please wait while the server creates a new DOS disk image and then adds the file(s).";
									cmds.add("dw disk create " + this.spinnerDrive.getSelection());
									cmds.add("dw disk dos format " + this.spinnerDrive.getSelection());
								}
								*/
								
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
					
					/*
					if (!this.tltmAppend.getSelection())
					{
						title = "Creating disk image...";
						msg = "Please wait while the server creates a new DOS disk image and then adds the file.";
						cmds.add("dw disk create " + this.spinnerDrive.getSelection());
						cmds.add("dw disk dos format " + this.spinnerDrive.getSelection());
					}
					*/
					
					cmds.add("dw disk dos add " + this.spinnerDrive.getSelection() + " " + url);
					
					sendCommandDialog(cmds, title, msg);
					
				}
				
				// OS9
				
			}
		}
	}


	private void showError(String title, String msg)
	{
		MessageBox messageBox = new MessageBox(MainWin.getDisplay().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
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
		final Shell shell = MainWin.getDisplay().getActiveShell();
		
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
