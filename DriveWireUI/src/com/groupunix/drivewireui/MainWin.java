package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import swing2swt.layout.BorderLayout;

import com.swtdesigner.SWTResourceManager;

public class MainWin {

	protected Shell shell;
	private static Connection connection;
	private Text text;
	private static Text text_1;
	private static Display display;
	private static LogViewerWin logViewerWin;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
		
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

	/**
	 * Open the window.
	 */
	public void open(Display display) {
		
		createContents();
		shell.open();
		shell.layout();
		
		ConnectWin window = new ConnectWin(shell,SWT.DIALOG_TRIM);
		window.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) 
			{
				if (connection != null)
				{
					connection.close();
				}
			
			}
		});
		shell.setSize(640, 514);
		shell.setText("DriveWire User Interface");
		shell.setLayout(new BorderLayout(0, 0));
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmConnectTo = new MenuItem(menu_1, SWT.NONE);
		mntmConnectTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				ConnectWin window = new ConnectWin(shell,SWT.DIALOG_TRIM);
				window.open();
			}
		});
		mntmConnectTo.setText("Connect to...");
		
		MenuItem mntmDisconnect = new MenuItem(menu_1, SWT.NONE);
		mntmDisconnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				connection.close();
			}
		});
		mntmDisconnect.setText("Disconnect");
		
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
		
		MenuItem mntmDisk = new MenuItem(menu, SWT.CASCADE);
		mntmDisk.setText("Disk");
		
		Menu menu_2 = new Menu(mntmDisk);
		mntmDisk.setMenu(menu_2);
		
		MenuItem mntmShow = new MenuItem(menu_2, SWT.CASCADE);
		mntmShow.setText("Show");
		
		Menu menu_11 = new Menu(mntmShow);
		mntmShow.setMenu(menu_11);
		
		MenuItem mntmAll = new MenuItem(menu_11, SWT.NONE);
		mntmAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw disk show");
			}
		});
		mntmAll.setText("Summary");
		
		MenuItem mntmDetail = new MenuItem(menu_11, SWT.NONE);
		mntmDetail.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskNo("dw disk show","");
			}
		});
		mntmDetail.setText("Detail...");
		
		MenuItem mntmInsert = new MenuItem(menu_2, SWT.NONE);
		mntmInsert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskFile("dw disk in ","Load", SWT.OPEN);
			}
		});
		mntmInsert.setText("Insert...");
		
		MenuItem mntmEject = new MenuItem(menu_2, SWT.NONE);
		mntmEject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskNo("dw disk eject","");
			}
		});
		mntmEject.setText("Eject...");
		
		MenuItem mntmReload = new MenuItem(menu_2, SWT.NONE);
		mntmReload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskNo("dw disk reload","");
			}
		});
		mntmReload.setText("Reload...");
		
		MenuItem mntmWriteProtect = new MenuItem(menu_2, SWT.NONE);
		mntmWriteProtect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskNo("dw disk wp","");
			}
		});
		mntmWriteProtect.setText("Write Protect...");
		
		MenuItem mntmWrite = new MenuItem(menu_2, SWT.NONE);
		mntmWrite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskFile("dw disk write ","Write", SWT.SAVE);
			}
		});
		mntmWrite.setText("Write...");
		
		MenuItem mntmCreate = new MenuItem(menu_2, SWT.NONE);
		mntmCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCmdDiskFile("dw disk create","Create", SWT.SAVE);
			}
		});
		mntmCreate.setText("Create...");
		
		new MenuItem(menu_2, SWT.SEPARATOR);
		
		MenuItem mntmDiskSet = new MenuItem(menu_2, SWT.CASCADE);
		mntmDiskSet.setText("Disk Set");
		
		Menu menu_12 = new Menu(mntmDiskSet);
		mntmDiskSet.setMenu(menu_12);
		
		MenuItem mntmShow_1 = new MenuItem(menu_12, SWT.CASCADE);
		mntmShow_1.setText("Show");
		
		Menu menu_13 = new Menu(mntmShow_1);
		mntmShow_1.setMenu(menu_13);
		
		MenuItem mntmAll_1 = new MenuItem(menu_13, SWT.NONE);
		mntmAll_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw disk set show");
			}
		});
		mntmAll_1.setText("All");
		
		MenuItem mntmDetail_1 = new MenuItem(menu_13, SWT.NONE);
		mntmDetail_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				
				sendDiskSetCmd("dw disk set show ","");
				
			}
		});
		mntmDetail_1.setText("Detail...");
		
		MenuItem mntmLoad = new MenuItem(menu_12, SWT.NONE);
		mntmLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sendDiskSetCmd("dw disk set load ","");
			}
		});
		mntmLoad.setText("Load...");
		
		MenuItem mntmServer = new MenuItem(menu, SWT.CASCADE);
		mntmServer.setText("Server");
		
		Menu menu_4 = new Menu(mntmServer);
		mntmServer.setMenu(menu_4);
		
		MenuItem mntmStatus = new MenuItem(menu_4, SWT.NONE);
		mntmStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw server status");
			}
		});
		mntmStatus.setText("Status");
		
		MenuItem mntmShow_2 = new MenuItem(menu_4, SWT.CASCADE);
		mntmShow_2.setText("Show");
		
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
		mntmLogViewer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if ((connection != null) && (connection.connected()))
				{
				
					if ((logViewerWin == null) || (logViewerWin.shell.isDisposed()))
					{
						logViewerWin = new LogViewerWin(shell,SWT.DIALOG_TRIM,connection.getHost(), connection.getPort());
						logViewerWin.open();
					}
					else
					{
						logViewerWin.shell.setFocus();
					}
			
				}
				else
				{
					addToDisplay("Cannot open log viewer until we are connected to a server.");
				}
				
			}
		});
		mntmLogViewer.setText("Log Viewer");
		
		MenuItem mntmConfig = new MenuItem(menu, SWT.CASCADE);
		mntmConfig.setText("Config");
		
		Menu menu_5 = new Menu(mntmConfig);
		mntmConfig.setMenu(menu_5);
		
		MenuItem mntmServer_1 = new MenuItem(menu_5, SWT.NONE);
		mntmServer_1.setText("Server...");
		
		MenuItem mntmInstanceConfig = new MenuItem(menu_5, SWT.NONE);
		mntmInstanceConfig.setText("Instance...");
		
		MenuItem mntmPrinterConfig = new MenuItem(menu_5, SWT.NONE);
		mntmPrinterConfig.setText("Printer...");
		
		MenuItem mntmMidi = new MenuItem(menu_5, SWT.CASCADE);
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
				window.open();
				
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
				window.open();
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
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		Menu menu_3 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_3);
		
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
		
		text_1 = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text_1.setLayoutData(BorderLayout.CENTER);
		text_1.setFont(SWTResourceManager.getFont("Lucida Console", 9, SWT.NORMAL));
		text_1.setEditable(false);
		
		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(BorderLayout.NORTH);
		
		ToolItem tltmShowDisks = new ToolItem(toolBar, SWT.NONE);
		tltmShowDisks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendCommand("dw disk show");
			}
		});
		tltmShowDisks.setImage(SWTResourceManager.getImage(MainWin.class, "/com/sun/java/swing/plaf/windows/icons/DetailsView.gif"));
		tltmShowDisks.setText("Show Disks");
		
		ToolItem tltmX = new ToolItem(toolBar, SWT.NONE);
		tltmX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(0);
			}
		});
		tltmX.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmX.setText("Insert X0");
		
		ToolItem tltmInsertX = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(1);
			}
		});
		tltmInsertX.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmInsertX.setText("Insert X1");
		
		ToolItem tltmInsertX_1 = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(2);
			}
		});
		tltmInsertX_1.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmInsertX_1.setText("Insert X2");
		
		ToolItem tltmInsertX_2 = new ToolItem(toolBar, SWT.NONE);
		tltmInsertX_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				quickInDisk(3);
			}
		});
		tltmInsertX_2.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/floppy.gif"));
		tltmInsertX_2.setText("Insert X3");
		
		ToolItem tltmLoadDiskSet = new ToolItem(toolBar, SWT.NONE);
		tltmLoadDiskSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				sendDiskSetCmd("dw disk set load ","");
			}
		});
		tltmLoadDiskSet.setImage(SWTResourceManager.getImage(MainWin.class, "/javax/swing/plaf/metal/icons/ocean/hardDrive.gif"));
		tltmLoadDiskSet.setText("Load Disk Set");
		
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



	protected void sendCmdDiskFile(String pre, String buttxt, int dialogType) 
	{
		if (connectionAlive())
		{
			ChooseDiskFileWin window = new ChooseDiskFileWin(shell,SWT.DIALOG_TRIM, buttxt, pre, dialogType);
			window.open();
		}
	}

	protected void sendCmdDiskNo(String pre, String post) 
	{
		if (connectionAlive())
		{
			ChooseDiskNoWin window = new ChooseDiskNoWin(shell,SWT.DIALOG_TRIM,pre,post);
			window.open();
		}
	}


	protected void sendDiskSetCmd(String pre, String post) 
	{
		if (connectionAlive())
		{
			ChooseDiskSetWin window = new ChooseDiskSetWin(shell,SWT.DIALOG_TRIM,pre,post);
			window.open();
		}
	}

	
	protected void quickInDisk(int diskno) 
	{
		if (connectionAlive())
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
	        }
		}   
	}

	protected static void sendCommand(String cmd) 
	{
		if (connectionAlive())
		{
			addToDisplay(System.getProperty("line.separator"));
			connection.sendCommand(cmd);
		
		}
		
	}



	private static boolean connectionAlive() 
	{
		if (connection != null)
		{
			if (connection.connected())
			{
				return true;
			}
			else
			{
				addToDisplay("Cannot send command, connection to DriveWire server lost.");
				return false;
			}
		}
		else
		{
			addToDisplay("Cannot send command, no connection to DriveWire server.");
			return false;
		}
	}

	public static void openConnection(String host, int port) 
	{
		if (connection != null)
		{
			connection.close();
		}
		
		connection = new Connection(host,port);
		connection.Connect();
		
		
	}

	public static void addToDisplay(final String txt) 
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  text_1.append(txt + System.getProperty("line.separator"));
						  // text_1.setText(txt + "\r\n" + text_1.getText());
					  }
				  });

	}

	public static LogViewerWin getLogViewerWin() 
	{
		return logViewerWin;
	}

	public static Connection getConnection() 
	{
		return connection;
	}
}
