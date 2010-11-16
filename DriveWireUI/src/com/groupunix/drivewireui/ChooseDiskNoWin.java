package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ChooseDiskNoWin extends Dialog {

	protected Object result;
	protected Shell shell;

	private String pre;
	private String post;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ChooseDiskNoWin(Shell parent, int style, String pre, String post) {
		super(parent, style);
		this.pre = pre;
		this.post = post;
		setText("Choose Disk...");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
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

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(346, 131);
		shell.setText(getText());
		
		final Spinner spinner = new Spinner(shell, SWT.BORDER);
		spinner.setSelection(4);
		spinner.setMaximum(255);
		spinner.setBounds(10, 70, 47, 22);
		
		Button btnX = new Button(shell, SWT.NONE);
		btnX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				doCommand(0);
			}
		});
		btnX.setBounds(10, 10, 75, 35);
		btnX.setText("X0");
		
		Button btnX_1 = new Button(shell, SWT.NONE);
		btnX_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				doCommand(1);
			}
		});
		btnX_1.setBounds(91, 10, 75, 35);
		btnX_1.setText("X1");
		
		Button btnX_2 = new Button(shell, SWT.NONE);
		btnX_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				doCommand(2);
			}
		});
		btnX_2.setBounds(172, 10, 75, 35);
		btnX_2.setText("X2");
		
		Button btnX_3 = new Button(shell, SWT.NONE);
		btnX_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				doCommand(3);
			}
		});
		btnX_3.setBounds(253, 10, 75, 35);
		btnX_3.setText("X3");
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				doCommand(spinner.getSelection());
			}
		});
		btnOk.setText("Use value");
		btnOk.setBounds(63, 68, 75, 25);
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shell.close();
			}
		});
		btnCancel.setBounds(253, 68, 75, 25);
		btnCancel.setText("Cancel");

	}

	protected void doCommand(int i) 
	{
		MainWin.sendCommand(this.pre + " " + i + " " + post);
		shell.close();
	}
}
