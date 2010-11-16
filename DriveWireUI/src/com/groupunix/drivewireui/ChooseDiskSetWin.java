package com.groupunix.drivewireui;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ChooseDiskSetWin extends Dialog {

	protected Object result;
	protected Shell shell;

	protected Combo cmbDiskSet;

	private String pre;
	private String post;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param post 
	 * @param pre 
	 */
	public ChooseDiskSetWin(Shell parent, int style, String pre, String post) {
		super(parent, style);
		this.pre = pre;
		this.post = post;
		
		setText("Choose a disk set...");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		loadDiskSets(cmbDiskSet);
		cmbDiskSet.select(0);
		
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void loadDiskSets(Combo cmb) 
	{
		ArrayList<String> disksets = UIUtils.loadArrayList("disksets");
		
		Collections.sort(disksets);
		
		for(int i=0; i<disksets.size(); i++)
		{
			cmb.add(disksets.get(i));
		}
	}



	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(251, 100);
		shell.setText(getText());
		
		Button btnChoose = new Button(shell, SWT.NONE);
		btnChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand(pre + " " + cmbDiskSet.getText() + " " + post);
				shell.close();
			}
		});
		btnChoose.setBounds(74, 39, 75, 25);
		btnChoose.setText("Ok");
		
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
		
		cmbDiskSet = new Combo(shell, SWT.READ_ONLY);
		cmbDiskSet.setVisibleItemCount(10);
		cmbDiskSet.setBounds(10, 10, 222, 23);

	}
}
