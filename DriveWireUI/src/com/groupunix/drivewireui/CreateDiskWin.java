package com.groupunix.drivewireui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

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
		while (!shlCreateANew.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	
	private static void applyFont() 
	{
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
		
	}
	
	private void createContents() {
		shlCreateANew = new Shell(getParent(), getStyle());
		shlCreateANew.setSize(380, 206);
		shlCreateANew.setText("Create a new disk image...");
		shlCreateANew.setLayout(null);
		
		spinnerDrive = new Spinner(shlCreateANew, SWT.BORDER);
		spinnerDrive.setBounds(227, 26, 47, 22);
		spinnerDrive.setMaximum(255);
		
		Label lblInsertNewDisk = new Label(shlCreateANew, SWT.NONE);
		lblInsertNewDisk.setBounds(36, 29, 185, 19);
		lblInsertNewDisk.setAlignment(SWT.RIGHT);
		lblInsertNewDisk.setText("Create new disk for drive:");
		
		Label lblPath = new Label(shlCreateANew, SWT.NONE);
		lblPath.setBounds(22, 68, 65, 19);
		lblPath.setText("Path:");
		
		textPath = new Text(shlCreateANew, SWT.BORDER);
		textPath.setBounds(21, 89, 298, 21);
		
		btnFile = new Button(shlCreateANew, SWT.NONE);
		btnFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// create a file chooser
				final DWServerFileChooser fileChooser = new DWServerFileChooser(textPath.getText());
				
				// 	configure the file dialog
				
				fileChooser.setFileHidingEnabled(false);
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			
				
				// 	show the file dialog
				int answer = fileChooser.showDialog(fileChooser, "Choose path for new file...");
							
				// 	check if a file was selected
				if (answer == JFileChooser.APPROVE_OPTION)
				{
					final File selected =  fileChooser.getSelectedFile();

					textPath.setText(selected.getPath());
			
				}
			
			}
		});
		btnFile.setBounds(322, 88, 29, 23);
		btnFile.setText("...");
		
		Button btnCreate = new Button(shlCreateANew, SWT.NONE);
		btnCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					MainWin.sendCommand("dw disk create " + spinnerDrive.getSelection() + " " + textPath.getText());
				
					MainWin.refreshDiskTable();
					
					e.display.getActiveShell().close();
				} 
				catch (DWUIOperationFailedException e1) 
				{
					MainWin.showError("Error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
				} 
				catch (IOException e1) 
				{
					MainWin.showError("Error sending command", e1.getMessage(), UIUtils.getStackTrace(e1));
				}
				
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
