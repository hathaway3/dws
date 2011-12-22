package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class UIConfigWin extends Dialog {

	protected Object result;
	protected Shell shlUserInterfaceConfiguration;
	protected Display display;
	private Button btnLocalServer;
	private Button btnApply;
	private Button btnTerminateServerOn;
	private FormData fd_btnLocalServer;
	private Button btnRememberWindowPositions;
	private Button btnReopenDiskWindows;
	private Button btnBackupConfigurationBefore;
	private Button btnOpenLLog;
	private Button btnRunInsafe;
	private Button btnRemoteFileDialogs;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public UIConfigWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		loadSettings();
		toggleStuff();
		
		
		
		btnRememberWindowPositions = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		FormData fd_btnRememberWindowPositions = new FormData();
		fd_btnRememberWindowPositions.right = new FormAttachment(btnLocalServer, 0, SWT.RIGHT);
		fd_btnRememberWindowPositions.top = new FormAttachment(0, 31);
		fd_btnRememberWindowPositions.left = new FormAttachment(0, 26);
		btnRememberWindowPositions.setLayoutData(fd_btnRememberWindowPositions);
		btnRememberWindowPositions.setText("Remember window positions");
		
		btnReopenDiskWindows = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		fd_btnLocalServer.top = new FormAttachment(btnReopenDiskWindows, 15);
		FormData fd_btnReopenDiskWindows = new FormData();
		fd_btnReopenDiskWindows.top = new FormAttachment(btnRememberWindowPositions, 13);
		fd_btnReopenDiskWindows.right = new FormAttachment(btnLocalServer, 0, SWT.RIGHT);
		fd_btnReopenDiskWindows.left = new FormAttachment(0, 26);
		btnReopenDiskWindows.setLayoutData(fd_btnReopenDiskWindows);
		btnReopenDiskWindows.setText("Re-open disk windows at startup");
		
		btnBackupConfigurationBefore = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		FormData fd_btnBackupConfigurationBefore = new FormData();
		fd_btnBackupConfigurationBefore.right = new FormAttachment(btnLocalServer, 0, SWT.RIGHT);
		fd_btnBackupConfigurationBefore.top = new FormAttachment(btnLocalServer, 13);
		fd_btnBackupConfigurationBefore.left = new FormAttachment(0, 52);
		btnBackupConfigurationBefore.setLayoutData(fd_btnBackupConfigurationBefore);
		btnBackupConfigurationBefore.setText("Backup configuration before run");
		
		btnOpenLLog = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		FormData fd_btnOpenLLog = new FormData();
		fd_btnOpenLLog.right = new FormAttachment(btnLocalServer, 0, SWT.RIGHT);
		fd_btnOpenLLog.top = new FormAttachment(btnBackupConfigurationBefore, 6);
		fd_btnOpenLLog.left = new FormAttachment(0, 52);
		btnOpenLLog.setLayoutData(fd_btnOpenLLog);
		btnOpenLLog.setText("Open L5 log viewer");
		
		btnRunInsafe = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		FormData fd_btnRunInsafe = new FormData();
		fd_btnRunInsafe.right = new FormAttachment(btnLocalServer, 0, SWT.RIGHT);
		fd_btnRunInsafe.top = new FormAttachment(btnOpenLLog, 6);
		fd_btnRunInsafe.left = new FormAttachment(0, 52);
		btnRunInsafe.setLayoutData(fd_btnRunInsafe);
		btnRunInsafe.setText("Run in \"safe mode\"");
		
		btnTerminateServerOn = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		
		FormData fd_btnTerminateServerOn = new FormData();
		fd_btnTerminateServerOn.right = new FormAttachment(btnLocalServer, 0, SWT.RIGHT);
		fd_btnTerminateServerOn.top = new FormAttachment(btnRunInsafe, 16);
		fd_btnTerminateServerOn.left = new FormAttachment(0, 26);
		btnTerminateServerOn.setLayoutData(fd_btnTerminateServerOn);
		btnTerminateServerOn.setText("Terminate server on exit");
		
		shlUserInterfaceConfiguration.pack();
		
		btnRemoteFileDialogs = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		FormData fd_btnRemoteFileDialogs = new FormData();
		fd_btnRemoteFileDialogs.right = new FormAttachment(btnLocalServer, 0, SWT.RIGHT);
		fd_btnRemoteFileDialogs.top = new FormAttachment(btnTerminateServerOn, 16);
		fd_btnRemoteFileDialogs.left = new FormAttachment(0, 26);
		btnRemoteFileDialogs.setLayoutData(fd_btnRemoteFileDialogs);
		btnRemoteFileDialogs.setText("Use remote file dialogs");
		
		
		

		Composite composite = new Composite(shlUserInterfaceConfiguration, SWT.NONE);
		
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(100, -10);
		fd_composite.right = new FormAttachment(100, -10);
		fd_composite.left = new FormAttachment(0, 52);
		fd_composite.top = new FormAttachment(btnRemoteFileDialogs, 20);
		composite.setLayoutData(fd_composite);
		
		FillLayout fl_composite = new FillLayout(SWT.HORIZONTAL);
		fl_composite.spacing = 10;
		composite.setLayout(fl_composite);
		
		Button btnOk = new Button(composite, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applySettings();
				
				e.display.getActiveShell().close();
			}
		});
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setText("Cancel");
		
		
		
		btnApply = new Button(composite, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applySettings();
			}
		});
		btnApply.setText("Apply");
		
		btnApply.setEnabled(false);
		
		
		
		shlUserInterfaceConfiguration.pack();
		
		shlUserInterfaceConfiguration.open();
		shlUserInterfaceConfiguration.layout();
		
		
		display = getParent().getDisplay();
		while (!shlUserInterfaceConfiguration.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}





	private void toggleStuff() 
	{
		
	}

	
	private void loadSettings()
	{
		this.btnLocalServer.setSelection(MainWin.config.getBoolean("LocalServer", true));
		
		
	}
	

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlUserInterfaceConfiguration = new Shell(getParent(), getStyle());
		shlUserInterfaceConfiguration.setSize(281, 344);
		//shlUserInterfaceConfiguration.setSize(320, 307);
		shlUserInterfaceConfiguration.setText("User Interface Configuration");
		shlUserInterfaceConfiguration.setLayout(new FormLayout());
		
		btnLocalServer = new Button(shlUserInterfaceConfiguration, SWT.CHECK);
		fd_btnLocalServer = new FormData();
		fd_btnLocalServer.left = new FormAttachment(0, 26);
		fd_btnLocalServer.right = new FormAttachment(100, -30);
		btnLocalServer.setLayoutData(fd_btnLocalServer);
		btnLocalServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnApply.setEnabled(true);
			}
		});
		btnLocalServer.setText("Start local server when client opens");
		
		


	}

	protected void applySettings() 
	{

		
		
		
		MainWin.config.setProperty("TermServerOnExit", this.btnTerminateServerOn.getSelection());
		
		MainWin.config.setProperty("LocalServer", this.btnLocalServer.getSelection());

		
	
		btnApply.setEnabled(false);
	}



		protected Button getBtnLocalServer() {
		return btnLocalServer;
	}
	protected Button getBtnRememberWindowPositions() {
		return btnRememberWindowPositions;
	}
	protected Button getBtnReopenDiskWindows() {
		return btnReopenDiskWindows;
	}
	protected Button getBtnBackupConfigurationBefore() {
		return btnBackupConfigurationBefore;
	}
	protected Button getBtnOpenLLog() {
		return btnOpenLLog;
	}
	protected Button getBtnRunInsafe() {
		return btnRunInsafe;
	}
	protected Button getBtnTerminateServerOn() {
		return btnTerminateServerOn;
	}
	protected Button getBtnRemoteFileDialogs() {
		return btnRemoteFileDialogs;
	}
}
