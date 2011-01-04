package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

public class StatusWin extends Dialog {

	protected Object result;
	protected Shell shlServerStatus;
	private Label labelVersion;
	private Label labelMemory;
	private Label lblInstances;
	private Label labelConfigPath;
	private Label lblFreeMemory;
	private Label lblDVersion;
	private Label lblDTotMem;
	private Label lblDFreeMem;
	private Label lblDConfig;
	private Label lblDInstances;
	private Group grpInstance;
	private Label lblLastOpcode;
	private Label lblLastGetstat;
	private Label lblLastSetstat;
	private Label lblLastDrive;
	private Label lblLastLsn;
	private Label lblLastError;
	private Label lblDDevice;
	private Label lblDOpCode;
	private Label lblDGetStat;
	private Label lblDSetStat;
	private Label lblDDrive;
	private Label lblDLSN;
	private Label lblDError;
	private Label lblLastChecksum;
	private Label lblDChecksum;
	private Combo comboRefresh;

	private Thread refreshT;
	private Display display;
	private Label lblAutoRefresh;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public StatusWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 * @throws DWUIOperationFailedException 
	 * @throws IOException 
	 */
	public Object open() throws IOException, DWUIOperationFailedException 
	{
		createContents();
		
		loadInfo();
		
		shlServerStatus.open();
		shlServerStatus.layout();
		display = getParent().getDisplay();
		while (!shlServerStatus.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void loadInfoAsync()
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  try {
							loadInfo();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DWUIOperationFailedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					  }
				  });
	}
	
	private void loadInfo() throws IOException, DWUIOperationFailedException 
	{
		// server
		
		String version = "";
		String versiondate = "";
		String configpath = "";
		String instances = "";
		String totmem = "";
		String freemem = "";
		
		
		ArrayList<String> res = UIUtils.loadArrayList(MainWin.getInstance(),"ui server show status");
				
		
		for (int i = 0;i<res.size();i++)
		{
			Pattern p_setitem = Pattern.compile("^(.+):\\s(.+)");
			Matcher m = p_setitem.matcher(res.get(i));
			  
			if (m.find())
			{
				if (m.group(1).equals("version"))
					version = m.group(2);
				
				if (m.group(1).equals("versiondate"))
					versiondate = m.group(2);
					
				if (m.group(1).equals("totmem"))
					totmem = m.group(2);
				
				if (m.group(1).equals("freemem"))
					freemem = m.group(2);
				
				if (m.group(1).equals("configpath"))
					configpath = m.group(2);
				
				if (m.group(1).equals("instances"))
					instances = m.group(2);
					
			}
				
		}
		
		this.lblDVersion.setText(version + " (" + versiondate + ")");
		this.lblDTotMem.setText(totmem + " KB");
		this.lblDFreeMem.setText(freemem + " KB");
		
		this.lblDConfig.setText(configpath);
		this.lblDInstances.setText(instances);
		
		
		// instance

		String devicetype = "";
		String devicerate = "";
		String devicename = "";
		String deviceconnected = "";
	
		
		res = UIUtils.loadArrayList(MainWin.getInstance(),"ui instance status");
				
		
		for (int i = 0;i<res.size();i++)
		{
			Pattern p_setitem = Pattern.compile("^(.+):\\s(.+)");
			Matcher m = p_setitem.matcher(res.get(i));
			  
			if (m.find())
			{
				if (m.group(1).equals("name"))
					this.grpInstance.setText(" Instance: " + m.group(2) + " ");
				
				if (m.group(1).equals("devicetype"))
					devicetype = m.group(2);
					
				if (m.group(1).equals("devicerate"))
					devicerate = m.group(2);
				
				if (m.group(1).equals("devicename"))
					devicename = m.group(2);
				
				if (m.group(1).equals("deviceconnected"))
					deviceconnected = m.group(2);
				
				if (m.group(1).equals("lastopcode"))
					this.lblDOpCode.setText(m.group(2));
					
				if (m.group(1).equals("lastgetstat"))
					this.lblDGetStat.setText(m.group(2));
				
				if (m.group(1).equals("lastsetstat"))
					this.lblDSetStat.setText(m.group(2));
				
				if (m.group(1).equals("lastdrive"))
					this.lblDDrive.setText(m.group(2));

				if (m.group(1).equals("lasterror"))
					this.lblDError.setText(m.group(2));

				if (m.group(1).equals("lastlsn"))
					this.lblDLSN.setText(m.group(2));
				
				if (m.group(1).equals("lastchecksum"))
					this.lblDChecksum.setText(m.group(2));
			
			}
				
		}
		
		String dev = devicetype + ", " + devicename;
		
		if (!devicerate.equals("-1"))
			dev += " @ " + devicerate + " bps";
		
		if (deviceconnected.equals("true"))
		{
			dev += ", connected";
		}
		else
		{
			dev += ", disconnected";
		}
		this.lblDDevice.setText(dev);
		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlServerStatus = new Shell(getParent(), getStyle());
		shlServerStatus.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) 
			{
				stopRefreshThread();
			}
		});
		shlServerStatus.setSize(450, 439);
		shlServerStatus.setText("DriveWire Status");
		
		Button btnOk = new Button(shlServerStatus, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				stopRefreshThread();
				shlServerStatus.close();
			}
		});
		btnOk.setBounds(347, 376, 87, 25);
		btnOk.setText("Close");
		
		Button btnRefresh = new Button(shlServerStatus, SWT.NONE);
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try {
					loadInfo();
				}
				catch (DWUIOperationFailedException e1) 
				{
					MainWin.showError("Error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
				} 
				catch (IOException e1) 
				{
					MainWin.showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
			}
		});
		btnRefresh.setBounds(167, 376, 118, 25);
		btnRefresh.setText("Refresh now");
		
		Group grpServerStatus = new Group(shlServerStatus, SWT.NONE);
		grpServerStatus.setText(" Server ");
		grpServerStatus.setBounds(10, 20, 424, 135);
		
		labelVersion = new Label(grpServerStatus, SWT.NONE);
		labelVersion.setBounds(10, 24, 95, 18);
		labelVersion.setAlignment(SWT.RIGHT);
		labelVersion.setText("Version:");
		
		lblDVersion = new Label(grpServerStatus, SWT.NONE);
		lblDVersion.setBounds(111, 24, 295, 18);
		
		labelMemory = new Label(grpServerStatus, SWT.NONE);
		labelMemory.setBounds(7, 48, 99, 18);
		labelMemory.setAlignment(SWT.RIGHT);
		labelMemory.setText("Total Memory:");
		
		lblDTotMem = new Label(grpServerStatus, SWT.NONE);
		lblDTotMem.setBounds(112, 48, 105, 18);
		
		lblFreeMemory = new Label(grpServerStatus, SWT.NONE);
		lblFreeMemory.setBounds(200, 48, 95, 18);
		lblFreeMemory.setAlignment(SWT.RIGHT);
		lblFreeMemory.setText("Free Memory:");
		
		lblDFreeMem = new Label(grpServerStatus, SWT.NONE);
		lblDFreeMem.setBounds(301, 48, 105, 18);
		
		labelConfigPath = new Label(grpServerStatus, SWT.NONE);
		labelConfigPath.setBounds(10, 72, 95, 18);
		labelConfigPath.setAlignment(SWT.RIGHT);
		labelConfigPath.setText("Config:");
		
		lblDConfig = new Label(grpServerStatus, SWT.NONE);
		lblDConfig.setBounds(111, 72, 295, 18);
		
		lblInstances = new Label(grpServerStatus, SWT.NONE);
		lblInstances.setBounds(10, 96, 95, 18);
		lblInstances.setAlignment(SWT.RIGHT);
		lblInstances.setText("Instances:");
		
		lblDInstances = new Label(grpServerStatus, SWT.NONE);
		lblDInstances.setBounds(111, 96, 295, 18);
		
		grpInstance = new Group(shlServerStatus, SWT.NONE);
		grpInstance.setText(" Instance ");
		grpInstance.setBounds(10, 171, 424, 165);
		
		lblLastOpcode = new Label(grpInstance, SWT.NONE);
		lblLastOpcode.setText("Last OpCode:");
		lblLastOpcode.setAlignment(SWT.RIGHT);
		lblLastOpcode.setBounds(10, 53, 95, 18);
		
		lblLastGetstat = new Label(grpInstance, SWT.NONE);
		lblLastGetstat.setText("Last GetStat:");
		lblLastGetstat.setAlignment(SWT.RIGHT);
		lblLastGetstat.setBounds(10, 77, 95, 18);
		
		lblLastSetstat = new Label(grpInstance, SWT.NONE);
		lblLastSetstat.setText("Last SetStat:");
		lblLastSetstat.setAlignment(SWT.RIGHT);
		lblLastSetstat.setBounds(199, 77, 95, 18);
		
		lblLastDrive = new Label(grpInstance, SWT.NONE);
		lblLastDrive.setText("Last Drive:");
		lblLastDrive.setAlignment(SWT.RIGHT);
		lblLastDrive.setBounds(10, 101, 95, 18);
		
		lblLastLsn = new Label(grpInstance, SWT.NONE);
		lblLastLsn.setText("Last LSN:");
		lblLastLsn.setAlignment(SWT.RIGHT);
		lblLastLsn.setBounds(199, 101, 95, 18);
		
		lblLastError = new Label(grpInstance, SWT.NONE);
		lblLastError.setText("Last Error:");
		lblLastError.setAlignment(SWT.RIGHT);
		lblLastError.setBounds(10, 125, 95, 18);
		
		lblDDevice = new Label(grpInstance, SWT.NONE);
		lblDDevice.setText("");
		lblDDevice.setBounds(20, 27, 386, 18);
		
		lblDOpCode = new Label(grpInstance, SWT.NONE);
		lblDOpCode.setText("");
		lblDOpCode.setBounds(111, 53, 295, 18);
		
		lblDGetStat = new Label(grpInstance, SWT.NONE);
		lblDGetStat.setBounds(111, 77, 105, 18);
		
		lblDSetStat = new Label(grpInstance, SWT.NONE);
		lblDSetStat.setBounds(300, 77, 105, 18);
		
		lblDDrive = new Label(grpInstance, SWT.NONE);
		lblDDrive.setBounds(111, 101, 105, 18);
		
		lblDLSN = new Label(grpInstance, SWT.NONE);
		lblDLSN.setBounds(300, 101, 105, 18);
		
		lblDError = new Label(grpInstance, SWT.NONE);
		lblDError.setText("");
		lblDError.setBounds(111, 125, 83, 18);
		
		lblLastChecksum = new Label(grpInstance, SWT.NONE);
		lblLastChecksum.setText("Last Checksum:");
		lblLastChecksum.setAlignment(SWT.RIGHT);
		lblLastChecksum.setBounds(200, 125, 95, 18);
		
		lblDChecksum = new Label(grpInstance, SWT.NONE);
		lblDChecksum.setBounds(301, 125, 105, 18);
		
		comboRefresh = new Combo(shlServerStatus, SWT.READ_ONLY);
		comboRefresh.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) 
			{
				// kill thread if running
				stopRefreshThread();
				
				
				
				if (comboRefresh.getSelectionIndex() > 0)
				{
					final int delay = getRefreshDelayFor(comboRefresh.getSelectionIndex());
					refreshT = (new Thread() {
							 
							public void run()
							{
								boolean wanttodie = false;
								
								while (!wanttodie)
								{
									try 
									{
										Thread.sleep(delay);
										loadInfoAsync();
									} 
									catch (InterruptedException e) 
									{
										wanttodie = true;
									} 
						
								}
								
							}
		
					});
					refreshT.start();
					
				}
				
			}

			private int getRefreshDelayFor(int val) 
			{
				switch (val)
				{
					case 1: return(1000); 
					case 2: return(2000); 
					case 3: return(5000);
					case 4: return(10000);
					case 5: return(30000);
					case 6: return(60000);
					
				}
				return 1000;
			}
		});
		comboRefresh.setItems(new String[] {"Off", "Every second", "2 seconds", "5 seconds", "10 seconds", "30 seconds", "60 seconds"});
		comboRefresh.setBounds(10, 378, 118, 23);
		comboRefresh.select(0);
		
		lblAutoRefresh = new Label(shlServerStatus, SWT.NONE);
		lblAutoRefresh.setBounds(10, 359, 118, 18);
		lblAutoRefresh.setText("Auto Refresh");

	}
	protected void stopRefreshThread() 
	{
		if (refreshT != null)
		{
			if (refreshT.isAlive())
			{
				refreshT.interrupt();
			}
		}	
	}

	protected Label getLabelVersion() {
		return labelVersion;
	}
	protected Label getLabelMemory() {
		return labelMemory;
	}
	protected Label getLblInstances() {
		return lblInstances;
	}
	protected Label getLabelConfigPath() {
		return labelConfigPath;
	}
	protected Label getLblDInstances() {
		return lblDInstances;
	}
	protected Label getLblDConfig() {
		return lblDConfig;
	}
	protected Label getLblDTotMem() {
		return lblDTotMem;
	}
	protected Label getLblDVersion() {
		return lblDVersion;
	}
	protected Label getLblDFreeMem() {
		return lblDFreeMem;
	}
	protected Group getGrpInstance() {
		return grpInstance;
	}
	protected Combo getComboRefresh() {
		return comboRefresh;
	}
}
