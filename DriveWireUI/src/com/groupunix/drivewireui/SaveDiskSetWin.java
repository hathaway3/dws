package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SaveDiskSetWin extends Dialog {


	protected Shell shell;

	protected Combo cmbDiskSet;


	

	public SaveDiskSetWin(Shell parent, int style) {
		super(parent, style);

		
		setText("Choose a diskset name to save as...");
	}


	public void open() throws IOException, DWUIOperationFailedException {
		createContents();
		
		UIUtils.getDWConfigSerial();
		
		loadDiskSets(cmbDiskSet);
		
		if (MainWin.getInstanceConfig().containsKey("CurrentDiskSet") && (cmbDiskSet.indexOf(MainWin.getInstanceConfig().getString("CurrentDiskSet")) > -1))
		{
			cmbDiskSet.select(cmbDiskSet.indexOf(MainWin.getInstanceConfig().getString("CurrentDiskSet")));
		}
		else
		{
			cmbDiskSet.select(0);
		}
		
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
	}

	private void loadDiskSets(Combo cmb) throws IOException, DWUIOperationFailedException 
	{
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> disksets = (List<HierarchicalConfiguration>)MainWin.dwconfig.configurationsAt("diskset");
		
		
		for(int i=0; i<disksets.size(); i++)
		{
			cmb.add(disksets.get(i).getString("Name","?noname?"));
		}
	}



	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(251, 100);
		shell.setText(getText());
		
		Button btnChoose = new Button(shell, SWT.NONE);
		btnChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (!cmbDiskSet.getText().equals(""))
				{
					MainWin.sendCommand("dw disk set save " + cmbDiskSet.getText());
					shell.close();
				}
			}
		});
		btnChoose.setBounds(74, 39, 75, 25);
		btnChoose.setText("Save");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shell.close();
			}
		});
		btnCancel.setBounds(157, 39, 75, 25);
		btnCancel.setText("Cancel");
		
		cmbDiskSet = new Combo(shell, SWT.NONE);
		cmbDiskSet.setVisibleItemCount(10);
		cmbDiskSet.setBounds(10, 10, 222, 23);

	}
}
