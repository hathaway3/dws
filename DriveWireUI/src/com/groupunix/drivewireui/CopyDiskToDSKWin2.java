package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.ProgressBar;

public class CopyDiskToDSKWin2 extends Dialog {

	protected Object result;
	protected Shell shlCreatedskFrom;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CopyDiskToDSKWin2(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlCreatedskFrom.open();
		shlCreatedskFrom.layout();
		Display display = getParent().getDisplay();
		while (!shlCreatedskFrom.isDisposed()) {
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
		shlCreatedskFrom = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlCreatedskFrom.setSize(450, 300);
		shlCreatedskFrom.setText("Create .DSK from floppy disk");
		
		Button btnNext = new Button(shlCreatedskFrom, SWT.NONE);
		btnNext.setEnabled(false);
		btnNext.setBounds(169, 237, 75, 25);
		btnNext.setText("Next >>");
		
		Button btnCancel = new Button(shlCreatedskFrom, SWT.NONE);
		btnCancel.setBounds(359, 237, 75, 25);
		btnCancel.setText("Cancel");
		
		Label lblTheServerIs = new Label(shlCreatedskFrom, SWT.NONE);
		lblTheServerIs.setBounds(23, 25, 341, 15);
		lblTheServerIs.setText("The server is now ready to receive the disk image.");
		
		Label lblOnYourCoco = new Label(shlCreatedskFrom, SWT.NONE);
		lblOnYourCoco.setBounds(23, 56, 395, 15);
		lblOnYourCoco.setText("On your CoCo, insert the source disk in drive 0 and enter the command:");
		
		Label lblBackupTo = new Label(shlCreatedskFrom, SWT.NONE);
		lblBackupTo.setFont(SWTResourceManager.getFont("vt100", 10, SWT.BOLD));
		lblBackupTo.setBounds(156, 85, 195, 15);
		lblBackupTo.setText("BACKUP 0 TO 4");
		
		ProgressBar progressBar = new ProgressBar(shlCreatedskFrom, SWT.BORDER);
		progressBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		progressBar.setSelection(40);
		progressBar.setBounds(44, 166, 341, 17);
		
		Label lblProgress = new Label(shlCreatedskFrom, SWT.NONE);
		lblProgress.setBounds(43, 139, 201, 15);
		lblProgress.setText("Progress:");

	}
}
