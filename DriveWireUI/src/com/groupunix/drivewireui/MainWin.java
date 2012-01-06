package com.groupunix.drivewireui;




import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.ArmEvent;



public class MainWin {

	static Logger logger = Logger.getLogger(MainWin.class);
	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %m%n");


	
	public static final String DWUIVersion = "4.0.2";
	public static final String DWUIVersionDate = "01/05/2012";
	
	
	public static final String default_Host = "127.0.0.1";
	public static final int default_Port = 6800;
	public static final int default_Instance = 0;
	
	public static final int default_DiskHistorySize = 20;
	public static final int default_ServerHistorySize = 20;
	public static final int default_CmdHistorySize = 20;
	
	public static final int default_TCPTimeout = 15000;
	
	
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
	
	

	static Table table;
	
	private static SashForm sashForm;
	
	private static MenuItem mntmFile;
	private static MenuItem mntmTools;
	private static MenuItem mntmHdbdosTranslation;

	
	private static Menu menu_file;
	private static Menu menu_tools;
	private static Menu menu_config;
	private static Menu menu_help;

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
	static TabFolder tabFolderOutput;
	
	
	
	private static SyncThread syncObj;
	
	private static DiskDef[] disks = new DiskDef[256];
	private static MIDIStatus midiStatus;
	
	public static Color colorWhite;
	public static Color colorRed;
	public static Color colorGreen;
	public static Color colorBlack;
	public static Color colorCmdTxt;
	public static Color colorGraphBG;
	public static Color colorGraphFG;
	public static Color colorMemGraphFreeH;
	public static Color colorMemGraphFreeL;
	
	public static Color colorMemGraphUsed;
	
	
	protected static Font fontDiskNumber;
	protected static Font fontDiskGraph;
	protected static Font fontGraphLabel;
	
	
	private static Boolean driveactivity = false;

	

	private static MenuItem mitemInsert;
	private static MenuItem mitemEject;
	private static MenuItem mitemExport;
	private static MenuItem mitemCreate;
	private static MenuItem mitemParameters;
	private static MenuItem mitemReload;
	private static MenuItem mitemController;
	
	protected static Vector<DiskStatusItem> diskStatusItems;
	protected static Vector<LogItem> logItems = new Vector<LogItem>();
	
	private static Table logTable;
	
	private static boolean ready = false;
	
	private static Image diskLEDgreen;
	private static Image diskLEDred;
	private static Image diskLEDdark;
	protected static Image diskBigLEDgreen;
	protected static Image diskBigLEDred;
	protected static Image diskBigLEDdark;
	
	private static DiskTableUpdateThread diskTableUpdater;
	protected static boolean safeshutdown = false;
	
	
	private static ServerConfigWin serverconfigwin;

	static UITaskMaster taskman;
	static ScrolledComposite scrolledComposite;
	
	public static Font logFont;
	public static Font dialogFont;
	public static ErrorHelpCache errorHelpCache = new ErrorHelpCache();
	private static Composite compositeList;
	private Composite compServer;

	protected static Image graphMemUse;
	protected static Image graphDiskOps;
	protected static Image graphVSerialOps;
	
	protected static Canvas canvasMemUse;
	protected static Canvas canvasDiskOps;
	protected static Canvas canvasVSerialOps;
	
	protected static ServerStatusItem serverStatus = new ServerStatusItem();
	private Menu menu_1;
	
	
	
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
			colorCmdTxt = new Color(display, 0x90,0x90,0x90);

			colorGraphBG = new Color(display, 50,50,50);
			colorGraphFG = new Color(display, 200,200,200);
			
			colorMemGraphFreeH = new Color(display, 80,255,80);
			colorMemGraphFreeL = new Color(display, 40,200,40);
			
			colorMemGraphUsed = new Color(display, 200, 50, 50);
			
			
			
			diskLEDgreen = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledgreen.png");
			diskLEDred = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledred.png");
			diskLEDdark = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-leddark.png");
			diskBigLEDgreen = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledgreen-big.png");
			diskBigLEDred = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-ledred-big.png");
			diskBigLEDdark = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-leddark-big.png");
			
			
			UIUtils.loadFonts();
			
			
			HashMap<String,Integer> fontmap = new HashMap<String,Integer>();
			
			fontmap.put("Droid Sans Mono", SWT.NORMAL);
			
