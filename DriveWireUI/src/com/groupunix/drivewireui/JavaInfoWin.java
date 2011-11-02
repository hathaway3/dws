package com.groupunix.drivewireui;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class JavaInfoWin extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text textInfo;
	Clipboard cb;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public JavaInfoWin(Shell parent, int style) {
		super(parent, style);
		setText("Java environment info");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		loadInfo();
		
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		cb = new Clipboard(display);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void loadInfo() 
	{
		String jitmp = new String();
		for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
			jitmp += e +"\n";
		}
		getTextInfo().setText(jitmp);
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(606, 517);
		shell.setText(getText());
		shell.setLayout(null);
		
		textInfo = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		textInfo.setBounds(10, 10, 580, 432);
		
		Button btnClose = new Button(shell, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				e.display.getActiveShell().close();
			}
		});
		btnClose.setBounds(258, 454, 75, 25);
		btnClose.setText("Close");
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
	
					String textData = textInfo.getText();
					if (textData.length() > 0) {
						TextTransfer textTransfer = TextTransfer.getInstance();
						cb.setContents(new Object[]{textData}, new Transfer[]{textTransfer});
					}

				
			}
		});
		btnNewButton.setBounds(10, 454, 157, 25);
		btnNewButton.setText("Copy to clipboard");

	}
	protected Text getTextInfo() {
		return textInfo;
	}
}
