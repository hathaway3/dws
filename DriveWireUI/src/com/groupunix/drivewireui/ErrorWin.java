package com.groupunix.drivewireui;

import java.util.Random;

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
import org.eclipse.swt.widgets.Label;

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

		Display display = getParent().getDisplay();
		
		int x = getParent().getBounds().x + (getParent().getBounds().width / 2) - (shlAnErrorHas.getBounds().width / 2);
		int y = getParent().getBounds().y + (getParent().getBounds().height / 2) - (shlAnErrorHas.getBounds().height / 2);
		
		shlAnErrorHas.setLocation(x, y);
		
		shlAnErrorHas.open();
		shlAnErrorHas.layout();
		
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
		shlAnErrorHas.setSize(419, 274);
		shlAnErrorHas.setText(title);
		
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		btnClose = new Button(shlAnErrorHas, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
			
				e.display.getActiveShell().close();
			
			}
		});
		btnClose.setBounds(327, 211, 75, 25);
		btnClose.setText("Close");
		
		
		
		txtSummary = new Text(shlAnErrorHas, SWT.READ_ONLY | SWT.WRAP);
		txtSummary.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		txtSummary.setEditable(false);
		txtSummary.setBounds(10, 20, 329, 42);
		txtSummary.setText(summary);
		txtSummary.setFont(new Font(shlAnErrorHas.getDisplay(), f));
		
		textDetail = new Text(shlAnErrorHas, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		textDetail.setEditable(false);
		textDetail.setBounds(10, 67, 392, 127);
		textDetail.setText(detail);
		textDetail.setFont(new Font(shlAnErrorHas.getDisplay(), f));
		
		Button btnSubmitABug = new Button(shlAnErrorHas, SWT.NONE);
		btnSubmitABug.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(ErrorWin.class, "/bug.png"));
		btnSubmitABug.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				BugReportWin brwin = new BugReportWin(shlAnErrorHas,SWT.DIALOG_TRIM,title,summary,detail);
				brwin.open();
			
			}
		});
		btnSubmitABug.setBounds(10, 211, 185, 25);
		btnSubmitABug.setText("Submit a bug report...");
		
		Label lblNewLabel = new Label(shlAnErrorHas, SWT.NONE);
		Random rand = new Random();
		lblNewLabel.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(ErrorWin.class, "/a" + rand.nextInt(22) + ".png"));
		lblNewLabel.setBounds(353, 10, 48, 48);
	}
}