			logFont = UIUtils.findFont(display, fontmap, "WARNING", 50, 14);
			
			fontmap.clear();
			fontmap.put("Droid Sans", SWT.NORMAL);
			
			fontGraphLabel = UIUtils.findFont(display, fontmap, "WARNING", 42, 13);
			
			graphMemUse = new Image(null,270,110);
			graphDiskOps = new Image(null,270,110);
			graphVSerialOps = new Image(null,270,110);
			
			// start grapher
			Thread gt = new Thread(new GrapherThread());
			gt.setDaemon(true);
			gt.start();
			
			MainWin window = new MainWin();

			
			// macs are special, special things
			doMacStuff();
			
			// sync
			restartServerConn();
		
			
			
			// get this party started
			window.open(display, args);
			
			
		} 
		catch (Exception e) 
		{
			System.out.println("\nSomething's gone horribly wrong:\n");
			e.printStackTrace();
		
		}
		
		// game over.  flag to let threads know.
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
		
		//dwThread.setDaemon(true);
		dwThread.start();
	}

	private static void stopDWServer()
	{
		if ((dwThread != null) && (!dwThread.isInterrupted()))
		{
			int tid = MainWin.taskman.addTask("Stop local server");
			
			MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, "Stopping DriveWire server...");
			
			// interrupt the server..
			dwThread.interrupt();
			
			try 
			{
				dwThread.join(3000);
				MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_COMPLETE, "DriveWire server shut down.");
				
			} 
			catch (InterruptedException e) 
			{
				MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_FAILED, "Interrupted while waiting for the server to exit.");
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
		
		
		
		if (!connected)
		{
			MainWin.setItemsConnectionEnabled(false);
	
		}
		
		shell.open();
		shell.layout();

		
		
		
	
		
		//if (firsttimer) TODO
		
		// drive light and other animations
		
		 Runnable drivelightoff = new Runnable() 
		 	{
			 
			 private int ctr = 0;
			 
		      public void run() 
		      {
		    	  ctr++;
		    	  if ((ctr % 12 == 0) && (MainWin.driveactivity.booleanValue() == true))
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
			  	 
		    	
		    	  MainWin.taskman.rotateWaiters();
		    	  
		    	  display.timerExec(90, this);
		      }
		    };
		    
		    
	    display.timerExec(2000, drivelightoff);
		
	    
	    MainWin.ready = true;
		
	    int tid = MainWin.taskman.addTask("/splash");
		MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_COMPLETE, MainWin.DWUIVersion + " (" + MainWin.DWUIVersionDate + ")" );
	    
		MainWin.doSplashTimers(tid, true);
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
	}

	
	
	




	private static void doSplashTimers(final int tid, boolean both)
	{
		final Runnable anim2 =   new Runnable() {
			public void run()
			  {
				for (int i = 0;i<10;i++)
				{
					((UITaskCompositeSplash)MainWin.taskman.getTask(tid).getTaskcomp()).doAnim2(i);
	
				}
				
				for (int i = 9;i>=0;i--)
				{
					((UITaskCompositeSplash)MainWin.taskman.getTask(tid).getTaskcomp()).doAnim2(i);
					
				}
				

				((UITaskCompositeSplash)MainWin.taskman.getTask(tid).getTaskcomp()).showVer();
				
			  }
			  
		};
		
	    Runnable anim1 =   new Runnable() {
					  public void run()
					  {
						  
						  
						  if (((UITaskCompositeSplash)MainWin.taskman.getTask(tid).getTaskcomp()).doAnim())
						  	display.timerExec(10, this);
						  else
							display.timerExec(20, anim2);
					  }
				  };
				  
	    if (both)
	    	display.timerExec(750, anim1);
	    else
	    	display.timerExec(100, anim2);
	}


	
	

	

	private static void updateTitlebar() 
	{
		String txt = "DriveWire - " + host + ":" + port + " [" + instance + "]";

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
		mntmDisksetManager.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/world-link.png"));
		mntmDisksetManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DWBrowser dmw =  new DWBrowser(display);
				dmw.open();
			}
		});
		mntmDisksetManager.setText("DW Browser..");
		
		new MenuItem(menu_tools, SWT.SEPARATOR);
		
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
				MainWin.processClientCmd("/wizard");
			}
		});
		mntmInitialConfig.setText("Simple Config Wizard...");
		
		new MenuItem(menu_config, SWT.SEPARATOR);
		
		MenuItem mntmServer_1 = new MenuItem(menu_config, SWT.NONE);
		mntmServer_1.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/computer-edit.png"));
		mntmServer_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{

				
				//ConfigEditor ce = new ConfigEditor(display);
				//ce.open();
			
			}
		});
		mntmServer_1.setText("Configuration Editor...");
		mntmServer_1.setEnabled(false);

		
		mntmUserInterface = new MenuItem(menu_config, SWT.CASCADE);
		
		mntmUserInterface.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/image-edit.png"));
		
		mntmUserInterface.setText("User Interface Options");
		
		menu_1 = new Menu(mntmUserInterface);
		mntmUserInterface.setMenu(menu_1);
		
		final MenuItem mntmUseInternalServer = new MenuItem(menu_1, SWT.CHECK);
		mntmUseInternalServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (config.getBoolean("LocalServer",false))
				{
					config.setProperty("LocalServer", false);
					
					stopDWServer();
					
				}
				else
				{
					config.setProperty("LocalServer", true);
					
					startDWServer(null);
				}
			}
		});
		mntmUseInternalServer.setText("Use internal server");
		
		
		
		final MenuItem mntmUseRemoteFile = new MenuItem(menu_1, SWT.CHECK);
		mntmUseRemoteFile.setText("Use remote file dialogs");
		mntmUseRemoteFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (config.getBoolean("UseRemoteFilebrowser",false))
				{
					config.setProperty("UseRemoteFilebrowser", false);
				}
				else
				{
					config.setProperty("UseRemoteFilebrowser", true);
				}
			}
		});
		
	
		
		mntmUserInterface.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) 
			{
				mntmUseInternalServer.setSelection(config.getBoolean("LocalServer",false));
				mntmUseRemoteFile.setSelection(config.getBoolean("UseRemoteFilebrowser", false));	
			}
		});
		
		
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
				
				String path = getFile(false, false, "", "Choose a soundbank file to load...", "Open");
			
				if (path != null)
					MainWin.sendCommand("dw midi synth bank " + path );
			
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
		
		final MenuItem mntmPrinting = new MenuItem(menu_config, SWT.CASCADE);
		
		mntmPrinting.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/document-print.png"));
		mntmPrinting.setText("Current Printer");
		
		final Menu menu_2 = new Menu(mntmPrinting);
		mntmPrinting.setMenu(menu_2);
		
		mntmPrinting.addArmListener(new ArmListener() {
			public void widgetArmed(ArmEvent e) {
				for (MenuItem i : menu_2.getItems())
				{
					i.dispose();
				}
				
				try
				{
					List<String> res = UIUtils.loadList(MainWin.getInstance(), "ui instance printer");
					String curp = "";
					
					for (String l : res)
					{
						final String[] parts = l.split("\\|");
						if (parts.length > 1)
						{
							if (parts[0].equals("currentprinter"))
								curp = parts[1].trim();
							if (parts[0].equals("printer") && (parts.length == 3))
							{
								MenuItem tmp = new MenuItem(menu_2, SWT.CHECK);
								tmp.setText(parts[1] + ": " + parts[2].trim());
								
								if (parts[1].equals(curp))
									tmp.setSelection(true);
								else
									tmp.setSelection(false);
								
								tmp.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e) 
									{
										sendCommand("dw config set CurrentPrinter " + parts[1]);
										
									}
								});
								
							}
						}
					}
					
					
				} 
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (DWUIOperationFailedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		
		new MenuItem(menu_config, SWT.SEPARATOR);
		
		MenuItem mntmResetInstanceDevice = new MenuItem(menu_config, SWT.NONE);
		mntmResetInstanceDevice.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/toolbar/arrow-refresh.png"));
		mntmResetInstanceDevice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("ui instance reset protodev");
				
			}
		});
		mntmResetInstanceDevice.setText("Reset Instance Device");
		
		
		
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
				openURL(this.getClass(),"http://sourceforge.net/apps/mediawiki/drivewireserver/index.php");
			}
		});
		mntmDocumentation.setText("Documentation Wiki");
		
		
		
			
		new MenuItem(menu_help, SWT.SEPARATOR);
		
		MenuItem mntmSubmitBugReport = new MenuItem(menu_help, SWT.NONE);
		mntmSubmitBugReport.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/bug.png"));
		mntmSubmitBugReport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				BugReportWin brwin = new BugReportWin(shell,SWT.DIALOG_TRIM,"User submitted","User submitted", "User submitted");
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
				AboutWin window = new AboutWin(shell,SWT.SHELL_TRIM);
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
				
				if ((drive > -1) && (MainWin.disks != null) && (MainWin.disks[drive] != null))
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
					mitemReload.setText ("Reload disk in drive " + sdisk + "...");
					mitemExport.setText("Export image in drive " + sdisk + " to...");
					mitemCreate.setText ("Create new disk for drive " + sdisk + "...");
					mitemEject.setText ("Eject disk in drive " + sdisk);
					mitemParameters.setText ("Drive " + sdisk + " parameters...");
					mitemController.setText ("Open controller for drive " + sdisk + "...");
					
					mitemInsert.setEnabled(false);
					mitemCreate.setEnabled(false);
					mitemExport.setEnabled(false);	
					mitemEject.setEnabled(false);
					mitemParameters.setEnabled(false);
					mitemReload.setEnabled(false);
					mitemController.setEnabled(false);
					
					if ((MainWin.disks != null) && (MainWin.disks[sdisk] != null))
					{
						mitemInsert.setEnabled(true);
						mitemCreate.setEnabled(true);
						mitemController.setEnabled(true);
							
						if (MainWin.disks[sdisk].isLoaded())
						{
							mitemEject.setEnabled(true);
							mitemParameters.setEnabled(true);
							mitemExport.setEnabled(true);
							mitemReload.setEnabled(true);
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
		mitemInsert.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/disk-insert.png"));
		mitemInsert.setText ("Insert...");
		
		mitemReload = new MenuItem(diskPopup, SWT.NONE);
		mitemReload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWin.sendCommand("dw disk reload " + table.getSelectionIndex(), false);
			}
		});
		mitemReload.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/toolbar/arrow-refresh.png"));
		mitemReload.setText("Reload...");
		
		
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
		mitemCreate.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/new-disk-16.png"));
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
		
		mitemController = new MenuItem(diskPopup, SWT.NONE);
		mitemController.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int drive = table.getSelectionIndex();
				if ((drive > -1) && (MainWin.disks != null) && (MainWin.disks[drive] != null))
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
					showError("Disk system not initialized", "It seems our disk drive objects are null." , "Maybe the server is still starting up, or maybe it has a serious problem.");
				}
			}
		});
		mitemController.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/disk-controller.png"));
		mitemController.setText("Controller...");
		
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
		mitemParameters.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/disk-params.png"));
		mitemParameters.setText ("Parameters...");
		
		table.setMenu(diskPopup);
		
		
		
		tabFolderOutput = new TabFolder(sashForm, SWT.NONE);
		tabFolderOutput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolderOutput.getItems()[tabFolderOutput.getSelectionIndex()].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/inactive.png"));
			}
		});
		
		TabItem tbtmUi = new TabItem(tabFolderOutput, SWT.NONE);
		
		tbtmUi.setText("UI  ");
		
	 	
		scrolledComposite = new ScrolledComposite(tabFolderOutput, SWT.V_SCROLL |  SWT.DOUBLE_BUFFERED);
		tbtmUi.setControl(scrolledComposite);
		scrolledComposite.setAlwaysShowScrollBars(true);
		scrolledComposite.setBackground(MainWin.colorWhite);
		
		Composite scrollcontents = new Composite(scrolledComposite, SWT.DOUBLE_BUFFERED);
		scrollcontents.setBackground(MainWin.colorWhite);
		scrolledComposite.setContent(scrollcontents);
		scrolledComposite.setExpandHorizontal(true);
	   // scrolledComposite.setExpandVertical(true);
		
		MainWin.taskman = new UITaskMaster(scrollcontents);
		
		
		TabItem tbtmServer = new TabItem(tabFolderOutput, SWT.NONE);
		tbtmServer.setText("Server  ");
		
		Composite composite_1 = new Composite(tabFolderOutput, SWT.NONE);
		composite_1.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		tbtmServer.setControl(composite_1);
		composite_1.setLayout(new BorderLayout(0, 0));
		
		compServer = new Composite(composite_1, SWT.NONE);
		compServer.setLayoutData(BorderLayout.NORTH);
		compServer.setBackgroundImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/logging/serverbgbar.png"));
		
		
		canvasDiskOps = new Canvas(compServer,SWT.DOUBLE_BUFFERED);
		canvasDiskOps.setBounds(20,0, MainWin.graphDiskOps.getImageData().width, MainWin.graphDiskOps.getImageData().height);
		
		canvasDiskOps.addPaintListener(new PaintListener()
		{
		   public void paintControl(final PaintEvent event)
		   {
			   event.gc.drawImage(MainWin.graphDiskOps,0,0);
				
		   }
		 });
		
		
		canvasVSerialOps = new Canvas(compServer,SWT.DOUBLE_BUFFERED);
		canvasVSerialOps.setBounds(290,0, MainWin.graphVSerialOps.getImageData().width, MainWin.graphVSerialOps.getImageData().height);
		
		canvasVSerialOps.addPaintListener(new PaintListener()
		{
		   public void paintControl(final PaintEvent event)
		   {
			   event.gc.drawImage(MainWin.graphVSerialOps,0,0);
				
		   }
		 });
		
		
		
		canvasMemUse = new Canvas(compServer,SWT.DOUBLE_BUFFERED);
		canvasMemUse.setBounds(560,0, MainWin.graphMemUse.getImageData().width, MainWin.graphMemUse.getImageData().height);
		
		canvasMemUse.addPaintListener(new PaintListener()
		{
		   public void paintControl(final PaintEvent event)
		   {
			   event.gc.drawImage(MainWin.graphMemUse,0,0);
				
		   }
		 });
		
		
		
		logTable = new Table(composite_1, SWT.BORDER | SWT.VIRTUAL);
		
		logTable.addListener(SWT.SetData, new Listener() 
		{
			    public void handleEvent(Event event) {
			       TableItem item = (TableItem)event.item;
				       int i = event.index;
				       
				       if (i < MainWin.logItems.size())
				       {
				    	   item.setImage(0, org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/logging/" + MainWin.logItems.get(i).getLevel().toLowerCase() + ".png"));
					       item.setText(1, MainWin.logItems.get(i).getShortTimestamp());
					       item.setText(2, MainWin.logItems.get(i).getLevel());
					       item.setText(3, MainWin.logItems.get(i).getShortSource());
					       item.setText(4, MainWin.logItems.get(i).getThread());
					       item.setText(5, MainWin.logItems.get(i).getMessage());
				       }
				       else
				       {
				    	   item.setText(1,"hmmm");
				       }
				    }
				 });
		
		logTable.setHeaderVisible(true);
		
		TableColumn logImg = new TableColumn(logTable,SWT.RESIZE);
		logImg.setText("");
		logImg.setWidth(24);
		
		TableColumn logTime = new TableColumn(logTable,SWT.RESIZE);
		logTime.setText("Time");
		logTime.setWidth(80);
			
		TableColumn logLevel = new TableColumn(logTable,SWT.RESIZE);
		logLevel.setText("Level");
		logLevel.setWidth(60);
		
		TableColumn logSource = new TableColumn(logTable,SWT.RESIZE);
		logSource.setText("Source");
		logSource.setWidth(100);
		
		TableColumn logThread = new TableColumn(logTable,SWT.RESIZE);
		logThread.setText("Thread");
		logThread.setWidth(100);
		
		
		TableColumn logMessage = new TableColumn(logTable,SWT.RESIZE);
		logMessage.setText("Message");
		logMessage.setWidth(400);
		
		
		if ((config.getInt("SashForm_Weights(0)", 1) != 0) && (config.getInt("SashForm_Weights(1)", 1) != 0))
			setSashformWeights(new int[] { config.getInt("SashForm_Weights(0)", 391), config.getInt("SashForm_Weights(1)", 136)});
		
		
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

	

	
	
	
	
	
	
	

	public static void setSashformWeights(int[] w)
	{
		if ((!MainWin.shell.isDisposed()) && (sashForm != null) && (!sashForm.isDisposed()))
		{
			try
			{
				sashForm.setWeights(w);
			}
			catch (IllegalArgumentException e)
			{
				// dont care
			}
		}
	}

	public static int[] getSashformWeights()
	{
		return sashForm.getWeights();
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
				
				if (key.startsWith("_"))
				{
					if (key.length()>4)
						col.setText(key.substring(1, 2).toUpperCase() + key.substring(2));
					else if (key.length() > 1)
						col.setText(key.substring(1).toUpperCase());
				}
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


	static boolean isValidDisplayPos(Point p)
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
		
		  // open progress dialog
		  sdwin.open();
		  
		  // yeah right
		  sdwin.setStatus("Encouraging consistency...",10);
		  
		// kill sync
		  MainWin.host = null;
		  MainWin.syncObj.die();
		
		  
		  
		  if (config.getBoolean("TermServerOnExit",false) || config.getBoolean("LocalServer",false) )
		  {
			  
			  sdwin.setStatus("Stopping DriveWire server...",25);
			  // sendCommand("ui server terminate");
			  stopDWServer();
		  }

		  
		  
		  // save window pos
							
		  sdwin.setStatus("Saving main window layout...",40);
	
	
						  
		  config.setProperty("MainWin_Width", shell.getSize().x);
		  config.setProperty("MainWin_Height", shell.getSize().y);
			
		  config.setProperty("MainWin_x", shell.getLocation().x);
		  config.setProperty("MainWin_y", shell.getLocation().y);
			
			
		  //sanity check, wizard might have screwed these
		  if (!(sashForm.getWeights()[0] == 0) && !(sashForm.getWeights()[1] == 0))
			  config.setProperty("SashForm_Weights", sashForm.getWeights());
			
		  sdwin.setStatus("Saving main window layout...",50);

		  for (int i = 0;i<table.getColumnCount();i++)
		  {
			  config.setProperty("DiskTable_Items("+ i +")", table.getColumn(table.getColumnOrder()[i]).getData("param") );
			  config.setProperty(table.getColumn(i).getData("param") +"_ColWidth", table.getColumn(i).getWidth());
		  }
			
		  
		  sdwin.setStatus("Saving disk window layouts...",65);
		
		  
	
		  for (int i = 0;i<256;i++)
		  {
			  sdwin.setProgress(65 + (i / 8));
			
			  if (disks != null)
				  if ((disks[i] != null) && (disks[i].hasDiskwin()))
				  {
					  disks[i].getDiskwin().close();
					  config.setProperty("DiskWin_"+ i+"_open",true);
				  }
		  }
			
		  
		  sdwin.setStatus("Exiting...",100);
		  
		// finish drawing..?
		  while (display.readAndDispatch()) {
					
			 try
			{
				
				Thread.sleep(20);
			} catch (InterruptedException e)
			{
				//dont care, just catching a few redraws
			}
				
			}
	}


	

	
	
	
	static void debug(String string)
	{
		//System.out.println(string);
		
	}


	

	public static void openURL(Class cl, String url) 
	{
		// this odd bit of code tries to use the org.eclipse.swt.program.Program.launch method to open a native browser with a url in it.
		// usually that is straighforward, but on some systems it can crash the whole works, so we use invoke to call it carefully and catch crashes.
		boolean failed = false;
		
		Class<?> c;
		try {
			
			c = Class.forName("org.eclipse.swt.program.Program", true, cl.getClassLoader() );
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
			MainWin.showError("No browser available", "Could not open a browser automatically on this system.", "The URL I wanted to show you was:  " + url);
        	
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


	private static void quickInDisk(final int diskno)
	{
		MainWin.quickInDisk(shell, diskno);
	}
	
	public static void quickInDisk(final Shell theshell, final int diskno)
	{    
		final String curpath;
		
		if ((disks[diskno] != null) && (disks[diskno].isLoaded()))
			curpath = disks[diskno].getPath();
		else
			curpath = "";
		
		Thread t = new Thread(new Runnable() {
			  public void run()
			  {
				  String res = getFile(false,false,curpath,"Choose an image for drive " + diskno, "Open");
					
					if (res != null)
					{
						final List<String> cmds = new ArrayList<String>();
						cmds.add("dw disk insert "+ diskno + " " + res);
						display.asyncExec(
								  new Runnable() {
									  public void run()
									  {
										  SendCommandWin win = new SendCommandWin(theshell, SWT.DIALOG_TRIM, cmds, "Inserting disk image...", "Please wait while the image is inserted into drive " + diskno + ".");
										  win.open();
									  }
								  });
					}
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
				  if (cmd.startsWith("/"))
				  {
					  // client command
					  processClientCmd(cmd);
				  }
				  else
				  {
					  int tid = MainWin.taskman.addTask(cmd);
					  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, "Connecting to server...");
					  
					  Connection connection = new Connection(host,port,instance);
						
					  try 
					  {
						  connection.Connect();
						  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, "Sending command: " + cmd);
						  
						  connection.sendCommand(tid,cmd,instance,errtomainwin);
						  connection.close();
						  
					  } 
					  catch (UnknownHostException e) 
					  {
						  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_FAILED, e.getMessage() +  " You may have a DNS problem, or the server hostname may not be specified correctly.");
					  } 
					  catch (IOException e1) 
					  {
						// UIUtils.getStackTrace(e1)
						  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_FAILED, e1.getMessage() +  " You may have a connectivity problem, or the server may not be running.");
							
					  } 
					  catch (DWUIOperationFailedException e2) 
					  {
						  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_FAILED, e2.getMessage());
					  } 
				  }
			  } 
		});
		
		cmdT.start();

	}


	
	
	
	
	
	
	
	
	
	
	

	
	protected static void processClientCmd(String cmd)
	{
		 final int tid = MainWin.taskman.addTask(cmd);
		  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, "Client command...");
		  
		  if (cmd.equals("/fonts"))
		  {
			  display.asyncExec(
					  new Runnable() {
						  public void run()
						  {  
							  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_COMPLETE, UIUtils.listFonts());
						  }
					  });
		  }
		  else if (cmd.equals("/splash"))
		  {
			  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_COMPLETE, MainWin.DWUIVersion + " (" + MainWin.DWUIVersionDate + ")" );
			   
			  display.asyncExec(
					  new Runnable() {
						  public void run()
						  {
							  MainWin.doSplashTimers(tid, true);
						  }
					  });
		  }
		  else if (cmd.equals("/wizard"))
		  {
			
		  }
		  else if (cmd.equals("/dumperr"))
		  {
			  display.asyncExec(
					  new Runnable() {
						  public void run()
						  {  
							  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_COMPLETE, MainWin.errorHelpCache.dump());
						  }
					  });
		  }
		  else if (cmd.equals("/midistatus"))
		  {
			  display.asyncExec(
					  new Runnable() {
						  public void run()
						  {  
							  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_COMPLETE, UIUtils.dumpMIDIStatus(MainWin.midiStatus));
						  }
					  });
		  }
		  else
		  {
			  MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_FAILED, "Unknown client command");
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
		
		if (MainWin.getInstanceConfig().getString("DeviceType","").equals("dummy"))
		{
			processClientCmd("/wizard");
		}
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
		String res = "";
		for (int i = 0; i < MainWin.taskman.getNumTasks(); i ++)
		{
			res += "tid:"+i+",stat:"+taskman.getTask(i).getStatus() + ",txt:" + taskman.getTask(i).getText();
			
			if (taskman.getTask(i).getTaskcomp() != null)
			{
				if (taskman.getTask(i).getTaskcomp().cmd != null)
					res += ",cmd:" + taskman.getTask(i).getTaskcomp().cmd;
				
			}
			
			res += "\r\n";
			
		}
		
		return(res);
		
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




	public static String getServerText()
	{
		String res = "";
		
		for (LogItem e : MainWin.logItems)
		{
			res += e.getShortTimestamp() + "," + e.getLevel() + "," + e.getShortSource() + "," + e.getThread() + "," + e.getMessage() + "\r\n";
		}
		
		return(res);
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
			int tid = MainWin.taskman.addTask("Error");
			
			MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_FAILED, dwerror.getTextError());
		}
	}


	public static void addToServerLog(LogItem litem)
	{

		MainWin.logItems.add(litem);
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
		
						  logTable.setItemCount(logItems.size());
						  logTable.setTopIndex(logItems.size());
						  
						  if (MainWin.tabFolderOutput.getSelectionIndex() != 1)
						  {
							  MainWin.tabFolderOutput.getItems()[1].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/active.png"));
						  }
						  
					  }
				  });
	}
	
	
	
	
	
	



	public static void submitServerStatusEvent(final ServerStatusItem ssbuf)
	{
		//MainWin.debug(ssbuf.toString());
		
		synchronized(serverStatus)
		{
			MainWin.serverStatus  = ssbuf;
		}
		
	}


	public static void setDWCmdText(String cmd)
	{
		MainWin.txtYouCanEnter.setText(cmd);
		
	}


	public static void selectUIPage()
	{
		if (MainWin.tabFolderOutput.getSelectionIndex() != 0)
		{
			MainWin.tabFolderOutput.setSelection(0);
		}
	}
}
