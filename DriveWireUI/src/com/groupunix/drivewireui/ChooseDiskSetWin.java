package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
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
import org.eclipse.swt.widgets.Shell;

public class ChooseDiskSetWin extends Dialog {

	protected Object result;
	protected static Shell shell;

	protected Combo cmbDiskSet;

	private String pre;
	private String post;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param post 
	 * @param pre 
	 */
	public ChooseDiskSetWin(Shell parent, int style, String pre, String post) {
		super(parent, style);
		this.pre = pre;
		this.post = post;
		
		setText("Choose a disk set...");
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
		
		UIUtils.getDWConfigSerial();
		
		loadDiskSets(cmbDiskSet);
		cmbDiskSet.select(0);
		
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

	
	private static void applyFont() 
	{
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		Control[] controls = shell.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shell.getDisplay(), f));
		}
	}
	
	
	private void loadDiskSets(Combo cmb) throws IOException, DWUIOperationFailedException 
	{
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> disksets = (List<HierarchicalConfiguration>)MainWin.dwconfig.configurationsAt("diskset");
		
		
		for(int i=0; i<disksets.size(); i++)
		{
			cmb.add(disksets.get(i).getString("Name","?noname?"));
		}
	}



	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(251, 100);
		shell.setText(getText());
		
		Button btnChoose = new Button(shell, SWT.NONE);
		btnChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand(pre + " " + cmbDiskSet.getText() + " " + post);
				shell.close();
			}
		});
		btnChoose.setBounds(74, 39, 75, 25);
		btnChoose.setText("Ok");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shell.close();
			}
		});
		btnCancel.setBounds(157, 39, 75, 25);
		btnCancel.setText("Cancel");
		
		cmbDiskSet = new Combo(shell, SWT.READ_ONLY);
		cmbDiskSet.setVisibleItemCount(10);
		cmbDiskSet.setBounds(10, 10, 222, 23);

	}
}
