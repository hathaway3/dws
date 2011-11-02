package com.groupunix.drivewireui;



import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import swing2swt.layout.BorderLayout;

import com.groupunix.drivewireserver.DriveWireServer;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;


public class MainWin {



	public static final String DWUIVersion = "4.0.0";
	public static final String DWUIVersionDate = "10/25/2011";
	
	public static final String default_Host = "127.0.0.1";
	public static final int default_Port = 6800;
	public static final int default_Instance = 0;
	
	public static final int default_DiskHistorySize = 10;
	public static final int default_ServerHistorySize = 10;
	public static final int default_CmdHistorySize = 10;
	
	public static final String default_MainFont = "Lucida Console";
	public static final int default_MainFontSize = 9;
	public static final int default_MainFontStyle = 0;
	
	public static final String default_LogFont = "Lucida Console";
	public static final int default_LogFontSize = 9;
	public static final int default_LogFontStyle = 0;
	
	public static final String default_DialogFont = "Segoe UI";
	public static final int default_DialogFontSize = 9;
	public static final int default_DialogFontStyle = 0;
	
	public static final String default_MainFont_Linux = "Monospace";
	public static final int default_MainFontSize_Linux = 9;
	public static final int default_MainFontStyle_Linux = 0;
	
	public static final String default_LogFont_Linux = "Monospace";
	public static final int default_LogFontSize_Linux = 9;
	public static final int default_LogFontStyle_Linux = 0;
	
	public static final String default_DialogFont_Linux = "Sans";
	public static final int default_DialogFontSize_Linux = 9;
	public static final int default_DialogFontStyle_Linux = 0;
	
	
	
	public static final int default_TCPTimeout = 15000;
	public static final int default_ServerSyncInterval = 2000;
	
	
	public static XMLConfiguration config;
	public static HierarchicalConfiguration dwconfig;
	public static final String configfile = "drivewireUI.xml";
	
	private static DiskDef currentDisk = new DiskDef();
	
	protected static Shell shell;

	private Text txtYouCanEnter;
	private static int cmdhistpos = 0;
	private static Display display;
	
	private static String host;
	private static int port;
	private static int instance;
	
	
	
	private static boolean firsttimer = false;
	private static Text textDWOutput;
	private static Table table;
	private static Combo textDiskURI;
	private static Label labelDiskSectors;
	private static Label labelDiskLSN;
	private static Label labelDiskReads;
	private static Label labelDiskWrites;
	private static Label lblDiskUri;
	
	private static Button btnEject;
	private static Button btnWrite;
	private static Button btnReload;
	private static Label lblSectors2;
	private static Label lblCurrentLsn;
	private static Label lblReads;
	private static Label lblWrites;
	private static Button buttonFile;
	
	private static SashForm sashForm;
	private static SashForm sashForm_1;
	private static Composite composite;
	private static Button btnAdvanced;

	private static MenuItem mntmHdbdosTranslation;
	private static Menu menuDWCmdHelp;
	private static MenuItem mntmLoadHelp;
	private static MenuItem mntmRebuildHelpOn;
	
	private static Thread syncThread = null;
	public static int dwconfigserial = -1;
	private static Label lblDiskDriveImg;
	private static Label lblDiskDriveLED;
	
	private static DropdownSelectionListener disksetDDListener;
	private static ToolItem tltmLoadDiskSet;
	private static Label lblConStatus;
	private static Canvas canvasDriveNo;
	private static Menu menuMIDIOutputs;
	private static MenuItem mntmLockInstruments;
	private static Menu menuMIDIProfiles;
	private static MenuItem mntmSetProfile;
	private static Text textServer;
	private static Thread dwThread;
	private static Thread logThread;
	private static TabFolder tabFolderOutput;
	
	
	class DropdownSelectionListener extends SelectionAdapter {
		  private ToolItem dropdown;

		  private Menu menu;

		  public DropdownSelectionListener(ToolItem dropdown) {
		    this.dropdown = dropdown;
		    menu = new Menu(dropdown.getParent().getShell());
		  }

