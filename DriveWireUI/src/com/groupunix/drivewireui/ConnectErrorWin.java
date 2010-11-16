package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ConnectErrorWin extends Dialog {

	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ConnectErrorWin(Shell parent, int style) {
		super(parent, style);
		setText("Error");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(String errtxt) {
		createContents(errtxt);
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
	private void createContents(String errtxt) {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(465, 139);
		shell.setText(getText());
		
		Label lblSomethingHasGone = new Label(shell, SWT.NONE);
		lblSomethingHasGone.setAlignment(SWT.CENTER);
		lblSomethingHasGone.setBounds(10, 20, 439, 37);
		lblSomethingHasGone.setText(errtxt);
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shell.close();
			}
		});
		btnOk.setBounds(190, 76, 75, 25);
		btnOk.setText("Ok");

	}
}
