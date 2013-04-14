package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class CreateDiskWin extends Dialog {

	protected Object result;
	protected static Shell shlCreateANew;
	private Text textPath;
	private Spinner spinnerDrive;
	private Button btnFile;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CreateDiskWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		applyFont();
		
		shlCreateANew.open();
		shlCreateANew.layout();
		Display display = getParent().getDisplay();
		
		int x = getParent().getBounds().x + (getParent().getBounds().width / 2) - (shlCreateANew.getBounds().width / 2);
		int y = getParent().getBounds().y + (getParent().getBounds().height / 2) - (shlCreateANew.getBounds().height / 2);
		
		shlCreateANew.setLocation(x, y);
		
		Label lblNewLabel = new Label(shlCreateANew, SWT.NONE);
		lblNewLabel.setImage(SWTResourceManager.getImage(CreateDiskWin.class, "/wizard/new-disk-32.png"));
		lblNewLabel.setBounds(319, 18, 32, 32);
		
		if (MainWin.getCurrentDiskNo() > -1)
		{
			this.spinnerDrive.setSelection(MainWin.getCurrentDiskNo());
		}
		
		while (!shlCreateANew.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	
	private static void applyFont() 
	{
		/*
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		Control[] controls = shlCreateANew.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlCreateANew.getDisplay(), f));
		}
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlCreateANew.getDisplay(), f));
		}
	*/	
	}
	
	private void createContents() {
		shlCreateANew = new Shell(getParent(), getStyle());
		shlCreateANew.setSize(380, 206);
		shlCreateANew.setText("Create a new disk image...");
		shlCreateANew.setLayout(null);
		
		spinnerDrive = new Spinner(shlCreateANew, SWT.BORDER);
		spinnerDrive.setBounds(202, 26, 47, 22);
		spinnerDrive.setMaximum(255);
		
		Label lblInsertNewDisk = new Label(shlCreateANew, SWT.NONE);
		lblInsertNewDisk.setBounds(22, 29, 174, 19);
		lblInsertNewDisk.setAlignment(SWT.RIGHT);
		lblInsertNewDisk.setText("Create new disk for drive:");
		
		Label lblPath = new Label(shlCreateANew, SWT.NONE);
		lblPath.setBounds(22, 68, 277, 19);
		lblPath.setText("File for new disk image (optional):");
		
		textPath = new Text(shlCreateANew, SWT.BORDER);
		textPath.setBounds(21, 89, 298, 21);
		
		if (MainWin.getInstanceConfig().containsKey("LocalDiskDir"))
			textPath.setText(MainWin.getInstanceConfig().getString("LocalDiskDir") + System.getProperty("file.separator"));
		
		btnFile = new Button(shlCreateANew, SWT.NONE);
		btnFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				final String curpath = textPath.getText();
				
				//SwingUtilities.invokeLater(new Runnable() 
				//{
						
				//		public void run() 
						{
							final String filename = MainWin.getFile(true, false, curpath, "File for new disk image..", "Create");
								// 	check if a file was selected
							if (filename != null)
							{
								if (!textPath.isDisposed())
								{
									shlCreateANew.getDisplay().asyncExec(new Runnable() 
									{
										
										public void run() 
										{
											textPath.setText(filename);
										}
									});
								}
							}
						}
					
			//	});
			
			}
		});
		btnFile.setBounds(322, 88, 29, 23);
		btnFile.setText("...");
		
		Button btnCreate = new Button(shlCreateANew, SWT.NONE);
		btnCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw disk create " + spinnerDrive.getSelection() + " " + textPath.getText());
					
				e.display.getActiveShell().close();
				
			}
		});
		btnCreate.setBounds(141, 137, 90, 30);
		btnCreate.setText("Create");
		
		Button btnCancel = new Button(shlCreateANew, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setBounds(276, 137, 75, 30);
		btnCancel.setText("Cancel");

	}

	protected Spinner getSpinner() {
		return spinnerDrive;
	}
	protected Button getBtnFile() {
		return btnFile;
	}
}
