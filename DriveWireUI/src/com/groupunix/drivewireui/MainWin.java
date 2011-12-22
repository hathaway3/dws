package com.groupunix.drivewireui;




import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.BorderLayout;

import com.groupunix.drivewireserver.DriveWireServer;
import com.swtdesigner.SWTResourceManager;



public class MainWin {

	static Logger logger = Logger.getLogger(MainWin.class);
	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %m%n");

	protected static final int LOG_WIN_MAX_LINES = 100;
	
	public static final String DWUIVersion = "4.0.0RC1";
	public static final String DWUIVersionDate = "12/15/2011";
	
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
	public static final int default_ServerSyncInterval = 1000;
	
	
	public static XMLConfiguration config;
	public static HierarchicalConfiguration dwconfig;
	public static final String configfile = "drivewireUI.xml";
	

	
	private static int currentDisk = 0;
	
	protected static Shell shell;

	private static Text txtYouCanEnter;
	private static int cmdhistpos = 0;
	private static Display display;
	
	private static String host;
	private static int port;
	private static int instance;
	
	private static Boolean connected = false;
	
	
	private static boolean firsttimer = false;
	private static Text textDWOutput;
	private static Table table;
	
	private static SashForm sashForm;
	
	private static MenuItem mntmFile;
	private static MenuItem mntmTools;
	private static MenuItem mntmHdbdosTranslation;
	private static Menu menuDWCmdHelp;
	private static MenuItem mntmLoadHelp;
	private static MenuItem mntmRebuildHelpOn;
	
	private static Menu menu_file;
	private static Menu menu_tools;
	private static Menu menu_config;
	private static Menu menu_help;
	
	private static MenuItem mntmDwCommands;
	private static MenuItem mntmChooseInstance;	
	private static MenuItem mntmInitialConfig;
	private static MenuItem mntmUserInterface;
	
	private static Thread syncThread = null;
	public static int dwconfigserial = -1;
	
	
	private static MenuItem mntmMidi;
	private static Menu menuMIDIOutputs;
	private static MenuItem mntmLockInstruments;
	private static Menu menuMIDIProfiles;
	private static MenuItem mntmSetProfile;
	private static MenuItem mntmSetOutput;
	private static Thread dwThread;
	private static TabFolder tabFolderOutput;
	
	
	
	private static SyncThread syncObj;
	
	private static DiskDef[] disks = new DiskDef[256];
	private static MIDIStatus midiStatus;
	
	public static Color colorWhite;
	public static Color colorRed;
	public static Color colorGreen;
	public static Color colorBlack;
	public static Color colorDiskBG;
	public static Color colorDiskFG;
	public static Color colorDiskGraphBG;
	public static Color colorDiskGraphLSN;
	public static Color colorDiskClean;
	public static Color colorDiskDirty;
	
	
	protected static Font fontDiskNumber;
	protected static Font fontDiskGraph;
	
	
	private static Boolean driveactivity = false;
	private static org.eclipse.swt.widgets.List listServerLog;
	

	private static MenuItem mitemInsert;
	private static MenuItem mitemEject;
	private static MenuItem mitemExport;
	private static MenuItem mitemCreate;
	private static MenuItem mitemParameters;
	protected static Vector<DiskStatusItem> diskStatusItems;

	private static boolean ready = false;
	private Composite compositeList;
	
	private static Image diskLEDgreen;
	private static Image diskLEDred;
	private static Image diskLEDdark;
	protected static Image diskBigLEDgreen;
	protected static Image diskBigLEDred;
	protected static Image diskBigLEDdark;
	
	private static DiskTableUpdateThread diskTableUpdater;
	protected static boolean safeshutdown = false;
	
	
	private static ServerConfigWin serverconfigwin;
	
	
	
	
	
	public static void main(String[] args) 
	{
		
		Thread.currentThread().setName("dwuiMain-" + Thread.currentThread().getId());
		Thread.currentThread().setContextClassLoader(MainWin.class.getClassLoader());
		
		// setup logging
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(logLayout));
		
		
		
		// get our client config
		loadConfig();
		
		// fire up a server
		if (config.getBoolean("LocalServer",true))
			startDWServer(args);

		
		try 
		{
			
			// make ourselves look pretty
			Display.setAppName("DriveWire");
			Display.setAppVersion(MainWin.DWUIVersion);
		
			display = new Display();
			
			colorWhite = new Color(display, 255,255,255);
			colorRed = new Color(display, 255,0,0);
			colorGreen = new Color(display, 0,255,0);
			colorBlack = new Color(display, 0,0,0);
			colorDiskBG = new Color(display, 0x49,0x48,0x48);
			colorDiskFG = new Color(display, 0xb5,0xb5,0xb5);
			colorDiskGraphBG = new Color(display, 0x89,0x89,0x89);
			colorDiskGraphLSN = new Color(display, 0x2d, 0x2d, 0x2d);
			colorDiskDirty = new Color(display, 255,0,0);
			colorDiskClean = new Color(display, 150,0,0);
			
			fontDiskNumber = new Font(display, "Franklin Gothic", 36, SWT.BOLD);
			fontDiskGraph = new Font(display, "Sans",8, SWT.NONE);
			
			diskLEDgreen = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledgreen.png");
			diskLEDred = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledred.png");
			diskLEDdark = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-leddark.png");
			diskBigLEDgreen = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledgreen-big.png");
			diskBigLEDred = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledred-big.png");
			diskBigLEDdark = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-leddark-big.png");
			
			MainWin window = new MainWin();

			// macs are special, special things
			doMacStuff();
			
			// sync
			restartServerConn();
		
			UIUtils.loadFonts();
			
			// get this party started
			window.open(display, args);
			
			
		} 
		catch (Exception e) 
		{
			System.out.println("\nSomething's gone horribly wrong:\n");
			e.printStackTrace();
		
		}
		
