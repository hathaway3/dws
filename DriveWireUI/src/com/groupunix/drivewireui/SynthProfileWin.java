package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;

public class SynthProfileWin extends Dialog {

	protected Object result;
	protected Shell shlChooseASynth;

	protected Combo combo;
	
	private ArrayList<String> profiles;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SynthProfileWin(Shell parent, int style) {
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
		
		loadSynthProfiles(combo);
		combo.select(0);
		
		Label lblAvailableSynthTranslations = new Label(shlChooseASynth, SWT.NONE);
		lblAvailableSynthTranslations.setBounds(10, 10, 217, 15);
		lblAvailableSynthTranslations.setText("Available synth translations:");
		
		shlChooseASynth.open();
		shlChooseASynth.layout();
		Display display = getParent().getDisplay();
		while (!shlChooseASynth.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void loadSynthProfiles(Combo cmb) throws IOException, DWUIOperationFailedException 
	{
		profiles = UIUtils.loadArrayList("ui server show synthprofiles");
		
		Collections.sort(profiles);
		
		for(int i=0; i<profiles.size(); i++)
		{
			
			cmb.add(profiles.get(i));
		}
	}
	
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlChooseASynth = new Shell(getParent(), getStyle());
		shlChooseASynth.setSize(245, 123);
		shlChooseASynth.setText("Choose Translation Profile...");
		
		combo = new Combo(shlChooseASynth, SWT.READ_ONLY);
		combo.setBounds(10, 34, 217, 23);
		
		
		Button btnOk = new Button(shlChooseASynth, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (combo.getSelectionIndex() > -1)
				{
					MainWin.sendCommand("dw midi synth profile " + profiles.get(combo.getSelectionIndex()).split(" ")[0]);
				}
				shlChooseASynth.close();
				
			}
		});
		btnOk.setBounds(71, 63, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlChooseASynth, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlChooseASynth.close();
			}
		});
		btnCancel.setBounds(152, 63, 75, 25);
		btnCancel.setText("Cancel");

	}

}
