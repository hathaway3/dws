package com.groupunix.drivewireui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;

public class InstanceConfigWin extends Dialog {

	
	
	protected Object result;
	protected static Shell shlInstanceConfiguration;
	private Text textName;
	private Text textTCPClientHost;
	private Text textTCPClientPort;
	private Text textTCPServerPort;
	private Text textListenAddress;
	private Text textTelnetBanner;
	private Text textTelnetNoPorts;
	private Text textTelnetPreAuth;
	private Text textTelnetBanned;
	private Text textTermPort;
	private Combo textRateOverride;
	private Text textMIDIsoundbank;
	private Combo textMIDIprofile;
	private Combo cmbDefaultDiskSet;
	private Combo comboDevType;
	private Combo textSerialPort;
	private Combo comboCocoModel;
	private Button btnStartAutomatically;
	private Button btnLogOpcodes;
	private Button btnLogProtocolDevice;
	private Button btnEvenOppoll;
	private Button btnLogVirtualDevice;
	private Button btnLogMidiDevice;
	private Button btnDrivewireMode;
	
	private static Composite compositeP1;
	private static Composite compositeP2;
	private static Composite compositeP3;
	private static Composite compositeP5;
	private static Group grpMidiOptions;
	private static Group grpTelnetOptions;
	private Button btnDetect;
	private static Group grpProtocol;
	private Button btnDetectTurbo;
	private static Group grpDisk;
	private Text textDiskMaxSectors;
	private Text textDiskSectorSize;
	private Text textDiskMaxDrives;
	private Text textNameObjectDir;
	private Label lblNamedObjDir;
	private Button btnPadPartialSectors;
	

