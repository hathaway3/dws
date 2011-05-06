package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.swtdesigner.SWTResourceManager;

public class ErrorWin extends Dialog {

	protected Object result;
	protected static Shell shlAnErrorHas;
	private Text txtDetail;
	private Text txtSummary;

	private Button btnClose;
	private Button btnDetail;
	
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
		applyFont();
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

	
	private static void applyFont() 
	{
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		Control[] controls = shlAnErrorHas.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlAnErrorHas.getDisplay(), f));
		}
		
	}
	
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlAnErrorHas = new Shell(getParent(), getStyle());
		shlAnErrorHas.setSize(441, 119);
		shlAnErrorHas.setText(title);
		
		btnClose = new Button(shlAnErrorHas, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlAnErrorHas.close();
			
			}
		});
		btnClose.setBounds(187, 57, 75, 25);
		btnClose.setText("Close");
		
		
		
		txtSummary = new Text(shlAnErrorHas, SWT.NONE);
		txtSummary.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		txtSummary.setEditable(false);
		txtSummary.setBounds(21, 20, 392, 21);
		txtSummary.setText(summary);
		
		btnDetail = new Button(shlAnErrorHas, SWT.NONE);
		btnDetail.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlAnErrorHas.setSize(441, 500);
				
				txtDetail = new Text(shlAnErrorHas, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
				txtDetail.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				txtDetail.setEditable(false);
				txtDetail.setBounds(21, 40, 385, 390);
				txtDetail.setText(detail);
			
				btnClose.setBounds(187, 440, 75, 25);
				btnDetail.setBounds(21,440,85,25);
				btnDetail.setVisible(false);
				
			}
		});
		btnDetail.setBounds(21, 57, 85, 25);
		btnDetail.setText("Show Detail");
	}
}
