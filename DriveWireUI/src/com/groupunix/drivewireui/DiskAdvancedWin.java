package com.groupunix.drivewireui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DiskAdvancedWin extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text textSizeLimit;
	private Text textOffset;


	private Button btnWriteProtect;
	private Button btnSyncToSource;
	private Button btnSyncFromSource;
	private Button btnExpand;
	private Button btnSizeLimit;
	private Label lblSizeLimit;
	private Button btnNamedObject;
	private Button btnOffset;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DiskAdvancedWin(Shell parent, int style) {
		super(parent, style);
		setText("Advanced disk settings");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		applySettings();
		
		
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

	
	private void toggleStuff()
	{
		if (MainWin.getCurrentDisk().getSizelimit() > -1)
		{
			this.btnSizeLimit.setSelection(true);
			this.textSizeLimit.setEnabled(true);
		}
		else
		{
			this.btnSizeLimit.setSelection(false);
			this.textSizeLimit.setEnabled(false);
		}
		
		if (MainWin.getCurrentDisk().getOffset() != 0)
		{
			this.btnOffset.setSelection(true);
			this.textOffset.setEnabled(true);
		}
		else
		{
			this.btnOffset.setSelection(false);
			this.textOffset.setEnabled(false);
		}
	}
	

	private void applySettings() 
	{


		MainWin.loadSelectedDiskDetails();
		
		this.textSizeLimit.setText(MainWin.getCurrentDisk().getSizelimit() + "");

		
		this.textOffset.setText(MainWin.getCurrentDisk().getOffset() + "");

		
		this.btnWriteProtect.setSelection(MainWin.getCurrentDisk().isWriteprotect());
		this.btnSyncToSource.setSelection(MainWin.getCurrentDisk().isSync());
		this.btnSyncFromSource.setSelection(MainWin.getCurrentDisk().isSyncfromsource());
		this.btnExpand.setSelection(MainWin.getCurrentDisk().isExpand());
		this.btnNamedObject.setSelection(MainWin.getCurrentDisk().isNamedobject());
		
		toggleStuff();
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(597, 485);
		shell.setText(getText());
		
		Button btnUndo = new Button(shell, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applySettings();
			}
		});
		btnUndo.setBounds(10, 422, 75, 25);
		btnUndo.setText("Undo");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shell.close();
			}
		});
		btnCancel.setBounds(506, 422, 75, 25);
		btnCancel.setText("Cancel");
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (verifySettings())
				{
					if (saveSettings());
					{
						shell.close();
					}
				}
			}
		});
		btnOk.setBounds(265, 422, 75, 25);
		btnOk.setText("Ok");
		
		btnWriteProtect = new Button(shell, SWT.CHECK);
		btnWriteProtect.setBounds(24, 28, 146, 16);
		btnWriteProtect.setText("Write protect disk");
		
		btnSyncToSource = new Button(shell, SWT.CHECK);
		btnSyncToSource.setText("Sync to source");
		btnSyncToSource.setBounds(24, 59, 146, 16);
		
		btnSyncFromSource = new Button(shell, SWT.CHECK);
		btnSyncFromSource.setText("Sync from source");
		btnSyncFromSource.setBounds(24, 133, 146, 16);
		
		btnExpand = new Button(shell, SWT.CHECK);
		btnExpand.setText("Allow expansion");
		btnExpand.setBounds(22, 199, 146, 16);
		
		btnNamedObject = new Button(shell, SWT.CHECK);
		btnNamedObject.setText("Disk is a named object");
		btnNamedObject.setBounds(24, 366, 146, 16);
		
		btnSizeLimit = new Button(shell, SWT.CHECK);
		btnSizeLimit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (btnSizeLimit.getSelection())
				{
					textSizeLimit.setEnabled(true);
				}
				else
				{
					textSizeLimit.setEnabled(false);
				}
			}
		});
		btnSizeLimit.setText("Limit size of image");
		btnSizeLimit.setBounds(24, 245, 146, 16);
		
		textSizeLimit = new Text(shell, SWT.BORDER);
	
		textSizeLimit.setBounds(77, 267, 64, 21);
		
		lblSizeLimit = new Label(shell, SWT.NONE);
		lblSizeLimit.setAlignment(SWT.RIGHT);
		lblSizeLimit.setBounds(10, 270, 63, 15);
		lblSizeLimit.setText("Size limit:");
		
		Label lblOffset = new Label(shell, SWT.NONE);
		lblOffset.setAlignment(SWT.RIGHT);
		lblOffset.setText("Offset:");
		lblOffset.setBounds(24, 328, 49, 15);
		
		textOffset = new Text(shell, SWT.BORDER);
		textOffset.setBounds(77, 326, 64, 21);
		
		Label lblW = new Label(shell, SWT.NONE);
		lblW.setBounds(176, 28, 391, 25);
		lblW.setText("If enabled, any attempt to write to this image will return an error.");
		
		Label lblIfDisabledWrites = new Label(shell, SWT.NONE);
		lblIfDisabledWrites.setText("If disabled, writes will succeed but changes  will not be written back\r\nto the source image.  The modified in-memory image can be written \r\nto the source or an alternate path at any time using the \"Write to..\" \r\nbutton or 'dw disk write' command.");
		lblIfDisabledWrites.setBounds(176, 60, 391, 60);
		
		Label lblIfEnabledThe = new Label(shell, SWT.NONE);
		lblIfEnabledThe.setText("If enabled, the server will attempt to detect changes in the source \r\nimage and update it's in-memory image automatically.  The in-memory \r\nimage will overwrite the source image in the event of a conflict.");
		lblIfEnabledThe.setBounds(174, 133, 391, 60);
		
		Label lblIfEnabledThe_1 = new Label(shell, SWT.NONE);
		lblIfEnabledThe_1.setText("If enabled, the server will allow reads and write beyond the current size\r\nof the source image, expanding the image as neccessary.\r\n");
		lblIfEnabledThe_1.setBounds(174, 199, 391, 40);
		
		Label lblIfEnabledThe_2 = new Label(shell, SWT.NONE);
		lblIfEnabledThe_2.setText("If enabled, read or writes beyond the specified sector will return an error,\r\neven if they exist in the source image. \r\n");
		lblIfEnabledThe_2.setBounds(174, 245, 391, 40);
		
		btnOffset = new Button(shell, SWT.CHECK);
		btnOffset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (btnOffset.getSelection())
				{
					textOffset.setEnabled(true);
				}
				else
				{
					textOffset.setEnabled(false);
				}
				
			}
		});
		btnOffset.setText("Use R/W offset");
		btnOffset.setBounds(24, 306, 146, 16);
		
		Label lblIfEnabledAll = new Label(shell, SWT.NONE);
		lblIfEnabledAll.setText("If enabled, all read and write operations will be shifted by the specified \r\nnumber of sectors.  Any request for sector X will be done to sector \r\n(X + offset) in the image.  \r\n");
		lblIfEnabledAll.setBounds(176, 306, 391, 54);
		
		Label lblIfEnabledThis = new Label(shell, SWT.NONE);
		lblIfEnabledThis.setText("If enabled, this disk will be available to requests made from software\r\nlike CoCoBoot which uses named object operations.\r\n");
		lblIfEnabledThis.setBounds(176, 366, 391, 40);

	}

	protected boolean saveSettings() 
	{
		boolean res = true;
		
		MainWin.getCurrentDisk().setExpand(this.btnExpand.getSelection());
		MainWin.getCurrentDisk().setNamedobject(this.btnNamedObject.getSelection());
		MainWin.getCurrentDisk().setSync(this.btnSyncToSource.getSelection());
		MainWin.getCurrentDisk().setSyncfromsource(this.btnSyncFromSource.getSelection());
		MainWin.getCurrentDisk().setWriteprotect(this.btnWriteProtect.getSelection());
		
		if (this.btnOffset.getSelection())
		{
			MainWin.getCurrentDisk().setOffset(Integer.parseInt(this.textOffset.getText()));
		}
		else
		{
			MainWin.getCurrentDisk().setOffset(0);
		}
		
		if (this.btnSizeLimit.getSelection())
		{
			MainWin.getCurrentDisk().setSizelimit(Integer.parseInt(this.textSizeLimit.getText()));
		}
		else
		{
			MainWin.getCurrentDisk().setSizelimit(-1);
		}
		
		try 
		{
			UIUtils.writeDiskDef(MainWin.getInstance(), MainWin.getCurrentDisk().getDrive(), MainWin.getCurrentDisk());
		} 
		catch (IOException e) 
		{
			MainWin.showError("Error saving disk settings", "An I/O error occurred" , e.getMessage());
			return false;
		} 
		catch (DWUIOperationFailedException e) 
		{
			MainWin.showError("Error saving disk settings", "An DW error occurred" , e.getMessage());
			return false;
		}
		
		return(res);
	}

	protected boolean verifySettings() 
	{
		try 
		{
			UIUtils.getDWConfigSerial();
		} 
		catch (IOException e) 
		{
			MainWin.showError("Error validating data", "An I/O error occurred" , e.getMessage());
			return false;
		} 
		catch (DWUIOperationFailedException e) 
		{
			MainWin.showError("Error validating data", "A DW error occurred" , e.getMessage());
			return false;
		}
		
		if (this.btnOffset.getSelection())
		{	
			if (!UIUtils.validateNum(this.textOffset.getText(),(MainWin.dwconfig.getInt("DiskMaxSectors",16777215) * -1),MainWin.dwconfig.getInt("DiskMaxSectors",16777215)))
			{
				MainWin.showError("Invalid value entered", "Data entered for Offset is not valid" , "Valid range is " + (MainWin.dwconfig.getInt("DiskMaxSectors",16777215) * -1) + " to " + MainWin.dwconfig.getInt("DiskMaxSectors",16777215));
				return false;
			}
		}
		
		
		if (this.btnSizeLimit.getSelection())
		{
			if (!UIUtils.validateNum(this.textSizeLimit.getText(),-1,MainWin.dwconfig.getInt("DiskMaxSectors",16777215)))
			{
				MainWin.showError("Invalid value entered", "Data entered for Size Limit is not valid" , "Valid range is -1 to " + MainWin.dwconfig.getInt("DiskMaxSectors",16777215));
				return false;
			}
		}
		
		return true;
	}

	protected Button getBtnWriteProtectDisk() {
		return btnWriteProtect;
	}
	protected Button getBtnSyncToSource() {
		return btnSyncToSource;
	}
	protected Button getBtnSyncFromSource() {
		return btnSyncFromSource;
	}
	protected Button getBtnAllowImageTo() {
		return btnExpand;
	}
	protected Button getBtnLimitSizeOf() {
		return btnSizeLimit;
	}
	protected Label getLblSizeLimit() {
		return lblSizeLimit;
	}
	protected Button getBtnNamedObject() {
		return btnNamedObject;
	}
	protected Text getTextOffset() {
		return textOffset;
	}
	protected Button getBtnOffset() {
		return btnOffset;
	}
	protected Text getTextSizeLimit() {
		return textSizeLimit;
	}
}
