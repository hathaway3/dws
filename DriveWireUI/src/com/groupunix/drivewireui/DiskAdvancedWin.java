package com.groupunix.drivewireui;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class DiskAdvancedWin extends Dialog {

	protected Object result;
	protected  Shell shell;
	
	private DiskDef disk;
	private Table tableParams;
	private Text textItemTitle;
	
	private HierarchicalConfiguration paramDefs;
	private Text textDescription;
	
	private String wikiurl = "http://sourceforge.net/apps/mediawiki/drivewireserver/index.php?title=Using_DriveWire";
	private Link linkWiki;
	private Button btnToggle;
	

	private TableColumn tblclmnNewValue;
	private Button btnApply;
	private Display display;
	private Text textInt;
	private Label lblInt;
	
	private MenuItem mntmAddToTable;
	private MenuItem mntmRemoveFromTable;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DiskAdvancedWin(Shell parent, int style, DiskDef disk) {
		super(parent, style);
		setText("Parameters for drive " + disk.getDriveNo());
		this.disk = disk;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() 
	{
		if (MainWin.config.configurationAt("DiskParams") != null)
		{
			this.paramDefs = MainWin.config.configurationAt("DiskParams");
		}
		else
			this.paramDefs = new HierarchicalConfiguration();
		

		
		createContents();
		
		//applyFont();
		
		
		display = getParent().getDisplay();
		
		int x = getParent().getBounds().x + (getParent().getBounds().width / 2) - (shell.getBounds().width / 2);
		int y = getParent().getBounds().y + (getParent().getBounds().height / 2) - (shell.getBounds().height / 2);
		
		shell.setLocation(x, y);
		shell.open();
		shell.layout();
		
		
		tableParams = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
				
		tableParams.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayItem(tableParams.getItem(tableParams.getSelectionIndex()).getText(0), tableParams.getSelectionIndex());
			}

			
		});
		tableParams.setBounds(10, 10, 496, 284);
		tableParams.setHeaderVisible(true);
		tableParams.setLinesVisible(true);
		
		TableColumn tblclmnParameter = new TableColumn(tableParams, SWT.NONE);
		tblclmnParameter.setWidth(100);
		tblclmnParameter.setText("Parameter");
		
		TableColumn tblclmnValue = new TableColumn(tableParams, SWT.NONE);
		tblclmnValue.setWidth(289);
		tblclmnValue.setText("Current Value");
		
		tblclmnNewValue = new TableColumn(tableParams, SWT.NONE);
		
		tblclmnNewValue.setWidth(83);
		tblclmnNewValue.setText("New Value");
		
		Menu menu = new Menu(tableParams);
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				
				if (MainWin.getTPIndex(tableParams.getItem(tableParams.getSelectionIndex()).getText(0)) > -1)
				{
					mntmAddToTable.setEnabled(false);
					mntmRemoveFromTable.setEnabled(true);
				}
				else
				{
					mntmAddToTable.setEnabled(true);
					mntmRemoveFromTable.setEnabled(false);
				}
			}
		});
		tableParams.setMenu(menu);
		
		mntmAddToTable = new MenuItem(menu, SWT.NONE);
		mntmAddToTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				MainWin.addDiskTableColumn(tableParams.getItem(tableParams.getSelectionIndex()).getText(0));
				
			}
		});
		mntmAddToTable.setText("Add to main display");
		
		mntmRemoveFromTable = new MenuItem(menu, SWT.NONE);
		mntmRemoveFromTable.setText("Remove from main display");
		mntmRemoveFromTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				MainWin.removeDiskTableColumn(tableParams.getItem(tableParams.getSelectionIndex()).getText(0));
				
			}
		});
		
		textItemTitle = new Text(shell, SWT.READ_ONLY);
		textItemTitle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		textItemTitle.setFont(SWTResourceManager.getBoldFont(display.getSystemFont()));
		textItemTitle.setBounds(10, 311, 335, 21);
		
		textDescription = new Text(shell, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		textDescription.setText("Select an option above to view details or make changes.");
		textDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		textDescription.setBounds(10, 338, 496, 43);
		
		linkWiki = new Link(shell, SWT.NONE);
		linkWiki.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWin.doDisplayAsync(new Runnable() {

					

					@Override
					public void run() 
					{
						MainWin.openURL(this.getClass(),wikiurl);
					}
					
				});
			}
		});
		linkWiki.setBounds(14, 420, 70, 15);
		linkWiki.setText("<a>Wiki Help..</a>");
		linkWiki.setVisible(false);
		
		btnToggle = new Button(shell, SWT.CHECK);
		btnToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				doToggle(tableParams.getSelectionIndex(), btnToggle.getSelection()+"");
			}
		});
		btnToggle.setBounds(14, 383, 297, 16);
		btnToggle.setText("toggle");
		btnToggle.setVisible(false);
		
		btnApply = new Button(shell, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveChanges();
			}
		});
		btnApply.setEnabled(false);
		btnApply.setBounds(431, 420, 75, 25);
		btnApply.setText("Apply");
		
		textInt = new Text(shell, SWT.BORDER);
		textInt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doToggle(tableParams.getSelectionIndex(), textInt.getText());
		        	
			}
		});
		textInt.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				 String string = e.text;
			        char[] chars = new char[string.length()];
			        string.getChars(0, chars.length, chars, 0);
			        for (int i = 0; i < chars.length; i++) {
			          if (!('0' <= chars[i] && chars[i] <= '9')) 
			          {
			        	if (! ((i == 0) && (chars[0] == '-') ))
			        	{
			        		e.doit = false;
			            	return;
			        	}
			          }
			        }
			        
			}
		});
		textInt.setBounds(10, 383, 76, 21);
		textInt.setVisible(false);
		
		lblInt = new Label(shell, SWT.NONE);
		lblInt.setBounds(92, 384, 354, 15);
		lblInt.setText("Value");
		lblInt.setVisible(false);
		
		applySettings();
		applyToggle();
		
	
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return result;
	}

	
	protected void doToggle(int sel, String newval)
	{ 
		if (this.tableParams.getItem(sel).getText(1).equals(newval))
		{
			this.tableParams.getItem(sel).setText(2,"");
				
		}
		else
		{
			this.tableParams.getItem(sel).setText(2, newval);
			
		}
		
		applyToggle();
	}

	
	protected void applyToggle()
	{
		int i;
		for (i = 0; i < this.tableParams.getItemCount();i++)
		{
			if (!this.tableParams.getItem(i).getText(2).equals(""))
			{
				this.btnApply.setEnabled(true);
				break;
			}
		}
		
		if (i == this.tableParams.getItemCount())
		{
			this.btnApply.setEnabled(false);
		}
	}
	
	protected void displayItem(String key, int index)
	{
		
		this.shell.setRedraw(false);
		
		this.btnToggle.setVisible(false);
		this.linkWiki.setVisible(false);
		this.lblInt.setVisible(false);
		this.textInt.setVisible(false);
		
		if (key == null)
		{
			
			this.textItemTitle.setText("");
			this.textDescription.setText("No disk is inserted in drive " + this.disk.getDriveNo());

		}
		else
		{
			String title = key;
			
			if (key.startsWith("_"))
				title += " (System parameter)";
			
			this.textItemTitle.setText(title); 
			
			this.textDescription.setText(this.paramDefs.getString(key + "[@detail]","No definition for this parameter." ));
			
			if (this.paramDefs.containsKey(key + "[@wikiurl]"))
			{
				this.wikiurl = this.paramDefs.getString(key + "[@wikiurl]");
				this.linkWiki.setVisible(true);
			}
			
			String type = this.paramDefs.getString(key + "[@type]", "system");
			
			if (type.equals("boolean"))
			{
				this.btnToggle.setText(" " + this.paramDefs.getString(key + "[@toggletext]", "Enable option"));
				
				if (this.tableParams.getItem(index).getText(2).equals(""))
					this.btnToggle.setSelection(Boolean.parseBoolean(this.disk.getParam(key).toString()));
				else
					this.btnToggle.setSelection(Boolean.parseBoolean(this.tableParams.getItem(index).getText(2)));
				
				this.btnToggle.setVisible(true);
			}
			else if (type.equals("integer"))
			{
				this.lblInt.setText(this.paramDefs.getString(key + "[@inputtext]", "Value"));
				
				if (this.tableParams.getItem(index).getText(2).equals(""))
					this.textInt.setText(this.disk.getParam(key).toString());
				else
					this.textInt.setText(this.tableParams.getItem(index).getText(2));
				
				this.lblInt.setVisible(true);
				this.textInt.setVisible(true);
			}
		}
		
		this.shell.setRedraw(true);
	}

	
	
	
	
	
	
	
	
	
	
	
	

	private void applySettings() 
	{

		Iterator<String> itr = this.disk.getParams();
		
		String key;
		
		while (itr.hasNext())
		{
			key = itr.next();
			this.addOrUpdate(key, this.disk.getParam(key).toString());
		}
		
		
	}
	
	
	private void saveChanges()
	{
		for (int i = 0; i < this.tableParams.getItemCount();i++)
		{
			if (!this.tableParams.getItem(i).getText(2).equals(""))
			{
				MainWin.sendCommand("dw disk set " + this.disk.getDriveNo() + " " + this.tableParams.getItem(i).getText(0) + " " + this.tableParams.getItem(i).getText(2));
			}
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(522, 483);
		shell.setText(getText());
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				saveChanges();
				e.display.getActiveShell().close();
			}
		});
		btnOk.setBounds(270, 420, 75, 25);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setBounds(350, 420, 75, 25);
		btnCancel.setText("Cancel");

	}

	

	public void submitEvent(String key, String val)
	{
		addOrUpdate(key,val);
		this.applyToggle();
	}
	
	private void setLayout()
	{
		this.tableParams.removeAll();
		this.textDescription.setText("");
		this.textItemTitle.setText("");
		tableParams.setBackground(MainWin.colorWhite);
  		tableParams.setLinesVisible(true);
  		tableParams.setHeaderVisible(true);
	}
	
	
	private void addOrUpdate(String key, String val)
	{
		int i;
		
		if (key.equals("*eject"))
		{
			this.tableParams.removeAll();
			this.displayItem(null,-1);
		}
		
		if ((val!=null) && !key.startsWith("*"))
		{	
			for (i = 0;i<this.tableParams.getItemCount();i++)
			{
			
				if (this.tableParams.getItem(i).getText(0).equals(key))
				{
					// update value
					this.tableParams.getItem(i).setText(1,val);
					this.doToggle(i, this.tableParams.getItem(i).getText(2));
					
					// are we displaying this item right now
					if (i == this.tableParams.getSelectionIndex())
					{
						this.displayItem(key, i);
					}
					break;
				}
				else if (this.tableParams.getItem(i).getText(0).compareTo(key) > 0)
				{
					this.tableParams.setRedraw(false);
					
					// insert.. sort of
					TableItem item = new TableItem(this.tableParams, SWT.NONE);
					item.setText(0, key);
					item.setText(1, val);
					item.setText(2, "");
					
					for (int j = i;j<this.tableParams.getItemCount() - 1;j++)
					{
						item = new TableItem(this.tableParams, SWT.NONE);
						item.setText(0, this.tableParams.getItem(i).getText(0));
						item.setText(1, this.tableParams.getItem(i).getText(1));
						item.setText(2, this.tableParams.getItem(i).getText(2));
						
						this.tableParams.remove(i);
						
					}
					
					this.tableParams.setRedraw(true);
					
					break;
					
				}
			}
		

		
			if (i == this.tableParams.getItemCount())
			{

				TableItem item = new TableItem(this.tableParams, SWT.NONE);
				item.setText(0, key);
				item.setText(1, val);
				item.setText(2, "");
			
			}
		}
	}

	
	
	


	
	

}
