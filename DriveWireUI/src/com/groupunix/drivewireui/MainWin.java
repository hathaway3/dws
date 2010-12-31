package com.groupunix.drivewireui;


import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import swing2swt.layout.BorderLayout;

import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;


public class MainWin {

	public static final String DWUIVersion = "3.9.84";
	public static final String DWUIVersionDate = "12/28/2010";
	
	public static final String default_Host = "127.0.0.1";
	public static final int default_Port = 6800;
	public static final int default_Instance = 0;
	
	public static final int default_DiskHistorySize = 10;
	public static final int default_ServerHistorySize = 10;
	
	public static final String default_MainFont = "Lucida Console";
	public static final int default_MainFontSize = 9;
	public static final int default_MainFontStyle = 0;
	public static final String default_LogFont = "Lucida Console";
	public static final int default_LogFontSize = 9;
	public static final int default_LogFontStyle = 0;
	
	public static final String default_DialogFont = "Segoe UI";
	public static final int default_DialogFontSize = 9;
	public static final int default_DialogFontStyle = 0;
	
	public static final int default_TCPTimeout = 15000;
	
	public static XMLConfiguration config;
	public static final String configfile = "drivewireUI.xml";
	
	private static DiskDef currentDisk = new DiskDef();
	
	protected static Shell shell;

	private Text text;
	private static Display display;
	private static LogViewerWin logViewerWin;
	
	private static String host;
	private static int port;
	private static int instance;
	
	private static boolean firsttimer = false;
	private static Text textDWOutput;
	private static Table table;
	private static Combo textDiskURI;
	private static Text textDiskSizeLimit;
	private static Text textDiskOffset;
	
	private static Button btnDiskWriteProtect;
	private static Button btnDiskSync;
	private static Button btnDiskExpand;
	
	private static Label btnFilesystemIsWriteable;
	private static Label btnFileIsWriteable;
	private static Label btnFileIsRandom;
	private static Label labelDiskSectors;
	private static Label labelDiskLSN;
	private static Label labelDiskReads;
	private static Label labelDiskWrites;
	private static Label labelDiskDirty;
	private static Label lblDiskUri;
	
	private static Button btnEject;
	private static Button btnWrite;
	private static Button btnReload;
	
	private static Label lblSizeLimit;
	private static Label lblSectors;
	private static Label lblOffset;
	private static Label lblSectors1;
	private static Label lblSectors2;
	private static Label lblDirtySectors;
	private static Label lblCurrentLsn;
	private static Label lblReads;
	private static Label lblWrites;
	
	private static Button buttonRefresh;
	private static Button btnApply;
	private static Button buttonFile;
	
