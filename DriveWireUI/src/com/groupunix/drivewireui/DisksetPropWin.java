package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class DisksetPropWin extends Dialog {

	protected Object result;
	protected Shell shlDisksetProperties;
	private Text textDescription;
	private Text textNotes;
	private String currentdiskset;
	private Button btnEjectAllDisks;
	private Button btnTrackDiskChanges;
	private Button btnHdbdosTranslation;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DisksetPropWin(Shell parent, int style) {
		super(parent, style);
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
		
		loadSettings();

		
		shlDisksetProperties.open();
		shlDisksetProperties.layout();
		Display display = getParent().getDisplay();
		while (!shlDisksetProperties.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}


	private void loadSettings() throws IOException, DWUIOperationFailedException 
	{
		ArrayList<String> cds = UIUtils.loadArrayList(MainWin.getInstance(),"ui instance config show CurrentDiskSet");
		
		if (cds.size() != 1)
		{
			throw new DWUIOperationFailedException("Strange results from server");
		}
		
		this.currentdiskset = cds.get(0);
		shlDisksetProperties.setText("Properties for diskset '" + this.currentdiskset + "'");
		
		this.textDescription.setText("");
		this.textNotes.setText("");
		
		this.btnEjectAllDisks.setSelection(false);
		this.btnHdbdosTranslation.setSelection(false);
		this.btnTrackDiskChanges.setSelection(false);
		
		ArrayList<String> res = UIUtils.loadArrayList("ui diskset show " + this.currentdiskset);
		
	
		for (int i = 0;i<res.size();i++)
		{
			Pattern p_setitem = Pattern.compile("^(.+):\\s(.+)");
			Matcher m = p_setitem.matcher(res.get(i));
			  
			if (m.find())
			{
				if (m.group(1).equals("Description"))
					this.textDescription.setText(m.group(2));
				
				if (m.group(1).equals("Notes"))
					this.textNotes.setText(m.group(2));
					
				if (m.group(1).equals("SaveChanges"))
						this.btnTrackDiskChanges.setSelection(UIUtils.sTob(m.group(2)));
					
				if (m.group(1).equals("HDBDOSMode"))
						this.btnHdbdosTranslation.setSelection(UIUtils.sTob(m.group(2)));
				
				if (m.group(1).equals("EjectAllOnLoad"))
					this.btnEjectAllDisks.setSelection(UIUtils.sTob(m.group(2)));
				
					
			}
			  
				
		}
		
		
		
		
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlDisksetProperties = new Shell(getParent(), getStyle());
		shlDisksetProperties.setSize(450, 364);
		shlDisksetProperties.setText("Diskset Properties");
		
		Button btnOk = new Button(shlDisksetProperties, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				applySettings();
				shlDisksetProperties.close();
			}
		});
		btnOk.setBounds(180, 301, 75, 25);
		btnOk.setText("Ok");
		
		Button btnUndo = new Button(shlDisksetProperties, SWT.NONE);
		btnUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try {
					loadSettings();
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
		btnUndo.setBounds(10, 301, 75, 25);
		btnUndo.setText("Undo");
		
		Button btnCancel = new Button(shlDisksetProperties, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlDisksetProperties.close();
			}
		});
		btnCancel.setBounds(359, 301, 75, 25);
		btnCancel.setText("Cancel");
		
		btnTrackDiskChanges = new Button(shlDisksetProperties, SWT.CHECK);
		btnTrackDiskChanges.setBounds(37, 216, 226, 25);
		btnTrackDiskChanges.setText("Save disk changes automatically");
		
		btnHdbdosTranslation = new Button(shlDisksetProperties, SWT.CHECK);
		btnHdbdosTranslation.setText("Enable HDBDOS translation when this set is loaded");
		btnHdbdosTranslation.setBounds(37, 247, 364, 25);
		
		btnEjectAllDisks = new Button(shlDisksetProperties, SWT.CHECK);
		btnEjectAllDisks.setText("Eject all disks before loading this set");
		btnEjectAllDisks.setBounds(37, 185, 303, 25);
		
		textDescription = new Text(shlDisksetProperties, SWT.BORDER);
		textDescription.setBounds(37, 46, 364, 21);
		
		textNotes = new Text(shlDisksetProperties, SWT.BORDER | SWT.V_SCROLL);
		textNotes.setBounds(37, 100, 364, 71);
		
		Label lblDescription = new Label(shlDisksetProperties, SWT.NONE);
		lblDescription.setBounds(37, 24, 194, 21);
		lblDescription.setText("Description");
		
		Label lblNotes = new Label(shlDisksetProperties, SWT.NONE);
		lblNotes.setText("Notes");
		lblNotes.setBounds(37, 77, 194, 21);

	}
	
	
	protected void applySettings() 
	{
		// send settings to server
		
		MainWin.sendCommand("ui diskset set " + this.currentdiskset + " Description " + this.textDescription.getText());
		MainWin.sendCommand("ui diskset set " + this.currentdiskset + " Notes " + this.textNotes.getText());
		MainWin.sendCommand("ui diskset set " + this.currentdiskset + " SaveChanges " + UIUtils.bTos(this.btnTrackDiskChanges.getSelection()));
		MainWin.sendCommand("ui diskset set " + this.currentdiskset + " HDBDOSMode " + UIUtils.bTos(this.btnHdbdosTranslation.getSelection()));
		MainWin.sendCommand("ui diskset set " + this.currentdiskset + " EjectAllOnLoad " + UIUtils.bTos(this.btnEjectAllDisks.getSelection()));
		
	}

	protected Button getBtnEjectAllDisks() {
		return btnEjectAllDisks;
	}
	protected Button getBtnTrackDiskChanges() {
		return btnTrackDiskChanges;
	}
	protected Button getBtnHdbdosTranslation() {
		return btnHdbdosTranslation;
	}
}