		  public void add(String item) {
		    MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		    menuItem.setText(item);
		    menuItem.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		        MenuItem selected = (MenuItem) event.widget;
		        MainWin.loadDiskSet(selected.getText());
		      }
		    });
		  }

		  public void widgetSelected(SelectionEvent event) {
		    if (event.detail == SWT.ARROW) {
		      ToolItem item = (ToolItem) event.widget;
		      Rectangle rect = item.getBounds();
		      Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
		      menu.setLocation(pt.x, pt.y + rect.height);
		      menu.setVisible(true);
		    }
		  }

		public void clear() 
		{
			menu.dispose();
			menu = new Menu(dropdown.getParent().getShell());
		}
	}
	
	
	
	
	public static void main(String[] args) 
	{
		Thread.currentThread().setName("dwuiMain-" + Thread.currentThread().getId());
		Thread.currentThread().setContextClassLoader(MainWin.class.getClassLoader());
		loadConfig();
		
		startDWServer(args);
		System.out.println("wtf");
		try 
		{
			applyServerSync();

			Display.setAppName("DriveWire");
			Display.setAppVersion(MainWin.DWUIVersion);
		
			display = new Display();
			
			/*
			 try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
					//don't care if this works..
				} catch (ClassNotFoundException e) {
					
				} catch (InstantiationException e) {
					
				} catch (IllegalAccessException e) {
					
				} catch (UnsupportedLookAndFeelException e) {
					
				}
			*/
			
			
			MainWin window = new MainWin();

			doMacStuff();
			
			logThread = new Thread(new LogInputThread());
			logThread.start();
			
			window.open(display, args);
			
			
		} 
		catch (Exception e) 
		{
			System.out.println("\nSomething's gone horribly wrong:\n");
			e.printStackTrace();
		
		}
		
		host = null;
				
	}


	private static void startDWServer(final String[] args) 
	{
		dwThread = new Thread(new Runnable() {

			@Override
			public void run() 
			{

				try 
				{
					DriveWireServer.main(args);
				} 
				catch (ConfigurationException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		dwThread.start();
	}

	private static void stopDWServer()
	{
		if ((dwThread != null) && (!dwThread.isInterrupted()))
		{
			// interrupt the server..
			dwThread.interrupt();
			
			try 
			{
				dwThread.join(3000);
			} 
			catch (InterruptedException e) 
			{
				MainWin.showError("Interrupted", "We were interrupted while waiting for the server to exit.", e.getMessage());
			}
			
		}
	}
	

	private static void doMacStuff() 
	{
		Menu systemMenu = display.getSystemMenu();
		if (systemMenu != null) 
		{
			// we've got a mac
			
			
		}
		
	}


	public static void applyServerSync() 
	{
		if (config.getBoolean("ServerSync",true))
		{
			if ((syncThread == null) || (syncThread.isAlive() == false))
			{
				syncThread = new Thread(new SyncThread());
				syncThread.start();
			}
		}
	}

	private static void loadConfig() 
	{
		try 
    	{
			
			File f = new File(configfile);
			
			if (f.exists())
			{
				config = new XMLConfiguration(configfile);
			}
			else
			{
				
				config = new XMLConfiguration();
				config.setFileName(configfile);
				config.addProperty("AutoCreated", true);
				config.save();
				
				firsttimer = true;
					
			}
			
			config.setAutoSave(true);
			
			MainWin.host = config.getString("LastHost",default_Host);
			MainWin.port = config.getInt("LastPort",default_Port);
			MainWin.instance = config.getInt("LastInstance",default_Instance);
			
		} 
    	catch (ConfigurationException e1) 
    	{
    		System.out.println("Fatal - Could not process config file '" + configfile + "'.  Please consult the documentation.");
    		System.exit(-1);
		} 
  
		
	}

	/**
	 * Open the window.
	 */
	public void open(Display display, String[] args) {
		
		createContents();
		
		applyFont();
		
		shell.open();
		shell.layout();

		if (MainWin.getHost() != null) syncConfig(); 

		for (int i = 0;i<256;i++)
		{	
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0,i+"");
		}
		
		MainWin.currentDisk = new DiskDef();
		displayCurrentDisk();
		
		
		FontData f = MainWin.getMainFont();
		
		
		MainWin.setFont(f);
		
		
		
		
		if (firsttimer)
		{
		 MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		        
		 messageBox.setMessage("It looks like this is the first time the client has been started.\n\nWould you like to go through a simple, step by step configuration wizard now?");
		 messageBox.setText("No client configuration found... but that's OK!");
		        int response = messageBox.open();
		        if (response == SWT.YES)
		        {
		        	InitialConfigWin icw = new InitialConfigWin(shell,SWT.DIALOG_TRIM);
		        	icw.open();
		        	
		        }
		}
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public static FontData getMainFont() 
	{
		FontData f;
		
		if (System.getProperties().getProperty("os.name", "Unknown").startsWith("Linux"))
		{
			f = new FontData(config.getString("MainFont",default_MainFont_Linux), config.getInt("MainFontSize", default_MainFontSize_Linux), config.getInt("MainFontStyle", default_MainFontStyle_Linux) );
		}
		else
		{
			f = new FontData(config.getString("MainFont",default_MainFont), config.getInt("MainFontSize", default_MainFontSize), config.getInt("MainFontStyle", default_MainFontStyle) );
		}
		
		return(f);
	}
	
	public static FontData getDialogFont()
	{
		FontData f;
		
		if (System.getProperties().getProperty("os.name", "Unknown").startsWith("Linux"))
		{
			f = new FontData(config.getString("DialogFont",default_DialogFont_Linux), config.getInt("DialogFontSize", default_DialogFontSize_Linux), config.getInt("DialogFontStyle", default_DialogFontStyle_Linux) );
		}
		else
		{
			f = new FontData(config.getString("DialogFont",default_DialogFont), config.getInt("DialogFontSize", default_DialogFontSize), config.getInt("DialogFontStyle", default_DialogFontStyle) );
			
		}
		
		return(f);
	}
	
	
	public static FontData getLogFont()
	{
		FontData f;
		
		if (System.getProperties().getProperty("os.name", "Unknown").startsWith("Linux"))
		{
			f = new FontData(config.getString("LogFont",default_LogFont_Linux), config.getInt("LogFontSize", default_LogFontSize_Linux), config.getInt("LogFontStyle", default_LogFontStyle_Linux) );
		}
		else
		{
			f = new FontData(config.getString("LogFont",default_LogFont), config.getInt("LogFontSize", default_LogFontSize), config.getInt("LogFontStyle", default_LogFontStyle) );
			
		}
		
		return(f);
	}
	

	private static void applyFont() 
	{
		FontData f = MainWin.getDialogFont();
		
		
		Control[] controls = MainWin.composite.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(display, f));
		}
		
	}

	private static void updateTitlebar() 
	{
		String txt = "DW4 UI - " + host + ":" + port + " [" + instance + "]";

		shell.setText(txt);
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setImage(SWTResourceManager.getImage(MainWin.class, "/dw4square.jpg"));
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) 
			{
				
				doShutdown();
			}
		});
		shell.setSize(770, 514);
		shell.setText("DriveWire User Interface");
		shell.setLayout(new BorderLayout(0, 2));
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmChooseServer = new MenuItem(menu_1, SWT.NONE);
		mntmChooseServer.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/computer-go.png"));
		mntmChooseServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				ChooseServerWin chooseServerWin = new ChooseServerWin(shell,SWT.DIALOG_TRIM);
				chooseServerWin.open();
				
			}
		});
		mntmChooseServer.setText("Choose server...");
		
		MenuItem mntmChooseInstance = new MenuItem(menu_1, SWT.NONE);
		mntmChooseInstance.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/view-list-tree-4.png"));
		mntmChooseInstance.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				ChooseInstanceWin window = new ChooseInstanceWin(shell,SWT.DIALOG_TRIM);
				
				try 
				{
					window.open();
				} 
				catch (DWUIOperationFailedException e1) 
				{
					showError("Error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
				} 
				catch (IOException e1) 
				{
					showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
			
			}
		});
		mntmChooseInstance.setText("Choose instance...");
		
		new MenuItem(menu_1, SWT.SEPARATOR);
		
		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/application-exit-5.png"));
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shell.close();
				System.exit(0);
			}
		});
		mntmExit.setText("Exit");
		
		MenuItem mntmServer = new MenuItem(menu, SWT.CASCADE);

		mntmServer.setText("Tools");
		
		Menu menu_4 = new Menu(mntmServer);
		menu_4.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) 
			{
				MainWin.syncConfig();
			}
		});
		mntmServer.setMenu(menu_4);
		
		MenuItem mntmDisksetManager = new MenuItem(menu_4, SWT.NONE);
		mntmDisksetManager.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/database-edit.png"));
		mntmDisksetManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DisksetManagerWin dmw = new DisksetManagerWin(shell,SWT.DIALOG_TRIM);
				dmw.open();
			}
		});
		mntmDisksetManager.setText("Diskset manager..");
		
		MenuItem mntmCreate = new MenuItem(menu_4, SWT.NONE);
		mntmCreate.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/media-floppy-new.png"));
		mntmCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				CreateDiskWin window = new CreateDiskWin(shell,SWT.DIALOG_TRIM);
	
				window.open();
				
			
			}
		});
		
				mntmCreate.setText("Create dsk..");
		
		MenuItem mntmEjectAllDisks = new MenuItem(menu_4, SWT.NONE);
		mntmEjectAllDisks.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/media-eject.png"));
		mntmEjectAllDisks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw disk eject all");
			}
		});
		mntmEjectAllDisks.setText("Eject all disks");
		
		MenuItem mntmRefreshDiskTable = new MenuItem(menu_4, SWT.NONE);
		mntmRefreshDiskTable.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/view-refresh-6.png"));
		mntmRefreshDiskTable.setText("Refresh disk table");
		
		mntmHdbdosTranslation = new MenuItem(menu_4, SWT.CHECK);
		mntmHdbdosTranslation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (mntmHdbdosTranslation.getSelection())
				{
					MainWin.sendCommand("dw config set HDBDOSMode true");
				}
				else
				{
					MainWin.sendCommand("dw config set HDBDOSMode false");
				}
				
	
			}
		});
		mntmHdbdosTranslation.setText("HDBDOS translation");
		
		new MenuItem(menu_4, SWT.SEPARATOR);
		
		MenuItem mntmStatus = new MenuItem(menu_4, SWT.NONE);
		mntmStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/computer-server.png"));
		mntmStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				StatusWin window = new StatusWin(shell,SWT.DIALOG_TRIM);
				
				try
				{
					window.open();
				}
				catch (DWUIOperationFailedException e1) 
				{
					showError("Error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
				} 
				catch (IOException e1) 
				{
					showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
				
			}
		});
		mntmStatus.setText("Server status");
		
		
		MenuItem mntmConfig = new MenuItem(menu, SWT.CASCADE);
		mntmConfig.setText("Config");
		
		Menu menu_5 = new Menu(mntmConfig);
		mntmConfig.setMenu(menu_5);
		
		MenuItem mntmInitialConfig = new MenuItem(menu_5, SWT.NONE);
		mntmInitialConfig.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wand.png"));
		mntmInitialConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				InitialConfigWin window = new InitialConfigWin(shell,SWT.DIALOG_TRIM);
				window.open();
			}
		});
		mntmInitialConfig.setText("Simple config wizard...");
		
		new MenuItem(menu_5, SWT.SEPARATOR);
		
		MenuItem mntmServer_1 = new MenuItem(menu_5, SWT.NONE);
		mntmServer_1.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/computer-edit.png"));
		mntmServer_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				ServerConfigWin window = new ServerConfigWin(shell,SWT.DIALOG_TRIM);

				try 
				{
					window.open();
				} 
				catch (DWUIOperationFailedException e1) 
				{
					showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				} 
				catch (IOException e1) 
				{
					showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
			
			}
		});
		mntmServer_1.setText("Server...");
		
		MenuItem mntmInstanceConfig = new MenuItem(menu_5, SWT.NONE);
		mntmInstanceConfig.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/cog-edit.png"));
		mntmInstanceConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				InstanceConfigWin window = new InstanceConfigWin(shell,SWT.DIALOG_TRIM);

				try 
				{
					window.open();
				} 
				catch (DWUIOperationFailedException e1) 
				{
					showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				} 
				catch (IOException e1) 
				{
					showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
			
			}
		});
		mntmInstanceConfig.setText("Instance...");
		
		MenuItem mntmUserInterface = new MenuItem(menu_5, SWT.NONE);
		mntmUserInterface.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/image-edit.png"));
		mntmUserInterface.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				UIConfigWin window = new UIConfigWin(shell,SWT.DIALOG_TRIM);
				window.open();
			
			}
		});
		mntmUserInterface.setText("User interface...");
		
		MenuItem mntmMidi = new MenuItem(menu_5, SWT.CASCADE);
		mntmMidi.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//doLoadMIDIMenus();
			}
		});
		mntmMidi.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/audio-keyboard.png"));
		mntmMidi.setText("MIDI");
		
		Menu menu_6 = new Menu(mntmMidi);
		mntmMidi.setMenu(menu_6);
		
		menu_6.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) 
			{
				MainWin.syncConfig();
			}
		});
		
		MenuItem mntmSetOutput = new MenuItem(menu_6, SWT.CASCADE);
		mntmSetOutput.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/audio-volume-high.png"));
		mntmSetOutput.setText("Set output");
		
		menuMIDIOutputs = new Menu(mntmSetOutput);
		mntmSetOutput.setMenu(menuMIDIOutputs);
				
		
		MenuItem mntmLoadSoundbank = new MenuItem(menu_6, SWT.CASCADE);
		mntmLoadSoundbank.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				SwingUtilities.invokeLater(new Runnable() {
				
				public void run() 
				{	
				DWServerFileChooser fileChooser = new DWServerFileChooser();
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				fileChooser.setDialogTitle("Choose soundbank to load...");
				
				// 	show the file dialog
				int answer = fileChooser.showOpenDialog(fileChooser);
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					MainWin.sendCommand("dw midi synth bank " + selected.getPath() );
			
				}		
				}
			});
			}
		});
		mntmLoadSoundbank.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/music.png"));
		mntmLoadSoundbank.setText("Load soundbank...");
		
		
		
		mntmSetProfile = new MenuItem(menu_6, SWT.CASCADE);
		mntmSetProfile.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/format-text-columns.png"));
		mntmSetProfile.setText("Set profile");
		
		menuMIDIProfiles = new Menu(mntmSetProfile);
		mntmSetProfile.setMenu(menuMIDIProfiles);
		
		mntmLockInstruments = new MenuItem(menu_6, SWT.CHECK);
		mntmLockInstruments.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw midi synth lock");
			}
		});
		mntmLockInstruments.setText("Lock instruments");
		
		new MenuItem(menu_5, SWT.SEPARATOR);
		
		MenuItem mntmResetInstanceDevice = new MenuItem(menu_5, SWT.NONE);
		mntmResetInstanceDevice.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/document-quick_restart.png"));
		mntmResetInstanceDevice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("ui instance reset protodev");
				
			}
		});
		mntmResetInstanceDevice.setText("Reset instance device");
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		Menu menu_3 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_3);
		
		MenuItem mntmDocumentation = new MenuItem(menu_3, SWT.NONE);
		mntmDocumentation.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/world-link.png"));
		mntmDocumentation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				openURL("http://sourceforge.net/apps/mediawiki/drivewireserver/index.php");
			}
		});
		mntmDocumentation.setText("Documentation Wiki");
		
		MenuItem mntmDwCommands = new MenuItem(menu_3, SWT.CASCADE);
		mntmDwCommands.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/book-open.png"));
		mntmDwCommands.setText("Help on server");
		
		menuDWCmdHelp = new Menu(mntmDwCommands);
		mntmDwCommands.setMenu(menuDWCmdHelp);
		
		mntmLoadHelp = new MenuItem(menuDWCmdHelp, SWT.NONE);
		mntmLoadHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadDWCmdHelpMenu();
			}
		});
		mntmLoadHelp.setText("Load help from server");
		
		mntmRebuildHelpOn = new MenuItem(menuDWCmdHelp, SWT.NONE);
		mntmRebuildHelpOn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWin.sendCommand("dw server help gentopics");
			}
		});
		mntmRebuildHelpOn.setText("Rebuild help on server");
		
		new MenuItem(menuDWCmdHelp, SWT.SEPARATOR);
		
		MenuItem mntmnoHelpTopics = new MenuItem(menuDWCmdHelp, SWT.NONE);
		mntmnoHelpTopics.setEnabled(false);
		mntmnoHelpTopics.setText("(No help topics loaded)");
		
		new MenuItem(menu_3, SWT.SEPARATOR);
		
		MenuItem mntmSubmitBugReport = new MenuItem(menu_3, SWT.NONE);
		mntmSubmitBugReport.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/bug.png"));
		mntmSubmitBugReport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				BugReportWin brwin = new BugReportWin(shell,SWT.DIALOG_TRIM,"User generated","User generated", "User generated");
				brwin.open();
			}
		});
		mntmSubmitBugReport.setText("Submit bug report...");
		
		new MenuItem(menu_3, SWT.SEPARATOR);
		
		MenuItem mntmAbout = new MenuItem(menu_3, SWT.NONE);
		mntmAbout.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/help-about-3.png"));
		mntmAbout.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				AboutWin window = new AboutWin(shell,SWT.DIALOG_TRIM);
				window.open();
			}
		});
		mntmAbout.setText("About...");
		
		txtYouCanEnter = new Text(shell, SWT.BORDER);
		txtYouCanEnter.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (txtYouCanEnter.getText().equals("Hint: You can enter 'dw' commands here.  Enter dw by itself for help."))
				{
					txtYouCanEnter.setText("");
				}
			}
		});
		
		txtYouCanEnter.setText("Hint: You can enter 'dw' commands here.  Enter dw by itself for help.");
		txtYouCanEnter.setLayoutData(BorderLayout.SOUTH);
		
		Composite tbarcomp = new Composite(shell, SWT.RIGHT);
		tbarcomp.setLayoutData(BorderLayout.NORTH);
		tbarcomp.setLayout(null);
	    
		
		ToolBar toolBar = new ToolBar(tbarcomp, SWT.RIGHT);
		toolBar.setBounds(0, 0, 725, 30);
		//toolBar.setLayoutData(BorderLayout.NORTH);
		
		ToolItem toolItem_1 = new ToolItem(toolBar, SWT.SEPARATOR);
		toolItem_1.setWidth(4);
		
		ToolItem tltmX = new ToolItem(toolBar, SWT.NONE);
		tltmX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() 
					{	
					 quickInDisk(0);
					}
				});
			}
		});
		tltmX.setImage(SWTResourceManager.getImage(MainWin.class, "/floppy_in_16.png"));
		tltmX.setText("Insert X0");
		
		ToolItem tltmInsertX = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(1);
			}
		});
		tltmInsertX.setImage(SWTResourceManager.getImage(MainWin.class, "/floppy_in_16.png"));
		tltmInsertX.setText("Insert X1");
		
		ToolItem tltmInsertX_1 = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(2);
			}
		});
		tltmInsertX_1.setImage(SWTResourceManager.getImage(MainWin.class, "/floppy_in_16.png"));
		tltmInsertX_1.setText("Insert X2");
		
		ToolItem tltmInsertX_2 = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(3);
			}
		});
		tltmInsertX_2.setImage(SWTResourceManager.getImage(MainWin.class, "/floppy_in_16.png"));
		tltmInsertX_2.setText("Insert X3");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		tltmLoadDiskSet = new ToolItem(toolBar, SWT.DROP_DOWN);
		tltmLoadDiskSet.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/database-go.png"));
		tltmLoadDiskSet.setText("No DiskSet");
		
	    disksetDDListener = new DropdownSelectionListener(tltmLoadDiskSet);
	    tltmLoadDiskSet.addSelectionListener(disksetDDListener);
	    
	    new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    
	    
	    ToolItem tltmSaveDiskSet = new ToolItem(toolBar, SWT.NONE);
	    tltmSaveDiskSet.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) 
	    	{
	    		SaveDiskSetWin window = new SaveDiskSetWin(shell,SWT.DIALOG_TRIM);
	    		try 
	    		{
	    			window.open();
	    			MainWin.syncConfig();
	    		} 
	    		catch (IOException e1) 
	    		{
	    			showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
	    		} 
	    		catch (DWUIOperationFailedException e1) 
	    		{
	    			showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
	    		}
	    		
	    	}
	    });
	    tltmSaveDiskSet.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/database-save.png"));
	    tltmSaveDiskSet.setText("Write Diskset");
	    
	    new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    ToolItem tltmDisksetManager = new ToolItem(toolBar, SWT.NONE);
	    tltmDisksetManager.addSelectionListener(new SelectionAdapter() {
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		DisksetManagerWin dmw = new DisksetManagerWin(shell,SWT.DIALOG_TRIM);
				dmw.open();
	    	}
	    });
	    tltmDisksetManager.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/database-edit.png"));
	    tltmDisksetManager.setText("Diskset Manager");
	    
	    lblConStatus = new Label(tbarcomp, SWT.NONE);
	    lblConStatus.setBounds(731, 0, 23, 30);
	    lblConStatus.setImage(null);
		
	    updateDisksetDD();
	    
	    
	    
		sashForm = new SashForm(shell, SWT.SMOOTH | SWT.VERTICAL);
		sashForm.setLayoutData(BorderLayout.CENTER);
		
		
		
		sashForm_1 = new SashForm(sashForm, SWT.SMOOTH);
		
		table = new Table(sashForm_1, SWT.BORDER | SWT.FULL_SELECTION);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				loadSelectedDiskDetails();
				displayCurrentDisk();
			}
		});
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tblclmnDrive = new TableColumn(table, SWT.NONE);
		tblclmnDrive.setWidth(28);
		tblclmnDrive.setText("X#");
		
		TableColumn tblclmnUri = new TableColumn(table, SWT.NONE);
		tblclmnUri.setWidth(232);
		tblclmnUri.setText("Path");
		
		composite = new Composite(sashForm_1, SWT.BORDER);
		composite.setLayout(null);
		
		textDiskURI = new Combo(composite, SWT.BORDER);
		textDiskURI.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 13)
				{
					MainWin.sendCommand("dw disk insert " + MainWin.currentDisk.getDrive() + " " + textDiskURI.getText());
					MainWin.loadSelectedDiskDetails();
					MainWin.displayCurrentDisk();
				}
			}
		});
	
		textDiskURI.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWin.sendCommand("dw disk insert " + MainWin.currentDisk.getDrive() + " " + textDiskURI.getText());
				MainWin.loadSelectedDiskDetails();
				MainWin.displayCurrentDisk();
			}
		});
		textDiskURI.setToolTipText("Enter a file path or any valid URI that points to the disk image you would like to use");

		textDiskURI.setBounds(10, 31, 408, 23);
		
		lblDiskUri = new Label(composite, SWT.NONE);
		lblDiskUri.setBounds(10, 10, 408, 15);
		lblDiskUri.setText("Disk X URI:");
		
		buttonFile = new Button(composite, SWT.NONE);
		buttonFile.setToolTipText("Click to browse files");
		buttonFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(MainWin.currentDisk.getDrive());
			}
		});
		buttonFile.setBounds(424, 31, 32, 23);
		buttonFile.setText("...");
		
		btnEject = new Button(composite, SWT.NONE);
		btnEject.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/media-eject.png"));
		btnEject.setToolTipText("Eject this image");
		btnEject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw disk eject " + MainWin.currentDisk.getDrive());
				
				loadSelectedDiskDetails();
				displayCurrentDisk(); 
				
			}
		});
		btnEject.setBounds(10, 123, 71, 28);
		btnEject.setText("Eject");
		
		btnWrite = new Button(composite, SWT.NONE);
		btnWrite.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/document-save-4.png"));
		btnWrite.setToolTipText("Write the current image to an alternate destination");
		btnWrite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				writeDiskTo(MainWin.currentDisk.getDrive());
			}
		});
		btnWrite.setBounds(87, 123, 94, 28);
		btnWrite.setText("Write to..");
		
		btnReload = new Button(composite, SWT.NONE);
		btnReload.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/floppy_in_16.png"));
		btnReload.setToolTipText("Reread the source image (losing any unsynced changes)");
		btnReload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw disk reload " + MainWin.currentDisk.getDrive());
				loadSelectedDiskDetails();
				displayCurrentDisk();
			}
		});
		btnReload.setText("Reload");
		btnReload.setBounds(187, 123, 88, 28);
		
		lblSectors2 = new Label(composite, SWT.RIGHT);
		lblSectors2.setBounds(10, 72, 75, 21);
		lblSectors2.setText("Total sectors:");
		
		lblCurrentLsn = new Label(composite, SWT.RIGHT);
		lblCurrentLsn.setText("Current LSN:");
		lblCurrentLsn.setBounds(11, 94, 74, 23);
		
		lblReads = new Label(composite, SWT.RIGHT);
		lblReads.setText("Reads:");
		lblReads.setBounds(135, 72, 75, 21);
		
		lblWrites = new Label(composite, SWT.RIGHT);
		lblWrites.setText("Writes:");
		lblWrites.setBounds(135, 94, 75, 18);
		
		labelDiskSectors = new Label(composite, SWT.NONE);
		labelDiskSectors.setToolTipText("The numbers of sectors that exist in this disk image");
		labelDiskSectors.setBounds(91, 72, 60, 23);
		
		labelDiskLSN = new Label(composite, SWT.NONE);
		labelDiskLSN.setToolTipText("The destination sector of the most recent seek operation");
		labelDiskLSN.setBounds(91, 94, 60, 18);
		
		labelDiskReads = new Label(composite, SWT.NONE);
		labelDiskReads.setToolTipText("The number of read requests made to this image");
		labelDiskReads.setBounds(216, 72, 60, 23);
		
		labelDiskWrites = new Label(composite, SWT.NONE);
		labelDiskWrites.setToolTipText("The number of write requests made to this image");
		labelDiskWrites.setBounds(216, 94, 59, 18);
		
		btnAdvanced = new Button(composite, SWT.NONE);
		btnAdvanced.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/kcontrol-3.png"));
		btnAdvanced.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DiskAdvancedWin window = new DiskAdvancedWin(shell,SWT.DIALOG_TRIM);
				try 
				{
					window.open();
					
					loadSelectedDiskDetails();
					displayCurrentDisk();
					
				} 
				catch (Exception e1)
				{
					
				}
			}
		});
		btnAdvanced.setBounds(291, 123, 166, 28);
		btnAdvanced.setText("Advanced options...");
		
		
		//Composite lblDriveNo = new Composite(composite, SWT.NONE);
		//lblDriveNo.setForeground(org.eclipse.wb.swt.SWTResourceManager.getColor(255, 255, 255));

       
        //mucking about with fonts
       

        FontData fd = MainWin.getMainFont();

        fd.setHeight(6);
        fd.setStyle(SWT.BOLD);
      
		
		//set the transparent canvas on the shell
        canvasDriveNo = new Canvas(composite, SWT.NO_TRIM);
        canvasDriveNo.setBounds(310, 93, 25, 15);
        canvasDriveNo.setVisible(false);
        canvasDriveNo.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(83, 83, 83));
        
        canvasDriveNo.setForeground(org.eclipse.wb.swt.SWTResourceManager.getColor(150,150,150));
        canvasDriveNo.setFont(new Font(display, fd));
        canvasDriveNo.addPaintListener(new PaintListener() {
                            public void paintControl(PaintEvent e) {
                            	if ((MainWin.currentDisk != null) && (MainWin.currentDisk.getDrive() > -1))
                            	{
                            		e.gc.drawString(String.format("%3d", MainWin.currentDisk.getDrive()), 0,0, true);
                            	}
                            	
                            	
                            }
                        });

  	
		
		lblDiskDriveLED = new Label(composite, SWT.NONE);
		lblDiskDriveLED.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive-leddark.png"));
		lblDiskDriveLED.setBounds(313, 77, 12, 6);
		lblDiskDriveLED.setVisible(false);
		
		lblDiskDriveImg = new Label(composite, SWT.NONE);
		lblDiskDriveImg.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive1.png"));
		lblDiskDriveImg.setBounds(293, 68, 162, 46);
		lblDiskDriveImg.setVisible(false);
                        
                        
		sashForm_1.setWeights(new int[] {283, 468});
		
		tabFolderOutput = new TabFolder(sashForm, SWT.NONE);
		tabFolderOutput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolderOutput.getItems()[tabFolderOutput.getSelectionIndex()].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/user-invisible-2.png"));
			}
		});
		
		TabItem tbtmUi = new TabItem(tabFolderOutput, SWT.NONE);
	
		tbtmUi.setText("UI  ");
		
		textDWOutput = new Text(tabFolderOutput, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		tbtmUi.setControl(textDWOutput);
		textDWOutput.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		TabItem tbtmServer = new TabItem(tabFolderOutput, SWT.NONE);
		tbtmServer.setText("Server  ");
		
		Composite composite_1 = new Composite(tabFolderOutput, SWT.NONE);
		composite_1.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tbtmServer.setControl(composite_1);
		composite_1.setLayout(new GridLayout(2, false));
		
		textServer = new Text(composite_1, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		textServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		textServer.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		Composite compositeSrvControl = new Composite(composite_1, SWT.NONE);
		compositeSrvControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		compositeSrvControl.setLayout(new FillLayout(SWT.VERTICAL));
		
		Button btnServerStart = new Button(compositeSrvControl, SWT.NONE);
		
		btnServerStart.setToolTipText("Start Local Server");
		btnServerStart.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/media-playback-start-4.png"));
		
		Button btnServerStop = new Button(compositeSrvControl, SWT.NONE);
		
		btnServerStop.setToolTipText("Stop Server");
		btnServerStop.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/media-playback-stop-4.png"));
		
		Button btnServerStatus = new Button(compositeSrvControl, SWT.NONE);
		btnServerStatus.setToolTipText("Server Status");
		btnServerStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/system-run-3.png"));
		sashForm.setWeights(new int[] {161, 241});
		
		
		txtYouCanEnter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) 
			{
				if (e.character == 13)
				{
					if (!txtYouCanEnter.getText().trim().equals(""))
					{
						MainWin.sendCommand(txtYouCanEnter.getText().trim(), true);
						MainWin.addCommandToHistory(txtYouCanEnter.getText().trim());
						txtYouCanEnter.setText("");
						MainWin.cmdhistpos = 0;
					}
				}
				else if (e.keyCode == 16777217)
				{
					// up
					if (config.getInt("CmdHistorySize",default_CmdHistorySize) > 0)
					{
						
						@SuppressWarnings("unchecked")
						List<String> cmdhist = config.getList("CmdHistory",null);
						
						if (cmdhist != null)
						{
							if (cmdhist.size() > MainWin.cmdhistpos)
							{
								MainWin.cmdhistpos++;
								txtYouCanEnter.setText(cmdhist.get(cmdhist.size() - MainWin.cmdhistpos));
								txtYouCanEnter.setSelection(txtYouCanEnter.getText().length() + 1);
							}
						}
						
					}
				}
				else if (e.keyCode == 16777218)
				{
					// down
					if (config.getInt("CmdHistorySize",default_CmdHistorySize) > 0)
					{
						@SuppressWarnings("unchecked")
						List<String> cmdhist = config.getList("CmdHistory",null);
						
						if (MainWin.cmdhistpos > 1)
						{
							MainWin.cmdhistpos--;
							txtYouCanEnter.setText(cmdhist.get(cmdhist.size() - MainWin.cmdhistpos));
							txtYouCanEnter.setSelection(txtYouCanEnter.getText().length() + 1);
							
						}
						else if (MainWin.cmdhistpos == 1)
						{
							MainWin.cmdhistpos--;
							txtYouCanEnter.setText("");
						}
					}
				}
			}
		});
	}

	
	
	


	protected void doShutdown() 
	{
		
		if (config.getBoolean("TermServerOnExit",true))
		{
			// sendCommand("ui server terminate");
			MainWin.logThread.interrupt();
			stopDWServer();
		}
		
	}


	private static void updateDisksetDD() 
	{
		 disksetDDListener.clear();
		 MainWin.tltmLoadDiskSet.setText("No DiskSet");
		 
		if ((MainWin.dwconfig != null) && (MainWin.getInstanceConfig() != null))
		{
			
			@SuppressWarnings("unchecked")
			List<HierarchicalConfiguration> disksets = MainWin.dwconfig.configurationsAt("diskset");
			
			ArrayList<String> ps = new ArrayList<String>();
			for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
			{
				ps.add(it.next().getString("Name","?noname?"));
			}
			
			Collections.sort(ps);
			
			for (String ds: ps)
			{
				disksetDDListener.add(ds);
				
			}
			
			
			if (MainWin.getInstanceConfig().containsKey("CurrentDiskSet"))
			{
				MainWin.tltmLoadDiskSet.setText(MainWin.getInstanceConfig().getString("CurrentDiskSet"));
			}
			
		}
		 
	}

	protected static void loadDiskSet(String dsname) 
	{
		MainWin.sendCommand("dw disk insert " + dsname);
		MainWin.syncConfig();
		updateDisksetDD();
	}

	

	
	
	
	protected void loadDWCmdHelpMenu() 
	{

		
		ArrayList<String> topics;
		try 
		{
			topics = UIUtils.loadArrayList(MainWin.instance, "ui server show helptopics");
			
			// clear menu
			for (MenuItem mi : MainWin.menuDWCmdHelp.getItems())
			{
				if (!mi.equals(MainWin.mntmLoadHelp) && !mi.equals(MainWin.mntmRebuildHelpOn))
				{
					mi.dispose();
				}
			}
			
			// put a pretty seperator in
			new MenuItem(MainWin.menuDWCmdHelp, SWT.SEPARATOR);
			
			Collections.sort(topics);
			for (final String topic: topics)
			{
				String[] parts = topic.split(" ");
				if (parts.length < 2)
				{
					// just link - special case for 'dw'
					MenuItem mntmTmp = new MenuItem(MainWin.menuDWCmdHelp, SWT.NONE);
					mntmTmp.setText(topic);
					mntmTmp.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) 
						{
							MainWin.sendCommand("dw server help show " + topic);
						}
					});
				} 
				else if (parts.length == 2)	
				{
					// root nodes in main menu
					MenuItem mntmTmp = new MenuItem(MainWin.menuDWCmdHelp, SWT.CASCADE);
					mntmTmp.setText(topic);
					
					Menu tmpMenu = new Menu(mntmTmp);
					mntmTmp.setMenu(tmpMenu);
					
					mntmTmp.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) 
						{
							MainWin.sendCommand("dw server help show " + topic);
						}
					});
				}
				else
				{
					Menu tmpMenu = MainWin.menuDWCmdHelp;
					
					for (int i = 2;i<parts.length;i++)
					{
						// what are we looking for..
						String target = parts[0] + " " + parts[1];
						for (int j=2;j<i;j++)
						{
							target += " " + parts[j];
						}
	
						
						// find parent menu
						for (MenuItem mi : tmpMenu.getItems())
						{
							
							if (mi.getText().equals(target))
							{

								if (mi.getMenu() == null)
								{
	
									// change to a submenu
									mi.dispose();
									MenuItem mntmTmp = new MenuItem(tmpMenu, SWT.CASCADE);
									mntmTmp.setText(target);
									Menu mtmp = new Menu(mntmTmp);
									mntmTmp.setMenu(mtmp);
									tmpMenu = mtmp;
								}
								else
								{
									tmpMenu = mi.getMenu();
								}
								break;
							}
						}
						
					}
					
					MenuItem mntmTmp = new MenuItem(tmpMenu, SWT.NONE);
					mntmTmp.setText(topic);
					mntmTmp.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) 
						{
							MainWin.sendCommand("dw server help show " + topic);
						}
					});
					
					
					
				}
				
			}
			
			MainWin.addToDisplay("Loaded " + topics.size() + " topics from server.");
			
		} catch (IOException e) 
		{
			MainWin.showError("IO Error", "Unable to load help due to an error", e.getMessage());
		} 
		catch (DWUIOperationFailedException e) 
		{
			MainWin.showError("DW Error", "Unable to load help due to an error", e.getMessage());
		}
		

	}

	protected void openURL(String url) 
	{
		// this odd bit of code tries to use the org.eclipse.swt.program.Program.launch method to open a native browser with a url in it.
		// usually that is straighforward, but on some systems it can crash the whole works, so we use invoke to call it carefully and catch crashes.
		boolean failed = false;
		
		Class<?> c;
		try {
			
			c = Class.forName("org.eclipse.swt.program.Program", true, this.getClass().getClassLoader() );
			java.lang.reflect.Method launch = c.getMethod("launch",new Class[]{ String.class });
			launch.invoke((Object)null, url);
			
			
		} 
		catch (ClassNotFoundException e) 
		{
			failed = true;
		} 
		catch (SecurityException e) 
		{
			failed = true;
		} 
		catch (NoSuchMethodException e) 
		{
			failed = true;
		} 
		catch (IllegalArgumentException e) 
		{
			failed = true;
		} 
		catch (IllegalAccessException e) 
		{
			failed = true;
		} 
		catch (InvocationTargetException ex)
	    {
        	failed = true;
		}	
		
		if (failed)
		{
			MainWin.addToDisplay("Could not open a browser automatically on this system.");
        	MainWin.addToDisplay("The URL I wanted to show you was:  " + url);
		}
	}

	protected static void syncConfig() 
	{
		try 
		{
			UIUtils.getDWConfigSerial();
			MainWin.updateTitlebar();
			MainWin.updateDisksetDD();
			
			
			if ((MainWin.dwconfig != null) && (MainWin.getInstanceConfig() != null))
			{
				MainWin.mntmHdbdosTranslation.setSelection(MainWin.getInstanceConfig().getBoolean("HDBDOSMode", false));
				//MainWin.mntmLockInstruments.setSelection(MainWin.getInstanceConfig().getBoolean("MIDISynthLock", false));
			
				MainWin.updateMidiMenus();
			}

			
		} 
		catch (IOException e) 
		{
			// ignore..
		} 
		catch (DWUIOperationFailedException e) 
		{
			// ignore..?
		}
		
	}

	private static void updateMidiMenus() throws IOException, DWUIOperationFailedException 
	{
		ArrayList<String> mds = UIUtils.loadArrayList(MainWin.getInstance(), "ui instance midistatus");
		
		String mididev = "none";
		String midiprof = "none";
	
		
		if (mds.size() == 2)
		{
			mididev = mds.get(0).trim();
			midiprof = mds.get(1).trim();
		}
		
		mds = UIUtils.loadArrayList("ui server show synthprofiles");
		
		MainWin.menuMIDIProfiles.dispose();
		MainWin.menuMIDIProfiles = new Menu(MainWin.mntmSetProfile);
		MainWin.mntmSetProfile.setMenu(MainWin.menuMIDIProfiles);
				
		for (String i : mds)
		{
			final String[] parts = i.split(" ");
			
			MenuItem tmp = new MenuItem(MainWin.menuMIDIProfiles, SWT.CHECK);
			tmp.setText(i);
			
			tmp.setSelection(parts[0].equals(midiprof));
			tmp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) 
				{
					sendCommand(("dw midi synth profile " + parts[0]));
				}
			});
		}	
		
		
		mds = UIUtils.loadArrayList("ui server show mididevs");
		
		for (String i : mds)
		{
			try
			{
				String[] parts = i.split(" ");
				final int j = Integer.parseInt(parts[0]);
				
				if (j < MainWin.menuMIDIOutputs.getItemCount())
				{
					// nop?
				}
				else
				{
					MenuItem tmp = new MenuItem(MainWin.menuMIDIOutputs, SWT.CHECK);
					tmp.setText(i);
					
					tmp.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) 
						{
							sendCommand(("dw midi output " + j));
						}
					});
				
				}
				
				MainWin.menuMIDIOutputs.getItem(j).setSelection(i.endsWith(mididev));
			
				
			}
			catch (NumberFormatException e)
			{
				// dont care
			}
		}	
	}


	@SuppressWarnings("unchecked")
	protected static void addCommandToHistory(String cmd) 
	{

		List<String> cmdhist = config.getList("CmdHistory",null);
		
		if (config.getInt("CmdHistorySize",default_CmdHistorySize) > 0)
		{
			if (cmdhist == null)
			{
			
				config.addProperty("CmdHistory", cmd);
				cmdhist = config.getList("CmdHistory",null);
			}
		
			
			if (cmdhist.size() >= config.getInt("CmdHistorySize",default_CmdHistorySize))
			{	
				cmdhist.remove(0);
			}
				
			cmdhist.add(cmd);
			config.setProperty("CmdHistory", cmdhist);
		}	
	}

	

	public static void displayCurrentDiskAsync() 
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  // did disk change..
						  MainWin.displayCurrentDiskStats(true);
					  }
				  });
	}

	public static void displayCurrentDiskStats(Boolean highlight) 
	{
		
		lblDiskDriveLED.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive-leddark.png"));
		
		if (highlight)
		{
			// highlight changes..
			if (MainWin.labelDiskLSN.getText().equals(MainWin.currentDisk.getLsn()+""))
			{
				MainWin.labelDiskLSN.setForeground(new Color(display, 0,0,0));
			}
			else
			{
				MainWin.labelDiskLSN.setForeground(new Color(display, 0,0,255));
				lblDiskDriveLED.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive-ledgreen.png"));
			}
		
			
			if (MainWin.labelDiskReads.getText().equals(MainWin.currentDisk.getReads()+""))
			{
				MainWin.labelDiskReads.setForeground(new Color(display, 0,0,0));
			}
			else
			{
				MainWin.labelDiskReads.setForeground(new Color(display, 0,0,255));
				lblDiskDriveLED.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive-ledgreen.png"));
			}
		
			if (MainWin.labelDiskWrites.getText().equals(MainWin.currentDisk.getWrites()+""))
			{
				MainWin.labelDiskWrites.setForeground(new Color(display, 0,0,0));
			}
			else
			{
				MainWin.labelDiskWrites.setForeground(new Color(display, 0,0,255));
				lblDiskDriveLED.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive-ledred.png"));
			}
		
			if (MainWin.labelDiskSectors.getText().equals(MainWin.currentDisk.getSectors()+""))
			{
				MainWin.labelDiskSectors.setForeground(new Color(display, 0,0,0));
			}
			else
			{
				MainWin.labelDiskSectors.setForeground(new Color(display, 0,0,255));
			}
			
			
			
			
		}
		
		// set values
		MainWin.labelDiskSectors.setText(MainWin.currentDisk.getSectors()+"");
		MainWin.labelDiskLSN.setText(MainWin.currentDisk.getLsn()+"");
		MainWin.labelDiskReads.setText(MainWin.currentDisk.getReads()+"");
		MainWin.labelDiskWrites.setText(MainWin.currentDisk.getWrites()+"");

	}
	
	
	public static void displayCurrentDisk() 
	{
		if (MainWin.currentDisk.getDrive() > -1)
		{
			MainWin.lblDiskUri.setText("Disk " + MainWin.currentDisk.getDrive() + " URI:");
			MainWin.textDiskURI.setEnabled(true);

			MainWin.buttonFile.setEnabled(true);

			
		}
		else
		{
			MainWin.lblDiskUri.setText("Select a drive from the list on the left to display details");
			MainWin.textDiskURI.setEnabled(false);

			MainWin.buttonFile.setEnabled(false);
			
		}
		
		MainWin.textDiskURI.removeAll();
		
		List<String> fh = MainWin.getDiskHistory();
			
		if (fh != null)
		{
			for (int i = fh.size() - 1;i > -1;i--)
			{
				MainWin.textDiskURI.add(fh.get(i));
			}
		
		}
			
	
		
		if (MainWin.textDiskURI.indexOf(MainWin.currentDisk.getPath()) > -1)
		{
			MainWin.textDiskURI.select(MainWin.textDiskURI.indexOf(MainWin.currentDisk.getPath()));
		}
		else
		{
			MainWin.textDiskURI.setText(MainWin.currentDisk.getPath() );
		}
		
		
		MainWin.displayCurrentDiskStats(false);
		
		
		if (MainWin.currentDisk.isLoaded())
		{

			
			MainWin.labelDiskSectors.setEnabled(true);
			MainWin.labelDiskLSN.setEnabled(true);
			MainWin.labelDiskReads.setEnabled(true);
			MainWin.labelDiskWrites.setEnabled(true);
			
			MainWin.btnEject.setEnabled(true);
			MainWin.btnWrite.setEnabled(true);
			MainWin.btnReload.setEnabled(true);
			

			MainWin.lblSectors2.setEnabled(true);
			MainWin.lblCurrentLsn.setEnabled(true);
			MainWin.lblReads.setEnabled(true);
			MainWin.lblWrites.setEnabled(true);
			
			MainWin.btnAdvanced.setEnabled(true);
			MainWin.lblDiskDriveLED.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive-leddark.png"));
			MainWin.lblDiskDriveLED.setVisible(true);
			MainWin.lblDiskDriveImg.setVisible(true);
			MainWin.canvasDriveNo.redraw();
			MainWin.canvasDriveNo.setVisible(true);

		}
		else
		{

			
			MainWin.labelDiskSectors.setEnabled(false);
			MainWin.labelDiskLSN.setEnabled(false);
			MainWin.labelDiskReads.setEnabled(false);
			MainWin.labelDiskWrites.setEnabled(false);
			
			MainWin.btnEject.setEnabled(false);
			MainWin.btnWrite.setEnabled(false);
			MainWin.btnReload.setEnabled(false);

			MainWin.lblSectors2.setEnabled(false);
			MainWin.lblCurrentLsn.setEnabled(false);
			MainWin.lblReads.setEnabled(false);
			MainWin.lblWrites.setEnabled(false);
			
			MainWin.btnAdvanced.setEnabled(false);
			MainWin.lblDiskDriveLED.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/diskdrive-leddark.png"));
			MainWin.lblDiskDriveLED.setVisible(false);
			MainWin.lblDiskDriveImg.setVisible(false);
			MainWin.canvasDriveNo.setVisible(false);
		}
		
	}

	public static void loadSelectedDiskDetails() 
	{
		int target = -1;
		
		if (table.getSelectionIndex() > -1)
		{
			target = table.getSelectionIndex();
		}
		else if (MainWin.currentDisk.getDrive() > -1)
		{
			target = MainWin.currentDisk.getDrive();
		}
		
		if (target > -1)
		{
			
			try
			{
				MainWin.currentDisk = UIUtils.getDiskDef(MainWin.instance, target);
				MainWin.currentDisk.setDrive(target);
				table.getItem(target).setText(1,MainWin.shortenLocalURI(MainWin.currentDisk.getPath()));
				
			}
			catch (DWUIOperationFailedException e1) 
			{
				showError("DW error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
			} 
			catch (IOException e1) 
			{
				showError("IO error sending command", e1.getMessage(), "You may have a connectivity problem, or the server may not be running.");
			}
	
		}
	}

	public static void refreshDiskTableAsync() throws IOException, DWUIOperationFailedException 
	{
		if (display != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						try 
						{
							MainWin.refreshDiskTable();
						} 
						catch (IOException e) 
						{
							MainWin.addToDisplay("Error while refreshing disk table: " + e.getMessage());
						} 
						catch (DWUIOperationFailedException e) 
						{
							MainWin.addToDisplay("Error while refreshing disk table: " + e.getMessage());
						}
						  
					  }
				  });
		}
	}
	
	public static void refreshDiskTable() throws IOException, DWUIOperationFailedException 
	{
		MainWin.syncConfig();
		
		MainWin.table.setRedraw(false);
		
		for (int i = 0;i<256;i++)
		{
			TableItem item;
			if (table.getItemCount() < i+1)
			{
				item = new TableItem(table, SWT.NONE);
			}
			else
			{
				item = table.getItem(i);
			}
			
			item.setText(0,i+"");
			item.setText(1,"");
		}
				
		ArrayList<String> disks = UIUtils.loadArrayList(MainWin.instance, "ui instance disk show");
		
		for (int i = 0;i<disks.size();i++)
		{
			String[] dp = disks.get(i).split(" ");
			
			if (dp.length > 1)
			{
				
				table.getItem(Integer.parseInt(dp[0])).setText(1, MainWin.shortenLocalURI(disks.get(i).substring(dp[0].length()+1)));
			}
			
		}
		
		MainWin.table.setRedraw(true);
		
		
		loadSelectedDiskDetails();
		displayCurrentDisk();
		
	}

	protected void writeDiskTo(final int diskno) 
	{
		// create a file chooser
		final DWServerFileChooser fileChooser;

		if ((table.getItem(diskno) != null) && !table.getItem(diskno).getText(1).equals(""))
		{
			fileChooser = new DWServerFileChooser(table.getItem(diskno).getText(1));
		}
		else
		{
			fileChooser = new DWServerFileChooser();
		}
		// 	configure the file dialog
		
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setDialogTitle("Write image in drive " + diskno + " to...");
		if ((table.getItem(diskno) != null) && !table.getItem(diskno).getText(1).equals(""))
		{
			fileChooser.setSelectedFile(new DWServerFile(table.getItem(diskno).getText(1)));
		}
		
		// 	show the file dialog
		int answer = fileChooser.showSaveDialog(fileChooser);
					
		// 	check if a file was selected
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			final File selected =  fileChooser.getSelectedFile();

			MainWin.sendCommand("dw disk write "+ diskno + " " + selected.getPath());
	
		}		
	
			
	}


	

	
	protected void quickInDisk(int diskno)
	{    
		
		
		// create a file chooser
		final DWServerFileChooser fileChooser;

		
		//if ((table.getItem(diskno) != null) && !table.getItem(diskno).getText(1).equals(""))
		//{
		//	fileChooser = new DWServerFileChooser(table.getItem(diskno).getText(1));
		//}
		//else
		//{
			fileChooser = new DWServerFileChooser();
		//}
		
		
		// configure the file dialog

		 
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setDialogTitle("Choose an image for drive " + diskno + "...");
		
		//if ((table.getItem(diskno) != null) && !table.getItem(diskno).getText(1).equals(""))
		//{
		//	fileChooser.setSelectedFile(new DWServerFile(table.getItem(diskno).getText(1)));
		//}
		
		// show the file dialog
		int answer = fileChooser.showOpenDialog(fileChooser);
						
		// check if a file was selected
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			final File selected =  fileChooser.getSelectedFile();

        	sendCommand("dw disk insert "+ diskno + " " + selected.getPath());
        	addDiskFileToHistory(selected.getPath());
        	
        	try {
				refreshDiskTable();
        	}
        	catch (DWUIOperationFailedException e1) 
        	{
        		showError("Error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
        	} 
        	catch (IOException e1) 
        	{
        		showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
        	}
        	
        	loadSelectedDiskDetails();
			displayCurrentDisk();
        	
        }
	}

	protected static void sendCommand(String cmd)
	{
		sendCommand(cmd, true);
	}
	
	protected static void sendCommand(String cmd, boolean errtomainwin) 
	{
		
		Connection connection = new Connection(host,port,instance);
		
		try 
		{
		
			connection.Connect();
			connection.sendCommand(cmd,instance);
			connection.close();

		} 
		catch (UnknownHostException e) 
		{
	
			showError("'Unknown host' error while sending command", e.getMessage(), "You may have a connectivity problem, or the server hostname may not be specified correctly." , errtomainwin);
		} 
		catch (IOException e1) 
		{
			// UIUtils.getStackTrace(e1)
	
			showError("IO error sending command", e1.getMessage(), "You may have a connectivity problem, or the server may not be running.", errtomainwin);
			
		} catch (DWUIOperationFailedException e2) 
		{

			showError("DW error sending command", e2.getMessage(), "", errtomainwin);
		} 
		
		
		
		
	}


	public static void showError(final String title,final String summary,final String detail)
	{
		showError(title,summary,detail,false);
	}

	public static void showError(final String title,final String summary,final String detail, boolean errtm)
	{
	
		if (errtm)
		{
			addToDisplay("\nError: " + summary + " " + detail);
		}
		else
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  ErrorWin ew = new ErrorWin(shell,SWT.DIALOG_TRIM,title,summary,detail);
						  ew.open();
					  }
				  });
		}
	}


	public static void addToDisplay(final String txt) 
	{
		if (textDWOutput != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  
						  textDWOutput.append(System.getProperty("line.separator") + txt);
						  if (MainWin.tabFolderOutput.getSelectionIndex() != 0)
						  {
							  MainWin.tabFolderOutput.getItems()[0].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/user-online-2.png"));
						  }
					  }
				  });
		}
	}
	
	public static void addToServerDisplay(final String txt) 
	{
		if (textServer != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  textServer.append(txt);
						  if (MainWin.tabFolderOutput.getSelectionIndex() != 1)
						  {
							  MainWin.tabFolderOutput.getItems()[1].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/user-online-2.png"));
						  }
					  }
				  });
		}
	}
	
	

	
	public static String getHost()
	{
		return host;
	}

	public static int getPort()
	{
		return port;
	}
	
	public static int getInstance()
	{
		return instance;
	}

	public static void setHost(String h) 
	{
		host = h;
		config.setProperty("LastHost",h);
		updateTitlebar();
	}

	public static void setPort(String p) 
	{
		try
		{
			port = Integer.parseInt(p);
			config.setProperty("LastPort", p);
			updateTitlebar();
		}
		catch (NumberFormatException e)
		{
			showError("Invalid port number","'" + p + "' is not a valid port number.","Valid port numbers are 1-65535.");
		}
		
		
	}

	@SuppressWarnings("unchecked")
	public static void addDiskFileToHistory(String filename) 
	{
		List<String> diskhist = config.getList("DiskHistory",null);
		
		
		if (config.getInt("DiskHistorySize",default_DiskHistorySize) > 0)
		{
			if (diskhist == null)
			{
			
				config.addProperty("DiskHistory", filename);
				diskhist = config.getList("DiskHistory",null);
			}
			else 
			{
				diskhist.remove(filename);
				
				if (diskhist.size() >= config.getInt("DiskHistorySize",default_DiskHistorySize))
				{
					diskhist.remove(0);
				}	
				
				diskhist.add(filename);
			}
			
			config.setProperty("DiskHistory", diskhist);
			
		}
	
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<String> getDiskHistory()
	{
		return(config.getList("DiskHistory",null));
	}

	public static void setFont(FontData newFont) 
	{
		textDWOutput.setFont(new Font(display, newFont));
		config.setProperty("MainFont", newFont.getName());
        config.setProperty("MainFontSize", newFont.getHeight());
        config.setProperty("MainFontStyle", newFont.getStyle());
	}

	public static void setLogFont(FontData newFont) 
	{
		textServer.setFont(new Font(display, newFont));
		config.setProperty("LogFont", newFont.getName());
        config.setProperty("LogFontSize", newFont.getHeight());
        config.setProperty("LogFontStyle", newFont.getStyle());
	}

	@SuppressWarnings("unchecked")
	public static List<String> getServerHistory() 
	{
		return(config.getList("ServerHistory",null));
	}

	@SuppressWarnings("unchecked")
	public static void addServerToHistory(String server) 
	{
		List<String> shist = config.getList("ServerHistory",null);
		
		if (shist == null)
		{
			if (config.getInt("ServerHistorySize",default_ServerHistorySize) > 0)
			{
				config.addProperty("ServerHistory", server);
			}
		}
		else if (!shist.contains(server))
		{
			if (shist.size() >= config.getInt("ServerHistorySize",default_ServerHistorySize))
			{
				shist.remove(0);
			}
			
			shist.add(server);
			config.setProperty("ServerHistory", shist);
			
		}
		
	}

	public static void setInstance(int inst) 
	{
		MainWin.instance = inst;
		config.setProperty("LastInstance", inst);
		updateTitlebar();
	}

	public static void setDialogFont(FontData newFont) 
	{
		config.setProperty("DialogFont", newFont.getName());
        config.setProperty("DialogFontSize", newFont.getHeight());
        config.setProperty("DialogFontStyle", newFont.getStyle());
        
        applyFont();
	
	}
	protected Button getBtnAdvanced() {
		return btnAdvanced;
	}
	
	public static DiskDef getCurrentDisk()
	{
		return(MainWin.currentDisk);
	}

	public static HierarchicalConfiguration getInstanceConfig() 
	{
		if (MainWin.dwconfig != null)
		{
			@SuppressWarnings("unchecked")
			List<HierarchicalConfiguration> handlerconfs = (List<HierarchicalConfiguration>)MainWin.dwconfig.configurationsAt("instance");
			
			if (MainWin.getInstance() < handlerconfs.size())
			{
				return handlerconfs.get(MainWin.getInstance());
			}
		}
		return(null);
	}
	protected Label getLblDiskDriveImg() {
		return lblDiskDriveImg;
	}
	protected Label getLblDiskDriveLED() {
		return lblDiskDriveLED;
	}
	protected Label getLblConStatus() {
		return lblConStatus;
	}


	public static void setConStatusConnect() 
	{
		if (MainWin.lblConStatus != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  MainWin.lblConStatus.setRedraw(false);
						  MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/network-transmit-2.png"));
						  MainWin.lblConStatus.setRedraw(true);
					  }
				  });
		}
	}
	
	public static void setConStatusNone() 
	{
		if (MainWin.lblConStatus != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  MainWin.lblConStatus.setRedraw(false);
						  MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/network-idle-2.png"));
						  MainWin.lblConStatus.setRedraw(true);
					  }
				  });
		}
	}
	
	public static void setConStatusRead() 
	{
		if (MainWin.lblConStatus != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  MainWin.lblConStatus.setRedraw(false);
						  MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/network-receive-2.png"));
						  MainWin.lblConStatus.setRedraw(true);
					  }
				  });
		}
	}
	
	public static void setConStatusWrite() 
	{
		if (MainWin.lblConStatus != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  MainWin.lblConStatus.setRedraw(false);
						  MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/network-transmit-2.png"));
						  MainWin.lblConStatus.setRedraw(true);
					  }
				  });
		}
	}


	public static void setConStatusError() 
	{
		if (MainWin.lblConStatus != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  MainWin.lblConStatus.setRedraw(false);
						  MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/network-error-2.png"));
						  MainWin.lblConStatus.setRedraw(true);
					  }
				  });
		}
	}
	
	public static String shortenLocalURI(String df) 
	{
		if (df.startsWith("file:///"))
		{
			if (df.charAt(9) == ':')
			{
				return df.substring(8);
			}
			else
			{
				return df.substring(7);
			}
		}
		return(df);
	}
	protected Canvas getCanvasDriveNo() {
		return canvasDriveNo;
	}
	protected Text getTextServer() {
		return textServer;
	}


	public static void doDisplayAsync(Runnable runnable) 
	{
		display.asyncExec(runnable);
	}
	protected TabFolder getTabFolderOutput() {
		return tabFolderOutput;
	}
}