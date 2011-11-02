package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MIDIOutputWin extends Dialog {

	protected Object result;
	protected static Shell shlChooseMidiOutput;

	private Combo combo;
	private ArrayList<String> outputdevs;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public MIDIOutputWin(Shell parent, int style) {
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
		
		applyFont();
		
		loadOutputDevs(combo);
		combo.select(0);
		
		shlChooseMidiOutput.open();
		shlChooseMidiOutput.layout();
		Display display = getParent().getDisplay();
		while (!shlChooseMidiOutput.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}


	private static void applyFont() 
	{
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		Control[] controls = shlChooseMidiOutput.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlChooseMidiOutput.getDisplay(), f));
		}
	}
	
	
	private void createContents() {
		shlChooseMidiOutput = new Shell(getParent(), getStyle());
		shlChooseMidiOutput.setSize(310, 119);
		shlChooseMidiOutput.setText("Choose MIDI Output Device...");
		
		combo = new Combo(shlChooseMidiOutput, SWT.READ_ONLY);
		combo.setBounds(10, 31, 283, 23);
		
		
		Label lblAvailableMidiOutput = new Label(shlChooseMidiOutput, SWT.NONE);
		lblAvailableMidiOutput.setBounds(10, 10, 296, 15);
		lblAvailableMidiOutput.setText("Available MIDI output devices:");
		
		Button btnOk = new Button(shlChooseMidiOutput, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand("dw midi out " + outputdevs.get(combo.getSelectionIndex()).split(" ")[0]);
				e.display.getActiveShell().close();
			}
		});
		btnOk.setBounds(105, 60, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlChooseMidiOutput, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setBounds(219, 60, 75, 25);
		btnCancel.setText("Cancel");

	}

	private void loadOutputDevs(Combo cmb) throws IOException, DWUIOperationFailedException 
	{
		outputdevs = UIUtils.loadArrayList("ui server show mididevs");
		
		for(int i=0; i<outputdevs.size(); i++)
		{
			
			cmb.add(outputdevs.get(i));
		}
	}
	
}
