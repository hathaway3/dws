package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ChooseDiskFileWin extends Dialog {

	protected Object result;
	protected Shell shlChooseDiskNumber;
	private Combo txtDisk;
	private String pre;
	private String buttxt;
	private Button btnOk;
	private Button butDisk;
	private Combo cmbDisk;
	private Combo combo;
	private ArrayList<String> disks;
	private int disk;
	

	public ChooseDiskFileWin(Shell parent, int style, int disk, String buttxt, String pre) 
	{
		super(parent, style);
		this.pre = pre;
		this.buttxt = buttxt;
		this.disk = disk;
		
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 * @throws DWUIOperationFailedException 
	 * @throws IOException 
	 */
	public Object open() throws IOException, DWUIOperationFailedException {
		createContents();
		
		loadFileHistory();
		
		loadDisks(cmbDisk);

		
		shlChooseDiskNumber.open();
		shlChooseDiskNumber.layout();
		Display display = getParent().getDisplay();
		while (!shlChooseDiskNumber.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	
	private void loadFileHistory() 
	{
		List<String> fh = MainWin.getDiskHistory();
		
		if (fh != null)
		{
			for (int i = fh.size() - 1;i > -1;i--)
			{
				this.txtDisk.add(fh.get(i));
			}
			
		}
		
	}

	private void loadDisks(Combo cmb) throws IOException
	{
		try {
			disks = UIUtils.loadArrayList("ui server show localdisks");
			
			Collections.sort(disks);
			
			for(int i=0; i<disks.size(); i++)
			{
				cmb.add(disks.get(i).split("/")[disks.get(i).split("/").length - 1]);
			}
			
		} 
		catch (DWUIOperationFailedException e) 
		{
			// couldn't load server localdisks, but that's ok
			
			cmb.setEnabled(false);
			
		}
		
	
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlChooseDiskNumber = new Shell(getParent(), getStyle());
		shlChooseDiskNumber.setSize(370, 167);
		shlChooseDiskNumber.setText("Choose file...");
		
		
		
		Label lblFileType = new Label(shlChooseDiskNumber, SWT.RIGHT);
		lblFileType.setBounds(10, 24, 77, 15);
		lblFileType.setText("Path source:");
		
		txtDisk = new Combo(shlChooseDiskNumber, SWT.BORDER);
		txtDisk.setBounds(93, 52, 225, 21);
		
		Label lblFileName = new Label(shlChooseDiskNumber, SWT.NONE);
		lblFileName.setAlignment(SWT.RIGHT);
		lblFileName.setBounds(20, 55, 67, 15);
		lblFileName.setText("File path:");
		
		butDisk = new Button(shlChooseDiskNumber, SWT.NONE);
		butDisk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
			
					 FileDialog fd = new FileDialog(shlChooseDiskNumber, SWT.OPEN);
				        fd.setText("Choose a local disk image...");
				        fd.setFilterPath("");
				        String[] filterExt = { "*.dsk", "*.*" };
				        fd.setFilterExtensions(filterExt);
				        String selected = fd.open();
					
				        if (!(selected == null))
				        	txtDisk.setText(selected);
					
				
			
			}
		});
		butDisk.setBounds(319, 50, 28, 25);
		butDisk.setText("...");
		
		btnOk = new Button(shlChooseDiskNumber, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (combo.getSelectionIndex() == 0)
				{
					MainWin.sendCommand(pre + " " + disk + " " + txtDisk.getText());
					//add to history
					MainWin.addDiskFileToHistory(txtDisk.getText());
				}
				else if (combo.getSelectionIndex() == 1)
				{
					MainWin.sendCommand(pre + " " + disk + " " + disks.get(cmbDisk.getSelectionIndex()));
				}
				
				shlChooseDiskNumber.close();
			}
		});
		btnOk.setBounds(141, 99, 75, 25);
		btnOk.setText(this.buttxt);
		
		Button btnCancel = new Button(shlChooseDiskNumber, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlChooseDiskNumber.close();
			}
		});
		btnCancel.setBounds(272, 99, 75, 25);
		btnCancel.setText("Cancel");
		
		cmbDisk = new Combo(shlChooseDiskNumber, SWT.NONE);
		cmbDisk.setVisibleItemCount(15);
		cmbDisk.setBounds(93, 52, 254, 23);

		
		combo = new Combo(shlChooseDiskNumber, SWT.READ_ONLY);
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) 
			{
				if (combo.getSelectionIndex() == 0)
				{
					txtDisk.setVisible(true);
					butDisk.setVisible(true);
					cmbDisk.setVisible(false);
				}
				else if (combo.getSelectionIndex() == 1)
				{
					txtDisk.setVisible(false);
					butDisk.setVisible(false);
					cmbDisk.setVisible(true);
				}
			}
		});
		combo.setItems(new String[] {"From local file or URI", "From server disk directory"});
		combo.setBounds(93, 21, 254, 23);
		combo.select(0);
		
	}
}