	private static SashForm sashForm;
	private static SashForm sashForm_1;
	private static Composite composite;
	
	
	public static void main(String[] args) 
	{
		
		loadConfig();
		
			try 
				{
					display = new Display();
					
			
					MainWin window = new MainWin();
					
					window.open(display);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
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
				//f.createNewFile(); 
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
	public void open(Display display) {
		
		createContents();
		
		applyFont();
		
		shell.open();
		shell.layout();

		updateTitlebar();

		for (int i = 0;i<256;i++)
		{
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0,i+"");
		}
		
		MainWin.currentDisk = new DiskDef();
		displayCurrentDisk();
		
		FontData f = new FontData(config.getString("MainFont",default_MainFont), config.getInt("MainFontSize", default_MainFontSize), config.getInt("MainFontStyle", default_MainFontStyle) );
		MainWin.setFont(f);
		
		if (firsttimer)
		{
		 MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		        
		 messageBox.setMessage("It looks like this is the first time the client has been run.\n\nWould you like to run the simple configuration wizard now?");
		 messageBox.setText("New Installation");
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

	private static void applyFont() 
	{
		FontData f = new FontData(config.getString("DialogFont",default_DialogFont), config.getInt("DialogFontSize", default_DialogFontSize), config.getInt("DialogFontStyle", default_DialogFontStyle) );
		
		
		Control[] controls = MainWin.composite.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(display, f));
		}
		
	}

	private static void updateTitlebar() 
	{
		shell.setText("DriveWire User Interface - " + host + ":" + port + " [" + instance + "]");
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setImage(SWTResourceManager.getImage(MainWin.class, "/dw4logo5.png"));
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) 
			{
				if (config.getBoolean("TermServerOnExit",false))
				{
					sendCommand("ui server terminate");
				}
			}
		});
		shell.setSize(763, 514);
		shell.setText("DriveWire User Interface");
		shell.setLayout(new BorderLayout(0, 0));
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmChooseServer = new MenuItem(menu_1, SWT.NONE);
		mntmChooseServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				ChooseServerWin chooseServerWin = new ChooseServerWin(shell,SWT.DIALOG_TRIM);
				chooseServerWin.open();
				
			}
		});
		mntmChooseServer.setText("Choose Server...");
		
		MenuItem mntmChooseInstance = new MenuItem(menu_1, SWT.NONE);
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
		mntmChooseInstance.setText("Choose Instance...");
		
		new MenuItem(menu_1, SWT.SEPARATOR);
		
		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				System.exit(0);
			}
		});
		mntmExit.setText("Exit");
		
		MenuItem mntmServer = new MenuItem(menu, SWT.CASCADE);
		mntmServer.setText("Tools");
		
		Menu menu_4 = new Menu(mntmServer);
		mntmServer.setMenu(menu_4);
		
		MenuItem mntmDisksetProperties = new MenuItem(menu_4, SWT.NONE);
		
		mntmDisksetProperties.setText("Diskset properties..");
		
		MenuItem mntmCreate = new MenuItem(menu_4, SWT.NONE);
		
				mntmCreate.setText("Create dsk...");
		
		MenuItem mntmdskDisk = new MenuItem(menu_4, SWT.CASCADE);
		mntmdskDisk.setText(".dsk <-> disk");
		
		Menu menu_10 = new Menu(mntmdskDisk);
		mntmdskDisk.setMenu(menu_10);
		
		MenuItem mntmTransferdskTo = new MenuItem(menu_10, SWT.NONE);
		mntmTransferdskTo.setText("Copy .dsk to floppy disk");
		
		MenuItem mntmTransferFloppyDisk = new MenuItem(menu_10, SWT.NONE);
		mntmTransferFloppyDisk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				CopyDiskToDSKWin window = new CopyDiskToDSKWin(shell,SWT.DIALOG_TRIM);
				window.open();
				
			
			}
		});
		mntmTransferFloppyDisk.setText("Copy floppy disk to .dsk");
		
		MenuItem mntmHdbdos = new MenuItem(menu_4, SWT.CASCADE);
		mntmHdbdos.setText("HDBDOS");
		
		Menu menu_2 = new Menu(mntmHdbdos);
		mntmHdbdos.setMenu(menu_2);
		
		MenuItem mntmHdbdosTranslation = new MenuItem(menu_2, SWT.CASCADE);
		mntmHdbdosTranslation.setText("HDBDOS translation");
		
		Menu menu_11 = new Menu(mntmHdbdosTranslation);
		mntmHdbdosTranslation.setMenu(menu_11);
		
		MenuItem mntmOn = new MenuItem(menu_11, SWT.NONE);
		mntmOn.setText("On");
		
		MenuItem mntmOff = new MenuItem(menu_11, SWT.NONE);
		mntmOff.setText("Off");
		
		MenuItem mntmCreateDiskset = new MenuItem(menu_2, SWT.CASCADE);
		mntmCreateDiskset.setText("Create diskset");
		
		Menu menu_9 = new Menu(mntmCreateDiskset);
		mntmCreateDiskset.setMenu(menu_9);
		
		MenuItem mntmMapMultidiskImage = new MenuItem(menu_9, SWT.NONE);
		mntmMapMultidiskImage.setText("Map multidisk image to individual disks");
		
		MenuItem mntmMapMultipleDisks = new MenuItem(menu_9, SWT.NONE);
		mntmMapMultipleDisks.setText("Map multiple disks to HDBDOS drives");
		
		new MenuItem(menu_4, SWT.SEPARATOR);
		
		MenuItem mntmStatus = new MenuItem(menu_4, SWT.NONE);
		mntmStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw server status");
			}
		});
		mntmStatus.setText("Server status");
		
		MenuItem mntmShow_2 = new MenuItem(menu_4, SWT.CASCADE);
		mntmShow_2.setText("Show server");
		
		Menu menu_21 = new Menu(mntmShow_2);
		mntmShow_2.setMenu(menu_21);
		
		MenuItem mntmHandlers = new MenuItem(menu_21, SWT.NONE);
		mntmHandlers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw server show handlers");
			}
		});
		mntmHandlers.setText("Handlers");
		
		MenuItem mntmThreads = new MenuItem(menu_21, SWT.NONE);
		mntmThreads.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw server show threads");
			}
		});
		mntmThreads.setText("Threads");
		
		new MenuItem(menu_4, SWT.SEPARATOR);
		
		MenuItem mntmLogViewer = new MenuItem(menu_4, SWT.NONE);
		mntmLogViewer.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if ((logViewerWin == null) || (logViewerWin.shell.isDisposed()))
				{
					logViewerWin = new LogViewerWin(shell,SWT.DIALOG_TRIM, getHost(), getPort());
					try 
					{
						logViewerWin.open();
						
					} 
					catch (UnknownHostException e1) 
					{
						showError("Error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
					} 
					catch (IOException e1) 
					{
						showError("Error sending command",e1.getMessage(),  UIUtils.getStackTrace(e1));
						
					} 
				}
				else
				{
					logViewerWin.shell.setFocus();
				}
				
			}
		});
		mntmLogViewer.setText("Log Viewer");
		
		MenuItem mntmMidi = new MenuItem(menu, SWT.CASCADE);
		mntmMidi.setText("MIDI");
		
		Menu menu_6 = new Menu(mntmMidi);
		mntmMidi.setMenu(menu_6);
		
		MenuItem mntmShowStatus = new MenuItem(menu_6, SWT.NONE);
		mntmShowStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sendCommand("dw midi status");
			}
		});
		mntmShowStatus.setText("Show status");
		
		MenuItem mntmSetOutput = new MenuItem(menu_6, SWT.NONE);
		mntmSetOutput.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MIDIOutputWin window = new MIDIOutputWin(shell,SWT.DIALOG_TRIM);
				
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
		mntmSetOutput.setText("Set output...");
		
		MenuItem mntmSynth = new MenuItem(menu_6, SWT.CASCADE);
		mntmSynth.setText("Synth");
		
		Menu menu_7 = new Menu(mntmSynth);
		mntmSynth.setMenu(menu_7);
		
		MenuItem mntmShowStatus_1 = new MenuItem(menu_7, SWT.NONE);
		mntmShowStatus_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw midi synth status");
			}
		});
		mntmShowStatus_1.setText("Show status");
		
		MenuItem mntmShow_3 = new MenuItem(menu_7, SWT.CASCADE);
		mntmShow_3.setText("Show");
		
		Menu menu_8 = new Menu(mntmShow_3);
		mntmShow_3.setMenu(menu_8);
		
		MenuItem mntmChannels = new MenuItem(menu_8, SWT.NONE);
		mntmChannels.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw midi synth show channels");
			}
		});
		mntmChannels.setText("Channels");
		
		MenuItem mntmInstruments = new MenuItem(menu_8, SWT.NONE);
		mntmInstruments.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw midi synth show instr");
			}
		});
		mntmInstruments.setText("Instruments");
		
		MenuItem mntmProfiles = new MenuItem(menu_8, SWT.NONE);
		mntmProfiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw midi synth show profiles");
			}
		});
		mntmProfiles.setText("Profiles");
		
		MenuItem mntmLoadSoundbank = new MenuItem(menu_7, SWT.NONE);
		mntmLoadSoundbank.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				 FileDialog fd = new FileDialog(shell, SWT.OPEN);
			        fd.setText("Choose a soundbank file...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	sendCommand("dw midi synth bank " + selected);
			        }
				
			
			}
		});
		mntmLoadSoundbank.setText("Load soundbank...");
		
		MenuItem mntmSetProfile = new MenuItem(menu_7, SWT.NONE);
		mntmSetProfile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				SynthProfileWin window = new SynthProfileWin(shell,SWT.DIALOG_TRIM);
				
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
		mntmSetProfile.setText("Set profile...");
		
		MenuItem mntmLockInstruments = new MenuItem(menu_7, SWT.NONE);
		mntmLockInstruments.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw midi synth lock");
			}
		});
		mntmLockInstruments.setText("Lock instruments");
		
		MenuItem mntmConfig = new MenuItem(menu, SWT.CASCADE);
		mntmConfig.setText("Config");
		
		Menu menu_5 = new Menu(mntmConfig);
		mntmConfig.setMenu(menu_5);
		
		MenuItem mntmInitialConfig = new MenuItem(menu_5, SWT.NONE);
		mntmInitialConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				InitialConfigWin window = new InitialConfigWin(shell,SWT.DIALOG_TRIM);
				window.open();
			}
		});
		mntmInitialConfig.setText("Simple Config...");
		
		new MenuItem(menu_5, SWT.SEPARATOR);
		
		MenuItem mntmServer_1 = new MenuItem(menu_5, SWT.NONE);
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
		mntmUserInterface.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				UIConfigWin window = new UIConfigWin(shell,SWT.DIALOG_TRIM);
				window.open();
			
			}
		});
		mntmUserInterface.setText("User Interface...");
		
		new MenuItem(menu_5, SWT.SEPARATOR);
		
		MenuItem mntmResetInstanceDevice = new MenuItem(menu_5, SWT.NONE);
		mntmResetInstanceDevice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("ui instance reset protodev");
				
			}
		});
		mntmResetInstanceDevice.setText("Reset Instance Device");
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		Menu menu_3 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_3);
		
		MenuItem mntmDocumentation = new MenuItem(menu_3, SWT.NONE);
		mntmDocumentation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// open wiki in browser
				org.eclipse.swt.program.Program.launch("http://sourceforge.net/apps/mediawiki/drivewireserver/index.php");
			}
		});
		mntmDocumentation.setText("Documentation");
		
		new MenuItem(menu_3, SWT.SEPARATOR);
		
		MenuItem mntmAbout = new MenuItem(menu_3, SWT.NONE);
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
		
		text = new Text(shell, SWT.BORDER);
		text.setLayoutData(BorderLayout.SOUTH);
		
		
		
		ToolBar toolBar = new ToolBar(shell, SWT.RIGHT);
		toolBar.setLayoutData(BorderLayout.NORTH);
		
		ToolItem tltmShowDisks = new ToolItem(toolBar, SWT.NONE);
		tltmShowDisks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				
				try 
				{
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
			}
		});
		tltmShowDisks.setImage(SWTResourceManager.getImage(MainWin.class, "/com/sun/java/swing/plaf/windows/icons/DetailsView.gif"));
		tltmShowDisks.setText("Refresh Disks");
		
		ToolItem tltmLoadDiskSet = new ToolItem(toolBar, SWT.NONE);
		tltmLoadDiskSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendDiskSetCmd("dw disk set load ","");
			}
		});
		tltmLoadDiskSet.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/hardDrive.gif"));
		tltmLoadDiskSet.setText("Load Diskset");
		
		ToolItem tltmSaveDiskSet = new ToolItem(toolBar, SWT.NONE);
		tltmSaveDiskSet.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/hardDrive.gif"));
		tltmSaveDiskSet.setText("Save Diskset");
		
		ToolItem tltmX = new ToolItem(toolBar, SWT.NONE);
		tltmX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					quickInDisk(0);
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
		tltmX.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmX.setText("Insert X0");
		
		ToolItem tltmInsertX = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					quickInDisk(1);
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
		tltmInsertX.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmInsertX.setText("Insert X1");
		
		ToolItem tltmInsertX_1 = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					quickInDisk(2);
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
		tltmInsertX_1.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmInsertX_1.setText("Insert X2");
		
		ToolItem tltmInsertX_2 = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					quickInDisk(3);
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
		tltmInsertX_2.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmInsertX_2.setText("Insert X3");
		
		ToolItem tltmShowServer = new ToolItem(toolBar, SWT.NONE);
		tltmShowServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw server status");
			}
		});
		tltmShowServer.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/computer.gif"));
		tltmShowServer.setText("Server Status");
		
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
		tblclmnUri.setWidth(202);
		tblclmnUri.setText("URI");
		
		composite = new Composite(sashForm_1, SWT.BORDER);
		composite.setLayout(null);
		
		textDiskURI = new Combo(composite, SWT.BORDER);
		textDiskURI.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) 
			{
				toggleApplyButton();
			}
		});
		textDiskURI.setBounds(10, 33, 446, 23);
		
		btnDiskWriteProtect = new Button(composite, SWT.CHECK);
		btnDiskWriteProtect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) 
			{
				toggleApplyButton();
			}
		});
		btnDiskWriteProtect.setBounds(10, 71, 145, 16);
		btnDiskWriteProtect.setText("Write protect");
		
		btnDiskSync = new Button(composite, SWT.CHECK);
		btnDiskSync.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) 
			{
				toggleApplyButton();
			}
		});
		btnDiskSync.setBounds(10, 93, 145, 16);
		btnDiskSync.setText("Sync changes to source");
		
		btnDiskExpand = new Button(composite, SWT.CHECK);
		btnDiskExpand.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) 
			{
				toggleApplyButton();
			}
		});
		btnDiskExpand.setBounds(10, 115, 144, 16);
		btnDiskExpand.setText("Allow expansion");
		
		textDiskSizeLimit = new Text(composite, SWT.BORDER);
		textDiskSizeLimit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) 
			{
				toggleApplyButton();
			}
		});
		textDiskSizeLimit.setBounds(76, 144, 76, 21);
		
		lblSizeLimit = new Label(composite, SWT.NONE);
		lblSizeLimit.setAlignment(SWT.RIGHT);
		lblSizeLimit.setBounds(10, 147, 60, 15);
		lblSizeLimit.setText("Size limit:");
		
		lblSectors = new Label(composite, SWT.NONE);
		lblSectors.setBounds(158, 147, 55, 15);
		lblSectors.setText("sectors");
		
		lblOffset = new Label(composite, SWT.NONE);
		lblOffset.setText("Offset:");
		lblOffset.setAlignment(SWT.RIGHT);
		lblOffset.setBounds(10, 174, 60, 15);
		
		textDiskOffset = new Text(composite, SWT.BORDER);
		textDiskOffset.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) 
			{
				toggleApplyButton();
			}
		});
		textDiskOffset.setBounds(76, 171, 76, 21);
		
		lblSectors1 = new Label(composite, SWT.NONE);
		lblSectors1.setText("sectors");
		lblSectors1.setBounds(158, 174, 55, 15);
		
		lblDiskUri = new Label(composite, SWT.NONE);
		lblDiskUri.setBounds(10, 12, 393, 15);
		lblDiskUri.setText("Disk X URI:");
		
		btnApply = new Button(composite, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applyDiskSettings();
				
			}
		});
		btnApply.setBounds(409, 211, 75, 25);
		btnApply.setText("Apply");
		
		buttonFile = new Button(composite, SWT.NONE);
		buttonFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Choose a local disk image...");
		        fd.setFilterPath("");
		        String[] filterExt = { "*.dsk", "*.*" };
		        fd.setFilterExtensions(filterExt);
		        String selected = fd.open();
			        
		        if (selected != null)
		        {
		        	textDiskURI.setText(selected);
		        }
			}
		});
		buttonFile.setBounds(457, 32, 27, 24);
		buttonFile.setText("...");
		
		btnEject = new Button(composite, SWT.NONE);
		btnEject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw disk eject " + MainWin.currentDisk.getDrive());
				loadSelectedDiskDetails();
				displayCurrentDisk(); 
				
			}
		});
		btnEject.setBounds(10, 211, 60, 25);
		btnEject.setText("Eject");
		
		btnWrite = new Button(composite, SWT.NONE);
		btnWrite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskFile(MainWin.currentDisk.getDrive(), "dw disk write ","Write");
			}
		});
		btnWrite.setBounds(76, 211, 75, 25);
		btnWrite.setText("Write to...");
		
		btnReload = new Button(composite, SWT.NONE);
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
		btnReload.setBounds(158, 211, 60, 25);
		
		lblSectors2 = new Label(composite, SWT.RIGHT);
		lblSectors2.setBounds(179, 71, 81, 15);
		lblSectors2.setText("Total sectors:");
		
		lblDirtySectors = new Label(composite, SWT.RIGHT);
		lblDirtySectors.setText("Dirty sectors:");
		lblDirtySectors.setBounds(310, 115, 75, 15);
		
		lblCurrentLsn = new Label(composite, SWT.RIGHT);
		lblCurrentLsn.setText("Current LSN:");
		lblCurrentLsn.setBounds(179, 93, 81, 15);
		
		lblReads = new Label(composite, SWT.RIGHT);
		lblReads.setText("Reads:");
		lblReads.setBounds(310, 71, 75, 15);
		
		lblWrites = new Label(composite, SWT.RIGHT);
		lblWrites.setText("Writes:");
		lblWrites.setBounds(310, 93, 75, 15);
		
		btnFilesystemIsWriteable = new Label(composite, SWT.CHECK);
		btnFilesystemIsWriteable.setEnabled(false);
		btnFilesystemIsWriteable.setBounds(317, 145, 152, 16);
		btnFilesystemIsWriteable.setText("Filesystem is writeable");
		
		btnFileIsWriteable = new Label(composite, SWT.CHECK);
		btnFileIsWriteable.setEnabled(false);
		btnFileIsWriteable.setText("File is writeable");
		btnFileIsWriteable.setBounds(317, 161, 152, 16);
		
		btnFileIsRandom = new Label(composite, SWT.CHECK);
		btnFileIsRandom.setEnabled(false);
		btnFileIsRandom.setText("File is random writeable");
		btnFileIsRandom.setBounds(317, 176, 152, 16);
		
		labelDiskSectors = new Label(composite, SWT.NONE);
		labelDiskSectors.setBounds(266, 71, 60, 15);
		
		labelDiskLSN = new Label(composite, SWT.NONE);
		labelDiskLSN.setBounds(266, 93, 60, 15);
		
		labelDiskReads = new Label(composite, SWT.NONE);
		labelDiskReads.setBounds(391, 71, 60, 15);
		
		labelDiskWrites = new Label(composite, SWT.NONE);
		labelDiskWrites.setBounds(391, 93, 59, 15);
		
		labelDiskDirty = new Label(composite, SWT.NONE);
		labelDiskDirty.setBounds(391, 115, 60, 15);
		
		buttonRefresh = new Button(composite, SWT.NONE);
		buttonRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if ((MainWin.currentDisk != null) && (MainWin.currentDisk.getDrive() > -1))
				{
					loadSelectedDiskDetails();
					displayCurrentDisk();
				}
			}
		});
		buttonRefresh.setImage(SWTResourceManager.getImage(MainWin.class, "/icons/progress/ani/6.png"));
		buttonRefresh.setBounds(376, 211, 27, 25);
		
		sashForm_1.setWeights(new int[] {295, 579});
		
		textDWOutput = new Text(sashForm, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		textDWOutput.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		sashForm.setWeights(new int[] {247, 161});
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) 
			{
				if (e.character == 13)
				{
					
					MainWin.sendCommand(text.getText());
					
					text.setText("");
				}
			}
		});

	}



	protected void toggleApplyButton() 
	{
		MainWin.btnApply.setEnabled(false);
		
		try 
		{
		
			if (!MainWin.textDiskURI.getText().equals(MainWin.currentDisk.getPath()))
				MainWin.btnApply.setEnabled(true);
		
			if (Integer.parseInt(MainWin.textDiskOffset.getText()) != (MainWin.currentDisk.getOffset()))
				MainWin.btnApply.setEnabled(true);
		
			if (Integer.parseInt(MainWin.textDiskSizeLimit.getText()) != (MainWin.currentDisk.getSizelimit()))
				MainWin.btnApply.setEnabled(true);
		
		
			if (MainWin.btnDiskWriteProtect.getSelection() != MainWin.currentDisk.isWriteprotect())
				MainWin.btnApply.setEnabled(true);
		
			if (MainWin.btnDiskExpand.getSelection() != MainWin.currentDisk.isExpand())
				MainWin.btnApply.setEnabled(true);
		
			if (MainWin.btnDiskSync.getSelection() != MainWin.currentDisk.isSync())
				MainWin.btnApply.setEnabled(true);
		}
		catch (NumberFormatException e)
		{
			// do we care?
		}
		
	}

	protected void applyDiskSettings() 
	{
		
		if (!MainWin.textDiskURI.getText().equals(""))
		{
			if (!MainWin.textDiskURI.getText().equals(MainWin.currentDisk.getPath()))
			{
				// disk insert
				sendCommand("dw disk insert " + MainWin.currentDisk.getDrive() + " " + MainWin.textDiskURI.getText());
				MainWin.addDiskFileToHistory(MainWin.textDiskURI.getText());
			}
		}
		
		if (!MainWin.textDiskOffset.getText().equals(""))
		{
			if (UIUtils.validateNum(MainWin.textDiskOffset.getText(), 0))
			{
				int offset = Integer.parseInt(MainWin.textDiskOffset.getText());
				if (!(offset == MainWin.currentDisk.getOffset()))
				{
					sendCommand("dw disk offset " + MainWin.currentDisk.getDrive() + " " + offset);
				}
			}
			else
			{
				showError("Invalid Offset","Disk offset must be numeric and >= 0","Enter the offset for read and write operations in sectors.\r\nThe default is 0.");
				return;
			}
		}
		
		if (!MainWin.textDiskSizeLimit.getText().equals(""))
		{
			if (UIUtils.validateNum(MainWin.textDiskSizeLimit.getText(), -1))
			{
				int limit = Integer.parseInt(MainWin.textDiskSizeLimit.getText());
				if (!(limit == MainWin.currentDisk.getSizelimit()))
				{
					sendCommand("dw disk limit " + MainWin.currentDisk.getDrive() + " " + limit);
				}
			}
			else
			{
				showError("Invalid Size Limit","Disk size limit must be numeric and >= -1","Enter the size limit for this disk in sectors.\r\nTo disable the limit, enter -1.");
				return;
			}
		}
		
		
		if (MainWin.btnDiskWriteProtect.getSelection() != MainWin.currentDisk.isWriteprotect())
		{
			sendCommand("dw disk wp " + MainWin.currentDisk.getDrive() + " " + MainWin.btnDiskWriteProtect.getSelection());
		}
		
		if (MainWin.btnDiskSync.getSelection() != MainWin.currentDisk.isSync())
		{
			sendCommand("dw disk sync " + MainWin.currentDisk.getDrive() + " " + MainWin.btnDiskSync.getSelection());
		}

		if (MainWin.btnDiskExpand.getSelection() != MainWin.currentDisk.isExpand())
		{
			sendCommand("dw disk expand " + MainWin.currentDisk.getDrive() + " " + MainWin.btnDiskExpand.getSelection());
		}

		
		// update display
		loadSelectedDiskDetails();
		displayCurrentDisk();
	}



	public static void displayCurrentDisk() 
	{
		if (MainWin.currentDisk.getDrive() > -1)
		{
			MainWin.lblDiskUri.setText("Disk " + MainWin.currentDisk.getDrive() + " URI:");
			MainWin.textDiskURI.setEnabled(true);
			MainWin.buttonRefresh.setEnabled(true);
			MainWin.btnApply.setEnabled(true);
			MainWin.buttonFile.setEnabled(true);
		}
		else
		{
			MainWin.lblDiskUri.setText("Select a drive from the list on the left to display details");
			MainWin.textDiskURI.setEnabled(false);
			MainWin.buttonRefresh.setEnabled(false);
			MainWin.btnApply.setEnabled(false);
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
		
		MainWin.textDiskOffset.setText(MainWin.currentDisk.getOffset() + "");
		MainWin.textDiskSizeLimit.setText(MainWin.currentDisk.getSizelimit() + "");
		
		MainWin.btnDiskExpand.setSelection(MainWin.currentDisk.isExpand());
		MainWin.btnDiskSync.setSelection(MainWin.currentDisk.isSync());
		MainWin.btnDiskWriteProtect.setSelection(MainWin.currentDisk.isWriteprotect());
		
		MainWin.btnFilesystemIsWriteable.setEnabled(MainWin.currentDisk.isFswriteable());
		MainWin.btnFileIsWriteable.setEnabled(MainWin.currentDisk.isWriteable());
		MainWin.btnFileIsRandom.setEnabled(MainWin.currentDisk.isRandomwriteable());
		
		MainWin.labelDiskSectors.setText(MainWin.currentDisk.getSectors()+"");
		MainWin.labelDiskLSN.setText(MainWin.currentDisk.getLsn()+"");
		MainWin.labelDiskReads.setText(MainWin.currentDisk.getReads()+"");
		MainWin.labelDiskWrites.setText(MainWin.currentDisk.getWrites()+"");
		MainWin.labelDiskDirty.setText(MainWin.currentDisk.getDirty()+"");
		
		
		if (MainWin.currentDisk.isLoaded())
		{
			MainWin.textDiskOffset.setEnabled(true);
			MainWin.textDiskSizeLimit.setEnabled(true);
			
			MainWin.btnDiskExpand.setEnabled(true);
			MainWin.btnDiskSync.setEnabled(true);
			MainWin.btnDiskWriteProtect.setEnabled(true);
			
			MainWin.labelDiskSectors.setEnabled(true);
			MainWin.labelDiskLSN.setEnabled(true);
			MainWin.labelDiskReads.setEnabled(true);
			MainWin.labelDiskWrites.setEnabled(true);
			MainWin.labelDiskDirty.setEnabled(true);
			
			MainWin.btnEject.setEnabled(true);
			MainWin.btnWrite.setEnabled(true);
			MainWin.btnReload.setEnabled(true);
			
			MainWin.lblSizeLimit.setEnabled(true);
			MainWin.lblSectors.setEnabled(true);
			MainWin.lblOffset.setEnabled(true);
			MainWin.lblSectors1.setEnabled(true);
			MainWin.lblSectors2.setEnabled(true);
			MainWin.lblDirtySectors.setEnabled(true);
			MainWin.lblCurrentLsn.setEnabled(true);
			MainWin.lblReads.setEnabled(true);
			MainWin.lblWrites.setEnabled(true);
			
			
		}
		else
		{
			MainWin.textDiskOffset.setEnabled(false);
			MainWin.textDiskSizeLimit.setEnabled(false);
			
			MainWin.btnDiskExpand.setEnabled(false);
			MainWin.btnDiskSync.setEnabled(false);
			MainWin.btnDiskWriteProtect.setEnabled(false);
			
			MainWin.btnFilesystemIsWriteable.setEnabled(false);
			MainWin.btnFileIsWriteable.setEnabled(false);
			MainWin.btnFileIsRandom.setEnabled(false);
			
			MainWin.labelDiskSectors.setEnabled(false);
			MainWin.labelDiskLSN.setEnabled(false);
			MainWin.labelDiskReads.setEnabled(false);
			MainWin.labelDiskWrites.setEnabled(false);
			MainWin.labelDiskDirty.setEnabled(false);
			
			MainWin.btnEject.setEnabled(false);
			MainWin.btnWrite.setEnabled(false);
			MainWin.btnReload.setEnabled(false);
			
			MainWin.lblSizeLimit.setEnabled(false);
			MainWin.lblSectors.setEnabled(false);
			MainWin.lblOffset.setEnabled(false);
			MainWin.lblSectors1.setEnabled(false);
			MainWin.lblSectors2.setEnabled(false);
			MainWin.lblDirtySectors.setEnabled(false);
			MainWin.lblCurrentLsn.setEnabled(false);
			MainWin.lblReads.setEnabled(false);
			MainWin.lblWrites.setEnabled(false);
			
		}
		
		MainWin.btnApply.setEnabled(false);
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
				table.getItem(target).setText(1,MainWin.currentDisk.getPath());
				
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
	}

	public static void refreshDiskTable() throws IOException, DWUIOperationFailedException 
	{
		MainWin.table.removeAll();
		
		for (int i = 0;i<256;i++)
		{
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0,i+"");
		}
				
		ArrayList<String> disks = UIUtils.loadArrayList(MainWin.instance, "ui instance disk show");
		
		for (int i = 0;i<disks.size();i++)
		{
			String[] dp = disks.get(i).split(" ");
			
			if (dp.length > 1)
			{
				
				table.getItem(Integer.parseInt(dp[0])).setText(1,disks.get(i).substring(dp[0].length()+1));
			}
			
		}
	
		
	}

	protected void sendCmdDiskFile(int disk, String pre, String buttxt) 
	{
		ChooseDiskFileWin window = new ChooseDiskFileWin(shell,SWT.DIALOG_TRIM, disk, buttxt, pre);

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


	protected void sendDiskSetCmd(String pre, String post) 
	{
		ChooseDiskSetWin window = new ChooseDiskSetWin(shell,SWT.DIALOG_TRIM,pre,post);
		try 
		{
			window.open();
			
			refreshDiskTable();
			loadSelectedDiskDetails();
			displayCurrentDisk();
			
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

	
	protected void quickInDisk(int diskno) throws IOException, DWUIOperationFailedException 
	{
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Choose a local disk image...");
        fd.setFilterPath("");
        String[] filterExt = { "*.dsk", "*.*" };
        fd.setFilterExtensions(filterExt);
        String selected = fd.open();
	        
        if (selected != null)
        {
        	sendCommand("dw disk in "+ diskno + " " + selected);
        	addDiskFileToHistory(selected);
        	
        	refreshDiskTable();
			loadSelectedDiskDetails();
			displayCurrentDisk();
        	
        }
	}

	
	protected static void sendCommand(String cmd) 
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
			showError("Unknown host error while sending command", e.getMessage(), UIUtils.getStackTrace(e));
		} 
		catch (IOException e1) 
		{
			showError("IO error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
			
		} catch (DWUIOperationFailedException e2) 
		{
			showError("DW error sending command", e2.getMessage(), UIUtils.getStackTrace(e2));
		} 
		
		
		
		
	}



	public static void showError(final String title,final String summary,final String detail)
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


	public static void addToDisplay(final String txt) 
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  textDWOutput.append(txt + System.getProperty("line.separator"));

						  // text_1.setText(txt + "\r\n" + text_1.getText());
					  }
				  });

	}

	public static LogViewerWin getLogViewerWin() 
	{
		return logViewerWin;
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
		
		if (diskhist == null)
		{
			if (config.getInt("DiskHistorySize",default_DiskHistorySize) > 0)
			{
				config.addProperty("DiskHistory", filename);
			}
		}
		else if (!diskhist.contains(filename))
		{
			if (diskhist.size() >= config.getInt("DiskHistorySize",default_DiskHistorySize))
			{
				diskhist.remove(0);
			}
			
			diskhist.add(filename);
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
		if ((logViewerWin != null) && (!logViewerWin.shell.isDisposed()))
		{
			logViewerWin.setFont(newFont);
		}
		
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
}