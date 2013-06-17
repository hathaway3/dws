package com.groupunix.drivewireui;

import java.util.Iterator;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

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
	private MenuItem mntmSetToDefault;
	private MenuItem mntmWikiHelp;
	
	private Spinner spinner;
	private Text textIntHex;
	private Label lblD;
	private Label lblH;
	private boolean whoa = false;
	
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
		if (MainWin.master == null || MainWin.master.getMaxIndex("diskparams") < 0)
			this.paramDefs = new HierarchicalConfiguration();
		else
			this.paramDefs = MainWin.master.configurationAt("diskparams");
		

		
		createContents();
		
		
		
		
		display = getParent().getDisplay();
		
		int x = getParent().getBounds().x + (getParent().getBounds().width / 2) - (shell.getBounds().width / 2);
		int y = getParent().getBounds().y + (getParent().getBounds().height / 2) - (shell.getBounds().height / 2);
		
		shell.setLocation(x, y);
		shell.open();
		shell.layout();
		
		
		
		applySettings();
		applyToggle();
		
	
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return result;
	}

	
	protected void setIntFromHex(String text)
	{
		this.whoa  = true;
		try
		{
			if ((text != null) && (text != ""))
			{
				
				this.textInt.setText( Integer.parseInt(text.toLowerCase(), 16) +"" );
			}
			else
			{
				this.textInt.setText("");
			}
		}
		catch (NumberFormatException e)
		{
			// dont care
		}
		this.whoa = false;
	}

	protected void setHexFromInt(String text)
	{
		this.whoa  = true;
		try
		{
			if ((text != null) && (text != ""))
			{
				
				this.textIntHex.setText(Integer.toString(Integer.parseInt(text) , 16));
				
				
			}
			else
			{
				this.textIntHex.setText("");
			}
		}
		catch (NumberFormatException e)
		{
			// dont care
		}
		this.whoa  = false;
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
		
		btnToggle.setVisible(false);
		linkWiki.setVisible(false);
		lblInt.setVisible(false);
		textInt.setVisible(false);
		spinner.setVisible(false);
		textIntHex.setVisible(false);
		lblD.setVisible(false);
		lblH.setVisible(false);
		
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
				this.textIntHex.setVisible(true);
				this.lblD.setVisible(true);
				this.lblH.setVisible(true);
			}
			else if (type.equals("spinner"))
			{
				this.lblInt.setText(this.paramDefs.getString(key + "[@inputtext]", "Value"));
				
				try
				{
					if (this.paramDefs.containsKey(key + "[@min]"))
					{
						this.spinner.setMinimum(this.paramDefs.getInt(key + "[@min]"));
					}
					
					if (this.paramDefs.containsKey(key + "[@max]"))
					{
						this.spinner.setMaximum(this.paramDefs.getInt(key + "[@max]"));
					}
					
					if (this.tableParams.getItem(index).getText(2).equals(""))
						this.spinner.setSelection(Integer.parseInt(this.disk.getParam(key).toString()));
					else
						this.spinner.setSelection(Integer.parseInt(this.tableParams.getItem(index).getText(2)));
					
					
					
				}
				catch (NumberFormatException e)
				{
					MainWin.showError("Non numeric value?", "Somehow, we've managed to get a non numeric value into the config in a place where only numbers are allowed.", "This is not normal..  why don't you submit a bug report and let me know how this happened.");
				}
				
				this.lblInt.setVisible(true);
				this.spinner.setVisible(true);
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
		shell.setSize(528, 501);
		shell.setText(getText());
		GridLayout gl_shell = new GridLayout(8, false);
		gl_shell.marginTop = 5;
		gl_shell.marginRight = 5;
		gl_shell.marginLeft = 5;
		gl_shell.marginBottom = 5;
		shell.setLayout(gl_shell);
		
		
		
		tableParams = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		tableParams.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 8, 1));
				
		tableParams.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displayItem(tableParams.getItem(tableParams.getSelectionIndex()).getText(0), tableParams.getSelectionIndex());
			}

			
		});
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
				
				mntmAddToTable.setEnabled(false);
				mntmRemoveFromTable.setEnabled(false);
				mntmSetToDefault.setEnabled(false);
				mntmWikiHelp.setEnabled(false);
				mntmWikiHelp.setText("Wiki help...");
				mntmAddToTable.setText("Add item to main display");
				
				if (MainWin.getTPIndex(tableParams.getItem(tableParams.getSelectionIndex()).getText(0)) > -1)
				{
					mntmRemoveFromTable.setEnabled(true);
				}
				else
				{
					mntmAddToTable.setEnabled(true);
				}
				
				if (tableParams.getItem(tableParams.getSelectionIndex()).getText(0) != null)
				{
					mntmAddToTable.setText("Add " + tableParams.getItem(tableParams.getSelectionIndex()).getText(0) + " to main display");
					if (!tableParams.getItem(tableParams.getSelectionIndex()).getText(0).startsWith("_") && paramDefs.containsKey(tableParams.getItem(tableParams.getSelectionIndex()).getText(0) + "[@default]"))
					{
						if ((! tableParams.getItem(tableParams.getSelectionIndex()).getText(1).equals(paramDefs.getString(tableParams.getItem(tableParams.getSelectionIndex()).getText(0) + "[@default]") )) || !(tableParams.getItem(tableParams.getSelectionIndex()).getText(2).equals("") ))   
						{
							mntmSetToDefault.setEnabled(true);
						}
					}
				}
				
				if (tableParams.getItem(tableParams.getSelectionIndex()).getText(0) != null)
				{
					if (paramDefs.containsKey(tableParams.getItem(tableParams.getSelectionIndex()).getText(0) + "[@wikiurl]"))
					{
						mntmWikiHelp.setText("Wiki help for " + tableParams.getItem(tableParams.getSelectionIndex()).getText(0) + "...");
						mntmWikiHelp.setEnabled(true);
					}
				}
				
			}
		});
		tableParams.setMenu(menu);
		
		
		
		
		mntmSetToDefault = new MenuItem(menu, SWT.NONE);
		mntmSetToDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				doToggle(tableParams.getSelectionIndex(), paramDefs.getString(tableParams.getItem(tableParams.getSelectionIndex()).getText(0) + "[@default]" , ""));
				displayItem(tableParams.getItem(tableParams.getSelectionIndex()).getText(0),tableParams.getSelectionIndex());
			}
		});
		mntmSetToDefault.setText("Set to default value");
		
		@SuppressWarnings("unused")
		MenuItem spacer = new MenuItem(menu, SWT.SEPARATOR);
				
		mntmWikiHelp = new MenuItem(menu, SWT.NONE);
		mntmWikiHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				MainWin.openURL(this.getClass(), paramDefs.getString( tableParams.getItem(tableParams.getSelectionIndex()).getText(0) + "[@wikiurl]" , "")  );
				
			}
		});
		mntmWikiHelp.setText("Wiki help...");
		
		
		@SuppressWarnings("unused")
		MenuItem spacer2 = new MenuItem(menu, SWT.SEPARATOR);
		
		mntmAddToTable = new MenuItem(menu, SWT.NONE);
		mntmAddToTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				MainWin.addDiskTableColumn(tableParams.getItem(tableParams.getSelectionIndex()).getText(0));
				
			}
		});
		mntmAddToTable.setText("Add item to main display");
		
		mntmRemoveFromTable = new MenuItem(menu, SWT.NONE);
		mntmRemoveFromTable.setText("Remove from main display");
		mntmRemoveFromTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				MainWin.removeDiskTableColumn(tableParams.getItem(tableParams.getSelectionIndex()).getText(0));
				
			}
		});
		
		
		textItemTitle = new Text(shell, SWT.READ_ONLY);
		textItemTitle.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		textItemTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1));
		textItemTitle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		//textItemTitle.setFont(SWTResourceManager.getBoldFont(display.getSystemFont()));
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		textDescription = new Text(shell, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		textDescription.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		GridData gd_textDescription = new GridData(SWT.FILL, SWT.FILL, false, false, 8, 1);
		gd_textDescription.heightHint = 51;
		textDescription.setLayoutData(gd_textDescription);
		textDescription.setText("Select an option above to view details or make changes.");
		textDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		btnToggle = new Button(shell, SWT.CHECK);
		GridData gd_btnToggle = new GridData(SWT.FILL, SWT.CENTER, false, false, 8, 1);
		gd_btnToggle.horizontalIndent = 5;
		btnToggle.setLayoutData(gd_btnToggle);
		btnToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				doToggle(tableParams.getSelectionIndex(), btnToggle.getSelection()+"");
			}
		});
		btnToggle.setText("toggle");
		btnToggle.setVisible(false);
		
		textInt = new Text(shell, SWT.BORDER);
		textInt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		textInt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doToggle(tableParams.getSelectionIndex(), textInt.getText());
				if (!whoa)
					setHexFromInt(((Text) e.getSource()).getText());
			}
		});
		textInt.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				 String string = e.text;
			        char[] chars = new char[string.length()];
			        string.getChars(0, chars.length, chars, 0);
			        for (int i = 0; i < chars.length; i++) {
			          if (!(chars[i] >= '0' && chars[i] <= '9')) 
			          {
			         	
			        	if (! ( (i == 0) && (chars[0] == '-') && (e.start == 0) ) )
			        	{
			        		e.doit = false;
			            	return;
			        	}
			          }
			        }
			        
			        
			        
			}
		});
		textInt.setVisible(false);
		
		
		lblD = new Label(shell, SWT.NONE);
		lblD.setText("d");
		lblD.setVisible(false);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		textIntHex = new Text(shell, SWT.BORDER);
		textIntHex.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		textIntHex.setVisible(false);
		
		textIntHex.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!whoa)
					setIntFromHex(((Text) e.getSource()).getText());
			}
		});
		textIntHex.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
					e.text = e.text.toLowerCase();
				    char[] chars = new char[e.text.length()];
			        e.text.getChars(0, chars.length, chars, 0);
			        for (int i = 0; i < chars.length; i++) {
			          if (!(chars[i] >= '0' && chars[i] <= '9') && !(chars[i] >= 'a' && chars[i] <= 'f')) 
			          {
			        	  if (! ( (i == 0) && (chars[0] == '-') && (e.start == 0) ) )
			        	  {
			        		  e.doit = false;
			        		  return;
			        	  }
			        	
			          }
			        }
			}
		});
		
		lblH = new Label(shell, SWT.NONE);
		lblH.setText("h");
		lblH.setVisible(false);
		
		spinner = new Spinner(shell, SWT.BORDER);
		spinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		spinner.setVisible(false);
		
		spinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doToggle(tableParams.getSelectionIndex(), spinner.getSelection() + "");
		        	
			}
		});
		
		lblInt = new Label(shell, SWT.NONE);
		lblInt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		lblInt.setText("Value");
		lblInt.setVisible(false);
		
		linkWiki = new Link(shell, SWT.NONE);
		GridData gd_linkWiki = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_linkWiki.verticalIndent = 25;
		linkWiki.setLayoutData(gd_linkWiki);
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
		linkWiki.setText("<a>Wiki Help..</a>");
		linkWiki.setVisible(true);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 1, 1));
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				saveChanges();
				e.display.getActiveShell().close();
			}
		});
		btnOk.setText(" Ok ");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setText("Cancel");
		
		btnApply = new Button(shell, SWT.NONE);
		btnApply.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveChanges();
			}
		});
		btnApply.setEnabled(false);
		btnApply.setText("Apply");
		
		
		

	}

	

	public void submitEvent(String key, String val)
	{
		addOrUpdate(key,val);
		this.applyToggle();
	}
	
	/*
	private void setLayout()
	{
		this.tableParams.removeAll();
		this.textDescription.setText("");
		this.textItemTitle.setText("");
		tableParams.setBackground(MainWin.colorWhite);
  		tableParams.setLinesVisible(true);
  		tableParams.setHeaderVisible(true);
	}
	*/
	
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
	protected Spinner getSpinner() {
		return spinner;
	}
	protected Text getTextIntHex() {
		return textIntHex;
	}
	protected Label getLblD() {
		return lblD;
	}
	protected Label getLblH() {
		return lblH;
	}
}
