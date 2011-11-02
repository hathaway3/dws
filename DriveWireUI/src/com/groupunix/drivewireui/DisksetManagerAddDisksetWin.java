package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.wb.swt.SWTResourceManager;

public class DisksetManagerAddDisksetWin extends Dialog {

	protected Object result;
	protected Shell shlAddNewDiskset;
	private Text textName;
	private Combo comboCopy;
	private Label lblNameInUse;

	private int parentx = 0;
	private int parenty = 0;
	
	private String dsname = null;
	private String dscopy = null;
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DisksetManagerAddDisksetWin(Shell parent, int style) {
		super(parent, style);
		parentx = parent.getLocation().x;
		parenty = parent.getLocation().y;
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public String[] open() {
		boolean oktogo = false;
		
		createContents();
		
		applyFont();
		lblNameInUse.setVisible(false);
		try {
			loadDiskSets(comboCopy);
			oktogo = true;
		} catch (IOException e) {
			MainWin.showError("IO error", "Could not load list of disksets", e.getMessage());
		} catch (DWUIOperationFailedException e) {
			MainWin.showError("DW error", "Could not load list of disksets", e.getMessage());
		}
		
		if (oktogo)
		{
			shlAddNewDiskset.setLocation(parentx + 140, parenty + 100);
			shlAddNewDiskset.open();
			shlAddNewDiskset.layout();
			Display display = getParent().getDisplay();
			while (!shlAddNewDiskset.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}
		
		return(new String[]{ dsname, dscopy });
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
	
	private void applyFont() 
	{
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		Control[] controls = this.shlAddNewDiskset.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(this.shlAddNewDiskset.getDisplay(), f));
		}
	}
	
	
	private void createContents() {
		shlAddNewDiskset = new Shell(getParent(), getStyle());
		shlAddNewDiskset.setSize(388, 188);
		shlAddNewDiskset.setText("Add new diskset...");
		
		textName = new Text(shlAddNewDiskset, SWT.BORDER);
		textName.setBounds(130, 20, 127, 21);
		
		Label lblNewLabel = new Label(shlAddNewDiskset, SWT.NONE);
		lblNewLabel.setAlignment(SWT.RIGHT);
		lblNewLabel.setBounds(21, 23, 103, 15);
		lblNewLabel.setText("Diskset name:");
		
		comboCopy = new Combo(shlAddNewDiskset, SWT.NONE);
		comboCopy.setBounds(130, 62, 127, 23);
		
		Label lblCopyDisksFrom = new Label(shlAddNewDiskset, SWT.NONE);
		lblCopyDisksFrom.setAlignment(SWT.RIGHT);
		lblCopyDisksFrom.setBounds(21, 65, 103, 15);
		lblCopyDisksFrom.setText("Copy disks from:");
		
		Label lbloptional = new Label(shlAddNewDiskset, SWT.NONE);
		lbloptional.setBounds(263, 65, 55, 15);
		lbloptional.setText("(optional)");
		
		Button btnNewButton = new Button(shlAddNewDiskset, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (!textName.getText().equals(""))
				{
					if (UIUtils.getDiskset(textName.getText()) == null)
					{
						dsname = textName.getText();
						
						if (!comboCopy.getText().equals(""))
						{
							dscopy = comboCopy.getText();
						}
						
						e.display.getActiveShell().close();
					}
					else
					{
						lblNameInUse.setVisible(true);
					}
				}
				
			}
		});
		btnNewButton.setBounds(130, 125, 127, 25);
		btnNewButton.setText("Add new diskset");
		
		Button btnNewButton_1 = new Button(shlAddNewDiskset, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				e.display.getActiveShell().close();
			}
		});
		btnNewButton_1.setBounds(297, 125, 75, 25);
		btnNewButton_1.setText("Cancel");
		
		lblNameInUse = new Label(shlAddNewDiskset, SWT.NONE);
		lblNameInUse.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblNameInUse.setBounds(263, 23, 94, 15);
		lblNameInUse.setText("Name in use!");

	}
	
	protected Text getTextName() {
		return textName;
	}
	protected Combo getComboCopy() {
		return comboCopy;
	}
	protected Label getLblNameInUse() {
		return lblNameInUse;
	}
}
