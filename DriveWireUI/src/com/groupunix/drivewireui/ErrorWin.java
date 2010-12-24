package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import com.swtdesigner.SWTResourceManager;

public class ErrorWin extends Dialog {

	protected Object result;
	protected Shell shlAnErrorHas;
	private Text txtDetail;
	private Text txtSummary;

	private String title;
	private String summary;
	private String detail;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ErrorWin(Shell parent, int style, String title, String summary, String detail) {
		super(parent, style);
		this.title = title;
		this.summary = summary;
		this.detail = detail;
		
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlAnErrorHas.open();
		shlAnErrorHas.layout();
		Display display = getParent().getDisplay();
		while (!shlAnErrorHas.isDisposed()) {
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
		shlAnErrorHas = new Shell(getParent(), getStyle());
		shlAnErrorHas.setSize(441, 293);
		shlAnErrorHas.setText(title);
		
		Button btnClose = new Button(shlAnErrorHas, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlAnErrorHas.close();
			
			}
		});
		btnClose.setBounds(173, 230, 75, 25);
		btnClose.setText("Close");
		
		txtDetail = new Text(shlAnErrorHas, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtDetail.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		txtDetail.setEditable(false);
		txtDetail.setBounds(21, 60, 392, 156);
		txtDetail.setText(detail);
		
		txtSummary = new Text(shlAnErrorHas, SWT.NONE);
		txtSummary.setEditable(false);
		txtSummary.setBounds(21, 20, 392, 21);
		txtSummary.setText(summary);
	}

}
