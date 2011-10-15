package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

public class ErrorWin extends Dialog {

	protected Object result;
	protected Shell shlAnErrorHas;

	private Text txtSummary;

	private Button btnClose;
	
	private String title;
	private String summary;
	private String detail;
	private Text textDetail;
	
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
		shlAnErrorHas.setSize(441, 274);
		shlAnErrorHas.setText(title);
		
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		btnClose = new Button(shlAnErrorHas, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
			
				shlAnErrorHas.close();
			
			}
		});
		btnClose.setBounds(338, 211, 75, 25);
		btnClose.setText("Close");
		
		
		
		txtSummary = new Text(shlAnErrorHas, SWT.WRAP);
		txtSummary.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		txtSummary.setEditable(false);
		txtSummary.setBounds(21, 20, 392, 42);
		txtSummary.setText(summary);
		txtSummary.setFont(new Font(shlAnErrorHas.getDisplay(), f));
		
		textDetail = new Text(shlAnErrorHas, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		textDetail.setEditable(false);
		textDetail.setBounds(21, 60, 392, 134);
		textDetail.setText(detail);
		textDetail.setFont(new Font(shlAnErrorHas.getDisplay(), f));
		
		Button btnSubmitABug = new Button(shlAnErrorHas, SWT.NONE);
		btnSubmitABug.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				BugReportWin brwin = new BugReportWin(shlAnErrorHas,SWT.DIALOG_TRIM,title,summary,detail);
				brwin.open();
			
			}
		});
		btnSubmitABug.setBounds(21, 211, 158, 25);
		btnSubmitABug.setText("Submit a bug report...");
	}
}
