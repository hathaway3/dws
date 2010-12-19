package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class InstanceConfigWin extends Dialog {

	private HashMap<String,String> values = new HashMap<String,String>();

	
	protected Object result;
	protected Shell shlInstanceConfiguration;
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
	private Text textMIDIprofile;
	private Text textDefaultDiskSet;
	private Combo comboDevType;
	private Text textSerialPort;
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
	private Button btnHdbdosMode;
	
	
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
		
		loadSettings();
		
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
		settings.add("HDBDOSMode");
		settings.add("DW3Only");
		settings.add("LogDeviceBytes");
		settings.add("LogVPortBytes");
		settings.add("LogMIDIBytes");
		settings.add("LogOpCode");
		settings.add("LogOpCodePolls");
		settings.add("RateOverride");
		settings.add("OpTimeSendsDOW");
		
		values = UIUtils.getInstanceSettings(MainWin.getInstance(),settings);
		
		applySettings();
		
	}

	
	private HashMap<String, String> getChangedValues() 
	{
		HashMap<String,String> res = new HashMap<String,String>();
		
/*		addIfChanged(res,"LogToConsole",UIUtils.bTos(this.btnLogToConsole.getSelection()));
		addIfChanged(res,"LogToFile",UIUtils.bTos(this.btnLogToFile.getSelection()));
		addIfChanged(res,"UIEnabled",UIUtils.bTos(this.btnUIEnabled.getSelection()));
		addIfChanged(res,"LogFile",this.textLogFile.getText());
		addIfChanged(res,"LogFormat",this.textLogFormat.getText());
		addIfChanged(res,"UIPort",this.textUIPort.getText());
		addIfChanged(res,"DiskLazyWriteInterval",this.textLazyWrite.getText());
		addIfChanged(res,"LocalDiskDir",this.textLocalDiskDir.getText());
		addIfChanged(res,"LogLevel",this.comboLogLevel.getItem(this.comboLogLevel.getSelectionIndex()));
	*/
		return(res);
	}

	private void addIfChanged(HashMap<String, String> map, String key, String value) 
	{
		if (!values.get(key).equals(value))
			map.put(key, value);
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
		
		if (values.get("Name") != null)
			this.textName.setText(values.get("Name"));
		
		if (values.get("DeviceType") != null)
			this.comboDevType.select(this.comboDevType.indexOf(values.get("DeviceType")));
		
		if (values.get("CocoModel") != null)
			this.comboCocoModel.select(this.comboCocoModel.indexOf(values.get("CocoModel")));
		
		if (values.get("SerialDevice") != null)
			this.textSerialPort.setText(values.get("SerialDevice"));
		
		if (values.get("TCPDevicePort") != null)
			this.textTCPServerPort.setText(values.get("TCPDevicePort"));
		
		if (values.get("TCPClientPort") != null)
			this.textTCPClientPort.setText(values.get("TCPClientPort"));
		
		if (values.get("TCPClientHost") != null)
			this.textTCPClientHost.setText(values.get("TCPClientHost"));
		
		if (values.get("AutoStart") == null)
		{
			this.btnStartAutomatically.setSelection(true);
		}
		else
		{
			this.btnStartAutomatically.setSelection(UIUtils.sTob(values.get("AutoStart")));
		}
		

		// devices page
		
		if (values.get("PrinterDir") != null)
			this.textPrinterDir.setText(values.get("PrinterDir"));
		
		if (values.get("PrinterCharacterFile") != null)
			this.textCharacterFile.setText(values.get("PrinterCharacterFile"));
		
		if (values.get("PrinterColumns") != null)
			this.textPrinterCol.setText(values.get("PrinterColumns"));
		
		if (values.get("PrinterLines") != null)
			this.textPrinterRow.setText(values.get("PrinterLines"));
		
		if (values.get("PrinterType") != null)
			this.comboPrinterType.select(this.comboPrinterType.indexOf(values.get("PrinterType")));
		
		if (values.get("MIDISynthDefaultSoundbank") != null)
			this.textMIDIsoundbank.setText(values.get("MIDISynthDefaultSoundbank"));
		
		if (values.get("MIDISynthDefaultProfile") != null)
			this.textMIDIprofile.setText(values.get("MIDISynthDefaultProfile"));
		
		
		// networking page
		
		if (values.get("ListenAddress") != null)
			this.textListenAddress.setText(values.get("ListenAddress"));
		
		if (values.get("TermPort") != null)
			this.textTermPort.setText(values.get("TermPort"));
		
		if (values.get("TelnetBannerFile") != null)
			this.textTelnetBanner.setText(values.get("TelnetBannerFile"));
		
		if (values.get("TelnetBannedFile") != null)
			this.textTelnetBanned.setText(values.get("TelnetBannedFile"));
		
		if (values.get("TelnetNoPortsBannerFile") != null)
			this.textTelnetNoPorts.setText(values.get("TelnetNoPortsBannerFile"));
		
		if (values.get("TelnetPreAuthFile") != null)
			this.textTelnetPreAuth.setText(values.get("TelnetPreAuthFile"));
		
		if (values.get("TelnetPasswdFile") != null)
			this.textTelnetPasswd.setText(values.get("TelnetPasswdFile"));
		
		
		// ip access page
		
		if (values.get("TelnetBanned") != null)
			this.textIPBanned.setText(values.get("TelnetBanned"));
		
		if (values.get("GeoIPLookup") != null)
			this.btnUseGeoipLookups.setSelection(UIUtils.sTob(values.get("GeoIPLookup")));
		
		if (values.get("GeoIPDatabaseFile") != null)
			this.textGeoIPfile.setText(values.get("GeoIPDatabaseFile"));
		
		if (values.get("GeoIPBannedCountries") != null)
			this.textIPBannedCountries.setText(values.get("GeoIPBannedCountries"));
		
		if (values.get("GeoIPBannedCities") != null)
			this.textIPBannedCities.setText(values.get("GeoIPBannedCities"));
		
		
		// advanced page
		
		if (values.get("RateOverride") != null)
			this.textRateOverride.setText(values.get("RateOverride"));
		
		if (values.get("DefaultDiskSet") != null)
			this.textDefaultDiskSet.setText(values.get("DefaultDiskSet"));
		
		
		if (values.get("HDBDOSMode") != null)
			this.btnHdbdosMode.setSelection(UIUtils.sTob(values.get("HDBDOSMode")));
		
		if (values.get("DW3Only") != null)
			this.btnDrivewireMode.setSelection(UIUtils.sTob(values.get("DW3Only")));
		
		if (values.get("OpTimeSendsDOW") != null)
			this.btnOptimeSendsDow.setSelection(UIUtils.sTob(values.get("OpTimeSendsDOW")));
		
		if (values.get("LogDeviceBytes") != null)
			this.btnLogProtocolDevice.setSelection(UIUtils.sTob(values.get("LogDeviceBytes")));
		
		if (values.get("LogVPortBytes") != null)
			this.btnLogVirtualDevice.setSelection(UIUtils.sTob(values.get("LogVPortBytes")));
		
		if (values.get("LogMIDIBytes") != null)
			this.btnLogMidiDevice.setSelection(UIUtils.sTob(values.get("LogMIDIBytes")));
		
		if (values.get("LogOpCode") != null)
			this.btnLogOpcodes.setSelection(UIUtils.sTob(values.get("LogOpCode")));
		
		if (values.get("LogOpCodePolls") != null)
			this.btnEvenOppoll.setSelection(UIUtils.sTob(values.get("LogOpCodePolls")));
		
		
		
	}
	
	
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlInstanceConfiguration = new Shell(getParent(), getStyle());
		shlInstanceConfiguration.setSize(377, 459);
		shlInstanceConfiguration.setText("Instance Configuration");
		shlInstanceConfiguration.setLayout(null);
		
		TabFolder tabFolder = new TabFolder(shlInstanceConfiguration, SWT.NONE);
		tabFolder.setBounds(10, 9, 353, 385);
		
		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText("Connection");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmConnection.setControl(composite);
		
		textName = new Text(composite, SWT.BORDER);
		textName.setBounds(110, 27, 182, 21);
		
		Label lblInstanceName = new Label(composite, SWT.NONE);
		lblInstanceName.setAlignment(SWT.RIGHT);
		lblInstanceName.setBounds(14, 30, 90, 15);
		lblInstanceName.setText("Instance name:");
		
		comboDevType = new Combo(composite, SWT.READ_ONLY);
		comboDevType.setItems(new String[] {"serial", "tcp", "tcpclient"});
		comboDevType.setBounds(110, 104, 91, 23);
		comboDevType.select(0);
		
		Label lblDeviceType = new Label(composite, SWT.NONE);
		lblDeviceType.setAlignment(SWT.RIGHT);
		lblDeviceType.setBounds(24, 107, 80, 15);
		lblDeviceType.setText("Device type:");
		
		Label lblSerialPort = new Label(composite, SWT.NONE);
		lblSerialPort.setAlignment(SWT.RIGHT);
		lblSerialPort.setBounds(24, 148, 80, 15);
		lblSerialPort.setText("Serial device:");
		
		textTCPClientHost = new Text(composite, SWT.BORDER);
		textTCPClientHost.setBounds(110, 227, 182, 21);
		
		textTCPClientPort = new Text(composite, SWT.BORDER);
		textTCPClientPort.setBounds(110, 254, 76, 21);
		
		Label lblTcpPort = new Label(composite, SWT.NONE);
		lblTcpPort.setAlignment(SWT.RIGHT);
		lblTcpPort.setBounds(14, 230, 90, 15);
		lblTcpPort.setText("TCP client host:");
		
		Label lblTcpHost = new Label(composite, SWT.NONE);
		lblTcpHost.setAlignment(SWT.RIGHT);
		lblTcpHost.setBounds(14, 257, 90, 15);
		lblTcpHost.setText("TCP client port:");
		
		comboCocoModel = new Combo(composite, SWT.READ_ONLY);
		comboCocoModel.setItems(new String[] {"1", "2", "3"});
		comboCocoModel.setBounds(110, 64, 50, 23);
		comboCocoModel.select(2);
		
		Label lblCocoModel = new Label(composite, SWT.NONE);
		lblCocoModel.setAlignment(SWT.RIGHT);
		lblCocoModel.setBounds(24, 67, 80, 15);
		lblCocoModel.setText("CoCo model:");
		
		textTCPServerPort = new Text(composite, SWT.BORDER);
		textTCPServerPort.setBounds(110, 188, 76, 21);
		
		Label lblTcpServerPort = new Label(composite, SWT.NONE);
		lblTcpServerPort.setAlignment(SWT.RIGHT);
		lblTcpServerPort.setBounds(16, 191, 88, 15);
		lblTcpServerPort.setText("TCP server port:");
		
		btnStartAutomatically = new Button(composite, SWT.CHECK);
		btnStartAutomatically.setBounds(110, 301, 184, 16);
		btnStartAutomatically.setText("Start automatically");
		
		textSerialPort = new Text(composite, SWT.BORDER);
		textSerialPort.setBounds(110, 145, 182, 21);
		
		TabItem tbtmDevices = new TabItem(tabFolder, SWT.NONE);
		tbtmDevices.setText("Devices");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmDevices.setControl(composite_1);
		
		Group grpPrintingOptions = new Group(composite_1, SWT.NONE);
		grpPrintingOptions.setText(" Printing Options ");
		grpPrintingOptions.setBounds(10, 21, 314, 207);
		
		Label lblOutputTo = new Label(grpPrintingOptions, SWT.NONE);
		lblOutputTo.setBounds(20, 38, 55, 15);
		lblOutputTo.setAlignment(SWT.RIGHT);
		lblOutputTo.setText("Output to:");
		
		textPrinterDir = new Text(grpPrintingOptions, SWT.BORDER);
		textPrinterDir.setBounds(81, 35, 191, 21);
		
		Button button = new Button(grpPrintingOptions, SWT.NONE);
		button.setBounds(273, 33, 26, 25);
		button.setText("...");
		
		comboPrinterType = new Combo(grpPrintingOptions, SWT.READ_ONLY);
		comboPrinterType.setItems(new String[] {"FX80", "TEXT"});
		comboPrinterType.setBounds(81, 62, 102, 23);
		comboPrinterType.select(0);
		
		Label lblPrinter = new Label(grpPrintingOptions, SWT.NONE);
		lblPrinter.setBounds(8, 65, 67, 15);
		lblPrinter.setAlignment(SWT.RIGHT);
		lblPrinter.setText("Printer type:");
		
		textCharacterFile = new Text(grpPrintingOptions, SWT.BORDER);
		textCharacterFile.setBounds(81, 100, 191, 21);
		
		Button button_1 = new Button(grpPrintingOptions, SWT.NONE);
		button_1.setBounds(273, 98, 26, 25);
		button_1.setText("...");
		
		Label lblCharacters = new Label(grpPrintingOptions, SWT.NONE);
		lblCharacters.setBounds(8, 103, 67, 15);
		lblCharacters.setAlignment(SWT.RIGHT);
		lblCharacters.setText("Characters:");
		
		textPrinterCol = new Text(grpPrintingOptions, SWT.BORDER);
		textPrinterCol.setBounds(81, 138, 76, 21);
		
		textPrinterRow = new Text(grpPrintingOptions, SWT.BORDER);
		textPrinterRow.setBounds(81, 165, 76, 21);
		
		Label lblColumns = new Label(grpPrintingOptions, SWT.NONE);
		lblColumns.setBounds(10, 141, 65, 15);
		lblColumns.setAlignment(SWT.RIGHT);
		lblColumns.setText("Columns:");
		
		Label lblLines = new Label(grpPrintingOptions, SWT.NONE);
		lblLines.setBounds(20, 168, 55, 15);
		lblLines.setAlignment(SWT.RIGHT);
		lblLines.setText("Lines:");
		
		Group grpMidiOptions = new Group(composite_1, SWT.NONE);
		grpMidiOptions.setText(" MIDI Options ");
		grpMidiOptions.setBounds(10, 244, 314, 103);
		
		Label lblDefaultSoundbank = new Label(grpMidiOptions, SWT.NONE);
		lblDefaultSoundbank.setAlignment(SWT.RIGHT);
		lblDefaultSoundbank.setBounds(10, 37, 110, 15);
		lblDefaultSoundbank.setText("Default soundbank:");
		
		textMIDIsoundbank = new Text(grpMidiOptions, SWT.BORDER);
		textMIDIsoundbank.setBounds(126, 34, 149, 21);
		
		Button button_8 = new Button(grpMidiOptions, SWT.NONE);
		button_8.setText("...");
		button_8.setBounds(276, 32, 26, 25);
		
		textMIDIprofile = new Text(grpMidiOptions, SWT.BORDER);
		textMIDIprofile.setBounds(126, 61, 76, 21);
		
		Label lblDefaultProfile = new Label(grpMidiOptions, SWT.NONE);
		lblDefaultProfile.setAlignment(SWT.RIGHT);
		lblDefaultProfile.setBounds(10, 64, 110, 15);
		lblDefaultProfile.setText("Default profile:");
		
		TabItem tbtmNetworking_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNetworking_1.setText("Networking");
		
		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmNetworking_1.setControl(composite_2);
		
		textListenAddress = new Text(composite_2, SWT.BORDER);
		textListenAddress.setBounds(154, 19, 156, 21);
		
		Label lblBindAddress = new Label(composite_2, SWT.NONE);
		lblBindAddress.setBounds(23, 22, 133, 15);
		lblBindAddress.setText("Listen interface address:");
		
		Group grpTelnetOptions = new Group(composite_2, SWT.NONE);
		grpTelnetOptions.setText(" Telnet server text files ");
		grpTelnetOptions.setBounds(10, 92, 320, 149);
		
		textTelnetBanner = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetBanner.setBounds(71, 26, 200, 21);
		
		Label lblBanner = new Label(grpTelnetOptions, SWT.NONE);
		lblBanner.setBounds(10, 29, 55, 15);
		lblBanner.setAlignment(SWT.RIGHT);
		lblBanner.setText("Banner:");
		
		Label lblNoPorts = new Label(grpTelnetOptions, SWT.NONE);
		lblNoPorts.setAlignment(SWT.RIGHT);
		lblNoPorts.setBounds(10, 56, 55, 15);
		lblNoPorts.setText("No ports:");
		
		Label lblPreauth = new Label(grpTelnetOptions, SWT.NONE);
		lblPreauth.setAlignment(SWT.RIGHT);
		lblPreauth.setBounds(10, 84, 55, 15);
		lblPreauth.setText("Pre-auth:");
		
		Label lblBanned = new Label(grpTelnetOptions, SWT.NONE);
		lblBanned.setAlignment(SWT.RIGHT);
		lblBanned.setBounds(10, 111, 55, 15);
		lblBanned.setText("Banned:");
		
		Button button_2 = new Button(grpTelnetOptions, SWT.NONE);
		button_2.setBounds(271, 24, 28, 25);
		button_2.setText("...");
		
		textTelnetNoPorts = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetNoPorts.setBounds(71, 53, 200, 21);
		
		textTelnetPreAuth = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetPreAuth.setBounds(71, 81, 200, 21);
		
		textTelnetBanned = new Text(grpTelnetOptions, SWT.BORDER);
		textTelnetBanned.setBounds(71, 108, 200, 21);
		
		Button button_3 = new Button(grpTelnetOptions, SWT.NONE);
		button_3.setText("...");
		button_3.setBounds(271, 51, 28, 25);
		
		Button button_4 = new Button(grpTelnetOptions, SWT.NONE);
		button_4.setText("...");
		button_4.setBounds(271, 79, 28, 25);
		
		Button button_5 = new Button(grpTelnetOptions, SWT.NONE);
		button_5.setText("...");
		button_5.setBounds(271, 106, 28, 25);
		
		textTermPort = new Text(composite_2, SWT.BORDER);
		textTermPort.setBounds(181, 49, 57, 21);
		
		Label lblOsTermDevice = new Label(composite_2, SWT.NONE);
		lblOsTermDevice.setBounds(23, 52, 156, 15);
		lblOsTermDevice.setText("OS9 TERM device telnet port:");
		
		Group grpTelnetAuthentication = new Group(composite_2, SWT.NONE);
		grpTelnetAuthentication.setText(" Telnet authentication ");
		grpTelnetAuthentication.setBounds(10, 265, 320, 74);
		
		textTelnetPasswd = new Text(grpTelnetAuthentication, SWT.BORDER);
		textTelnetPasswd.setBounds(85, 33, 184, 21);
		
		Button button_6 = new Button(grpTelnetAuthentication, SWT.NONE);
		button_6.setText("...");
		button_6.setBounds(269, 31, 28, 25);
		
		Label lblPasswdFile = new Label(grpTelnetAuthentication, SWT.NONE);
		lblPasswdFile.setAlignment(SWT.RIGHT);
		lblPasswdFile.setBounds(10, 36, 69, 15);
		lblPasswdFile.setText("Passwd file:");
		
		TabItem tbtmAdvanced = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvanced.setText("IP Access");
		
		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmAdvanced.setControl(composite_3);
		
		textIPBanned = new Text(composite_3, SWT.BORDER | SWT.V_SCROLL);
		textIPBanned.setBounds(10, 44, 325, 72);
		
		Label lblBannedIpAddresses = new Label(composite_3, SWT.NONE);
		lblBannedIpAddresses.setBounds(10, 23, 123, 15);
		lblBannedIpAddresses.setText("Banned IP Addresses:");
		
		btnUseGeoipLookups = new Button(composite_3, SWT.CHECK);
		btnUseGeoipLookups.setBounds(10, 132, 123, 16);
		btnUseGeoipLookups.setText("Use GeoIP lookups");
		
		textGeoIPfile = new Text(composite_3, SWT.BORDER);
		textGeoIPfile.setBounds(95, 162, 201, 21);
		
		Label lblDatabaseFile = new Label(composite_3, SWT.NONE);
		lblDatabaseFile.setAlignment(SWT.RIGHT);
		lblDatabaseFile.setBounds(10, 165, 79, 15);
		lblDatabaseFile.setText("Database file:");
		
		Button button_7 = new Button(composite_3, SWT.NONE);
		button_7.setBounds(297, 160, 27, 25);
		button_7.setText("...");
		
		textIPBannedCities = new Text(composite_3, SWT.BORDER | SWT.V_SCROLL);
		textIPBannedCities.setBounds(10, 220, 325, 46);
		
		textIPBannedCountries = new Text(composite_3, SWT.BORDER | SWT.V_SCROLL);
		textIPBannedCountries.setBounds(10, 301, 325, 46);
		
		Label lblBannedCities = new Label(composite_3, SWT.NONE);
		lblBannedCities.setBounds(10, 199, 98, 15);
		lblBannedCities.setText("Banned Cities:");
		
		Label lblBannedCountries = new Label(composite_3, SWT.NONE);
		lblBannedCountries.setBounds(10, 280, 112, 15);
		lblBannedCountries.setText("Banned Countries:");
		
		TabItem tbtmAdvanced_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvanced_1.setText("Advanced");
		
		Composite composite_4 = new Composite(tabFolder, SWT.NONE);
		tbtmAdvanced_1.setControl(composite_4);
		
		btnHdbdosMode = new Button(composite_4, SWT.CHECK);
		btnHdbdosMode.setBounds(21, 107, 119, 16);
		btnHdbdosMode.setText("HDBDOS mode");
		
		btnDrivewireMode = new Button(composite_4, SWT.CHECK);
		btnDrivewireMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnDrivewireMode.setBounds(21, 129, 119, 16);
		btnDrivewireMode.setText("DriveWire 3 mode");
		
		textRateOverride = new Text(composite_4, SWT.BORDER);
		textRateOverride.setBounds(126, 20, 97, 21);
		
		Label lblBaudRateOverride = new Label(composite_4, SWT.NONE);
		lblBaudRateOverride.setBounds(21, 23, 105, 15);
		lblBaudRateOverride.setText("Baud rate override:");
		
		btnOptimeSendsDow = new Button(composite_4, SWT.CHECK);
		btnOptimeSendsDow.setBounds(21, 165, 199, 16);
		btnOptimeSendsDow.setText("OP_TIME sends DOW");
		
		btnLogOpcodes = new Button(composite_4, SWT.CHECK);
		btnLogOpcodes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnLogOpcodes.setBounds(21, 199, 119, 16);
		btnLogOpcodes.setText("Log opcodes");
		
		btnLogProtocolDevice = new Button(composite_4, SWT.CHECK);
		btnLogProtocolDevice.setBounds(21, 247, 186, 16);
		btnLogProtocolDevice.setText("Log protocol device bytes");
		
		btnEvenOppoll = new Button(composite_4, SWT.CHECK);
		btnEvenOppoll.setBounds(47, 221, 105, 16);
		btnEvenOppoll.setText("Even OP_POLL");
		
		btnLogVirtualDevice = new Button(composite_4, SWT.CHECK);
		btnLogVirtualDevice.setBounds(21, 269, 157, 16);
		btnLogVirtualDevice.setText("Log virtual port bytes");
		
		btnLogMidiDevice = new Button(composite_4, SWT.CHECK);
		btnLogMidiDevice.setBounds(21, 291, 157, 16);
		btnLogMidiDevice.setText("Log MIDI device bytes");
		
		textDefaultDiskSet = new Text(composite_4, SWT.BORDER);
		textDefaultDiskSet.setBounds(126, 54, 97, 21);
		
		Label lblDefaultDiskset = new Label(composite_4, SWT.NONE);
		lblDefaultDiskset.setAlignment(SWT.RIGHT);
		lblDefaultDiskset.setBounds(21, 57, 99, 15);
		lblDefaultDiskset.setText("Default diskset:");
		
		Button btnUndo = new Button(shlInstanceConfiguration, SWT.NONE);
		btnUndo.setBounds(10, 400, 75, 25);
		btnUndo.setText("Undo");
		
		Button btnOk = new Button(shlInstanceConfiguration, SWT.NONE);
		btnOk.setBounds(146, 400, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlInstanceConfiguration, SWT.NONE);
		btnCancel.setBounds(288, 400, 75, 25);
		btnCancel.setText("Cancel");

		
		
	}
}
