package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class RestartInstanceWin extends Dialog {

	protected Object result;
	protected Shell shlChooseAnInstance;

	private Combo cmbInstance;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public RestartInstanceWin(Shell parent, int style) {
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
		
		loadInstances(cmbInstance);
		cmbInstance.select(0);
		
		Label lblTheSelectedInstance = new Label(shlChooseAnInstance, SWT.WRAP);
		lblTheSelectedInstance.setBounds(10, 10, 297, 69);
		lblTheSelectedInstance.setText("The selected instance will be restarted.  Any open network connections will be closed.  All disk mappings and settings will be defaulted to the saved configuration.");
		
		shlChooseAnInstance.open();
		shlChooseAnInstance.layout();
		Display display = getParent().getDisplay();
		while (!shlChooseAnInstance.isDisposed()) {
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
		shlChooseAnInstance = new Shell(getParent(), getStyle());
		shlChooseAnInstance.setSize(319, 211);
		shlChooseAnInstance.setText("Choose an instance...");
		
		cmbInstance = new Combo(shlChooseAnInstance, SWT.READ_ONLY);
		cmbInstance.setBounds(70, 97, 229, 23);
		
		Label lblInstance = new Label(shlChooseAnInstance, SWT.NONE);
		lblInstance.setAlignment(SWT.RIGHT);
		lblInstance.setBounds(10, 100, 54, 15);
		lblInstance.setText("Instance:");
		
		Button btnOk = new Button(shlChooseAnInstance, SWT.NONE);
		btnOk.setBounds(120, 147, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shlChooseAnInstance, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlChooseAnInstance.close();
			
			}
		});
		btnCancel.setBounds(224, 147, 75, 25);
		btnCancel.setText("Cancel");

	}

	
	private void loadInstances(Combo cmb) throws IOException, DWUIOperationFailedException 
	{
		ArrayList<String> inst = UIUtils.loadArrayList("ui server show instances");

		for(int i=0; i<inst.size(); i++)
		{
			System.out.println("adding " + inst.get(i));
			cmb.add(inst.get(i));
		}
	}
	
	
}
