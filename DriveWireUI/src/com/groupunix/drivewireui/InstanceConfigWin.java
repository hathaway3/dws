package com.groupunix.drivewireui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.eclipse.swt.widgets.Text;

public class InstanceConfigWin extends Dialog {

	
	
	protected Object result;
	protected static Shell shlInstanceConfiguration;
	private Text textName;
	private Text textTCPClientHost;
	private Text textTCPClientPort;
	private Text textTCPServerPort;
	private Text textPrinterDir;
	private Text textCharacterFile;
	private Text textPrinterCol;
	private Text textPrinterRow;
	private Text textListenAddress;
	private Text textTelnetBanner;
	private Text textTelnetNoPorts;
	private Text textTelnetPreAuth;
	private Text textTelnetBanned;
	private Text textTermPort;
	private Text textTelnetPasswd;
	private Text textIPBanned;
	private Text textGeoIPfile;
	private Text textIPBannedCities;
	private Text textIPBannedCountries;
	private Combo textRateOverride;
	private Text textMIDIsoundbank;
	private Combo textMIDIprofile;
	private Combo cmbDefaultDiskSet;
	private Combo comboDevType;
	private Combo textSerialPort;
	private Combo comboCocoModel;
	private Button btnStartAutomatically;
	private Combo comboPrinterType;
	private Button btnUseGeoipLookups;
	private Button btnLogOpcodes;
	private Button btnLogProtocolDevice;
	private Button btnEvenOppoll;
	private Button btnLogVirtualDevice;
	private Button btnLogMidiDevice;
	private Button btnDrivewireMode;
	
	private static Composite compositeP1;
	private static Composite compositeP2;
	private static Composite compositeP3;
	private static Composite compositeP4;
	private static Composite compositeP5;
	private static Group grpPrintingOptions;
	private static Group grpMidiOptions;
	private static Group grpTelnetOptions;
	private static Group grpTelnetAuthentication;
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
	private Button buttonGeoipDB;
	

	private HierarchicalConfiguration iconf;
	private static Group grpLogging;
	
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
		applyFont();
		
		UIUtils.getDWConfigSerial();
		
		this.iconf = MainWin.getInstanceConfig();
		
		applySettings();
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
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
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
		
