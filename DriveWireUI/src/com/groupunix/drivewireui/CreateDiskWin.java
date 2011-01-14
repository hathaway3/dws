package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CreateDiskWin extends Dialog {

	protected Object result;
	protected Shell shlCreateANew;
	private Text textPath;
	private Spinner spinnerDrive;
	private Button btnLocal;
	private Button btnRemote;
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

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlCreateANew = new Shell(getParent(), getStyle());
		shlCreateANew.setSize(381, 343);
		shlCreateANew.setText("Create a new disk image...");
		shlCreateANew.setLayout(null);
		
		spinnerDrive = new Spinner(shlCreateANew, SWT.BORDER);
		spinnerDrive.setBounds(209, 26, 47, 22);
		spinnerDrive.setMaximum(255);
		
		Label lblInsertNewDisk = new Label(shlCreateANew, SWT.NONE);
		lblInsertNewDisk.setBounds(20, 29, 183, 19);
		lblInsertNewDisk.setAlignment(SWT.RIGHT);
		lblInsertNewDisk.setText("Create new disk for drive:");
		
		Group grpTypeOfPath = new Group(shlCreateANew, SWT.NONE);
		grpTypeOfPath.setBounds(26, 74, 325, 98);
		grpTypeOfPath.setText(" Type of path");
		
		btnLocal = new Button(grpTypeOfPath, SWT.RADIO);
		btnLocal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				btnFile.setVisible(true);
			}
		});
		btnLocal.setSelection(true);
		btnLocal.setBounds(25, 29, 294, 24);
		btnLocal.setText("Local file path or URI");
		
		btnRemote = new Button(grpTypeOfPath, SWT.RADIO);
		btnRemote.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				btnFile.setVisible(false);
			}
		});
		btnRemote.setBounds(25, 56, 282, 24);
		btnRemote.setText("File in server's disk directory");
		
		Label lblPath = new Label(shlCreateANew, SWT.NONE);
		lblPath.setBounds(26, 193, 51, 19);
		lblPath.setText("Path:");
		
		textPath = new Text(shlCreateANew, SWT.BORDER);
		textPath.setBounds(26, 218, 272, 21);
		
		btnFile = new Button(shlCreateANew, SWT.NONE);
		btnFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				 FileDialog fd = new FileDialog(shlCreateANew, SWT.SAVE);
			        fd.setText("Choose file name for the new image...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        if (selected != null)
			        {
			        	textPath.setText(selected);
			        }
			
			}
		});
		btnFile.setBounds(299, 213, 40, 30);
		btnFile.setText("...");
		
		Button btnCreate = new Button(shlCreateANew, SWT.NONE);
		btnCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					if (btnLocal.getSelection())
					{
						MainWin.sendCommand("dw disk create " + spinnerDrive.getSelection() + " " + textPath.getText());
					}
					else
					{
						ArrayList<String> res;
						res = UIUtils.loadArrayList(MainWin.getInstance(),"ui server config show LocalDiskDir");
					
						if (res.size() != 1)
						{
							throw new DWUIOperationFailedException("Strange results from server");
						}
					
						MainWin.sendCommand("dw disk create " + spinnerDrive.getSelection() + " " + res.get(0) + "/" + textPath.getText());
						
					}
				
					MainWin.refreshDiskTable();
					
					shlCreateANew.close();
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
		btnCreate.setBounds(141, 275, 90, 30);
		btnCreate.setText("Create");
		
		Button btnCancel = new Button(shlCreateANew, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlCreateANew.close();
			}
		});
		btnCancel.setBounds(276, 275, 75, 30);
		btnCancel.setText("Cancel");

	}

	protected Spinner getSpinner() {
		return spinnerDrive;
	}
	protected Button getBtnLocal() {
		return btnLocal;
	}
	protected Button getBtnRemote() {
		return btnRemote;
	}
	protected Button getBtnFile() {
		return btnFile;
	}
}
