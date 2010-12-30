package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;

public class DisksetPropWin extends Dialog {

	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DisksetPropWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
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
		shell.setSize(450, 300);
		shell.setText(getText());
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.setBounds(180, 237, 75, 25);
		btnOk.setText("Ok");
		
		Button btnUndo = new Button(shell, SWT.NONE);
		btnUndo.setBounds(10, 237, 75, 25);
		btnUndo.setText("Undo");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.setBounds(359, 237, 75, 25);
		btnCancel.setText("Cancel");
		
		Button btnTrackDiskChanges = new Button(shell, SWT.CHECK);
		btnTrackDiskChanges.setBounds(130, 138, 218, 25);
		btnTrackDiskChanges.setText("Track disk changes");
		
		Button btnHdbdosTranslation = new Button(shell, SWT.CHECK);
		btnHdbdosTranslation.setText("HDBDOS translation");
		btnHdbdosTranslation.setBounds(130, 172, 218, 25);

	}
}
