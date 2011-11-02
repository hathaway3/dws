package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;

public class SaveDiskSetWin extends Dialog {


	protected static Shell shell;

	protected Combo cmbDiskSet;


	

	public SaveDiskSetWin(Shell parent, int style) {
		super(parent, style);

		
		setText("Choose a diskset name to save as...");
	}


	public void open() throws IOException, DWUIOperationFailedException {
		createContents();
		
		applyFont();
		
		UIUtils.getDWConfigSerial();
		
		loadDiskSets(cmbDiskSet);
		
		Label lblChooseAnExisting = new Label(shell, SWT.WRAP);
		lblChooseAnExisting.setBounds(10, 10, 225, 37);
		lblChooseAnExisting.setText("Choose an existing diskset, or enter a new diskset name:");
		
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
		
		int x = getParent().getBounds().x + (getParent().getBounds().width / 2) - (shell.getBounds().width / 2);
		int y = getParent().getBounds().y + (getParent().getBounds().height / 2) - (shell.getBounds().height / 2);
		
		shell.setLocation(x, y);
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
	}

	
	private static void applyFont() 
	{
		FontData f = MainWin.getDialogFont();
		
		
		Control[] controls = shell.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shell.getDisplay(), f));
		}
	}
	
	
	private void loadDiskSets(Combo cmb) throws IOException, DWUIOperationFailedException 
	{
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> disksets = (List<HierarchicalConfiguration>)MainWin.dwconfig.configurationsAt("diskset");
		
		ArrayList<String> ps = new ArrayList<String>();
		
		for(int i=0; i<disksets.size(); i++)
		{
			ps.add(disksets.get(i).getString("Name","?noname?"));
		}
		
		Collections.sort(ps);
		
		for (String p : ps)
		{
			cmb.add(p);
		}
	}



	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(251, 146);
		shell.setText(getText());
		
		cmbDiskSet = new Combo(shell, SWT.NONE);
		cmbDiskSet.setVisibleItemCount(10);
		cmbDiskSet.setBounds(10, 53, 222, 23);
		
		Button btnChoose = new Button(shell, SWT.NONE);
		btnChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (!cmbDiskSet.getText().equals(""))
				{
					MainWin.sendCommand("dw disk write " + cmbDiskSet.getText());
					e.display.getActiveShell().close();
				}
			}
		});
		btnChoose.setBounds(76, 83, 75, 25);
		btnChoose.setText("Save");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setBounds(157, 83, 75, 25);
		btnCancel.setText("Cancel");

	}
}
