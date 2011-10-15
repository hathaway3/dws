package com.groupunix.drivewireui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Link;

public class InitialConfigWin extends Dialog {

	protected Object result;
	protected Shell shlInitialConfiguration;
	private Text textPort;
	private Text textHost;
	
	Composite compPage1;
	Composite compPage2;
	Composite compPage3;
	
	private Combo comboSerialDev;
	private Text textConnTest;
	private Label labelOK;
	private Label labelERR;
	private Label labelHost;
	private Button btnBack;
	private Button btnNext;
	private Combo comboCocoModel;
	
	private boolean connTested = false;
	
	private int page = 0;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public InitialConfigWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		updatePages();
		
		this.labelOK.setVisible(false);
		this.labelERR.setVisible(false);
		
		Link link = new Link(compPage2, SWT.NONE);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// open wiki in browser
				org.eclipse.swt.program.Program.launch("http://sourceforge.net/apps/mediawiki/drivewireserver/index.php");
			}
		});
		link.setBounds(10, 150, 406, 31);
		link.setText("If you have trouble connecting, please <a href=\"http://sourceforge.net/apps/mediawiki/drivewireserver/index.php\">consult the documentation.</a>");
		
		this.labelHost.setVisible(false);
		this.textHost.setVisible(false);
		

		
		shlInitialConfiguration.open();
		shlInitialConfiguration.layout();
		Display display = getParent().getDisplay();
		while (!shlInitialConfiguration.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlInitialConfiguration = new Shell(getParent(), getStyle());
		shlInitialConfiguration.setSize(453, 409);
		shlInitialConfiguration.setText("Initial Configuration");
		shlInitialConfiguration.setLayout(null);
		
		btnBack = new Button(shlInitialConfiguration, SWT.NONE);
		btnBack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (page == 0)
				{
					page = 1;
					updatePages();
				}
				else if (page == 1)
				{
					page = 0;
					updatePages();
					
				}
				else if (page == 2)
				{
					page = 1;
					updatePages();
				}
			
			}
		});
		btnBack.setBounds(10, 346, 75, 25);
		btnBack.setText("<< Back");
		
		btnNext = new Button(shlInitialConfiguration, SWT.NONE);
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (page == 0)
				{
					page = 1;
					updatePages();
				}
				else if (page == 1)
				{
					page = 2;
					updatePages();
				}
				else if (page == 2)
				{
					if (!comboSerialDev.getText().equals(""))
					{
						applyConfig();
					}
					else
					{
						MainWin.showError("We need a serial device", "In order to finish this wizard, we have to specify a serial device", "Please choose a valid serial device if possible.  If the desired device is not available, please exit this wizard and sort that out before continuing with DriveWire.\r\n\r\nIf you wanted to use some connection method other than serial, you'll have to use the regular instance config dialog.  This simple wizard only knows how to set up regular serial connections.");
					}
				}
				
				
			}
		});
		btnNext.setBounds(183, 346, 75, 25);
		btnNext.setText("Next >>");
		
		Button btnCancel = new Button(shlInitialConfiguration, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlInitialConfiguration.close();
			}
		});
		btnCancel.setBounds(361, 346, 75, 25);
		btnCancel.setText("Cancel");
		
		compPage1 = new Composite(shlInitialConfiguration, SWT.NONE);
		compPage1.setBounds(10, 10, 426, 314);
		
		Label lblThisWizardWill = new Label(compPage1, SWT.NONE);
		lblThisWizardWill.setBounds(10, 10, 406, 18);
		lblThisWizardWill.setText("This wizard will help you configure DriveWire 4.");
		
		Label lblFirstWeNeed = new Label(compPage1, SWT.WRAP);
		lblFirstWeNeed.setBounds(10, 39, 406, 38);
		lblFirstWeNeed.setText("First, we need to know how to communicate with the DriveWire server.  Where does the server run?");
		
		Composite composite = new Composite(compPage1, SWT.NONE);
		composite.setBounds(38, 83, 329, 55);
		
		Button btnLocalServer = new Button(composite, SWT.RADIO);
		btnLocalServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				labelHost.setVisible(false);
				textHost.setVisible(false);
			}
		});
		btnLocalServer.setSelection(true);
		btnLocalServer.setBounds(10, 0, 309, 26);
		btnLocalServer.setText("The server runs on this computer");
		
		Button btnRemoteServer = new Button(composite, SWT.RADIO);
		btnRemoteServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				labelHost.setVisible(true);
				textHost.setVisible(true);
			
			}
		});
		btnRemoteServer.setBounds(10, 32, 309, 23);
		btnRemoteServer.setText("The server runs on another computer");
		
		labelHost = new Label(compPage1, SWT.NONE);
		labelHost.setAlignment(SWT.RIGHT);
		labelHost.setBounds(10, 160, 239, 18);
		labelHost.setText("What is the IP address of the server?");
		
		textHost = new Text(compPage1, SWT.BORDER);
		textHost.setBounds(254, 157, 144, 21);
		textHost.setText("127.0.0.1");
		
		Label lblByDefaultThe = new Label(compPage1, SWT.WRAP);
		lblByDefaultThe.setBounds(10, 196, 406, 94);
		lblByDefaultThe.setText("By default, the server listens for clients on TCP port 6800.  \r\n\r\nYou can change this using the client once we have a connection to the server, or by editing the server's configuration file.\r\n");
		
		Label lblWhatTcpPort = new Label(compPage1, SWT.NONE);
		lblWhatTcpPort.setAlignment(SWT.RIGHT);
		lblWhatTcpPort.setBounds(10, 296, 239, 18);
		lblWhatTcpPort.setText("What TCP port does the server use?");
		
		textPort = new Text(compPage1, SWT.BORDER);
		textPort.setBounds(255, 293, 55, 21);
		textPort.setText("6800");
		
		compPage2 = new Composite(shlInitialConfiguration, SWT.NO_FOCUS);
		compPage2.setBounds(10, 10, 426, 314);
		
		Label lblOkNowLets = new Label(compPage2, SWT.NONE);
		lblOkNowLets.setBounds(10, 10, 406, 15);
		lblOkNowLets.setText("Ok, now let's make sure we can communicate with the server.");
		
		Label lblIfYouHavent = new Label(compPage2, SWT.WRAP);
		lblIfYouHavent.setBounds(10, 43, 406, 101);
		lblIfYouHavent.setText("If you haven't started the server, please start it now.  \r\n\r\nIf you aren't sure whether the server is running, there is no harm in using the \"Test Connection\" button here to find out.  If the connection is successful, you'll see a green \"OK\" logo and the current server version displayed.");
		
		Button btnTestConnection = new Button(compPage2, SWT.NONE);
		btnTestConnection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				labelOK.setVisible(false);
				labelERR.setVisible(false);
				
				textConnTest.setText("");
				
				// test server connection
				
				if (UIUtils.validateNum(textPort.getText(), 1, 65535))
				{	
					try 
					{
						Connection conn = new Connection(textHost.getText(), Integer.parseInt(textPort.getText()), 0);
					
						ArrayList<String> res = new ArrayList<String>();
					
						conn.Connect();
						res = conn.loadArrayList("ui server show version");
						conn.close();
					
						textConnTest.setText(res.get(0));
						labelOK.setVisible(true);
						connTested = true;
						
						
					
					} 
					catch (IOException e1) 
					{
						labelERR.setVisible(true);
						textConnTest.setText(e1.getMessage());
					}
					catch (NumberFormatException e2)
					{
						labelERR.setVisible(true);
						textConnTest.setText("Port number must be numeric.");
					}
				}
				else
				{
					labelERR.setVisible(true);
					textConnTest.setText("Valid TCP port range is 1 - 65535.");
				}
				
				btnNext.setEnabled(connTested);
				
			}
		});
		btnTestConnection.setBounds(140, 200, 135, 25);
		btnTestConnection.setText("Test Connection");
		
		labelOK = new Label(compPage2, SWT.SHADOW_NONE);
		labelOK.setImage(SWTResourceManager.getImage(InitialConfigWin.class, "/dw4logo5.png"));
		labelOK.setBounds(27, 239, 64, 54);
		
		textConnTest = new Text(compPage2, SWT.READ_ONLY | SWT.WRAP | SWT.CENTER);
		textConnTest.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		textConnTest.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		textConnTest.setBounds(97, 250, 203, 54);
		
		labelERR = new Label(compPage2, SWT.SHADOW_NONE);
		labelERR.setImage(SWTResourceManager.getImage(InitialConfigWin.class, "/javax/swing/plaf/metal/icons/ocean/error.png"));
		labelERR.setBounds(40, 250, 38, 43);
		
		compPage3 = new Composite(shlInitialConfiguration, SWT.NONE);
		compPage3.setBounds(10, 10, 426, 314);
		
		Label lblFinallyWeNeed = new Label(compPage3, SWT.WRAP);
		lblFinallyWeNeed.setBounds(10, 10, 406, 35);
		lblFinallyWeNeed.setText("Almost done.  We just need to tell the server how it will communicate with the CoCo.\r\n");
		
		Label lblTheServerCan = new Label(compPage3, SWT.WRAP);
		lblTheServerCan.setBounds(10, 59, 406, 49);
		lblTheServerCan.setText("The server can try to detect attached serial devices, but it isn't perfect on every platform.  Press \"Detect\" to give it a shot.  You can also enter the device name manually.");
		
		comboSerialDev = new Combo(compPage3, SWT.NONE);
		comboSerialDev.setBounds(120, 127, 160, 23);
		
		Label lblSerialDevice = new Label(compPage3, SWT.NONE);
		lblSerialDevice.setAlignment(SWT.RIGHT);
		lblSerialDevice.setBounds(10, 130, 104, 20);
		lblSerialDevice.setText("Serial device:");
		
		Button btnDetect = new Button(compPage3, SWT.NONE);
		btnDetect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try {
					MainWin.setHost(textHost.getText());
					MainWin.setPort(textPort.getText());
					MainWin.setInstance(0);
					
					
					ArrayList<String> ports = UIUtils.loadArrayList("ui server show serialdevs");
					
					comboSerialDev.removeAll();
					
					for (int i = 0;i<ports.size();i++)
						comboSerialDev.add(ports.get(i));
					
					if (ports.size()>0)
					{
						// set current device if possible
						if ((MainWin.getInstanceConfig() != null) && (MainWin.getInstanceConfig().containsKey("SerialDevice")) && (comboSerialDev.indexOf(MainWin.getInstanceConfig().getString("SerialDevice")) > -1))
						{
							comboSerialDev.select(comboSerialDev.indexOf(MainWin.getInstanceConfig().getString("SerialDevice")));
						}
						else
						{
							comboSerialDev.select(0);
						}
					}
					
					
				} 
				catch (IOException e1) 
				{
					MainWin.showError("Error listing serial devices", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
				catch (DWUIOperationFailedException e1) 
				{
					MainWin.showError("Error listing serial devices", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
				
				
				
			}
		});
		btnDetect.setBounds(286, 125, 75, 25);
		btnDetect.setText("Detect..");
		
		Label lblFinallyWeNeed_1 = new Label(compPage3, SWT.WRAP);
		lblFinallyWeNeed_1.setBounds(10, 178, 406, 56);
		lblFinallyWeNeed_1.setText("Finally, we need to know what speed to use for communications with the CoCo.   The rate is determined by the type of CoCo you have.");
		
		comboCocoModel = new Combo(compPage3, SWT.READ_ONLY);
		comboCocoModel.setItems(new String[] {"CoCo 1: 38400 bps", "CoCo 2: 57600 bps", "CoCo 3: 115200 bps"});
		comboCocoModel.setBounds(120, 250, 160, 23);
		comboCocoModel.select(2);
		
		Label lblCocoModel = new Label(compPage3, SWT.NONE);
		lblCocoModel.setAlignment(SWT.RIGHT);
		lblCocoModel.setBounds(10, 253, 104, 15);
		lblCocoModel.setText("CoCo model:");

	}

	protected void applyConfig() 
	{
		MainWin.setHost(this.textHost.getText());
		MainWin.setPort(this.textPort.getText());
		MainWin.setInstance(0);

		// auto instance
		HashMap<String,String> vals = new HashMap<String,String>();
		
		vals.put("Name", "CoCo " + (this.comboCocoModel.getSelectionIndex() + 1) + " on " + this.comboSerialDev.getText());
		vals.put("CocoModel", (this.comboCocoModel.getSelectionIndex() + 1) + "");
		vals.put("SerialDevice", this.comboSerialDev.getText());
		vals.put("DeviceType", "serial");
		vals.put("AutoStart", "true");
	
		
		try 
		{
			UIUtils.setInstanceSettings(0, vals);

			MainWin.sendCommand("ui instance reset protodev");
			
			MainWin.refreshDiskTable();
			
			MainWin.applyServerSync();
			
			shlInitialConfiguration.close();
			
		} 
		catch (UnknownHostException e) 
		{
			MainWin.showError("Error sending configuration", e.getMessage(), UIUtils.getStackTrace(e));
		} 
		catch (IOException e) 
		{
			MainWin.showError("Error sending configuration", e.getMessage(), UIUtils.getStackTrace(e));
		} 
		catch (DWUIOperationFailedException e) 
		{
			MainWin.showError("Error sending configuration", e.getMessage(), UIUtils.getStackTrace(e));
		}
				
	}

	protected void updatePages() 
	{
		this.labelERR.setVisible(false);
		this.labelOK.setVisible(false);
		this.textConnTest.setText("");
		
		if (this.page == 0)
		{
			this.btnBack.setEnabled(false);
			this.btnNext.setEnabled(true);
			this.btnNext.setText("Next >>");
			
			this.compPage1.setVisible(true);
			this.compPage2.setVisible(false);
			this.compPage3.setVisible(false);
			
		}
		else if (this.page == 1)
		{
			this.btnBack.setEnabled(true);
			this.btnNext.setEnabled(this.connTested);
			this.btnNext.setText("Next >>");
			
			
			this.compPage1.setVisible(false);
			this.compPage2.setVisible(true);
			this.compPage3.setVisible(false);
		}
		else if (this.page == 2)
		{
			this.btnBack.setEnabled(true);
			this.btnNext.setEnabled(true);
			this.btnNext.setText("Finish");
			
			this.compPage1.setVisible(false);
			this.compPage2.setVisible(false);
			this.compPage3.setVisible(true);
		}
	}
}