		controls = compositeP4.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		controls = compositeP5.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlInstanceConfiguration.getDisplay(), f));
		}
		
		
		controls = grpPrintingOptions.getChildren();
		
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
		
		controls = grpTelnetAuthentication.getChildren();
		
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
			ArrayList<String> ports = UIUtils.loadArrayList(cmd);
			
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
		addIfChanged(res,"PrinterDir",this.textPrinterDir.getText());
		addIfChanged(res,"PrinterCharacterFile",this.textCharacterFile.getText());
		addIfChanged(res,"PrinterColumns",this.textPrinterCol.getText());
		addIfChanged(res,"PrinterLines",this.textPrinterRow.getText());
		addIfChanged(res,"PrinterType",this.comboPrinterType.getText());
		addIfChanged(res,"MIDISynthDefaultSoundbank",this.textMIDIsoundbank.getText());
		addIfChanged(res,"MIDISynthDefaultProfile",this.textMIDIprofile.getText());
		
		// networking page
		addIfChanged(res,"ListenAddress",this.textListenAddress.getText());
		addIfChanged(res,"TermPort",this.textTermPort.getText());
		addIfChanged(res,"TelnetBannerFile",this.textTelnetBanner.getText());
		addIfChanged(res,"TelnetBannedFile",this.textTelnetBanned.getText());
		addIfChanged(res,"TelnetNoPortsBannerFile",this.textTelnetNoPorts.getText());
		addIfChanged(res,"TelnetPreAuthFile",this.textTelnetPreAuth.getText());
		addIfChanged(res,"TelnetPasswdFile",this.textTelnetPasswd.getText());
		
		// ip access page
		addIfChanged(res,"TelnetBanned",this.textIPBanned.getText());
		addIfChanged(res,"GeoIPLookup",UIUtils.bTos(this.btnUseGeoipLookups.getSelection()));
		addIfChanged(res,"GeoIPDatabaseFile",this.textGeoIPfile.getText());
		addIfChanged(res,"GeoIPBannedCountries",this.textIPBannedCountries.getText());
		addIfChanged(res,"GeoIPBannedCities",this.textIPBannedCities.getText());
		
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
		
		if (!UIUtils.validateNum(this.textTCPClientPort.getText(),1,65535))
		{
			MainWin.showError("Invalid value entered", "Data entered for TCP client port is not valid" , "Valid range is TCP port numbers, 1-65535.");
			return false;
		}
		
		if (!UIUtils.validateNum(this.textTCPServerPort.getText(),1,65535))
		{
			MainWin.showError("Invalid value entered", "Data entered for TCP server port is not valid" , "Valid range is TCP port numbers, 1-65535.");
			return false;
		}
		
		return true;
	}



	private void applySettings() 
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
		
		// devices page
		
		setTextValue("PrinterDir", this.textPrinterDir);
		setTextValue("PrinterCharacterFile", this.textCharacterFile);
		setTextValue("PrinterColumns", this.textPrinterCol);
		setTextValue("PrinterLines", this.textPrinterRow);
		
		setComboValue("PrinterType",this.comboPrinterType);
		
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
		setTextValue("TelnetPasswdFile", this.textTelnetPasswd);
		
		// ip access page
		
		setTextValue("TelnetBanned", this.textIPBanned);
		
		setBooleanValue("GeoIPLookups", this.btnUseGeoipLookups, false);
		
		setTextValue("GeoIPDatabaseFile", this.textGeoIPfile);
		setTextValue("GeoIPBannedCountries", this.textIPBannedCountries);
		setTextValue("GeoIPBannedCities", this.textIPBannedCities);
		
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
		
		grpPrintingOptions = new Group(compositeP2, SWT.NONE);
		grpPrintingOptions.setText(" Printing Options ");
		grpPrintingOptions.setBounds(10, 21, 372, 207);
		
		Label lblOutputTo = new Label(grpPrintingOptions, SWT.NONE);
		lblOutputTo.setBounds(10, 35, 111, 21);
		lblOutputTo.setAlignment(SWT.RIGHT);
		lblOutputTo.setText("Output to:");
		
		textPrinterDir = new Text(grpPrintingOptions, SWT.BORDER);
		textPrinterDir.setBounds(127, 32, 197, 21);
		
		Button button = new Button(grpPrintingOptions, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textPrinterDir.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textPrinterDir.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose printer output directory...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textPrinterDir.setText(selected.getPath());
			
				}
			}
		});
		button.setBounds(324, 31, 26, 25);
		button.setText("...");
		
		comboPrinterType = new Combo(grpPrintingOptions, SWT.READ_ONLY);
		comboPrinterType.setItems(new String[] {"FX80", "TEXT"});
		comboPrinterType.setBounds(127, 62, 102, 23);
		comboPrinterType.select(0);
		
		Label lblPrinter = new Label(grpPrintingOptions, SWT.NONE);
		lblPrinter.setBounds(10, 66, 111, 21);
		lblPrinter.setAlignment(SWT.RIGHT);
		lblPrinter.setText("Printer type:");
		
		textCharacterFile = new Text(grpPrintingOptions, SWT.BORDER);
		textCharacterFile.setBounds(127, 97, 197, 21);
		
		Button button_1 = new Button(grpPrintingOptions, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textCharacterFile.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textCharacterFile.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose character definition file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textCharacterFile.setText(selected.getPath());
			
				}
			}
		});
		button_1.setBounds(324, 96, 26, 25);
		button_1.setText("...");
		
		Label lblCharacters = new Label(grpPrintingOptions, SWT.NONE);
		lblCharacters.setBounds(10, 100, 111, 18);
		lblCharacters.setAlignment(SWT.RIGHT);
		lblCharacters.setText("Characters:");
		
		textPrinterCol = new Text(grpPrintingOptions, SWT.BORDER);
		textPrinterCol.setBounds(127, 135, 76, 21);
		
		textPrinterRow = new Text(grpPrintingOptions, SWT.BORDER);
		textPrinterRow.setBounds(127, 162, 76, 21);
		
		Label lblColumns = new Label(grpPrintingOptions, SWT.NONE);
		lblColumns.setBounds(10, 138, 111, 21);
		lblColumns.setAlignment(SWT.RIGHT);
		lblColumns.setText("Columns:");
		
		Label lblLines = new Label(grpPrintingOptions, SWT.NONE);
		lblLines.setBounds(10, 165, 111, 18);
		lblLines.setAlignment(SWT.RIGHT);
		lblLines.setText("Lines:");
		
		grpMidiOptions = new Group(compositeP2, SWT.NONE);
		grpMidiOptions.setText(" MIDI Options ");
		grpMidiOptions.setBounds(10, 244, 372, 103);
		
		Label lblDefaultSoundbank = new Label(grpMidiOptions, SWT.NONE);
		lblDefaultSoundbank.setAlignment(SWT.RIGHT);
		lblDefaultSoundbank.setBounds(10, 37, 136, 18);
		lblDefaultSoundbank.setText("Default soundbank:");
		
		textMIDIsoundbank = new Text(grpMidiOptions, SWT.BORDER);
		textMIDIsoundbank.setBounds(152, 34, 171, 21);
		
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
		button_8.setBounds(324, 32, 26, 25);
		
		textMIDIprofile = new Combo(grpMidiOptions, SWT.BORDER);
		textMIDIprofile.setBounds(152, 61, 166, 23);
		
		Label lblDefaultProfile = new Label(grpMidiOptions, SWT.NONE);
		lblDefaultProfile.setAlignment(SWT.RIGHT);
		lblDefaultProfile.setBounds(10, 64, 136, 20);
		lblDefaultProfile.setText("Default profile:");
		
		TabItem tbtmNetworking_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNetworking_1.setText("Networking");
		
		compositeP3 = new Composite(tabFolder, SWT.NONE);
		tbtmNetworking_1.setControl(compositeP3);
		
		textListenAddress = new Text(compositeP3, SWT.BORDER);
		textListenAddress.setBounds(212, 19, 156, 21);
		
		Label lblBindAddress = new Label(compositeP3, SWT.NONE);
		lblBindAddress.setAlignment(SWT.RIGHT);
		lblBindAddress.setBounds(23, 22, 183, 15);
		lblBindAddress.setText("Listen interface address:");
		
		grpTelnetOptions = new Group(compositeP3, SWT.NONE);
		grpTelnetOptions.setText(" Telnet server text files ");
		grpTelnetOptions.setBounds(10, 92, 372, 149);
		
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
		textTermPort.setBounds(212, 49, 57, 21);
		
		Label lblOsTermDevice = new Label(compositeP3, SWT.NONE);
		lblOsTermDevice.setAlignment(SWT.RIGHT);
		lblOsTermDevice.setBounds(23, 52, 183, 15);
		lblOsTermDevice.setText("OS9 TERM telnet port:");
		
		grpTelnetAuthentication = new Group(compositeP3, SWT.NONE);
		grpTelnetAuthentication.setText(" Telnet authentication ");
		grpTelnetAuthentication.setBounds(10, 265, 372, 74);
		
		textTelnetPasswd = new Text(grpTelnetAuthentication, SWT.BORDER);
		textTelnetPasswd.setBounds(115, 33, 209, 21);
		
		Button button_6 = new Button(grpTelnetAuthentication, SWT.NONE);
		button_6.setText("...");
		button_6.setBounds(324, 31, 28, 25);
		button_6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textTelnetPasswd.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textTelnetPasswd.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose telnet passwd file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textTelnetPasswd.setText(selected.getPath());
			
				}
			}
		});
		
		
		Label lblPasswdFile = new Label(grpTelnetAuthentication, SWT.NONE);
		lblPasswdFile.setAlignment(SWT.RIGHT);
		lblPasswdFile.setBounds(10, 36, 99, 18);
		lblPasswdFile.setText("Passwd file:");
		
		TabItem tbtmAdvanced = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvanced.setText("IP Access");
		
		compositeP4 = new Composite(tabFolder, SWT.NONE);
		tbtmAdvanced.setControl(compositeP4);
		
		textIPBanned = new Text(compositeP4, SWT.BORDER | SWT.V_SCROLL);
		textIPBanned.setBounds(10, 44, 372, 72);
		
		Label lblBannedIpAddresses = new Label(compositeP4, SWT.NONE);
		lblBannedIpAddresses.setBounds(10, 23, 247, 21);
		lblBannedIpAddresses.setText("Banned IP Addresses:");
		

		
		textGeoIPfile = new Text(compositeP4, SWT.BORDER);
		textGeoIPfile.setBounds(135, 162, 218, 21);
		
		Label lblDatabaseFile = new Label(compositeP4, SWT.NONE);
		lblDatabaseFile.setAlignment(SWT.RIGHT);
		lblDatabaseFile.setBounds(10, 165, 119, 18);
		lblDatabaseFile.setText("Database file:");
		
		buttonGeoipDB = new Button(compositeP4, SWT.NONE);
		buttonGeoipDB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textGeoIPfile.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
				fileChooser.setSelectedFile(textGeoIPfile.getText());
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose GeoIP db file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textGeoIPfile.setText(selected.getPath());
			
				}
			}
		});
		buttonGeoipDB.setBounds(355, 160, 27, 25);
		buttonGeoipDB.setText("...");
		
		textIPBannedCities = new Text(compositeP4, SWT.BORDER | SWT.V_SCROLL);
		textIPBannedCities.setBounds(10, 220, 372, 46);
		
		textIPBannedCountries = new Text(compositeP4, SWT.BORDER | SWT.V_SCROLL);
		textIPBannedCountries.setBounds(10, 301, 372, 46);
		
		Label lblBannedCities = new Label(compositeP4, SWT.NONE);
		lblBannedCities.setBounds(10, 199, 197, 21);
		lblBannedCities.setText("Banned Cities:");
		
		Label lblBannedCountries = new Label(compositeP4, SWT.NONE);
		lblBannedCountries.setBounds(10, 280, 197, 21);
		lblBannedCountries.setText("Banned Countries:");
		
		
		btnUseGeoipLookups = new Button(compositeP4, SWT.CHECK);
		btnUseGeoipLookups.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				updateToggledStuff();
			}
		});
		btnUseGeoipLookups.setBounds(10, 132, 230, 24);
		btnUseGeoipLookups.setText("Use GeoIP lookups");
		
		
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
		
		
		
		Button btnUndo = new Button(shlInstanceConfiguration, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applySettings();
			}
		});
		btnUndo.setBounds(10, 411, 75, 25);
		btnUndo.setText("Undo");
	
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
		btnOk.setBounds(177, 411, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlInstanceConfiguration, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setBounds(335, 411, 75, 25);
		btnCancel.setText("Cancel");

		
		
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
		
		if (btnUseGeoipLookups.getSelection())
		{
			textGeoIPfile.setEnabled(true);
			buttonGeoipDB.setEnabled(true);
			textIPBannedCities.setEnabled(true);
			textIPBannedCountries.setEnabled(true);
			
		}
		else
		{
			textGeoIPfile.setEnabled(false);
			buttonGeoipDB.setEnabled(false);
			textIPBannedCities.setEnabled(false);
			textIPBannedCountries.setEnabled(false);
			
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
	protected Button getButtonGeoipDB() {
		return buttonGeoipDB;
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
}
