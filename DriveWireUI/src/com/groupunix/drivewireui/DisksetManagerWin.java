package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;




public class DisksetManagerWin extends Dialog {

	protected Object result;
	protected static Shell shlDisksetManager;
	protected static Tree tree;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DisksetManagerWin(Shell parent, int style) {
		super(parent, style);
		setText("Diskset Manager");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() 
	{
		createContents();

		
		if (loadDisksets())
		{
			applyFont();
			
			shlDisksetManager.open();
			shlDisksetManager.layout();
			Display display = getParent().getDisplay();
			while (!shlDisksetManager.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}
		return result;
	}

	
	private static void applyFont() 
	{
		FontData f = MainWin.getDialogFont();
		
		Control[] controls = shlDisksetManager.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlDisksetManager.getDisplay(), f));
		}
		
		
		
		
		controls = tree.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlDisksetManager.getDisplay(), f));
		}
	}
	
	
	
	private boolean loadDisksets() 
	{
		
		try 
		{
			UIUtils.getDWConfigSerial();
			
		} 
		catch (DWUIOperationFailedException e1) 
		{
			MainWin.showError("DW error sending command", e1.getMessage() , UIUtils.getStackTrace(e1));
			return(false);
		} 
		catch (IOException e1) 
		{
			MainWin.showError("IO error sending command", e1.getMessage(), "You may have a connectivity problem, or the server may not be running.");
			return(false);
		}
		
		
		
		TreeItem curdiskset = null;
		
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> disksets = MainWin.dwconfig.configurationsAt("diskset");
		
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
			HierarchicalConfiguration dset = it.next();
	    
			
			TreeItem trtmDS = new TreeItem(tree, SWT.NONE);
			trtmDS.setText(dset.getString("Name","?noname?"));
			
			if (MainWin.getInstanceConfig().getString("CurrentDiskSet","").equals(dset.getString("Name","?noname?")))
			{
				curdiskset = trtmDS;
			}
			
			addDisksToTree(dset, trtmDS);
		}
		
		if (curdiskset != null)
		{
			tree.select(curdiskset);
			curdiskset.setExpanded(true);
			displayDiskset(curdiskset.getText());
			
		}
		
		return(true);
	}

	private void addDisksToTree(HierarchicalConfiguration dset, TreeItem trtmDS) 
	{
		if (dset != null)
		{
			@SuppressWarnings("unchecked")
			List<HierarchicalConfiguration> disks = dset.configurationsAt("disk");
		
			for(Iterator<HierarchicalConfiguration> itd = disks.iterator(); itd.hasNext();)
			{
				HierarchicalConfiguration disk = itd.next();
	    
			
				TreeItem trtmDisk = new TreeItem(trtmDS, SWT.NONE);
				trtmDisk.setText(1, disk.getString("drive","?nodrive?"));
				trtmDisk.setText(2, disk.getString("path","?nopath?"));
			}
		}	
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlDisksetManager = new Shell(getParent(), SWT.SHELL_TRIM);
		shlDisksetManager.setSize(681, 472);
		shlDisksetManager.setText("Diskset Manager");
		shlDisksetManager.setLayout(new BorderLayout(0, 0));
		
		tree = new Tree(shlDisksetManager, SWT.BORDER | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLayoutData(BorderLayout.CENTER);
		
		TreeColumn trclmnDiskSet = new TreeColumn(tree, SWT.NONE);
		trclmnDiskSet.setWidth(92);
		trclmnDiskSet.setText("Disk set");
		
		TreeColumn trclmnDisk = new TreeColumn(tree, SWT.NONE);
		trclmnDisk.setWidth(43);
		trclmnDisk.setText("Disk");
		
		TreeColumn trclmnDiskPath = new TreeColumn(tree, SWT.NONE);
		trclmnDiskPath.setWidth(508);
		trclmnDiskPath.setText("Path");
		
		Composite composite = new Composite(shlDisksetManager, SWT.NONE);
		composite.setLayoutData(BorderLayout.SOUTH);
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setBounds(0, 0, 665, 200);
		composite_1.setLayout(null);
		
		TabFolder tabFolder = new TabFolder(composite_1, SWT.NONE);
		tabFolder.setBounds(2, 2, 663, 166);
		
		TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem.setText("Disk set");
		
		TabItem tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_1.setText("Disk");
		
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setBounds(2, 171, 665, 25);
		
		Button btnApply = new Button(composite_2, SWT.NONE);
		btnApply.setBounds(580, 0, 75, 25);
		btnApply.setText("Apply");
		
		Button btnCancel = new Button(composite_2, SWT.NONE);
		btnCancel.setBounds(499, 0, 75, 25);
		btnCancel.setText("Cancel");
		
		Button btnOk = new Button(composite_2, SWT.NONE);
		
		btnOk.setBounds(418, 0, 75, 25);
		btnOk.setText("Ok");
		

	}

	
	protected void checkForDiskChanges() {
		// TODO Auto-generated method stub
		
	}

	
	protected void displayDisk(String dsname, String diskno) 
	{
		HierarchicalConfiguration disk = UIUtils.getDisksetDisk(dsname,Integer.parseInt(diskno));
		
	
		
		if (disk != null)
		{

		}
	}
	
	protected void displayDiskset(String dsname) 
	{
		HierarchicalConfiguration dset = UIUtils.getDiskset(dsname);
		
		if (dset != null)
		{

		}
	}

	
	
	protected Tree getTree() {
		return tree;
	}
}