	private HierarchicalConfiguration iconf;
	private static Group grpLogging;
	private Table tablePrinters;
	private Button btnApply;
	private Button btnEnableVirtualMidi;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public InstanceConfigWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 * @throws IOException 
	 * @throws DWUIOperationFailedException 
	 */
	public Object open() throws DWUIOperationFailedException, IOException {
		createContents();
		//applyFont();
		
		//UIUtils.getDWConfigSerial();
		
		this.iconf = MainWin.getInstanceConfig();
		
		loadSettings();
		updateToggledStuff();
		
		shlInstanceConfiguration.open();
		shlInstanceConfiguration.layout();
		Display display = getParent().getDisplay();
		while (!shlInstanceConfiguration.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private static void applyFont() 
	{
		FontData f = MainWin.getDialogFont();
		
		Control[] controls = shlInstanceConfiguration.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = compositeP1.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = compositeP2.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = compositeP3.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = compositeP5.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = grpMidiOptions.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = grpTelnetOptions.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = grpProtocol.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = grpDisk.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = grpLogging.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
	}
	
	
		


	
	private void loadCombo(String cmd, Combo combo) 
	{
		String prev = combo.getText();
		
		combo.removeAll();
		try {
			List<String> ports = UIUtils.loadList(cmd);
			
			for (int i = 0;i<ports.size();i++)
			{
				combo.add(ports.get(i));
			}
		} 
		catch (IOException e1) 
		{
			MainWin.showError("Error loading data", e1.getMessage(), UIUtils.getStackTrace(e1));
		}
		catch (DWUIOperationFailedException e1) 
		{
			
			MainWin.showError("Error loading data", e1.getMessage(), UIUtils.getStackTrace(e1));
		}
		
		combo.setText(prev);
	}

	
	
	private HashMap<String, String> getChangedValues() 
	{
		HashMap<String,String> res = new HashMap<String,String>();
		
		// connection page
		addIfChanged(res,"Name",this.textName.getText());
		addIfChanged(res,"DeviceType",this.comboDevType.getText());
		addIfChanged(res,"CocoModel",this.comboCocoModel.getText());
		addIfChanged(res,"SerialDevice",this.textSerialPort.getText());
		addIfChanged(res,"TCPDevicePort",this.textTCPServerPort.getText());
		addIfChanged(res,"TCPClientPort",this.textTCPClientPort.getText());
		addIfChanged(res,"TCPClientHost",this.textTCPClientHost.getText());
		addIfChanged(res,"AutoStart",UIUtils.bTos(this.btnStartAutomatically.getSelection()));
		
		// devices page

		addIfChanged(res,"MIDISynthDefaultSoundbank",this.textMIDIsoundbank.getText());
		addIfChanged(res,"MIDISynthDefaultProfile",this.textMIDIprofile.getText());
		
		// networking page
		addIfChanged(res,"ListenAddress",this.textListenAddress.getText());
		addIfChanged(res,"TermPort",this.textTermPort.getText());
		addIfChanged(res,"TelnetBannerFile",this.textTelnetBanner.getText());
		addIfChanged(res,"TelnetBannedFile",this.textTelnetBanned.getText());
		addIfChanged(res,"TelnetNoPortsBannerFile",this.textTelnetNoPorts.getText());
		addIfChanged(res,"TelnetPreAuthFile",this.textTelnetPreAuth.getText());
	
		// advanced page
		addIfChanged(res,"RateOverride",this.textRateOverride.getText());
		addIfChanged(res,"DefaultDiskSet",this.cmbDefaultDiskSet.getText());
		
		addIfChanged(res,"DW3Only",UIUtils.bTos(this.btnDrivewireMode.getSelection()));

		addIfChanged(res,"LogDeviceBytes",UIUtils.bTos(this.btnLogProtocolDevice.getSelection()));
		addIfChanged(res,"LogVPortBytes",UIUtils.bTos(this.btnLogVirtualDevice.getSelection()));
		addIfChanged(res,"LogMIDIBytes",UIUtils.bTos(this.btnLogMidiDevice.getSelection()));
		addIfChanged(res,"LogOpCode",UIUtils.bTos(this.btnLogOpcodes.getSelection()));
		addIfChanged(res,"LogOpCodePolls",UIUtils.bTos(this.btnEvenOppoll.getSelection()));
		
		addIfChanged(res,"DetectDATurbo",UIUtils.bTos(this.btnDetectTurbo.getSelection()));
		addIfChanged(res,"DiskMaxSectors",this.textDiskMaxSectors.getText());
		addIfChanged(res,"DiskSectorSize",this.textDiskSectorSize.getText());
		addIfChanged(res,"DiskMaxDrives",this.textDiskMaxDrives.getText());
		addIfChanged(res,"DiskPadPartialSectors",UIUtils.bTos(this.btnPadPartialSectors.getSelection()));
		addIfChanged(res,"NamedObjectDir",this.textNameObjectDir.getText());
		
		return(res);
	}

	private void addIfChanged(HashMap<String, String> map, String key, String value) 
	{
		if (!iconf.getString(key,"").equals(value))
		{ 
			map.put(key, value);
		}
	}

	private boolean validateValues() 
	{
		//TODO: more
		
		if (this.comboDevType.getText().equals("tcpclient"))
		{
			if (!UIUtils.validateNum(this.textTCPClientPort.getText(),1,65535))
			{
				MainWin.showError("Invalid value entered", "Data entered for TCP client port is not valid" , "Valid range is TCP port numbers, 1-65535.");
				return false;
			}
		}
		
		if (this.comboDevType.getText().equals("tcp"))
		{
			if (!UIUtils.validateNum(this.textTCPServerPort.getText(),1,65535))
			{
				MainWin.showError("Invalid value entered", "Data entered for TCP server port is not valid" , "Valid range is TCP port numbers, 1-65535.");
				return false;
			}
		}
		
		return true;
	}



	private void loadSettings() 
	{
		
		// connection page
		
		setTextValue("Name",this.textName);
		
		setComboValue("DeviceType",this.comboDevType);	
		setComboValue("CocoModel",this.comboCocoModel);
		setComboValue("SerialDevice",this.textSerialPort);
		
		setTextValue("TCPDevicePort",this.textTCPServerPort);
		setTextValue("TCPClientPort", this.textTCPClientPort);
		setTextValue("TCPClientHost", this.textTCPClientHost);
		
		setBooleanValue("AutoStart", this.btnStartAutomatically, true);
		
		setTextValue("MIDISynthDefaultSoundbank", this.textMIDIsoundbank);
		
		loadCombo("ui server show synthprofiles",textMIDIprofile);
		setComboValue("MIDISynthDefaultProfile", this.textMIDIprofile);
		
		// networking page
		
		setTextValue("ListenAddress", this.textListenAddress);
		setTextValue("TermPort", this.textTermPort);
		setTextValue("TelnetBannerFile", this.textTelnetBanner);
		setTextValue("TelnetBannedFile", this.textTelnetBanned);
		setTextValue("TelnetNoPortsBannerFile", this.textTelnetNoPorts);
		setTextValue("TelnetPreAuthFile", this.textTelnetPreAuth);
		
		// advanced page
		
		setComboValue("RateOverride", this.textRateOverride);
		
		loadCombo("ui diskset show",cmbDefaultDiskSet);
		setComboValue("DefaultDiskSet", this.cmbDefaultDiskSet);
		
		setBooleanValue("DW3Only", this.btnDrivewireMode, false);
		setBooleanValue("LogDeviceBytes", this.btnLogProtocolDevice, false);
		setBooleanValue("LogVPortBytes", this.btnLogVirtualDevice, false);
		setBooleanValue("LogMIDIBytes", this.btnLogMidiDevice, true);
		setBooleanValue("LogOpCode", this.btnLogOpcodes, false);
		setBooleanValue("LogOpCodePolls", this.btnEvenOppoll, false);
		
		setBooleanValue("DetectDATurbo", this.btnDetectTurbo, false);
		setBooleanValue("DiskPadPartialSectors", this.btnPadPartialSectors, false);
		setTextValue("DiskMaxSectors", this.textDiskMaxSectors);
		setTextValue("DiskMaxDrives", this.textDiskMaxDrives);
		setTextValue("DiskSectorSize", this.textDiskSectorSize);
		setTextValue("NamedObjectDir", this.textNameObjectDir);
		
		// Printers
		loadPrinterTable();
		
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void loadPrinterTable() 
	{
		this.tablePrinters.setRedraw(false);
		this.tablePrinters.removeAll();
		
		for (HierarchicalConfiguration pconf : (List<HierarchicalConfiguration>) iconf.configurationsAt("Printer"))
		{
			TableItem ti = new TableItem(this.tablePrinters, SWT.CHECK);
			ti.setText(0, pconf.getString("Name","?noname?"));
			ti.setText(1, pconf.getString("Driver", "?noname?"));
			ti.setText(2, pconf.getString("OutputDir",""));
			if (pconf.containsKey("Name") && iconf.containsKey("CurrentPrinter"))
			{
				if (pconf.getString("Name").equals(iconf.getString("CurrentPrinter")))
				{
					ti.setChecked(true);
				}
			}
		}
		
		this.tablePrinters.setRedraw(true);
	}

	private void setBooleanValue(String key, Button btn, boolean def) 
	{
		btn.setSelection(iconf.getBoolean(key, def));
	}

	private void setComboValue(String key, Combo combo) 
	{
		if (iconf.containsKey(key))
		{
			if (combo.indexOf(iconf.getString(key)) > -1)
			{
				combo.select(combo.indexOf(iconf.getString(key)));
			}
			else
			{
				combo.setText(iconf.getString(key));
			}
		}
		else
		{
			combo.select(-1);
			combo.setText("");
		}
	}

	private void setTextValue(String key, Text textObj) 
	{
		textObj.setText(iconf.getString(key, ""));
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlInstanceConfiguration = new Shell(getParent(), getStyle());
		shlInstanceConfiguration.setSize(426, 474);
		shlInstanceConfiguration.setText("Instance Configuration");
		shlInstanceConfiguration.setLayout(null);
		
		TabFolder tabFolder = new TabFolder(shlInstanceConfiguration, SWT.NONE);
		tabFolder.setBounds(10, 9, 400, 396);
		
		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText("Connection");
		
		compositeP1 = new Composite(tabFolder, SWT.NONE);
		tbtmConnection.setControl(compositeP1);
		
		textName = new Text(compositeP1, SWT.BORDER);
		textName.setBounds(168, 30, 182, 21);
		
		Label lblInstanceName = new Label(compositeP1, SWT.NONE);
		lblInstanceName.setAlignment(SWT.RIGHT);
		lblInstanceName.setBounds(14, 33, 148, 18);
		lblInstanceName.setText("Instance name:");
		
		
		
		Label lblDeviceType = new Label(compositeP1, SWT.NONE);
		lblDeviceType.setAlignment(SWT.RIGHT);
		lblDeviceType.setBounds(24, 110, 138, 20);
		lblDeviceType.setText("Device type:");
		
		Label lblSerialPort = new Label(compositeP1, SWT.NONE);
		lblSerialPort.setAlignment(SWT.RIGHT);
		lblSerialPort.setBounds(24, 151, 138, 20);
		lblSerialPort.setText("Serial device:");
		
		textTCPClientHost = new Text(compositeP1, SWT.BORDER);
		textTCPClientHost.setBounds(168, 230, 182, 21);
		
		textTCPClientPort = new Text(compositeP1, SWT.BORDER);
		textTCPClientPort.setBounds(168, 257, 76, 21);
		
		Label lblTcpPort = new Label(compositeP1, SWT.NONE);
		lblTcpPort.setAlignment(SWT.RIGHT);
		lblTcpPort.setBounds(14, 233, 148, 18);
		lblTcpPort.setText("TCP client host:");
		
		Label lblTcpHost = new Label(compositeP1, SWT.NONE);
		lblTcpHost.setAlignment(SWT.RIGHT);
		lblTcpHost.setBounds(14, 260, 148, 18);
		lblTcpHost.setText("TCP client port:");
		
		comboCocoModel = new Combo(compositeP1, SWT.READ_ONLY);
		comboCocoModel.setItems(new String[] {"1", "2", "3"});
		comboCocoModel.setBounds(168, 67, 50, 23);
		comboCocoModel.select(2);
		
		Label lblCocoModel = new Label(compositeP1, SWT.NONE);
		lblCocoModel.setAlignment(SWT.RIGHT);
		lblCocoModel.setBounds(24, 70, 138, 20);
		lblCocoModel.setText("CoCo model:");
		
		textTCPServerPort = new Text(compositeP1, SWT.BORDER);
		textTCPServerPort.setBounds(168, 191, 76, 21);
		
		Label lblTcpServerPort = new Label(compositeP1, SWT.NONE);
		lblTcpServerPort.setAlignment(SWT.RIGHT);
		lblTcpServerPort.setBounds(16, 194, 146, 18);
		lblTcpServerPort.setText("TCP server port:");
		
		btnStartAutomatically = new Button(compositeP1, SWT.CHECK);
		btnStartAutomatically.setBounds(110, 301, 184, 16);
		btnStartAutomatically.setText("Start automatically");
		
		textSerialPort = new Combo(compositeP1, SWT.BORDER);
		textSerialPort.setBounds(168, 148, 117, 23);
		
		btnDetect = new Button(compositeP1, SWT.NONE);
		btnDetect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				
				loadCombo("ui server show serialdevs",textSerialPort);
			}
		});
		btnDetect.setBounds(290, 146, 60, 25);
		btnDetect.setText("Detect");
		
		TabItem tbtmDevices = new TabItem(tabFolder, SWT.NONE);
		tbtmDevices.setText("Devices");
		
		compositeP2 = new Composite(tabFolder, SWT.NONE);
		tbtmDevices.setControl(compositeP2);
		
		grpMidiOptions = new Group(compositeP2, SWT.NONE);
		grpMidiOptions.setText(" MIDI Options ");
		grpMidiOptions.setBounds(10, 206, 372, 141);
		
		Label lblDefaultSoundbank = new Label(grpMidiOptions, SWT.NONE);
		lblDefaultSoundbank.setAlignment(SWT.RIGHT);
		lblDefaultSoundbank.setBounds(10, 71, 136, 18);
		lblDefaultSoundbank.setText("Default soundbank:");
		
		textMIDIsoundbank = new Text(grpMidiOptions, SWT.BORDER);
		textMIDIsoundbank.setBounds(152, 68, 171, 21);
		
		Button button_8 = new Button(grpMidiOptions, SWT.NONE);
		button_8.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textMIDIsoundbank.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textMIDIsoundbank.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose soundbank file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textMIDIsoundbank.setText(selected.getPath());
			
				}
			}
		});
		button_8.setText("...");
		button_8.setBounds(324, 66, 26, 25);
		
		textMIDIprofile = new Combo(grpMidiOptions, SWT.BORDER);
		textMIDIprofile.setBounds(152, 97, 171, 23);
		
		Label lblDefaultProfile = new Label(grpMidiOptions, SWT.NONE);
		lblDefaultProfile.setAlignment(SWT.RIGHT);
		lblDefaultProfile.setBounds(10, 100, 136, 20);
		lblDefaultProfile.setText("Default profile:");
		
		btnEnableVirtualMidi = new Button(grpMidiOptions, SWT.CHECK);
		btnEnableVirtualMidi.setBounds(63, 36, 227, 16);
		btnEnableVirtualMidi.setText("Enable virtual MIDI");
		
		tablePrinters = new Table(compositeP2, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		tablePrinters.setBounds(10, 21, 372, 124);
		tablePrinters.setHeaderVisible(true);
		tablePrinters.setLinesVisible(true);
		
		TableColumn tblclmnPrinterName = new TableColumn(tablePrinters, SWT.NONE);
		tblclmnPrinterName.setWidth(97);
		tblclmnPrinterName.setText("Printer name");
		
		TableColumn tblclmnDriver = new TableColumn(tablePrinters, SWT.NONE);
		tblclmnDriver.setWidth(65);
		tblclmnDriver.setText("Driver");
		
		TableColumn tblclmnOutputTo = new TableColumn(tablePrinters, SWT.NONE);
		tblclmnOutputTo.setWidth(205);
		tblclmnOutputTo.setText("Output to..");
		
		Button btnNewButton = new Button(compositeP2, SWT.NONE);
		btnNewButton.setImage(SWTResourceManager.getImage(InstanceConfigWin.class, "/printers/kcontrol-3.png"));
		btnNewButton.setBounds(150, 151, 68, 25);
		btnNewButton.setText("Edit");
		
		Button btnNewButton_1 = new Button(compositeP2, SWT.NONE);
		btnNewButton_1.setImage(SWTResourceManager.getImage(InstanceConfigWin.class, "/printers/edit-add-2.png"));
		btnNewButton_1.setBounds(224, 151, 65, 25);
		btnNewButton_1.setText("Add");
		
		Button btnRemove = new Button(compositeP2, SWT.NONE);
		btnRemove.setImage(SWTResourceManager.getImage(InstanceConfigWin.class, "/printers/edit-delete-2.png"));
		btnRemove.setBounds(295, 151, 87, 25);
		btnRemove.setText("Remove");
		
		TabItem tbtmNetworking_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNetworking_1.setText("Networking");
		
		compositeP3 = new Composite(tabFolder, SWT.NONE);
		tbtmNetworking_1.setControl(compositeP3);
		
		textListenAddress = new Text(compositeP3, SWT.BORDER);
		textListenAddress.setBounds(209, 58, 156, 21);
		
		Label lblBindAddress = new Label(compositeP3, SWT.NONE);
		lblBindAddress.setAlignment(SWT.RIGHT);
		lblBindAddress.setBounds(20, 61, 183, 15);
		lblBindAddress.setText("Listen interface address:");
		
		grpTelnetOptions = new Group(compositeP3, SWT.NONE);
		grpTelnetOptions.setText(" Telnet server text files ");
		grpTelnetOptions.setBounds(10, 209, 372, 149);
		
		textTelnetBanner = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetBanner.setBounds(115, 29, 209, 21);
		
		Label lblBanner = new Label(grpTelnetOptions, SWT.NONE);
		lblBanner.setBounds(10, 32, 99, 18);
		lblBanner.setAlignment(SWT.RIGHT);
		lblBanner.setText("Banner:");
		
		Label lblNoPorts = new Label(grpTelnetOptions, SWT.NONE);
		lblNoPorts.setAlignment(SWT.RIGHT);
		lblNoPorts.setBounds(10, 59, 99, 22);
		lblNoPorts.setText("No ports:");
		
		Label lblPreauth = new Label(grpTelnetOptions, SWT.NONE);
		lblPreauth.setAlignment(SWT.RIGHT);
		lblPreauth.setBounds(10, 87, 99, 21);
		lblPreauth.setText("Pre-auth:");
		
		Label lblBanned = new Label(grpTelnetOptions, SWT.NONE);
		lblBanned.setAlignment(SWT.RIGHT);
		lblBanned.setBounds(10, 114, 99, 18);
		lblBanned.setText("Banned:");
		
		Button button_2 = new Button(grpTelnetOptions, SWT.NONE);
		button_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textTelnetBanner.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textTelnetBanner.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose telnet banner file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textTelnetBanner.setText(selected.getPath());
			
				}
			}
		});
		button_2.setBounds(324, 27, 28, 25);
		button_2.setText("...");
		
		textTelnetNoPorts = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetNoPorts.setBounds(115, 56, 209, 21);
		
		textTelnetPreAuth = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetPreAuth.setBounds(115, 84, 209, 21);
		
		textTelnetBanned = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetBanned.setBounds(115, 111, 209, 21);
		
		Button button_3 = new Button(grpTelnetOptions, SWT.NONE);
		button_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textTelnetNoPorts.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textTelnetNoPorts.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose telnet no ports file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textTelnetNoPorts.setText(selected.getPath());
			
				}
	
			}
		});
		button_3.setText("...");
		button_3.setBounds(324, 54, 28, 25);
		
		Button button_4 = new Button(grpTelnetOptions, SWT.NONE);
		button_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textTelnetPreAuth.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textTelnetPreAuth.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose telnet pre-auth file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textTelnetPreAuth.setText(selected.getPath());
			
				}
			}
		});
		button_4.setText("...");
		button_4.setBounds(324, 82, 28, 25);
		
		Button button_5 = new Button(grpTelnetOptions, SWT.NONE);
		button_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textTelnetBanned.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textTelnetBanned.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose telnet banned file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textTelnetBanned.setText(selected.getPath());
			
				}
			}
		});
		button_5.setText("...");
		button_5.setBounds(324, 109, 28, 25);
		
		textTermPort = new Text(compositeP3, SWT.BORDER);
		textTermPort.setBounds(214, 134, 57, 21);
		
		Label lblOsTermDevice = new Label(compositeP3, SWT.NONE);
		lblOsTermDevice.setAlignment(SWT.RIGHT);
		lblOsTermDevice.setBounds(25, 137, 183, 15);
		lblOsTermDevice.setText("OS9 TERM telnet port:");
		
		
		TabItem tbtmAdvanced_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvanced_1.setText("Advanced");
		
		compositeP5 = new Composite(tabFolder, SWT.NONE);
		tbtmAdvanced_1.setControl(compositeP5);
		
		grpLogging = new Group(compositeP5, SWT.NONE);
		grpLogging.setText("Logging");
		grpLogging.setBounds(10, 10, 372, 111);
		
		btnLogProtocolDevice = new Button(grpLogging, SWT.CHECK);
		btnLogProtocolDevice.setBounds(166, 25, 184, 21);
		btnLogProtocolDevice.setText("Log protocol device bytes");
		
		btnLogVirtualDevice = new Button(grpLogging, SWT.CHECK);
		btnLogVirtualDevice.setBounds(166, 52, 196, 21);
		btnLogVirtualDevice.setText("Log virtual port bytes");
		
		btnLogMidiDevice = new Button(grpLogging, SWT.CHECK);
		btnLogMidiDevice.setBounds(166, 79, 184, 21);
		btnLogMidiDevice.setText("Log MIDI device bytes");
		
		btnEvenOppoll = new Button(grpLogging, SWT.CHECK);
		btnEvenOppoll.setBounds(38, 52, 126, 20);
		btnEvenOppoll.setText("Even OP_POLL");
		
		btnLogOpcodes = new Button(grpLogging, SWT.CHECK);
		btnLogOpcodes.setBounds(10, 25, 126, 21);
		btnLogOpcodes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				updateToggledStuff();
			}
		});
		btnLogOpcodes.setText("Log opcodes");
		
		grpProtocol = new Group(compositeP5, SWT.NONE);
		grpProtocol.setText("Protocol");
		grpProtocol.setBounds(10, 127, 372, 94);
		
		textRateOverride = new Combo(grpProtocol, SWT.BORDER);
		textRateOverride.setBounds(165, 59, 97, 23);
		textRateOverride.setItems(new String[] {"38400", "57600", "115200", "230400", "460800", "921600"});
		
		Label lblBaudRateOverride = new Label(grpProtocol, SWT.NONE);
		lblBaudRateOverride.setBounds(43, 62, 116, 18);
		lblBaudRateOverride.setAlignment(SWT.RIGHT);
		lblBaudRateOverride.setText("Baud rate override:");
		
		btnDrivewireMode = new Button(grpProtocol, SWT.CHECK);
		btnDrivewireMode.setBounds(166, 24, 155, 21);
		btnDrivewireMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnDrivewireMode.setText("DriveWire 3 mode");
		
		btnDetectTurbo = new Button(grpProtocol, SWT.CHECK);
		btnDetectTurbo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnDetectTurbo.setBounds(12, 24, 148, 21);
		btnDetectTurbo.setText("Detect 230k mode");
		
		grpDisk = new Group(compositeP5, SWT.NONE);
		grpDisk.setText("Disk");
		grpDisk.setBounds(10, 227, 372, 134);
		
		cmbDefaultDiskSet = new Combo(grpDisk, SWT.BORDER);
		cmbDefaultDiskSet.setBounds(266, 21, 96, 21);
		
		Label lblDefaultDiskset = new Label(grpDisk, SWT.NONE);
		lblDefaultDiskset.setBounds(163, 24, 97, 18);
		lblDefaultDiskset.setAlignment(SWT.RIGHT);
		lblDefaultDiskset.setText("Default diskset:");
		
		textDiskMaxSectors = new Text(grpDisk, SWT.BORDER);
		textDiskMaxSectors.setBounds(97, 21, 60, 21);
		
		textDiskSectorSize = new Text(grpDisk, SWT.BORDER);
		textDiskSectorSize.setBounds(97, 48, 60, 21);
		
		textDiskMaxDrives = new Text(grpDisk, SWT.BORDER);
		textDiskMaxDrives.setBounds(97, 75, 60, 21);
		
		Label lblMaxSectors = new Label(grpDisk, SWT.NONE);
		lblMaxSectors.setBounds(10, 24, 81, 15);
		lblMaxSectors.setText("Max sectors:");
		
		Label lblSectorSize = new Label(grpDisk, SWT.NONE);
		lblSectorSize.setText("Sector size:");
		lblSectorSize.setBounds(10, 51, 81, 15);
		
		Label lblMaxDrives = new Label(grpDisk, SWT.NONE);
		lblMaxDrives.setText("Max drives:");
		lblMaxDrives.setBounds(10, 78, 81, 15);
		
		btnPadPartialSectors = new Button(grpDisk, SWT.CHECK);
		btnPadPartialSectors.setBounds(182, 50, 139, 16);
		btnPadPartialSectors.setText("Pad partial sectors");
		
		textNameObjectDir = new Text(grpDisk, SWT.BORDER);
		textNameObjectDir.setBounds(134, 105, 187, 21);
		
		lblNamedObjDir = new Label(grpDisk, SWT.NONE);
		lblNamedObjDir.setText("Named object path:");
		lblNamedObjDir.setBounds(10, 108, 118, 15);
		
		Button buttonChooseNODir = new Button(grpDisk, SWT.NONE);
		buttonChooseNODir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textNameObjectDir.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textNameObjectDir.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose named object dir...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textNameObjectDir.setText(selected.getPath());
			
				}
			}
		});
		buttonChooseNODir.setBounds(324, 105, 27, 21);
		buttonChooseNODir.setText("...");
		
		
		comboDevType = new Combo(compositeP1, SWT.READ_ONLY);
		comboDevType.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) 
			{
				if (comboDevType.getSelectionIndex() > -1)
				{
					enableDevOptions(comboDevType.getItem(comboDevType.getSelectionIndex()));
				}
				
			}


		});
		comboDevType.setItems(new String[] {"serial", "tcp", "tcpclient"});
		comboDevType.setBounds(168, 107, 91, 23);
		comboDevType.select(0);
	
		Button btnOk = new Button(shlInstanceConfiguration, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (validateValues())
				{
					
					try 
					{
						boolean letsreset = false;
						
						HashMap<String, String> changes = getChangedValues();
						
						if (hasDeviceChange(changes))
						{
							 MessageBox messageBox = new MessageBox(shlInstanceConfiguration, SWT.ICON_QUESTION
							            | SWT.YES | SWT.NO);
							        messageBox.setMessage("You've changed one or more settings which can not take effect until the device for this instance is reset.  Would you like to reset it now?");
							        messageBox.setText("Reset Instance Device?");
							        int response = messageBox.open();
							        if (response == SWT.YES)
							        {
							        	letsreset = true;
							        }
						}
						
						UIUtils.setInstanceSettings(MainWin.getInstance(),changes);
						
						if (letsreset)
							MainWin.sendCommand("ui instance reset protodev");
						
						e.display.getActiveShell().close();
					} 
					catch (IOException e1) 
					{
						MainWin.showError("Error sending updated config", e1.getMessage() , UIUtils.getStackTrace(e1));
						
					} catch (DWUIOperationFailedException e2) 
					{
						MainWin.showError("Error sending updated config", e2.getMessage() , UIUtils.getStackTrace(e2));
					}
					
				}
			}
		});
		btnOk.setBounds(173, 411, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlInstanceConfiguration, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setBounds(254, 411, 75, 25);
		btnCancel.setText("Cancel");
		
		btnApply = new Button(shlInstanceConfiguration, SWT.NONE);
		btnApply.setBounds(335, 411, 75, 25);
		btnApply.setText("Apply");

		
		
	}

	protected void updateToggledStuff() 
	{
		if (btnLogOpcodes.getSelection())
		{
			btnEvenOppoll.setEnabled(true);
		}
		else
		{
			btnEvenOppoll.setEnabled(false);
		}
		
		
	}

	protected void enableDevOptions(String item) 
	{
		this.btnDetect.setEnabled(false);
		this.textSerialPort.setEnabled(false);
		this.textTCPClientHost.setEnabled(false);
		this.textTCPClientPort.setEnabled(false);
		this.textTCPServerPort.setEnabled(false);
		
		if (item.equals("serial"))
		{
			this.textSerialPort.setEnabled(true);
			this.btnDetect.setEnabled(true);
		}
		else if (item.equals("tcp"))
		{
			this.textTCPServerPort.setEnabled(true);
		}
		else if (item.equals("tcpclient"))
		{
			this.textTCPClientPort.setEnabled(true);
			this.textTCPClientHost.setEnabled(true);
		}
		
	}

	protected boolean hasDeviceChange(HashMap<String, String> changes) 
	{
		if (changes.containsKey("SerialDevice"))
			return true;
		
		if (changes.containsKey("DeviceType"))
			return true;
		
		if (changes.containsKey("CocoModel"))
			return true;
		
		if (changes.containsKey("TCPDevicePort"))
			return true;
		
		if (changes.containsKey("TCPClientPort"))
			return true;
		
		if (changes.containsKey("TCPClientHost"))
			return true;
		
		if (changes.containsKey("RateOverride"))
			return true;
		
		
		
		return false;
	}
	protected Button getBtnDetect() {
		return btnDetect;
	}
	protected Button getBtnPadPartialSectors() {
		return btnPadPartialSectors;
	}

	protected Group getGrpLogging() {
		return grpLogging;
	}
	protected Group getGrpProtocol() {
		return grpProtocol;
	}
	protected Group getGrpDisk() {
		return grpDisk;
	}
	protected Table getTablePrinters() {
		return tablePrinters;
	}
}
