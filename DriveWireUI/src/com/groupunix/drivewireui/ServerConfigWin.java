package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ServerConfigWin extends Dialog {

	protected Object result;
	protected Shell shlServerConfiguration;
	
	
	private HashMap<String,String> values = new HashMap<String,String>();
	

	private Button btnLogToConsole;
	private Button btnLogToFile;
	private Combo comboLogLevel;
	private Text textLogFile;
	private Text textLogFormat;
	private Button btnUIEnabled;
	private Text textUIPort;
	private Text textLazyWrite;
	private Text textLocalDiskDir;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ServerConfigWin(Shell parent, int style) {
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
		
		shlServerConfiguration.open();
		shlServerConfiguration.layout();
		Display display = getParent().getDisplay();
		while (!shlServerConfiguration.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void loadSettings() throws DWUIOperationFailedException, IOException 
	{
		ArrayList<String> settings = new ArrayList<String>();
		
		settings.add("LogToConsole");
		settings.add("LogToFile");
		settings.add("LogFile");
		settings.add("LogLevel");
		settings.add("LogFormat");
		settings.add("UIEnabled");
		settings.add("UIPort");
		settings.add("DiskLazyWriteInterval");
		settings.add("LocalDiskDir");
		
		values = UIUtils.getServerSettings(settings);
		
		applySettings();
		
	}

	

	
	
	private HashMap<String, String> getChangedValues() 
	{
		HashMap<String,String> res = new HashMap<String,String>();
		
		addIfChanged(res,"LogToConsole",UIUtils.bTos(this.btnLogToConsole.getSelection()));
		addIfChanged(res,"LogToFile",UIUtils.bTos(this.btnLogToFile.getSelection()));
		addIfChanged(res,"UIEnabled",UIUtils.bTos(this.btnUIEnabled.getSelection()));
		addIfChanged(res,"LogFile",this.textLogFile.getText());
		addIfChanged(res,"LogFormat",this.textLogFormat.getText());
		addIfChanged(res,"UIPort",this.textUIPort.getText());
		addIfChanged(res,"DiskLazyWriteInterval",this.textLazyWrite.getText());
		addIfChanged(res,"LocalDiskDir",this.textLocalDiskDir.getText());
		addIfChanged(res,"LogLevel",this.comboLogLevel.getItem(this.comboLogLevel.getSelectionIndex()));
	
		return(res);
	}

	private void addIfChanged(HashMap<String, String> map, String key, String value) 
	{
		if ((!values.containsKey(key)) || (!values.get(key).equals(value)))
		{ 
			map.put(key, value);
		}
		
	}

	private boolean validateValues() 
	{
		if (!UIUtils.validateNum(this.textLazyWrite.getText(),0))
		{
			MainWin.showError("Invalid value entered", "Data entered for DiskLazyWriteInterval is not valid" , "Valid range is positive integers");
			return false;
		}
		
		
		if (!UIUtils.validateNum(this.textUIPort.getText(),1,65535))
		{
			MainWin.showError("Invalid value entered", "Data entered for UI Port is not valid" , "Valid range is TCP port numbers, 1-65535.");
			return false;
		}
		
		
		
		return true;
	}



	private void applySettings() 
	{
		// apply settings, considering defaults
		
		if (values.containsKey("LogToConsole"))
		{
			this.btnLogToConsole.setSelection(UIUtils.sTob(values.get("LogToConsole")));
		}
		else
		{
			this.btnLogToConsole.setSelection(true);
		}
		
		
		if (values.containsKey("LogToFile"))
		{
			this.btnLogToFile.setSelection(UIUtils.sTob(values.get("LogToFile")));
		}
		else
		{
			this.btnLogToFile.setSelection(false);
		}
		
		
		if (values.containsKey("UIEnabled"))
		{
			this.btnUIEnabled.setSelection(UIUtils.sTob(values.get("UIEnabled")));
		}
		else
		{
			this.btnUIEnabled.setSelection(false);
		}
		
		
		if (values.containsKey("LogFile"))
		{
			this.textLogFile.setText(values.get("LogFile"));
		}
		else
		{
			this.textLogFile.setText("");
		}
	
		
		if (values.containsKey("LogFormat"))
		{
			this.textLogFormat.setText(values.get("LogFormat"));
		}
		else
		{
			this.textLogFormat.setText("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %26.26C: %m%n");
		}
		
		
		
		if (values.containsKey("UIPort"))
		{
			this.textUIPort.setText(values.get("UIPort"));
		}
		else
		{
			this.textUIPort.setText("");
		}
		
		
		if (values.containsKey("DiskLazyWriteInterval"))
		{
			this.textLazyWrite.setText(values.get("DiskLazyWriteInterval"));
		}
		else
		{
			this.textLazyWrite.setText("15000");
		}
		
		
		
		if (values.containsKey("LocalDiskDir"))
		{
			this.textLocalDiskDir.setText(values.get("LocalDiskDir"));
		}
		else
		{
			this.textLocalDiskDir.setText("");
		}
		
		
		if (values.containsKey("LogLevel"))
		{
			this.comboLogLevel.select(this.comboLogLevel.indexOf(values.get("LogLevel")));
		}
		else
		{
			this.comboLogLevel.select(this.comboLogLevel.indexOf("WARN"));
		
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlServerConfiguration = new Shell(getParent(), getStyle());
		shlServerConfiguration.setSize(377, 508);
		shlServerConfiguration.setText("Server Configuration");
		shlServerConfiguration.setLayout(null);
		
		Group grpLogging = new Group(shlServerConfiguration, SWT.NONE);
		grpLogging.setText(" Logging ");
		grpLogging.setBounds(15, 21, 339, 180);
		
		btnLogToConsole = new Button(grpLogging, SWT.CHECK);
		btnLogToConsole.setBounds(30, 53, 121, 16);
		btnLogToConsole.setText("Log to console");
		
		btnLogToFile = new Button(grpLogging, SWT.CHECK);
		btnLogToFile.setBounds(30, 75, 131, 16);
		btnLogToFile.setText("Log to file");
		
		comboLogLevel = new Combo(grpLogging, SWT.READ_ONLY);
		comboLogLevel.setItems(new String[] {"ALL", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"});
		comboLogLevel.setBounds(81, 20, 91, 23);
		
		Label lblLogLevel = new Label(grpLogging, SWT.NONE);
		lblLogLevel.setAlignment(SWT.RIGHT);
		lblLogLevel.setBounds(17, 23, 58, 15);
		lblLogLevel.setText("Log level:");
		
		Label lblLogFile = new Label(grpLogging, SWT.NONE);
		lblLogFile.setAlignment(SWT.RIGHT);
		lblLogFile.setBounds(20, 106, 55, 15);
		lblLogFile.setText("Log file:");
		
		textLogFile = new Text(grpLogging, SWT.BORDER);
		textLogFile.setBounds(81, 103, 208, 21);
		
		Button button = new Button(grpLogging, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
			
				 FileDialog fd = new FileDialog(shlServerConfiguration, SWT.SAVE);
			        fd.setText("Choose a log file...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textLogFile.setText(selected);
			        }
				
			
			}
		});
		button.setBounds(290, 101, 29, 25);
		button.setText("...");
		
		Label lblLineFormat = new Label(grpLogging, SWT.NONE);
		lblLineFormat.setAlignment(SWT.RIGHT);
		lblLineFormat.setBounds(10, 141, 65, 15);
		lblLineFormat.setText("Log format:");
		
		textLogFormat = new Text(grpLogging, SWT.BORDER);
		textLogFormat.setBounds(81, 138, 238, 21);
		
		Group grpUserInterfaceSupport = new Group(shlServerConfiguration, SWT.NONE);
		grpUserInterfaceSupport.setText(" User Interface Support");
		grpUserInterfaceSupport.setBounds(15, 218, 339, 67);
		
		btnUIEnabled = new Button(grpUserInterfaceSupport, SWT.CHECK);
		btnUIEnabled.setBounds(23, 30, 93, 16);
		btnUIEnabled.setText("UI Enabled");
		
		textUIPort = new Text(grpUserInterfaceSupport, SWT.BORDER);
		textUIPort.setBounds(261, 28, 55, 21);
		
		Label lblListenOnTcp = new Label(grpUserInterfaceSupport, SWT.NONE);
		lblListenOnTcp.setAlignment(SWT.RIGHT);
		lblListenOnTcp.setBounds(126, 31, 129, 15);
		lblListenOnTcp.setText("Listen on TCP port:");
		
		Group grpMiscellanous = new Group(shlServerConfiguration, SWT.NONE);
		grpMiscellanous.setText(" Disk Settings ");
		grpMiscellanous.setBounds(15, 305, 339, 112);
		
		Label lblDiskSyncLazy = new Label(grpMiscellanous, SWT.NONE);
		lblDiskSyncLazy.setAlignment(SWT.RIGHT);
		lblDiskSyncLazy.setBounds(10, 33, 187, 15);
		lblDiskSyncLazy.setText("Disk sync lazy write interval (ms):");
		
		textLazyWrite = new Text(grpMiscellanous, SWT.BORDER);
		textLazyWrite.setBounds(203, 30, 65, 21);
		
		textLocalDiskDir = new Text(grpMiscellanous, SWT.BORDER);
		textLocalDiskDir.setBounds(25, 73, 289, 21);
		
		Label lblLocalDiskDirectory = new Label(grpMiscellanous, SWT.NONE);
		lblLocalDiskDirectory.setBounds(25, 54, 155, 15);
		lblLocalDiskDirectory.setText("Local disk directory:");
		
		Button btnOk = new Button(shlServerConfiguration, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (validateValues())
				{
					
					try 
					{
						UIUtils.setServerSettings(getChangedValues());
						shlServerConfiguration.close();
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
		btnOk.setBounds(145, 444, 75, 25);
		btnOk.setText("Ok");
		
		Button btnUndo = new Button(shlServerConfiguration, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applySettings();
				
			}
		});
		btnUndo.setBounds(10, 444, 75, 25);
		btnUndo.setText("Undo");
		
		Button btnCancel = new Button(shlServerConfiguration, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlServerConfiguration.close();
			}
		});
		btnCancel.setBounds(279, 444, 75, 25);
		btnCancel.setText("Cancel");

	}
}
