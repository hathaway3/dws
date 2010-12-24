package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class CopyDiskToDSKWin extends Dialog {

	protected Object result;
	protected Shell shlCreatedskFrom;
	private Text text;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CopyDiskToDSKWin(Shell parent, int style) {
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
		shlCreatedskFrom = new Shell(getParent(), getStyle());
		shlCreatedskFrom.setSize(450, 300);
		shlCreatedskFrom.setText("Create .DSK from floppy disk");
		
		Label lblThisWizardWill = new Label(shlCreatedskFrom, SWT.NONE);
		lblThisWizardWill.setBounds(10, 27, 424, 30);
		lblThisWizardWill.setText("This wizard will help you create an image (.dsk file) from a floppy disk.");
		
		Label lblPleaseChooseA = new Label(shlCreatedskFrom, SWT.NONE);
		lblPleaseChooseA.setBounds(10, 63, 372, 21);
		lblPleaseChooseA.setText("Please choose a filename for the .dsk image we are going to create:");
		
		text = new Text(shlCreatedskFrom, SWT.BORDER);
		text.setBounds(20, 86, 343, 21);
		
		Button button = new Button(shlCreatedskFrom, SWT.NONE);
		button.setBounds(364, 84, 31, 25);
		button.setText("...");
		
		Composite composite = new Composite(shlCreatedskFrom, SWT.NONE);
		composite.setBounds(20, 153, 375, 56);
		
		Button btnADecbDisk = new Button(composite, SWT.RADIO);
		btnADecbDisk.setBounds(10, 10, 294, 16);
		btnADecbDisk.setText("A DECB disk, using HDBDOS's BACKUP command");
		
		Button btnAnOsDisk = new Button(composite, SWT.RADIO);
		btnAnOsDisk.setBounds(10, 32, 252, 16);
		btnAnOsDisk.setText("An OS9 disk, using the NitrOS9 dd utility");
		
		Label lblWhatTypeOf = new Label(shlCreatedskFrom, SWT.NONE);
		lblWhatTypeOf.setBounds(10, 132, 353, 15);
		lblWhatTypeOf.setText("What type of disk will we be using as the source?");
		
		Button btnNext = new Button(shlCreatedskFrom, SWT.NONE);
		btnNext.setBounds(169, 237, 75, 25);
		btnNext.setText("Next >>");
		
		Button btnCancel = new Button(shlCreatedskFrom, SWT.NONE);
		btnCancel.setBounds(359, 237, 75, 25);
		btnCancel.setText("Cancel");

	}

}
