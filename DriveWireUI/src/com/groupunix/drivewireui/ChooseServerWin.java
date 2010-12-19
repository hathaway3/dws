package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ChooseServerWin extends Dialog {

	protected Object result;
	protected Shell shlChooseServer;
	private Text txtPort;
	private Combo cmbHost;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ChooseServerWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlChooseServer.open();
		shlChooseServer.layout();
		Display display = getParent().getDisplay();
		while (!shlChooseServer.isDisposed()) {
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
		shlChooseServer = new Shell(getParent(), getStyle());
		shlChooseServer.setSize(326, 154);
		shlChooseServer.setText("Choose Server...");
		
		Button btnOk = new Button(shlChooseServer, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// TODO save recent hosts?
				MainWin.setHost(cmbHost.getText());
				MainWin.setPort(txtPort.getText());
				
				shlChooseServer.close();
			}
		});
		btnOk.setBounds(120, 91, 82, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlChooseServer, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlChooseServer.close();
			
			}
		});
		btnCancel.setBounds(235, 91, 75, 25);
		btnCancel.setText("Cancel");
		
		txtPort = new Text(shlChooseServer, SWT.BORDER);
		txtPort.setText(MainWin.getPort() + "");
		txtPort.setBounds(67, 50, 60, 21);
		
		Label lblHost = new Label(shlChooseServer, SWT.NONE);
		lblHost.setAlignment(SWT.RIGHT);
		lblHost.setBounds(23, 24, 38, 15);
		lblHost.setText("Host:");
		
		Label lblPort = new Label(shlChooseServer, SWT.NONE);
		lblPort.setAlignment(SWT.RIGHT);
		lblPort.setBounds(29, 53, 32, 15);
		lblPort.setText("Port:");
		
		cmbHost = new Combo(shlChooseServer, SWT.NONE);
		cmbHost.setBounds(67, 21, 214, 23);
		cmbHost.setText(MainWin.getHost());

	}
}