		// game over.  let threads know.
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
					logger.fatal(e.getMessage());
					System.exit(-1);
				}
			}
			
		});
		
		dwThread.setDaemon(true);
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




	private static void loadConfig() 
	{
		try 
    	{
			// dwui config..
			
			File f = new File(configfile);
			
			if (f.exists())
			{
				config = new XMLConfiguration(configfile);	
			}
			else
			{
				logger.info("Creating new UI config file");
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
			
			
			
			// server
			
			if (config.getBoolean("LocalServer",true))
			{
				f = new File(config.getString("ServerConfigFile", "config.xml"));
				
				if (!f.exists())
				{
					// try to make a default server config
					f = new File("default/serverconfig.xml");
					if (!f.exists())
					{
						logger.fatal("LocalServer is true, but server config can not be found or created.");
						System.exit(-1);
					}
					else
					{
						logger.info("Creating new server config file");
						try 
						{
							UIUtils.fileCopy(f.getCanonicalPath(), config.getString("ServerConfigFile", "config.xml"));
						} 
						catch (IOException e) 
						{
							logger.fatal("Error copying default server config: " + e.getMessage());
							System.exit(-1);
						}
					}
				}
			}
		} 
    	catch (ConfigurationException e1) 
    	{
    		System.out.println("Fatal - Could not process config file '" + configfile + "'.  Please consult the documentation.");
    		System.exit(-1);
		} 
  
		
	}


	
	


	public void open(final Display display, String[] args) {
		
		createContents();
		
		MainWin.diskTableUpdater = new DiskTableUpdateThread();
		Thread dtuThread = new Thread(MainWin.diskTableUpdater);
		dtuThread.setDaemon(true);
		dtuThread.start();
		
		
		//applyFont();
		
		if (!connected)
		{
			MainWin.setItemsConnectionEnabled(false);
	
		}
		
		shell.open();
		shell.layout();

		
		
		//FontData f = MainWin.getMainFont();
		//MainWin.setFont(f);
		
		
		
		if (firsttimer)
		{
			MainWin.addToDisplay("Welcome to DriveWire 4!");
			MainWin.addToDisplay(" ");
			MainWin.addToDisplay("Please choose a serial device using the Config, Instance tool.");
			MainWin.addToDisplay(" ");
		}
		
		// drive light
		
		 Runnable drivelightoff = new Runnable() 
		 	{
		      public void run() 
		      {
		    	  
		    	  if ((MainWin.driveactivity.booleanValue() == true))
		    	  {
	    			  
	    			  for (int i=0;i<256;i++)
	    			  {
	    				  if (MainWin.table.getItem(i) != null)
	    				  {
	    					  if ((disks[i] != null) && (disks[i].isLoaded()))
	    						  MainWin.diskTableUpdater.addUpdate(i,"LED",MainWin.diskLEDdark);
	    					 
	    				  }
	    				 
	    			  }

		    		  MainWin.driveactivity = false;
		    	  }
			  	  	
		    	  display.timerExec(1000, this);
		      }
		    };
		    
	    display.timerExec(1000, drivelightoff);
		
	    
	    MainWin.ready = true;
		
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
		/*
		FontData f = MainWin.getDialogFont();
		
		
		Control[] controls = MainWin.composite.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(display, f));
		}
		*/
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
		

		
		shell.setImage(SWTResourceManager.getImage(MainWin.class, "/dw/dw4square.jpg"));
		shell.addShellListener(new ShellAdapter() {
			

			@Override
			public void shellClosed(ShellEvent e) 
			{
				 doShutdown();
				
			}
		});
		
		// 		shell.setSize(config.getInt("MainWin_Width",753), config.getInt("MainWin_Height", 486));
		shell.setSize(config.getInt("MainWin_Width",753), config.getInt("MainWin_Height", 486));
		
		if (config.containsKey("MainWin_x") && config.containsKey("MainWin_y"))
		{
			Point p = new Point(config.getInt("MainWin_x",0), config.getInt("MainWin_y",0));
			
			if (MainWin.isValidDisplayPos(p))
				shell.setLocation(p);
		}
		
		shell.setText("DriveWire User Interface");
		shell.setLayout(new BorderLayout(0, 2));
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		
		
		// file menu
		
		mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		menu_file = new Menu(mntmFile);
		mntmFile.setMenu(menu_file);
		
		MenuItem mntmChooseServer = new MenuItem(menu_file, SWT.NONE);
		mntmChooseServer.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/computer-go.png"));
		mntmChooseServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				ChooseServerWin chooseServerWin = new ChooseServerWin(shell,SWT.DIALOG_TRIM);
				chooseServerWin.open();
				
			}
		});
		mntmChooseServer.setText("Choose server...");
		
		mntmChooseInstance = new MenuItem(menu_file, SWT.NONE);
		mntmChooseInstance.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/view-list-tree-4.png"));
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
		
		new MenuItem(menu_file, SWT.SEPARATOR);
		
		MenuItem mntmExit = new MenuItem(menu_file, SWT.NONE);
		mntmExit.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/application-exit-5.png"));
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{

				shell.close();
			}
		});
		mntmExit.setText("Exit");
		
		
		
		// tools menu
		
		mntmTools = new MenuItem(menu, SWT.CASCADE);

		mntmTools.setText("Tools");
		
		menu_tools = new Menu(mntmTools);
		mntmTools.setMenu(menu_tools);
		
		MenuItem mntmDisksetManager = new MenuItem(menu_tools, SWT.NONE);
		mntmDisksetManager.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/database-edit.png"));
		mntmDisksetManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//DisksetManagerWin dmw = new DisksetManagerWin(shell,SWT.DIALOG_TRIM);
				//dmw.open();
			}
		});
		mntmDisksetManager.setText("Diskset manager..");
		
		MenuItem mntmCreate = new MenuItem(menu_tools, SWT.NONE);
		mntmCreate.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/media-floppy-new.png"));
		mntmCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				CreateDiskWin window = new CreateDiskWin(shell,SWT.DIALOG_TRIM);
	
				window.open();
				
			
			}
		});
		
				mntmCreate.setText("Create dsk..");
		
		MenuItem mntmEjectAllDisks = new MenuItem(menu_tools, SWT.NONE);
		mntmEjectAllDisks.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/media-eject.png"));
		mntmEjectAllDisks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw disk eject all");
			}
		});
		mntmEjectAllDisks.setText("Eject all disks");
		
		new MenuItem(menu_tools, SWT.SEPARATOR);
		
		mntmHdbdosTranslation = new MenuItem(menu_tools, SWT.CHECK);
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
		
		new MenuItem(menu_tools, SWT.SEPARATOR);
		
		MenuItem mntmStatus = new MenuItem(menu_tools, SWT.NONE);
		mntmStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/computer-server.png"));
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
		
		
		

		// config menu
				
		MenuItem mntmConfig = new MenuItem(menu, SWT.CASCADE);
		mntmConfig.setText("Config");
		
		menu_config = new Menu(mntmConfig);
		mntmConfig.setMenu(menu_config);
		
		mntmInitialConfig = new MenuItem(menu_config, SWT.NONE);
		mntmInitialConfig.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/wand.png"));
		mntmInitialConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				InitialConfigWin window = new InitialConfigWin(shell,SWT.DIALOG_TRIM);
				window.open();
			}
		});
		mntmInitialConfig.setText("Simple config wizard...");
		
		new MenuItem(menu_config, SWT.SEPARATOR);
		
		MenuItem mntmServer_1 = new MenuItem(menu_config, SWT.NONE);
		mntmServer_1.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/computer-edit.png"));
		mntmServer_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				/*
				if ((MainWin.serverconfigwin == null) || (MainWin.serverconfigwin.shlServerConfiguration.isDisposed()))
				{
					serverconfigwin = new ServerConfigWin(shell,SWT.DIALOG_TRIM);
					
					try 
					{
						serverconfigwin.open();
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
				else
				{
					serverconfigwin.shlServerConfiguration.setActive();
				}
				
				*/
				
				ConfigEditor ce = new ConfigEditor(display);
				ce.open();
			
			}
		});
		mntmServer_1.setText("Server...");
		

		
		mntmUserInterface = new MenuItem(menu_config, SWT.NONE);
		mntmUserInterface.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/image-edit.png"));
		mntmUserInterface.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				UIConfigWin window = new UIConfigWin(shell,SWT.DIALOG_TRIM);
				window.open();
			
			}
		});
		mntmUserInterface.setText("User interface...");
		
		mntmMidi = new MenuItem(menu_config, SWT.CASCADE);

		mntmMidi.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/audio-keyboard.png"));
		mntmMidi.setText("MIDI");
		
		Menu menu_6 = new Menu(mntmMidi);
		mntmMidi.setMenu(menu_6);
		
		mntmSetOutput = new MenuItem(menu_6, SWT.CASCADE);
		mntmSetOutput.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/audio-volume-high.png"));
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
		mntmLoadSoundbank.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/music.png"));
		mntmLoadSoundbank.setText("Load soundbank...");
		
		
		
		mntmSetProfile = new MenuItem(menu_6, SWT.CASCADE);
		mntmSetProfile.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/format-text-columns.png"));
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
		
		new MenuItem(menu_config, SWT.SEPARATOR);
		
		MenuItem mntmResetInstanceDevice = new MenuItem(menu_config, SWT.NONE);
		mntmResetInstanceDevice.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/document-quick_restart.png"));
		mntmResetInstanceDevice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("ui instance reset protodev");
				
			}
		});
		mntmResetInstanceDevice.setText("Reset instance device");
		
		
		
		// help menu
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		menu_help = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_help);
		
		MenuItem mntmDocumentation = new MenuItem(menu_help, SWT.NONE);
		mntmDocumentation.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/world-link.png"));
		mntmDocumentation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				openURL("http://sourceforge.net/apps/mediawiki/drivewireserver/index.php");
			}
		});
		mntmDocumentation.setText("Documentation Wiki");
		
		mntmDwCommands = new MenuItem(menu_help, SWT.CASCADE);
		mntmDwCommands.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/book-open.png"));
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
		
		new MenuItem(menu_help, SWT.SEPARATOR);
		
		MenuItem mntmSubmitBugReport = new MenuItem(menu_help, SWT.NONE);
		mntmSubmitBugReport.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/bug.png"));
		mntmSubmitBugReport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				BugReportWin brwin = new BugReportWin(shell,SWT.DIALOG_TRIM,"User generated","User generated", "User generated");
				brwin.open();
			}
		});
		mntmSubmitBugReport.setText("Submit bug report...");
		
		new MenuItem(menu_help, SWT.SEPARATOR);
		
		MenuItem mntmAbout = new MenuItem(menu_help, SWT.NONE);
		mntmAbout.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/help-about-3.png"));
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
		
	   
	    
	    
	    
		sashForm = new SashForm(shell, SWT.SMOOTH | SWT.VERTICAL);

		sashForm.setLayoutData(BorderLayout.CENTER);

		
		compositeList = new Composite(sashForm, SWT.NONE);
		compositeList.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		
		// disk table
		
		table = new Table(compositeList, SWT.FULL_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) 
			{
				int drive = table.getSelectionIndex();
				
				if ((MainWin.disks != null) && (MainWin.disks[drive] != null))
				{
					if (MainWin.disks[drive].isLoaded())
					{
						if (MainWin.disks[drive].hasDiskwin())
						{
							MainWin.disks[drive].getDiskwin().shlDwDrive.setActive();
						}
						else
						{
							MainWin.disks[drive].setDiskwin(new DiskWin( MainWin.disks[drive],getDiskWinInitPos(drive).x,getDiskWinInitPos(drive).y));
							MainWin.disks[drive].getDiskwin().open(display);

						}
					}
					else
					{
						quickInDisk(drive);
					}
				
				}
			}
		});
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.currentDisk = table.getSelectionIndex();
				
			
			}
		});
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		createDiskTableColumns();
		
		
		
		// rmb menu for disks...
		
		Menu diskPopup = new Menu (shell, SWT.POP_UP);
		diskPopup.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) 
			{
				
				int sdisk = table.getSelectionIndex();
				if (sdisk > -1)
				{
					mitemInsert.setText ("Insert disk for drive " + sdisk + "...");
					mitemExport.setText("Export image in drive " + sdisk + " to...");
					mitemCreate.setText ("Create new disk for drive " + sdisk + "...");
					mitemEject.setText ("Eject disk in drive " + sdisk);
					mitemParameters.setText ("Drive " + sdisk + " parameters...");
					
					mitemInsert.setEnabled(false);
					mitemCreate.setEnabled(false);
					mitemExport.setEnabled(false);	
					mitemEject.setEnabled(false);
					mitemParameters.setEnabled(false);
					
					if ((MainWin.disks != null) && (MainWin.disks[sdisk] != null))
					{
						mitemInsert.setEnabled(true);
						mitemCreate.setEnabled(true);
							
						if (MainWin.disks[sdisk].isLoaded())
						{
							mitemEject.setEnabled(true);
							mitemParameters.setEnabled(true);
							mitemExport.setEnabled(true);
						}
					}
					
				}
			}
		});
		
		
		
		mitemInsert = new MenuItem (diskPopup, SWT.PUSH);
		mitemInsert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(table.getSelectionIndex());
			}
		});
		mitemInsert.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/floppy_in_16.png"));
		mitemInsert.setText ("Insert...");
		
		
		mitemExport = new MenuItem (diskPopup, SWT.PUSH);
		mitemExport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.writeDiskTo(MainWin.getCurrentDiskNo());
			}
		});
		mitemExport.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/document-save-as-5.png"));
		mitemExport.setText ("Export...");
		
		
		mitemCreate = new MenuItem (diskPopup, SWT.PUSH);
		mitemCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				CreateDiskWin window = new CreateDiskWin(shell,SWT.DIALOG_TRIM);
				
				window.open();
			}
		});
		mitemCreate.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/media-floppy-new.png"));
		mitemCreate.setText ("Create...");
		
		
	
		
		
		new MenuItem(diskPopup, SWT.SEPARATOR);
		
		
		mitemEject = new MenuItem (diskPopup, SWT.PUSH);
		mitemEject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw disk eject " + table.getSelectionIndex());
			}
		});
		mitemEject.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/media-eject.png"));
		mitemEject.setText ("Eject");
		
		
		
		new MenuItem(diskPopup, SWT.SEPARATOR);
		
		mitemParameters = new MenuItem (diskPopup, SWT.PUSH);
		mitemParameters.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (MainWin.disks[table.getSelectionIndex()].hasParamwin())
				{
					MainWin.disks[table.getSelectionIndex()].getParamwin().shell.setActive();
				}
				else
				{
					MainWin.disks[table.getSelectionIndex()].setParamwin(new DiskAdvancedWin(shell, SWT.DIALOG_TRIM, MainWin.disks[table.getSelectionIndex()]));
					MainWin.disks[table.getSelectionIndex()].getParamwin().open();

				}
			}
		});
		mitemParameters.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/kcontrol-3.png"));
		mitemParameters.setText ("Parameters...");
		
		table.setMenu(diskPopup);
		
		
		
		tabFolderOutput = new TabFolder(sashForm, SWT.NONE);
		tabFolderOutput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolderOutput.getItems()[tabFolderOutput.getSelectionIndex()].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/tabfolder/user-invisible-2.png"));
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
		
		listServerLog = new org.eclipse.swt.widgets.List(composite_1, SWT.V_SCROLL);
		listServerLog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeSrvControl = new Composite(composite_1, SWT.NONE);
		compositeSrvControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		compositeSrvControl.setLayout(new FillLayout(SWT.VERTICAL));
		
		Button btnServerStart = new Button(compositeSrvControl, SWT.NONE);
		btnServerStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startDWServer(null);
			}
		});
		
		btnServerStart.setToolTipText("Start Local Server");
		btnServerStart.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/server/media-playback-start-4.png"));
		
		Button btnServerStop = new Button(compositeSrvControl, SWT.NONE);
		btnServerStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendCommand("ui server terminate");
			}
		});
		
		btnServerStop.setToolTipText("Stop Server");
		btnServerStop.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/server/media-playback-stop-4.png"));
		
		Button btnServerStatus = new Button(compositeSrvControl, SWT.NONE);
		btnServerStatus.setToolTipText("Server Status");
		btnServerStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/server/system-run-3.png"));
		
		
		sashForm.setWeights(new int[] { config.getInt("SashForm_Weights(0)", 391), config.getInt("SashForm_Weights(1)", 136)});
		
		
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
								
								e.doit = false;
								
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

	


	private static void createDiskTableColumns()
	{
		synchronized(table)
		{
			for (String key: MainWin.getDiskTableParams())
			{
				TableColumn col = new TableColumn(table, SWT.NONE);
				col.setMoveable(true);
				col.setResizable(true);
				col.setData("param", key);
				
				col.setWidth(config.getInt(key +"_ColWidth",50));
				if (key.startsWith("_") && (key.length()>2))
					col.setText(key.substring(1, 2).toUpperCase() + key.substring(2));
				else if (!key.equals("LED") && key.length()>1)
					col.setText(key.substring(0, 1).toUpperCase() + key.substring(1));
			}
		}
	}


	private static List<String> getDiskTableParams()
	{
		return(Arrays.asList(MainWin.config.getStringArray("DiskTable_Items")));
	}


	public static int getTPIndex(String key)
	{
		synchronized(table)
		{
			for (int i = 0;i<table.getColumnCount();i++)
			{
				if (table.getColumn(i).getData("param").equals(key))
					return i;
			}
		}
		
		
		return(-1);
	}

	protected static Point getDiskWinInitPos(int drive)
	{
		Point res = new Point(MainWin.config.getInt("DiskWin_"+ drive +"_x",shell.getLocation().x + 20), MainWin.config.getInt("DiskWin_"+ drive +"_y",shell.getLocation().y + 20));
		
		if (!isValidDisplayPos(res))
				res = new Point(shell.getLocation().x + 20, shell.getLocation().y + 20);
				
		
		return(res);
	}


	private static boolean isValidDisplayPos(Point p)
	{
		// check for invalid saved position
		Monitor[] list = display.getMonitors();
	
		for (int i = 0; i < list.length; i++) 
		{
			if (list[i].getBounds().contains(p))
				return true;
		}
		return false;
	}





	protected static void doShutdown() 
	{
	  
		ShutdownWin sdwin = new ShutdownWin(shell, SWT.DIALOG_TRIM);
		
		sdwin.open();
		
		
		if (config.getBoolean("TermServerOnExit",true))
		{
			sdwin.setStatus("Stopping DriveWire server...",20);
			// sendCommand("ui server terminate");
			stopDWServer();
		}
		
		// save window pos
		sdwin.setStatus("Saving main window layout...",40);
		
		config.setProperty("MainWin_Width", shell.getSize().x);
		config.setProperty("MainWin_Height", shell.getSize().y);
		config.setProperty("MainWin_x", shell.getLocation().x);
		config.setProperty("MainWin_y", shell.getLocation().y);
		
		config.setProperty("SashForm_Weights", sashForm.getWeights());
		
		for (int i = 0;i<table.getColumnCount();i++)
		{
			config.setProperty("DiskTable_Items("+ i +")", table.getColumn(table.getColumnOrder()[i]).getData("param") );
			config.setProperty(table.getColumn(i).getData("param") +"_ColWidth", table.getColumn(i).getWidth());
		}
		
		sdwin.setStatus("Saving disk window layouts...",65);
		
		for (int i = 0;i<256;i++)
		{
			
			sdwin.setProgress(65 + (i / 8));
			if (disks[i].hasDiskwin())
			{
				disks[i].getDiskwin().close();
				config.setProperty("DiskWin_"+ i+"_open",true);
				
			}
		
		}
		
		sdwin.setStatus("Done...",100);
	
		
	}


	

	
	
	
	static void debug(String string)
	{
		System.out.println(string);
		
	}


	protected void loadDWCmdHelpMenu() 
	{

		
		List<String> topics;
		try 
		{
			topics = UIUtils.loadList(MainWin.instance, "ui server show helptopics");
			
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

	public void openURL(String url) 
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


	
	
	private static void updateMidiMenus()
	{

		// much more complicated than necessary so that changes are reflected in real time
		
		if (MainWin.midiStatus.isEnabled())
		{
			mntmMidi.setEnabled(true);
			
			// is menu built ok
			MenuItem[] profitems = MainWin.menuMIDIProfiles.getItems();
			String[] profiles = MainWin.midiStatus.getProfiles().toArray(new String[0]);
			
			boolean profok = false;
			
			if (profitems.length == profiles.length)
			{
				profok = true;
				
				for (int i = 0;i<profitems.length;i++)
				{
					if (profitems[i].getText().equals(MainWin.midiStatus.getProfile(profiles[i]).getDesc()))
					{
						profitems[i].setSelection(profiles[i].equals(MainWin.midiStatus.getCurrentProfile()));
					}
					else
					{
						profok = false;
					}
				}
			}
			
			if (!profok)
			{
				// rebuild the whole thing
				MainWin.menuMIDIProfiles.dispose();
				MainWin.menuMIDIProfiles = new Menu(MainWin.mntmSetProfile);
				MainWin.mntmSetProfile.setMenu(MainWin.menuMIDIProfiles);
			
			
				Iterator<String> itr = MainWin.midiStatus.getProfiles().iterator();
			
				while(itr.hasNext())
				{
					final String key = itr.next();
					MenuItem tmp = new MenuItem(MainWin.menuMIDIProfiles, SWT.CHECK);
					tmp.setText(MainWin.midiStatus.getProfile(key).getDesc());
				
					tmp.setSelection(key.equals(MainWin.midiStatus.getCurrentProfile()));
					tmp.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) 
						{
						sendCommand(("dw midi synth profile " + key));
						}
					});
				}	
			}
			
			
			// same for outputs.. is menu built ok
			MenuItem[] outitems = MainWin.menuMIDIOutputs.getItems();
			String[] outs = MainWin.midiStatus.getDevices().toArray(new String[0]);
			
			boolean outok = false;
			
			if (outitems.length == outs.length)
			{
				outok = true;
				
				for (int i = 0;i<outitems.length;i++)
				{
					if (outitems[i].getText().equals(outs[i]))
					{
						outitems[i].setSelection(outs[i].equals(MainWin.midiStatus.getCurrentDevice()));
					}
					else
					{
						outok = false;
					}
				}
			}
			
			if (!outok)
			{
			
				MainWin.menuMIDIOutputs.dispose();
				MainWin.menuMIDIOutputs = new Menu(MainWin.mntmSetOutput);
				MainWin.mntmSetOutput.setMenu(MainWin.menuMIDIOutputs);
			
				Iterator<String> itr = MainWin.midiStatus.getDevices().iterator();
			
				while (itr.hasNext())
				{
					final String key = itr.next();
					MenuItem tmp = new MenuItem(MainWin.menuMIDIOutputs, SWT.CHECK);
					tmp.setText(key);
				
						
					tmp.addSelectionListener(new SelectionAdapter() 
					{
						@Override
						public void widgetSelected(SelectionEvent e) 
						{
							sendCommand(("dw midi output " + MainWin.midiStatus.getDevice(key).getDevnum()));
						}
					});
					
				
					tmp.setSelection(key.equals(MainWin.midiStatus.getCurrentDevice()));
				}
				
			}	
			
			// voice lock
			mntmLockInstruments.setSelection(MainWin.midiStatus.isVoiceLock());
		}
		else
		{
			mntmMidi.setEnabled(false);
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

	



	
	
	


	
	public static void refreshDiskTable() 
	{
		
		MainWin.table.setRedraw(false);
		
		for (int i = 0;i<256;i++)
		{
			MainWin.clearDiskTableEntry(i);
			
			if ((disks[i] != null) && disks[i].isLoaded())
			{
				MainWin.setDiskTableEntryFile(i, disks[i].getPath());
				
				Iterator<String> itr = disks[i].getParams();
				
				while (itr.hasNext())
				{
					String key = itr.next();
					setDiskTableEntry(i,key,disks[i].getParam(key).toString());
				}
				
			}
			
		}
		
		if (MainWin.currentDisk >= 0)
		{
			table.setSelection(MainWin.currentDisk);
		}
	
		MainWin.table.setRedraw(true);
		
	}
		


	
	public static void setDiskTableEntry(int disk, String key, String val)
	{
		int col = MainWin.getTPIndex(key);
		
		if (col > -1)
			table.getItem(disk).setText(col,val);
	}


	protected void quickInDisk(final int diskno)
	{    
		final String curpath;
		
		if ((disks[diskno] != null) && (disks[diskno].isLoaded()))
			curpath = disks[diskno].getPath();
		else
			curpath = "";
		
		Thread t = new Thread(new Runnable() {
			  public void run()
			  {
				  String res = getFile(false,false,curpath,"Choose an image for drive " + diskno + " to...", "Open");
					
					if (res != null)
						MainWin.sendCommand("dw disk insert "+ diskno + " " + res);
			  }
			});

		t.start();
		
	}
	
	
	protected static void writeDiskTo(final int diskno) 
	{
		final String curpath;
		
		if (table.getItem(diskno) != null)
			curpath = table.getItem(diskno).getText(2);
		else
			curpath = "";
		
		Thread t = new Thread(new Runnable() {
					  public void run()
					  {
						  String res = getFile(true,false,curpath,"Write image in drive " + diskno + " to...", "Save");
							
							if (res != null)
								MainWin.sendCommand("dw disk write "+ diskno + " " + res);
					  }
					});
		
		t.start();
				
				
		
	}
	
	
	
	public static String getFile(final boolean save, final boolean dir, final String startpath, final String title, final String buttontext)
	{
			
		if (config.getBoolean("UseRemoteFilebrowser", false))
		{
			// remote
			
			RemoteFileBrowser rfb = new RemoteFileBrowser(save,dir,startpath,title,buttontext);
			
			
			try
			{
				SwingUtilities.invokeAndWait(rfb);
			} 
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (InvocationTargetException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			return (rfb.getSelected());
			
			
		}
		else
		{
			LocalFileBrowser lfb = new LocalFileBrowser(save,dir,startpath,title,buttontext);
			display.syncExec(lfb);
			
			return (lfb.getSelected());
		}
		
		
	}
	
	

	protected static void sendCommand(String cmd)
	{
		sendCommand(cmd, true);
	}
	
	protected static void sendCommand(final String cmd, final boolean errtomainwin) 
	{
	
		Thread cmdT = new Thread(new Runnable() 
		{
			  public void run() 
			  {
				  Connection connection = new Connection(host,port,instance);
					
				  try 
				  {
					  connection.Connect();
					  connection.sendCommand(cmd,instance,errtomainwin);
					  connection.close();
				  } 
				  catch (UnknownHostException e) 
				  {
					  showError("'Unknown host' while sending command", e.getMessage(), "You may have a DNS problem, or the server hostname may not be specified correctly." , errtomainwin);
				  } 
				  catch (IOException e1) 
				  {
					// UIUtils.getStackTrace(e1)
				
					  showError("IO error sending command", e1.getMessage(), "You may have a connectivity problem, or the server may not be running.", errtomainwin);
						
				  } 
				  catch (DWUIOperationFailedException e2) 
				  {
					  showError("DW error sending command", e2.getMessage(), "", errtomainwin);
				  } 
			  } 
		});
		
		cmdT.start();

	}


	public static void showError(String t, String s, String d)
	{
		showError(new DWError(t,s,d,true));
	}
	
	public static void showError(String t, String s, String d, boolean gui)
	{
		showError(new DWError(t,s,d,!gui));
	}
	

	public static void showError(final DWError dwerror)
	{
	
		if (dwerror.isGui())
		{
			display.asyncExec(
					  new Runnable() {
						  public void run()
						  {
							  ErrorWin ew = new ErrorWin(shell,SWT.DIALOG_TRIM,dwerror);
							  ew.open();
						  }
					  });
			
		}
		else
		{
			
			addToDisplay(System.getProperty("line.separator") + dwerror.getTextError());
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
						  if (!textDWOutput.isDisposed())
						  {
							  textDWOutput.append(System.getProperty("line.separator") + txt);
							  if (MainWin.tabFolderOutput.getSelectionIndex() != 0)
							  {
								  MainWin.tabFolderOutput.getItems()[0].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/tabfolder/user-online-2.png"));
							  }
						  }
					  }
				  });
		}
		else
		{
			System.out.println(txt);
		}
	}
	
	public static void addToServerDisplay(final String txt) 
	{
		
		if (listServerLog != null)
		{
			display.syncExec(
				  new Runnable() {
					  public void run()
					  {
						  listServerLog.setRedraw(false);
						  
						  listServerLog.add(txt);
						  // sort of crappy way to scroll.. ?
						  listServerLog.setSelection(listServerLog.getItemCount()-1);
						  listServerLog.deselectAll();
						  
						  listServerLog.setRedraw(true);
						  
						  if (MainWin.tabFolderOutput.getSelectionIndex() != 1)
						  {
							  MainWin.tabFolderOutput.getItems()[1].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/tabfolder/user-online-2.png"));
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

	

	public static void setConStatusConnect() 
	{
		
		if (MainWin.isReady())
		{
			
			synchronized(connected)
			{
				if (!connected)
				{
					display.syncExec(
							new Runnable() {
								public void run()
								{
							MainWin.connected = true;
							/*
							 MainWin.lblConStatus.setRedraw(false);
							 MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/constatus/network-transmit-2.png"));
							 MainWin.lblConStatus.setRedraw(true);
							*/
								
							MainWin.setItemsConnectionEnabled(true);
							
						
					  }
				  });
				}
			}
		}
		
		
	}
	
	protected static void setItemsConnectionEnabled(boolean en) 
	{
		// enable/disable all controls requiring a server connection
		
	
	
		MenuItem[] items = MainWin.menu_tools.getItems();
		
		for (int i = 0;i<items.length;i++)
		{
			items[i].setEnabled(en);
		}
		
		items = MainWin.menu_config.getItems();
		
		for (int i = 0;i<items.length;i++)
		{
			items[i].setEnabled(en);
		}
		
		mntmDwCommands.setEnabled(en);
		mntmChooseInstance.setEnabled(en);
		
		MainWin.table.setEnabled(en);
		MainWin.txtYouCanEnter.setEnabled(en);
		
		// stuff that stays enabled
		MainWin.mntmInitialConfig.setEnabled(true);
		MainWin.mntmUserInterface.setEnabled(true);
		
		
	}




	public static void setConStatusError() 
	{
		/*
		if (MainWin.lblConStatus != null)
		{
			synchronized(connected)
			{
				if (connected)
				{
					display.syncExec(
							new Runnable() {
								public void run()
								{
							  MainWin.lblConStatus.setRedraw(false);
							  MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/constatus/network-error-2.png"));
							  MainWin.lblConStatus.setRedraw(true);
						  
							  MainWin.connected = false;

							  MainWin.setItemsConnectionEnabled(false);
								
						  
					  }
				  
				  });
				}
			}
		}
	
		*/
	}


	
	public static void setConStatusTrying() 
	{
		/*
		if (MainWin.lblConStatus != null)
		{
			display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  MainWin.lblConStatus.setRedraw(false);
						  MainWin.lblConStatus.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/constatus/user-away.png"));
						  MainWin.lblConStatus.setRedraw(true);
					  }
				  });
		}
		*/
	}
	
	
	
	
	


	public static void doDisplayAsync(Runnable runnable) 
	{
		display.asyncExec(runnable);
	}
	
	protected TabFolder getTabFolderOutput() {
		return tabFolderOutput;
	}


	public static void setServerConfig(HierarchicalConfiguration serverConfig) 
	{
		MainWin.dwconfig = serverConfig;
	}


	public static void restartServerConn() 
	{
		if (syncObj != null)
		{
			MainWin.syncObj.die();
		}
		
		// start threads that talk with server
		syncObj = new SyncThread();
		syncThread = new Thread(syncObj);
		syncThread.setDaemon(true);
		syncThread.start();

		
	}


	public static void applyConfig() 
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  
						  
						  // hdbdos menu toggle
						  MainWin.mntmHdbdosTranslation.setSelection(MainWin.getInstanceConfig().getBoolean("HDBDOSMode", false));
						  
						  
					  }
				  });
		
	}
	
	
	public static void applyDisks() 
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
							refreshDiskTable();
							
					  }
				  });
		
	}


	
	public static DiskDef getCurrentDisk() 
	{
		return disks[MainWin.currentDisk];
	}

	public static int getCurrentDiskNo()
	{
		return(MainWin.currentDisk);
	}

	public static DiskDef getDiskDef(int dno) 
	{
		return(disks[dno]);
	}






	public static void setMidiStatus(MIDIStatus serverMidiStatus) 
	{
		MainWin.midiStatus = serverMidiStatus;
	}


	public static void applyMIDIStatus() 
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  // midi menus
						  updateMidiMenus();
						  
					  }
				  });
	}


	public static MIDIStatus getMidiStatus() 
	{
		return(MainWin.midiStatus);
	}


	public static void setDisks(DiskDef[] serverDisks) 
	{
		MainWin.disks = new DiskDef[serverDisks.length];
		System.arraycopy(serverDisks, 0, MainWin.disks, 0, serverDisks.length);
	}


	public static String getUIText() 
	{
		return MainWin.textDWOutput.getText();
	}







	public static void submitDiskEvent(final int disk, final String key, final String val)
	{

		
		// local display items
		
		if (disks[disk] == null)
		{
			debug("NULL disk in submitevent: " + disk);
			disks[disk] = new DiskDef(disk);
		}

		disks[disk].setParam(key, val);
		
		if (key.startsWith("*"))
		{
			if (key.equals("*insert"))
			{
				clearDiskTableEntry(disk);
				
				setDiskTableEntryFile(disk, val);
				
			}
			else if (key.equals("*eject"))
			{
				clearDiskTableEntry(disk);
			}
		}
		
		// update disk table 
		MainWin.diskTableUpdater.addUpdate(disk,key, val);
		
		if (key.equals("_reads") && !val.equals("0"))
		{
			MainWin.diskTableUpdater.addUpdate(disk,"LED",MainWin.diskLEDgreen);
			MainWin.driveactivity = true;
		}
		else if (key.equals("_writes") && !val.equals("0"))
		{
			MainWin.diskTableUpdater.addUpdate(disk,"LED",MainWin.diskLEDred);
			MainWin.driveactivity = true;
		}
		
	}

	
	private static void setDiskTableEntryFile(final int disk, final String val)
	{
		// sync?
		display.syncExec(new Runnable() {
			  public void run()
			  {
		
		// set file
				  int filecol = MainWin.getTPIndex("File");
				  if (filecol > -1)
					  table.getItem(disk).setText(filecol, UIUtils.getFilenameFromURI(val));
		 
		// set location
				  int loccol = MainWin.getTPIndex("Location");
				  if (loccol > -1)
					  table.getItem(disk).setText(loccol, UIUtils.getLocationFromURI(val));
				  
				  int ledcol = MainWin.getTPIndex("LED");
				  if (ledcol > -1)
					  table.getItem(disk).setImage(ledcol, MainWin.diskLEDdark);
		
			  }
		  });

	}


	private static void clearDiskTableEntry(final int disk)
	{
		
		
		// sync?
		display.syncExec(new Runnable() {
			  public void run()
			  {
				  synchronized(table)
				  {
					  int drivecol = MainWin.getTPIndex("Drive");
					  int ledcol = MainWin.getTPIndex("LED");
					  
					  String[] txt = new String[table.getColumnCount()];
					  for (int i = 0;i<txt.length;i++)
						  txt[i] = "";
					  
					  // make sure it exists
					  while (table.getItemCount() < (disk + 1))
					  {
						  TableItem item = new TableItem(table, SWT.NONE);
						  
						 
						  if (drivecol > -1)
							  item.setText(drivecol, disk+"");
					  }
					  
					  // clear all txt
					  table.getItem(disk).setText(txt);
					  
					  // set Drive #
					  if (drivecol > -1)
						  table.getItem(disk).setText(drivecol, disk+"");
					  
					  // clear image
					  if (ledcol > -1)
						  table.getItem(disk).setImage(ledcol,null);
					  
				  }
				  
			  }
		  });

		
	}



	protected org.eclipse.swt.widgets.List getListServerLog() {
		return listServerLog;
	}


	public static String getServerText()
	{
		StringBuilder sb = new StringBuilder(listServerLog.getItemCount() * 80);
		for (String l : listServerLog.getItems())
		{
			sb.append(l + "\r\n");
		}
		
		return(sb.toString());
	}


	public static void addToServerDisplay(List<String> lines)
	{
		for (String l : lines)
		{
			addToServerDisplay(l);
		}
	}


	public static boolean isReady()
	{
		return MainWin.ready;
	}


	public static void updateDiskTableItem(final int item, final String key, final Object object)
	{
		
		if (disks[item].isLoaded())
		{
			display.syncExec(new Runnable() {
					  public void run()
					  {
						  int keycol = MainWin.getTPIndex(key);

						  if (keycol > -1)
						  {
							  if (object.getClass().getSimpleName().equals("String"))
								  table.getItem(item).setText(keycol, object.toString());
							  else if (object.getClass().getSimpleName().equals("Image"))
								  table.getItem(item).setImage(keycol, (Image) object);
						  }
						  
					  }
				  });
		}
	}


	public static Display getDisplay()
	{
		return display;
	}


	


	public static void addDiskTableColumn(String key)
	{
		synchronized(table)
		{
			if (MainWin.getTPIndex(key) < 0)
			{
				MainWin.config.addProperty("DiskTable_Items", key);
				
				while(table.getColumnCount() > 0)
				{
					table.getColumn(0).dispose();
				}
				
				MainWin.createDiskTableColumns();
				
				MainWin.refreshDiskTable();
				
			}
		}
	}


	public static void removeDiskTableColumn(String key)
	{
		synchronized(table)
		{
			if (MainWin.getTPIndex(key) > -1)
			{
				MainWin.config.clearProperty("DiskTable_Items(" + MainWin.getTPIndex(key) + ")");
				
				while(table.getColumnCount() > 0)
				{
					table.getColumn(0).dispose();
				}
				
				MainWin.createDiskTableColumns();
				
				MainWin.refreshDiskTable();
				
			}
		}
		
	}


	public static void submitServerConfigEvent(String key, Object val)
	{
		if (val == null)
		{
			MainWin.dwconfig.clearProperty(key);
		}
		else
		{
			MainWin.dwconfig.setProperty(key, val);
		}
		
		if ((serverconfigwin != null) && (!serverconfigwin.shlServerConfiguration.isDisposed()))
		{
			serverconfigwin.submitEvent(key,val);
		}
	}


	
	
}