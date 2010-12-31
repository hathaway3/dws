package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class InstanceConfigWin extends Dialog {

	private HashMap<String,String> values = new HashMap<String,String>();

	
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
	private Text textRateOverride;
	private Text textMIDIsoundbank;
	private Combo textMIDIprofile;
	private Text textDefaultDiskSet;
	private Combo comboDevType;
	private Combo textSerialPort;
	private Combo comboCocoModel;
	private Button btnStartAutomatically;
	private Combo comboPrinterType;
	private Button btnUseGeoipLookups;
	
	
	private Button btnOptimeSendsDow;
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
		loadSettings();
		applySettings();
		
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
		
		
	}
	
	private void loadSettings() throws DWUIOperationFailedException, IOException 
	{
		ArrayList<String> settings = new ArrayList<String>();
		
		settings.add("Name");
		settings.add("DeviceType");
		settings.add("SerialDevice");
		settings.add("CocoModel");
		settings.add("AutoStart");
		settings.add("DefaultDiskSet");
		settings.add("TCPDevicePort");
		settings.add("TCPClientPort");
		settings.add("TCPClientHost");
		settings.add("TermPort");
		settings.add("ListenAddress");
		settings.add("TelnetBannerFile");
		settings.add("TelnetBannedFile");
		settings.add("TelnetNoPortsBannerFile");
		settings.add("TelnetPreAuthFile");
		settings.add("TelnetPasswdFile");
		settings.add("GeoIPLookup");
		settings.add("GeoIPDatabaseFile");
		settings.add("TelnetBanned");
		settings.add("GeoIPBannedCities");
		settings.add("GeoIPBannedCountries");
		settings.add("PrinterDir");
		settings.add("PrinterType");
		settings.add("PrinterCharacterFile");
		settings.add("PrinterColumns");
		settings.add("PrinterLines");
		settings.add("MIDISynthDefaultSoundbank");
		settings.add("MIDISynthDefaultProfile");
		settings.add("DW3Only");
		settings.add("LogDeviceBytes");
		settings.add("LogVPortBytes");
		settings.add("LogMIDIBytes");
		settings.add("LogOpCode");
		settings.add("LogOpCodePolls");
		settings.add("RateOverride");
		settings.add("OpTimeSendsDOW");
		
		values = UIUtils.getInstanceSettings(MainWin.getInstance(),settings);
		
		// combos
		//loadCombo("ui server show serialdevs",this.textSerialPort);
		loadCombo("ui server show synthprofiles",this.textMIDIprofile);
		
		
		
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
		addIfChanged(res,"DefaultDiskSet",this.textDefaultDiskSet.getText());
		
		addIfChanged(res,"DW3Only",UIUtils.bTos(this.btnDrivewireMode.getSelection()));
		addIfChanged(res,"OpTimeSendsDOW",UIUtils.bTos(this.btnOptimeSendsDow.getSelection()));
		addIfChanged(res,"LogDeviceBytes",UIUtils.bTos(this.btnLogProtocolDevice.getSelection()));
		addIfChanged(res,"LogVPortBytes",UIUtils.bTos(this.btnLogVirtualDevice.getSelection()));
		addIfChanged(res,"LogMIDIBytes",UIUtils.bTos(this.btnLogMidiDevice.getSelection()));
		addIfChanged(res,"LogOpCode",UIUtils.bTos(this.btnLogOpcodes.getSelection()));
		addIfChanged(res,"LogOpCodePolls",UIUtils.bTos(this.btnEvenOppoll.getSelection()));
		
		return(res);
	}

	private void addIfChanged(HashMap<String, String> map, String key, String value) 
	{
		if (values.get(key) == null)
		{
			if (!value.equals(""))
				map.put(key, value);
		}
		else if (!values.get(key).equals(value))
		{
			map.put(key, value);
		}
	}

	private boolean validateValues() 
	{
	/*	if (!UIUtils.validateNum(this.textLazyWrite.getText(),0))
		{
			MainWin.showError("Invalid value entered", "Data entered for DiskLazyWriteInterval is not valid" , "Valid range is positive integers");
			return false;
		}
		
		
		if (!UIUtils.validateNum(this.textUIPort.getText(),1,65535))
		{
			MainWin.showError("Invalid value entered", "Data entered for UI Port is not valid" , "Valid range is TCP port numbers, 1-65535.");
			return false;
		}
		
		*/
		
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
		
		setTextValue("RateOverride", this.textRateOverride);
		setTextValue("DefaultDiskSet", this.textDefaultDiskSet);
		
		setBooleanValue("DW3Only", this.btnDrivewireMode, false);
		setBooleanValue("OpTimeSendsDOW", this.btnOptimeSendsDow, false);
		setBooleanValue("LogDeviceBytes", this.btnLogProtocolDevice, false);
		setBooleanValue("LogVPortBytes", this.btnLogVirtualDevice, false);
		setBooleanValue("LogMIDIBytes", this.btnLogMidiDevice, true);
		setBooleanValue("LogOpCode", this.btnLogOpcodes, false);
		setBooleanValue("LogOpCodePolls", this.btnEvenOppoll, false);
		
		
	}
	
	
	
	private void setBooleanValue(String key, Button btn, boolean def) 
	{
		
		if (values.get(key) != null)
			btn.setSelection(UIUtils.sTob(values.get(key)));
		else
			btn.setSelection(def);
	
	}

	private void setComboValue(String key, Combo combo) 
	{
		if (values.get(key) != null)
		{
			if (combo.indexOf(values.get(key)) > -1)
			{
				combo.select(combo.indexOf(values.get(key)));
			}
			else
			{
				combo.setText(values.get(key));
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
		if (values.get(key) != null)
			textObj.setText(values.get(key));
		else
			textObj.setText("");	
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlInstanceConfiguration = new Shell(getParent(), getStyle());
		shlInstanceConfiguration.setSize(426, 459);
		shlInstanceConfiguration.setText("Instance Configuration");
		shlInstanceConfiguration.setLayout(null);
		
		TabFolder tabFolder = new TabFolder(shlInstanceConfiguration, SWT.NONE);
		tabFolder.setBounds(10, 9, 400, 385);
		
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
		
		comboDevType = new Combo(compositeP1, SWT.READ_ONLY);
		comboDevType.setItems(new String[] {"serial", "tcp", "tcpclient"});
		comboDevType.setBounds(168, 107, 91, 23);
		comboDevType.select(0);
		
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
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose printer output directory...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textPrinterDir.setText(selected);
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
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose character definition file...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textCharacterFile.setText(selected);
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
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose MIDI SoundBank...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textMIDIsoundbank.setText(selected);
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
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose telnet banner file...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textTelnetBanner.setText(selected);
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
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose telnet no ports banner...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textTelnetNoPorts.setText(selected);
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
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose telnet pre-auth banner...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textTelnetPreAuth.setText(selected);
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
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose telnet banned banner...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textTelnetBanned.setText(selected);
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
		
		btnUseGeoipLookups = new Button(compositeP4, SWT.CHECK);
		btnUseGeoipLookups.setBounds(10, 132, 230, 24);
		btnUseGeoipLookups.setText("Use GeoIP lookups");
		
		textGeoIPfile = new Text(compositeP4, SWT.BORDER);
		textGeoIPfile.setBounds(135, 162, 218, 21);
		
		Label lblDatabaseFile = new Label(compositeP4, SWT.NONE);
		lblDatabaseFile.setAlignment(SWT.RIGHT);
		lblDatabaseFile.setBounds(10, 165, 119, 18);
		lblDatabaseFile.setText("Database file:");
		
		Button button_7 = new Button(compositeP4, SWT.NONE);
		button_7.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				 FileDialog fd = new FileDialog(shlInstanceConfiguration, SWT.OPEN);
			        fd.setText("Choose GeoIP database file...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textGeoIPfile.setText(selected);
			        }
			}
		});
		button_7.setBounds(355, 160, 27, 25);
		button_7.setText("...");
		
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
		
		TabItem tbtmAdvanced_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvanced_1.setText("Advanced");
		
		compositeP5 = new Composite(tabFolder, SWT.NONE);
		tbtmAdvanced_1.setControl(compositeP5);
		
		btnDrivewireMode = new Button(compositeP5, SWT.CHECK);
		btnDrivewireMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnDrivewireMode.setBounds(21, 101, 224, 21);
		btnDrivewireMode.setText("DriveWire 3 mode");
		
		textRateOverride = new Text(compositeP5, SWT.BORDER);
		textRateOverride.setBounds(148, 20, 97, 21);
		
		Label lblBaudRateOverride = new Label(compositeP5, SWT.NONE);
		lblBaudRateOverride.setAlignment(SWT.RIGHT);
		lblBaudRateOverride.setBounds(10, 23, 132, 18);
		lblBaudRateOverride.setText("Baud rate override:");
		
		btnOptimeSendsDow = new Button(compositeP5, SWT.CHECK);
		btnOptimeSendsDow.setBounds(21, 289, 224, 21);
		btnOptimeSendsDow.setText("OP_TIME sends DOW");
		
		btnLogOpcodes = new Button(compositeP5, SWT.CHECK);
		btnLogOpcodes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnLogOpcodes.setBounds(21, 141, 186, 21);
		btnLogOpcodes.setText("Log opcodes");
		
		btnLogProtocolDevice = new Button(compositeP5, SWT.CHECK);
		btnLogProtocolDevice.setBounds(21, 194, 224, 21);
		btnLogProtocolDevice.setText("Log protocol device bytes");
		
		btnEvenOppoll = new Button(compositeP5, SWT.CHECK);
		btnEvenOppoll.setBounds(41, 168, 160, 20);
		btnEvenOppoll.setText("Even OP_POLL");
		
		btnLogVirtualDevice = new Button(compositeP5, SWT.CHECK);
		btnLogVirtualDevice.setBounds(21, 221, 186, 21);
		btnLogVirtualDevice.setText("Log virtual port bytes");
		
		btnLogMidiDevice = new Button(compositeP5, SWT.CHECK);
		btnLogMidiDevice.setBounds(21, 248, 205, 21);
		btnLogMidiDevice.setText("Log MIDI device bytes");
		
		textDefaultDiskSet = new Text(compositeP5, SWT.BORDER);
		textDefaultDiskSet.setBounds(148, 54, 97, 21);
		
		Label lblDefaultDiskset = new Label(compositeP5, SWT.NONE);
		lblDefaultDiskset.setAlignment(SWT.RIGHT);
		lblDefaultDiskset.setBounds(10, 57, 132, 18);
		lblDefaultDiskset.setText("Default diskset:");
		
		Button btnUndo = new Button(shlInstanceConfiguration, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applySettings();
			}
		});
		btnUndo.setBounds(10, 400, 75, 25);
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
						
						shlInstanceConfiguration.close();
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
		btnOk.setBounds(177, 400, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlInstanceConfiguration, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shlInstanceConfiguration.close();
			}
		});
		btnCancel.setBounds(335, 400, 75, 25);
		btnCancel.setText("Cancel");

		
		
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
		
		
		
		return false;
	}
	protected Button getBtnDetect() {
		return btnDetect;
	}
}
