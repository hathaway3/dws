package com.groupunix.drivewireui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConnectWin extends Dialog {
	
	protected Object result;
	protected Shell shell;
	private Text text;
	private Text text_1;


	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ConnectWin(Shell parent, int style) {
		super(parent, style);
		setText("Connect to...");
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
		shell.setSize(298, 155);
		shell.setText(getText());
		
		text = new Text(shell, SWT.BORDER);
		text.setText("127.0.0.1");
		text.setBounds(50, 21, 227, 21);
		
		Label lblHost = new Label(shell, SWT.NONE);
		lblHost.setBounds(10, 24, 34, 15);
		lblHost.setText("Host:");
		
		Label lblPort = new Label(shell, SWT.NONE);
		lblPort.setBounds(10, 51, 34, 15);
		lblPort.setText("Port:");
		
		text_1 = new Text(shell, SWT.BORDER);
		text_1.setText("6800");
		text_1.setBounds(50, 48, 45, 21);
		
		Button btnConnect = new Button(shell, SWT.NONE);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// validate -- wish i could figure out the right way to do this
				
				boolean doit = true;
				String nogo = "Unknown error";
				int port = 6800;
				
				try 
				{
					port = Integer.parseInt(text_1.getText());
				
					if ((port < 1) || (port > 65535))
					{
						text_1.setText("6800");
						doit = false;
						nogo = "Valid port range is 1 - 65535";
					}
				}
				catch (NumberFormatException er)
				{
					text_1.setText("6800");
					doit = false;
					nogo = "Port number must be numeric";
				}
				
				try 
				{
					InetAddress.getByName(text.getText());
				} 
				catch (UnknownHostException uhe) 
				{
					doit = false;
					nogo = "'" + text.getText() + "' could not be resolved to an IP address.";
				}
				
				if (!doit)
				{
					ConnectErrorWin window = new ConnectErrorWin(shell, SWT.DIALOG_TRIM);
					window.open(nogo);
				}
				else
				{
					MainWin.openConnection(text.getText(),port);
					shell.close();
				}
				
			}
		});
		btnConnect.setBounds(106, 90, 75, 25);
		btnConnect.setText("Connect");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shell.close();
				
			}
		});
		btnCancel.setBounds(202, 90, 75, 25);
		btnCancel.setText("Cancel");
	

	}


	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		return bindingContext;
	}
}
