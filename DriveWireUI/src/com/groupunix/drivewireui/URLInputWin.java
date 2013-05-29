package com.groupunix.drivewireui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class URLInputWin extends Dialog {

	protected String result = null;
	protected Shell shlEnterURL;
	private Combo cmbURL;
	private int diskno;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public URLInputWin(Shell parent, int diskno) 
	{
		super(parent, SWT.DIALOG_TRIM);
		this.diskno = diskno;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public String open() {
		createContents();
		
		loadURLHistory();
		
		shlEnterURL.open();
		shlEnterURL.layout();
		Display display = getParent().getDisplay();
		
		int x = getParent().getBounds().x + (getParent().getBounds().width / 2) - (shlEnterURL.getBounds().width / 2);
		int y = getParent().getBounds().y + (getParent().getBounds().height / 2) - (shlEnterURL.getBounds().height / 2);
		
		shlEnterURL.setLocation(x, y);
		
		while (!shlEnterURL.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	

	private void loadURLHistory()
	{
		if (MainWin.getDiskHistory() != null)
		{
			List<String> dhist = MainWin.getDiskHistory();
			for (String d : dhist)
			{
				cmbURL.add(d, 0);
			}
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlEnterURL = new Shell(getParent(), getStyle());
		shlEnterURL.setSize(452, 151);
		shlEnterURL.setText("Enter URL for disk image...");
		
		Button btnOk = new Button(shlEnterURL, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				result = cmbURL.getText();
				e.display.getActiveShell().close();
			}
		});
		btnOk.setBounds(248, 88, 82, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlEnterURL, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				e.display.getActiveShell().close();
			
			}
		});
		btnCancel.setBounds(350, 88, 75, 25);
		btnCancel.setText("Cancel");
		
		cmbURL = new Combo(shlEnterURL, SWT.NONE);

		cmbURL.setBounds(22, 40, 403, 23);
		
		Label lblEnterURL = new Label(shlEnterURL, SWT.NONE);
		lblEnterURL.setBounds(22, 19, 275, 15);
		lblEnterURL.setText("Enter a URL to load image for drive " + diskno + " from:");
		
	}
}
