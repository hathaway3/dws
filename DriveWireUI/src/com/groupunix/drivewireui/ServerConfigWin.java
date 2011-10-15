package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ServerConfigWin extends Dialog {

	protected Object result;
	protected static Shell shlServerConfiguration;
	
	
	private Button btnLogToConsole;
	private Button btnLogToFile;
	private Combo comboLogLevel;
	private Text textLogFile;
	private Text textLogFormat;
	private Button btnUIEnabled;
	private Text textUIPort;
	private Text textLazyWrite;
	private Text textLocalDiskDir;
	
	private static Group grpLogging;
	private static Group grpMiscellaneous;
	private static Group grpUserInterfaceSupport;
	

	
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
		applyFont();		
		 UIUtils.getDWConfigSerial();
		applySettings();
		
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

	
	private static void applyFont() 
	{
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		Control[] controls = shlServerConfiguration.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlServerConfiguration.getDisplay(), f));
			
		}
	
		controls = grpMiscellaneous.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlServerConfiguration.getDisplay(), f));
			
		}
		
		controls = grpUserInterfaceSupport.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlServerConfiguration.getDisplay(), f));
			
		}
		
		controls = grpLogging.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlServerConfiguration.getDisplay(), f));
			
		}
		
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
		if ((!MainWin.dwconfig.containsKey(key)) || (!MainWin.dwconfig.getProperty(key).equals(value)))
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
		
		this.btnLogToConsole.setSelection(MainWin.dwconfig.getBoolean("LogToConsole",true));
		this.btnLogToFile.setSelection(MainWin.dwconfig.getBoolean("LogToFile", false));
		this.btnUIEnabled.setSelection(MainWin.dwconfig.getBoolean("UIEnabled", false));
		
		this.textLogFile.setText(MainWin.dwconfig.getString("LogFile",""));
		
		this.textLogFormat.setText(MainWin.dwconfig.getString("LogFormat","%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %26.26C: %m%n"));
		this.textUIPort.setText(MainWin.dwconfig.getString("UIPort",""));
		this.textLazyWrite.setText(MainWin.dwconfig.getString("DiskLazyWriteInterval","15000"));
		this.textLocalDiskDir.setText(MainWin.dwconfig.getString("LocalDiskDir",""));
		this.comboLogLevel.select(this.comboLogLevel.indexOf(MainWin.dwconfig.getString("LogLevel","WARN")));
		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlServerConfiguration = new Shell(getParent(), getStyle());
		shlServerConfiguration.setSize(414, 508);
		shlServerConfiguration.setText("Server Configuration");
		shlServerConfiguration.setLayout(null);
		
		grpLogging = new Group(shlServerConfiguration, SWT.NONE);
		grpLogging.setText(" Logging ");
		grpLogging.setBounds(15, 21, 378, 180);
		
		btnLogToConsole = new Button(grpLogging, SWT.CHECK);
		btnLogToConsole.setBounds(30, 53, 214, 23);
		btnLogToConsole.setText("Log to console");
		
		btnLogToFile = new Button(grpLogging, SWT.CHECK);
		btnLogToFile.setBounds(30, 75, 214, 23);
		btnLogToFile.setText("Log to file");
		
		comboLogLevel = new Combo(grpLogging, SWT.READ_ONLY);
		comboLogLevel.setItems(new String[] {"ALL", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"});
		comboLogLevel.setBounds(105, 20, 91, 23);
		
		Label lblLogLevel = new Label(grpLogging, SWT.NONE);
		lblLogLevel.setAlignment(SWT.RIGHT);
		lblLogLevel.setBounds(17, 23, 82, 24);
		lblLogLevel.setText("Log level:");
		
		Label lblLogFile = new Label(grpLogging, SWT.NONE);
		lblLogFile.setAlignment(SWT.RIGHT);
		lblLogFile.setBounds(20, 106, 82, 18);
		lblLogFile.setText("Log file:");
		
		textLogFile = new Text(grpLogging, SWT.BORDER);
		textLogFile.setBounds(110, 103, 214, 21);
		
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
		button.setBounds(324, 101, 29, 25);
		button.setText("...");
		
		Label lblLineFormat = new Label(grpLogging, SWT.NONE);
		lblLineFormat.setAlignment(SWT.RIGHT);
		lblLineFormat.setBounds(11, 141, 91, 18);
		lblLineFormat.setText("Log format:");
		
		textLogFormat = new Text(grpLogging, SWT.BORDER);
		textLogFormat.setBounds(110, 138, 243, 21);
		
		grpUserInterfaceSupport = new Group(shlServerConfiguration, SWT.NONE);
		grpUserInterfaceSupport.setText(" User Interface Support");
		grpUserInterfaceSupport.setBounds(15, 218, 378, 67);
		
		btnUIEnabled = new Button(grpUserInterfaceSupport, SWT.CHECK);
		btnUIEnabled.setBounds(30, 30, 130, 19);
		btnUIEnabled.setText("UI Enabled");
		
		textUIPort = new Text(grpUserInterfaceSupport, SWT.BORDER);
		textUIPort.setBounds(299, 28, 55, 21);
		
		Label lblListenOnTcp = new Label(grpUserInterfaceSupport, SWT.NONE);
		lblListenOnTcp.setAlignment(SWT.RIGHT);
		lblListenOnTcp.setBounds(155, 31, 138, 18);
		lblListenOnTcp.setText("Listen on TCP port:");
		
		grpMiscellaneous = new Group(shlServerConfiguration, SWT.NONE);
		grpMiscellaneous.setText(" Disk Settings ");
		grpMiscellaneous.setBounds(15, 305, 378, 119);
		
		Label lblDiskSyncLazy = new Label(grpMiscellaneous, SWT.NONE);
		lblDiskSyncLazy.setAlignment(SWT.RIGHT);
		lblDiskSyncLazy.setBounds(10, 30, 231, 18);
		lblDiskSyncLazy.setText("Disk sync lazy write interval (ms):");
		
		textLazyWrite = new Text(grpMiscellaneous, SWT.BORDER);
		textLazyWrite.setToolTipText("Test test test");
		textLazyWrite.setBounds(247, 27, 65, 21);
		
		textLocalDiskDir = new Text(grpMiscellaneous, SWT.BORDER);
		textLocalDiskDir.setBounds(22, 80, 331, 21);
		
		Label lblLocalDiskDirectory = new Label(grpMiscellaneous, SWT.NONE);
		lblLocalDiskDirectory.setBounds(22, 61, 171, 18);
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
						// TODO: ? deal with changes while we were open
						
						//if (curserial == UIUtils.getDWConfigSerial())
						//{
							UIUtils.setServerSettings(getChangedValues());
							shlServerConfiguration.close();
						//}
						//else
						//{
							// something changed while we were open...
							
						//}
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
		btnOk.setBounds(163, 444, 75, 25);
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
		btnCancel.setBounds(318, 444, 75, 25);
		btnCancel.setText("Cancel");

	}
}
