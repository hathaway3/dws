package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import com.swtdesigner.SWTResourceManager;

public class UIConfigWin extends Dialog {

	protected Object result;
	protected Shell shlUserInterfaceConfiguration;
	protected Display display;
	private Text textMainFont;
	private Text textLogFont;
	private Spinner spinnerDiskHist;
	private Spinner spinnerServerHist;
	private Text textTCPTimeout;
	private Text textDialogFont;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public UIConfigWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		loadSettings();
		
		
		shlUserInterfaceConfiguration.open();
		shlUserInterfaceConfiguration.layout();
		display = getParent().getDisplay();
		while (!shlUserInterfaceConfiguration.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}


	private void loadSettings()
	{
		setFonts();
		
		this.spinnerDiskHist.setSelection(MainWin.config.getInt("DiskHistorySize",MainWin.default_DiskHistorySize));
		this.spinnerServerHist.setSelection(MainWin.config.getInt("ServerHistorySize",MainWin.default_ServerHistorySize));
		
		this.textTCPTimeout.setText(MainWin.config.getInt("TCPTimeout",MainWin.default_TCPTimeout) +"");
		
	}
	
	private void setFonts() 
	{
		FontData fd = new FontData(MainWin.config.getString("MainFont", MainWin.default_MainFont), MainWin.config.getInt("MainFontSize", MainWin.default_MainFontSize), MainWin.config.getInt("MainFontStyle", MainWin.default_MainFontStyle) );
		textMainFont.setFont(new Font(display, fd));
		textMainFont.setText(fd.getName() + " " + fd.getHeight());
		
		fd = new FontData(MainWin.config.getString("LogFont", MainWin.default_LogFont), MainWin.config.getInt("LogFontSize", MainWin.default_LogFontSize), MainWin.config.getInt("LogFontStyle", MainWin.default_LogFontStyle) );
		textLogFont.setFont(new Font(display, fd));
		textLogFont.setText(fd.getName() + " " + fd.getHeight());
		
		fd = new FontData(MainWin.config.getString("DialogFont", MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		textDialogFont.setFont(new Font(display, fd));
		textDialogFont.setText(fd.getName() + " " + fd.getHeight());
		
		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlUserInterfaceConfiguration = new Shell(getParent(), getStyle());
		shlUserInterfaceConfiguration.setSize(380, 327);
		shlUserInterfaceConfiguration.setText("User Interface Configuration");
		
		spinnerDiskHist = new Spinner(shlUserInterfaceConfiguration, SWT.BORDER);
		spinnerDiskHist.setMaximum(999);
		spinnerDiskHist.setBounds(153, 145, 47, 22);
		
		spinnerServerHist = new Spinner(shlUserInterfaceConfiguration, SWT.BORDER);
		spinnerServerHist.setMaximum(999);
		spinnerServerHist.setBounds(153, 173, 47, 22);
		
		Label lblDiskPathHistory = new Label(shlUserInterfaceConfiguration, SWT.NONE);
		lblDiskPathHistory.setAlignment(SWT.RIGHT);
		lblDiskPathHistory.setBounds(30, 148, 117, 15);
		lblDiskPathHistory.setText("Disk path history:");
		
		Label lblServerHistory = new Label(shlUserInterfaceConfiguration, SWT.NONE);
		lblServerHistory.setAlignment(SWT.RIGHT);
		lblServerHistory.setBounds(40, 176, 107, 15);
		lblServerHistory.setText("Server history:");
		
		Button btnOk = new Button(shlUserInterfaceConfiguration, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applySettings();
				
				shlUserInterfaceConfiguration.close();
			}
		});
		btnOk.setBounds(154, 264, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlUserInterfaceConfiguration, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlUserInterfaceConfiguration.close();
			}
		});
		btnCancel.setBounds(289, 264, 75, 25);
		btnCancel.setText("Cancel");
		
		Button btnUndo = new Button(shlUserInterfaceConfiguration, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				loadSettings();
			}
		});
		btnUndo.setBounds(10, 264, 75, 25);
		btnUndo.setText("Undo");
		
		Button btnSet = new Button(shlUserInterfaceConfiguration, SWT.NONE);
		btnSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
		        FontDialog dialog = new FontDialog(shlUserInterfaceConfiguration, SWT.NONE);
		        
		        FontData newFont = dialog.open();
		        if (newFont == null)
		          return;
		        
		        textMainFont.setFont(new Font(display, newFont));
		        textMainFont.setText(newFont.getName() + " " + newFont.getHeight());
			}
		});
		btnSet.setBounds(283, 64, 47, 25);
		btnSet.setText("Set...");
		
		Label lblMainFont = new Label(shlUserInterfaceConfiguration, SWT.NONE);
		lblMainFont.setAlignment(SWT.RIGHT);
		lblMainFont.setBounds(10, 69, 89, 15);
		lblMainFont.setText("Terminal font:");
		
		textMainFont = new Text(shlUserInterfaceConfiguration, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		textMainFont.setBounds(105, 66, 177, 23);
		
		Label lblLogFont = new Label(shlUserInterfaceConfiguration, SWT.NONE);
		lblLogFont.setText("Log font:");
		lblLogFont.setAlignment(SWT.RIGHT);
		lblLogFont.setBounds(30, 100, 69, 15);
		
		textLogFont = new Text(shlUserInterfaceConfiguration, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		textLogFont.setBounds(105, 97, 177, 23);
		
		Button button = new Button(shlUserInterfaceConfiguration, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
        FontDialog dialog = new FontDialog(shlUserInterfaceConfiguration, SWT.NONE);
		        
		        FontData newFont = dialog.open();
		        if (newFont == null)
		          return;
		        
		        textLogFont.setFont(new Font(display, newFont));
		        textLogFont.setText(newFont.getName() + " " + newFont.getHeight());
	
			
			}
		});
		button.setText("Set...");
		button.setBounds(283, 95, 47, 25);
		
		textTCPTimeout = new Text(shlUserInterfaceConfiguration, SWT.BORDER);
		textTCPTimeout.setBounds(153, 216, 76, 21);
		
		Label lblServerTcpResponse = new Label(shlUserInterfaceConfiguration, SWT.NONE);
		lblServerTcpResponse.setAlignment(SWT.RIGHT);
		lblServerTcpResponse.setBounds(10, 219, 137, 15);
		lblServerTcpResponse.setText("Server response timeout:");
		
		Label lblMs = new Label(shlUserInterfaceConfiguration, SWT.NONE);
		lblMs.setBounds(235, 180, 47, 15);
		lblMs.setText("ms");
		
		Label lblDialogFont = new Label(shlUserInterfaceConfiguration, SWT.NONE);
		lblDialogFont.setText("Dialog font:");
		lblDialogFont.setAlignment(SWT.RIGHT);
		lblDialogFont.setBounds(30, 38, 69, 15);
		
		textDialogFont = new Text(shlUserInterfaceConfiguration, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		textDialogFont.setText("<dynamic> 0");
		textDialogFont.setFont(SWTResourceManager.getFont("<dynamic>", 0, SWT.NORMAL));
		textDialogFont.setBounds(105, 35, 177, 23);
		
		Button button_1 = new Button(shlUserInterfaceConfiguration, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				 FontDialog dialog = new FontDialog(shlUserInterfaceConfiguration, SWT.NONE);
			        
			        FontData newFont = dialog.open();
			        if (newFont == null)
			          return;
			        
			        textDialogFont.setFont(new Font(display, newFont));
			        textDialogFont.setText(newFont.getName() + " " + newFont.getHeight());
		
			
			
			}
		});
		button_1.setText("Set...");
		button_1.setBounds(283, 33, 47, 25);

	}

	protected void applySettings() 
	{
		MainWin.setFont(textMainFont.getFont().getFontData()[0]);
		MainWin.setLogFont(textLogFont.getFont().getFontData()[0]);
		MainWin.setDialogFont(textDialogFont.getFont().getFontData()[0]);
		
		
		MainWin.config.setProperty("DiskHistorySize", this.spinnerDiskHist.getSelection());
		MainWin.config.setProperty("ServerHistorySize", this.spinnerServerHist.getSelection());
		
		if (UIUtils.validateNum(this.textTCPTimeout.getText(), 0))
				MainWin.config.setProperty("TCPTimeout", this.textTCPTimeout.getText());
		
	}
}
