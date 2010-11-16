package com.groupunix.drivewireui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ChooseURIWin extends Dialog {

	protected Object result;
	protected Shell shlChooseAFile;
	private Text text;
	private Button btnOk;
	private Button btnCancel;

	private String pre;
	private String post;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ChooseURIWin(Shell parent, int style, String pretext, String posttext) {
		super(parent, style);
		setText("Choose a URI...");
		this.pre = pretext;
		this.post = posttext;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlChooseAFile.open();
		shlChooseAFile.layout();
		Display display = getParent().getDisplay();
		while (!shlChooseAFile.isDisposed()) {
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
		shlChooseAFile = new Shell(getParent(), getStyle());
		shlChooseAFile.setSize(423, 129);
		shlChooseAFile.setText("Choose a file/URI...");
		
		text = new Text(shlChooseAFile, SWT.BORDER);
		text.setBounds(95, 25, 306, 21);
		
		Button btnLocalFile = new Button(shlChooseAFile, SWT.NONE);
		btnLocalFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
			
				 FileDialog fd = new FileDialog(shlChooseAFile, SWT.OPEN);
			        fd.setText("Choose a local disk image...");
			        fd.setFilterPath("");
			        String[] filterExt = { "*.dsk", "*.*" };
			        fd.setFilterExtensions(filterExt);
			        String selected = fd.open();
			        
			        text.setText(selected);
			}
		});
		btnLocalFile.setBounds(10, 23, 75, 25);
		btnLocalFile.setText("Local File...");
		
		btnOk = new Button(shlChooseAFile, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				MainWin.sendCommand(pre + " " + text.getText() + " " + post);
				shlChooseAFile.close();
			
			}
		});
		btnOk.setBounds(238, 62, 75, 25);
		btnOk.setText("Ok");
		
		btnCancel = new Button(shlChooseAFile, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				shlChooseAFile.close();
			}
		});
		btnCancel.setBounds(326, 62, 75, 25);
		btnCancel.setText("Cancel");

	}

}
